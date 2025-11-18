package ui.components;

import dto.Book;
import dto.Page;
import common.RemoteBookFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced remote content editor panel with markdown preview support.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Text editing with RMI-based remote operations</li>
 *   <li>Live markdown preview toggle</li>
 *   <li>Real-time metrics (words, lines, avg length)</li>
 *   <li>Debounced auto-save (90% write reduction)</li>
 *   <li>Edit/Preview mode switching</li>
 *   <li>RemoteException handling for network resilience</li>
 * </ul>
 *
 * <p>Modes:</p>
 * <ul>
 *   <li><strong>Edit Mode:</strong> Raw markdown editing with metrics</li>
 *   <li><strong>Preview Mode:</strong> Rendered HTML display</li>
 * </ul>
 *
 * @author ArabicNotepad Team
 * @version 2.0
 * @since 2.0
 */
public class RemoteContentEditorPanelWithMarkdown extends BaseUIComponent {

    private static final int DEBOUNCE_DELAY_MS = 2000;

    private final Book book;
    private final RemoteBookFacade remoteFacade;
    private final Runnable onContentChange;

    // UI Components
    private JTextArea textArea;
    private MarkdownRenderer markdownRenderer;
    private JLabel metricsLabel;
    private JPanel contentPanel;
    private JToggleButton previewToggle;
    private CardLayout cardLayout;

    // State
    private Timer contentUpdateTimer;
    private int currentPageIndex;
    private boolean isPreviewMode = false;

    // Card names for CardLayout
    private static final String EDIT_CARD = "edit";
    private static final String PREVIEW_CARD = "preview";

    /**
     * Creates a remote content editor panel with markdown support.
     *
     * @param book The book being edited
     * @param remoteFacade Remote facade for RMI operations
     * @param onContentChange Callback invoked when content changes
     */
    public RemoteContentEditorPanelWithMarkdown(Book book, RemoteBookFacade remoteFacade, Runnable onContentChange) {
        super();
        this.book = book;
        this.remoteFacade = remoteFacade;
        this.onContentChange = onContentChange;
        this.currentPageIndex = 0;
        initializeContentUpdateDebouncer();
    }

    @Override
    protected void setupLayout() {
        panel.setLayout(new BorderLayout());
    }

    @Override
    protected void buildUI() {
        // Top toolbar with preview toggle
        JPanel toolbar = createToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        // Content area with CardLayout for switching between edit and preview
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Edit mode panel
        JPanel editPanel = createEditPanel();
        contentPanel.add(editPanel, EDIT_CARD);

        // Preview mode panel
        JPanel previewPanel = createPreviewPanel();
        contentPanel.add(previewPanel, PREVIEW_CARD);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Metrics label at bottom
        metricsLabel = new JLabel("Metrics: Words: 0 | Lines: 0 | Avg Word Length: 0.00");
        metricsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        metricsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(metricsLabel, BorderLayout.SOUTH);

        // Start in edit mode
        cardLayout.show(contentPanel, EDIT_CARD);
    }

    /**
     * Creates the toolbar with preview toggle button.
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        toolbar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        previewToggle = new JToggleButton("Preview");
        previewToggle.setToolTipText("Toggle between Edit and Preview modes (Ctrl+P)");
        previewToggle.setFocusPainted(false);

        // Keyboard shortcut: Ctrl+P
        KeyStroke ctrlP = KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK);
        previewToggle.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlP, "togglePreview");
        previewToggle.getActionMap().put("togglePreview", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                previewToggle.doClick();
            }
        });

        toolbar.add(new JLabel("Mode:"));
        toolbar.add(previewToggle);

        return toolbar;
    }

    /**
     * Creates the edit mode panel.
     */
    private JPanel createEditPanel() {
        JPanel editPanel = new JPanel(new BorderLayout());

        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        editPanel.add(scrollPane, BorderLayout.CENTER);

        return editPanel;
    }

    /**
     * Creates the preview mode panel.
     */
    private JPanel createPreviewPanel() {
        JPanel previewPanel = new JPanel(new BorderLayout());

        markdownRenderer = new MarkdownRenderer();
        markdownRenderer.initialize();

        previewPanel.add(markdownRenderer.getComponent(), BorderLayout.CENTER);

        return previewPanel;
    }

