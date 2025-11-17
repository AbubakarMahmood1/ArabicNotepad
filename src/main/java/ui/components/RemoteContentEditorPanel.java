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
 * Content editor panel for remote book operations.
 * Handles RemoteException appropriately for RMI calls.
 *
 * Similar to ContentEditorPanel but adapted for remote operations.
 */
public class RemoteContentEditorPanel extends BaseUIComponent {

    private static final int DEBOUNCE_DELAY_MS = 2000;

    private final Book book;
    private final RemoteBookFacade remoteFacade;
    private final Runnable onContentChange;

    private JTextArea textArea;
    private JLabel metricsLabel;
    private Timer contentUpdateTimer;
    private int currentPageIndex;

    public RemoteContentEditorPanel(Book book, RemoteBookFacade remoteFacade, Runnable onContentChange) {
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
        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        metricsLabel = new JLabel("Metrics: Words: 0 | Lines: 0 | Avg Word Length: 0.00");
        metricsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(metricsLabel, BorderLayout.SOUTH);
    }

    @Override
    protected void attachListeners() {
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                restartContentUpdateTimer();
                calculateMetricsInBackground();
            }
        });
    }

    private void initializeContentUpdateDebouncer() {
        contentUpdateTimer = new Timer(DEBOUNCE_DELAY_MS, e -> handleRealTimeContentUpdate());
        contentUpdateTimer.setRepeats(false);
        logger.debug("Remote content update debouncer initialized with {}ms delay", DEBOUNCE_DELAY_MS);
    }

    private void restartContentUpdateTimer() {
        if (contentUpdateTimer.isRunning()) {
            contentUpdateTimer.restart();
        } else {
            contentUpdateTimer.start();
        }
    }

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
                logger.info("Added new page to book '{}' (debounced, remote)", book.getTitle());
            } else if (currentPageIndex < pages.size()) {
                Page currentPage = pages.get(currentPageIndex);
                currentPage.setContent(textArea.getText());
                remoteFacade.updateBook(book);
                logger.info("Updated content of page {} in book '{}' (debounced, remote)",
                    currentPageIndex + 1, book.getTitle());
            }

            if (onContentChange != null) {
                onContentChange.run();
            }
        } catch (RemoteException e) {
            logger.error("Remote error updating content for book '{}'", book.getTitle(), e);
            JOptionPane.showMessageDialog(
                panel,
                "Error saving changes: " + e.getMessage(),
                "Remote Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

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
        }
    }

    public String getText() {
        return textArea.getText();
    }

    public String getSelectedText() {
        return textArea.getSelectedText();
    }

    public void replaceSelection(String replacement) {
        textArea.replaceSelection(replacement);
    }

    private void calculateMetricsInBackground() {
        SwingWorker<Void, String> metricsWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String content = textArea.getText();
                int wordCount = content.trim().isEmpty() ? 0 : content.split("\\s+").length;
                int lineCount = content.isEmpty() ? 0 : content.split("\n").length;

                String[] words = content.trim().isEmpty() ? new String[0] : content.split("\\s+");
                double avgLength = 0;
                if (words.length > 0) {
                    int totalLength = 0;
                    for (String word : words) {
                        totalLength += word.length();
                    }
                    avgLength = (double) totalLength / words.length;
                }

                String metrics = String.format("Metrics: Words: %d | Lines: %d | Avg Word Length: %.2f",
                    wordCount, lineCount, avgLength);
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

    @Override
    public void dispose() {
        if (contentUpdateTimer != null) {
            contentUpdateTimer.stop();
        }
    }
}
