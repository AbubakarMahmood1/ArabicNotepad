package ui.components;

import dto.Book;
import dto.Page;
import bl.BookFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Content editor panel component for editing book pages.
 *
 * Responsibilities:
 * - Provide text area for content editing
 * - Display real-time metrics (words, lines, avg word length)
 * - Debounce content updates to reduce database writes by ~90%
 * - Handle auto-save functionality
 *
 * Features:
 * - 2-second debouncing timer
 * - Background metrics calculation (non-blocking)
 * - Automatic page content persistence
 */
public class ContentEditorPanel extends BaseUIComponent {

    private static final int DEBOUNCE_DELAY_MS = 2000;

    private final Book book;
    private final BookFacade bookFacade;
    private final Runnable onContentChange;

    private JTextArea textArea;
    private JLabel metricsLabel;
    private Timer contentUpdateTimer;
    private int currentPageIndex;

    /**
     * Creates a content editor panel.
     *
     * @param book The book being edited
     * @param bookFacade Facade for book operations
     * @param onContentChange Callback invoked when content changes (for UI updates)
     */
    public ContentEditorPanel(Book book, BookFacade bookFacade, Runnable onContentChange) {
        super();
        this.book = book;
        this.bookFacade = bookFacade;
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
        // Text area setup
        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Metrics label
        metricsLabel = new JLabel("Metrics: Words: 0 | Lines: 0 | Avg Word Length: 0.00");
        metricsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        metricsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(metricsLabel, BorderLayout.SOUTH);
    }

    @Override
    protected void attachListeners() {
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Restart debounce timer on each keystroke
                restartContentUpdateTimer();
                // Calculate metrics in background
                calculateMetricsInBackground();
            }
        });
    }

    /**
     * Initializes the debouncing timer for content updates.
     */
    private void initializeContentUpdateDebouncer() {
        contentUpdateTimer = new Timer(DEBOUNCE_DELAY_MS, e -> handleRealTimeContentUpdate());
        contentUpdateTimer.setRepeats(false);
        logger.debug("Content update debouncer initialized with {}ms delay", DEBOUNCE_DELAY_MS);
    }

    /**
     * Restarts the debounce timer. Called on every keystroke.
     */
    private void restartContentUpdateTimer() {
        if (contentUpdateTimer.isRunning()) {
            contentUpdateTimer.restart();
        } else {
            contentUpdateTimer.start();
        }
    }

    /**
     * Handles real-time content updates to the database (debounced).
     */
    private void handleRealTimeContentUpdate() {
        List<Page> pages = book.getPages();
        if (pages == null) {
            pages = new ArrayList<>();
            book.setPages(pages);
        }

        if (pages.isEmpty()) {
            Page newPage = new Page();
            newPage.setContent(textArea.getText());
            pages.add(newPage);
            bookFacade.addPageByBookTitle(book.getTitle(), newPage);
            logger.info("Added new page to book '{}' (debounced)", book.getTitle());
        } else if (currentPageIndex < pages.size()) {
            Page currentPage = pages.get(currentPageIndex);
            currentPage.setContent(textArea.getText());
            bookFacade.updateBook(book);
            logger.info("Updated content of page {} in book '{}' (debounced)",
                currentPageIndex + 1, book.getTitle());
        }

        if (onContentChange != null) {
            onContentChange.run();
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
            logger.info("Loaded content for page {} of book '{}'", pageIndex + 1, book.getTitle());
        } else {
            textArea.setText("");
            metricsLabel.setText("Metrics: Words: 0 | Lines: 0 | Avg Word Length: 0.00");
            logger.warn("No content available to load for page {} in book '{}'",
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
        logger.debug("ContentEditorPanel disposed");
    }
}