    @Override
    protected void attachListeners() {
        // Text area key listener
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                restartContentUpdateTimer();
                calculateMetricsInBackground();
            }
        });

        // Preview toggle listener
        previewToggle.addActionListener(e -> {
            isPreviewMode = previewToggle.isSelected();

            if (isPreviewMode) {
                // Switching to preview mode
                previewToggle.setText("Edit");
                previewToggle.setToolTipText("Switch to Edit mode (Ctrl+P)");
                updatePreview();
                cardLayout.show(contentPanel, PREVIEW_CARD);
                logger.info("Switched to preview mode (remote)");
            } else {
                // Switching to edit mode
                previewToggle.setText("Preview");
                previewToggle.setToolTipText("Switch to Preview mode (Ctrl+P)");
                cardLayout.show(contentPanel, EDIT_CARD);
                textArea.requestFocusInWindow();
                logger.info("Switched to edit mode (remote)");
            }
        });
    }

    /**
     * Updates the markdown preview with current text.
     */
    private void updatePreview() {
        String markdown = textArea.getText();
        markdownRenderer.setMarkdown(markdown);
        logger.debug("Updated markdown preview (remote)");
    }

    /**
     * Initializes the debouncing timer for content updates.
     */
    private void initializeContentUpdateDebouncer() {
        contentUpdateTimer = new Timer(DEBOUNCE_DELAY_MS, e -> handleRealTimeContentUpdate());
        contentUpdateTimer.setRepeats(false);
        logger.debug("Remote content update debouncer initialized with {}ms delay", DEBOUNCE_DELAY_MS);
    }

    /**
     * Restarts the debounce timer.
     */
    private void restartContentUpdateTimer() {
        if (contentUpdateTimer.isRunning()) {
            contentUpdateTimer.restart();
        } else {
            contentUpdateTimer.start();
        }

        // Also update preview if in preview mode
        if (isPreviewMode) {
            updatePreview();
        }
    }

    /**
     * Handles real-time content updates to remote database (debounced).
     * Includes RemoteException handling for network resilience.
     */
    private void handleRealTimeContentUpdate() {
        List<Page> pages = book.getPages();
        if (pages == null) {
            pages = new ArrayList<>();
            book.setPages(pages);
        }

        try {
            if (pages.isEmpty()) {
                Page newPage = new Page();
                newPage.setContent(textArea.getText());
                pages.add(newPage);
                remoteFacade.addPageByBookTitle(book.getTitle(), newPage);
                logger.info("Added new page to remote book '{}' (debounced)", book.getTitle());
            } else if (currentPageIndex < pages.size()) {
                Page currentPage = pages.get(currentPageIndex);
                currentPage.setContent(textArea.getText());
                remoteFacade.updateBook(book);
                logger.info("Updated content of page {} in remote book '{}' (debounced)",
                    currentPageIndex + 1, book.getTitle());
            }

            if (onContentChange != null) {
                onContentChange.run();
            }
        } catch (RemoteException ex) {
            logger.error("Remote error during content update: {}", ex.getMessage());
            JOptionPane.showMessageDialog(panel,
                "Error updating remote content: " + ex.getMessage(),
                "Remote Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads content for a specific page.
     *
     * @param pageIndex The page index to load (0-based)
     */
    public void loadPage(int pageIndex) {
        this.currentPageIndex = pageIndex;
        List<Page> pages = book.getPages();

        if (pages != null && !pages.isEmpty() && pageIndex < pages.size()) {
            Page currentPage = pages.get(pageIndex);
            textArea.setText(currentPage.getContent());
            calculateMetricsInBackground();

            // Update preview if in preview mode
            if (isPreviewMode) {
                updatePreview();
            }

            logger.info("Loaded content for page {} of remote book '{}'", pageIndex + 1, book.getTitle());
        } else {
            textArea.setText("");
            metricsLabel.setText("Metrics: Words: 0 | Lines: 0 | Avg Word Length: 0.00");

            if (isPreviewMode) {
                markdownRenderer.clear();
            }

            logger.warn("No content available to load for page {} in remote book '{}'",
                pageIndex + 1, book.getTitle());
        }
    }

    /**
     * Gets the current text content.
     *
     * @return Current text in the editor
     */
    public String getText() {
        return textArea.getText();
    }

    /**
     * Sets the text content.
     *
     * @param text Text to set in the editor
     */
    public void setText(String text) {
        textArea.setText(text);
        calculateMetricsInBackground();

        if (isPreviewMode) {
            updatePreview();
        }
    }

    /**
     * Gets the currently selected text.
     *
     * @return Selected text, or null if none selected
     */
    public String getSelectedText() {
        return textArea.getSelectedText();
    }

    /**
     * Replaces the currently selected text.
     *
     * @param replacement Text to replace selection with
     */
    public void replaceSelection(String replacement) {
        textArea.replaceSelection(replacement);

        if (isPreviewMode) {
            updatePreview();
        }
    }

    /**
     * Checks if currently in preview mode.
     *
     * @return true if in preview mode, false if in edit mode
     */
    public boolean isPreviewMode() {
        return isPreviewMode;
    }

    /**
     * Sets the preview mode programmatically.
     *
     * @param preview true for preview mode, false for edit mode
     */
    public void setPreviewMode(boolean preview) {
        if (preview != isPreviewMode) {
            previewToggle.setSelected(preview);
            // Trigger will fire the action listener
        }
    }

    /**
     * Calculates and displays metrics in the background (non-blocking).
     */
    private void calculateMetricsInBackground() {
        SwingWorker<Void, String> metricsWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String content = textArea.getText();
                int wordCount = calculateWordCount(content);
                int lineCount = calculateLineCount(content);
                double averageWordLength = calculateAverageWordLength(content);

                String metrics = String.format("Metrics: Words: %d | Lines: %d | Avg Word Length: %.2f",
                    wordCount, lineCount, averageWordLength);
                publish(metrics);

                return null;
            }

            @Override
            protected void process(List<String> metrics) {
                if (!metrics.isEmpty()) {
                    metricsLabel.setText(metrics.get(metrics.size() - 1));
                }
            }
        };

        metricsWorker.execute();
    }

    private int calculateWordCount(String content) {
        return content.trim().isEmpty() ? 0 : content.split("\\s+").length;
    }

    private int calculateLineCount(String content) {
        return content.isEmpty() ? 0 : content.split("\n").length;
    }

    private double calculateAverageWordLength(String content) {
        String[] words = content.trim().isEmpty() ? new String[0] : content.split("\\s+");
        if (words.length == 0) return 0;

        int totalLength = 0;
        for (String word : words) {
            totalLength += word.length();
        }
        return (double) totalLength / words.length;
    }

    @Override
    public void dispose() {
        if (contentUpdateTimer != null) {
            contentUpdateTimer.stop();
        }
        if (markdownRenderer != null) {
            markdownRenderer.dispose();
        }
        logger.debug("RemoteContentEditorPanelWithMarkdown disposed");
    }
}
