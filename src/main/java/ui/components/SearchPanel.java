package ui.components;

import dto.Book;
import dto.Page;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Search panel component for searching content within book pages.
 *
 * Responsibilities:
 * - Provide search field and button
 * - Search through all pages for matching content
 * - Navigate to first matching page
 * - Display search results feedback
 */
public class SearchPanel extends BaseUIComponent {

    private final Book book;
    private final Consumer<Integer> onSearchResult;

    private JTextField searchField;
    private JButton searchButton;
    private JLabel statusLabel;

    /**
     * Creates a search panel for the given book.
     *
     * @param book The book to search within
     * @param onSearchResult Callback invoked when search finds a match (passes page index)
     */
    public SearchPanel(Book book, Consumer<Integer> onSearchResult) {
        super();
        this.book = book;
        this.onSearchResult = onSearchResult;
    }

    @Override
    protected void setupLayout() {
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    }

    @Override
    protected void buildUI() {
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));

        searchField = new JTextField(20);
        searchField.setToolTipText("Enter search term");

        searchButton = new JButton("Search");
        searchButton.setToolTipText("Search through all pages");

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(statusLabel);
    }

    @Override
    protected void attachListeners() {
        searchButton.addActionListener(e -> performSearch());

        // Allow Enter key to trigger search
        searchField.addActionListener(e -> performSearch());
    }

    /**
     * Performs search through all book pages.
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            setStatus("Please enter a search term", Color.RED);
            logger.warn("Empty search term provided for book '{}'", book.getTitle());
            return;
        }

        List<Page> pages = book.getPages();
        if (pages == null || pages.isEmpty()) {
            setStatus("No pages to search", Color.RED);
            logger.warn("No pages available to search in book '{}'", book.getTitle());
            return;
        }

        // Search through all pages
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            if (page.getContent() != null && page.getContent().contains(searchTerm)) {
                // Found match
                onSearchResult.accept(i);
                setStatus(String.format("Found on page %d", i + 1), new Color(0, 128, 0));
                logger.info("Search term '{}' found on page {} of book '{}'",
                    searchTerm, i + 1, book.getTitle());

                // Highlight search term in the field
                searchField.selectAll();
                return;
            }
        }

        // No match found
        setStatus("No matches found", Color.RED);
        logger.warn("Search term '{}' not found in book '{}'", searchTerm, book.getTitle());

        JOptionPane.showMessageDialog(
            panel,
            "No matches found for: " + searchTerm,
            "Search Results",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Sets the status label text and color.
     *
     * @param message Status message
     * @param color Text color
     */
    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);

        // Clear status after 3 seconds
        Timer clearTimer = new Timer(3000, e -> statusLabel.setText(""));
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    /**
     * Clears the search field.
     */
    public void clearSearch() {
        searchField.setText("");
        statusLabel.setText("");
    }

    /**
     * Gets the current search term.
     *
     * @return Current search term in the field
     */
    public String getSearchTerm() {
        return searchField.getText().trim();
    }
}
