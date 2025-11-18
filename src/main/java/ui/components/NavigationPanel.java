package ui.components;

import dto.Book;
import dto.Page;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Navigation panel component for page navigation in the book editor.
 * Provides left/right buttons and displays current page number.
 *
 * Responsibilities:
 * - Display current page number and total pages
 * - Enable/disable navigation buttons based on current position
 * - Notify listeners when page navigation occurs
 */
public class NavigationPanel extends BaseUIComponent {

    private final Book book;
    private final Consumer<Integer> onPageChange;

    private JButton leftButton;
    private JButton rightButton;
    private JLabel pageNumberLabel;
    private int currentPageIndex;

    /**
     * Creates a navigation panel for the given book.
     *
     * @param book The book to navigate
     * @param onPageChange Callback invoked when page changes (passes new page index)
     */
    public NavigationPanel(Book book, Consumer<Integer> onPageChange) {
        super();
        this.book = book;
        this.onPageChange = onPageChange;
        this.currentPageIndex = 0;
    }

    @Override
    protected void setupLayout() {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
    }

    @Override
    protected void buildUI() {
        leftButton = new JButton("←");
        leftButton.setToolTipText("Previous Page");
        leftButton.setPreferredSize(new Dimension(50, 30));

        rightButton = new JButton("→");
        rightButton.setToolTipText("Next Page");
        rightButton.setPreferredSize(new Dimension(50, 30));

        int totalPages = book.getPages() != null ? book.getPages().size() : 0;
        pageNumberLabel = new JLabel(formatPageLabel(currentPageIndex, totalPages));
        pageNumberLabel.setPreferredSize(new Dimension(120, 30));
        pageNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(leftButton);
        panel.add(pageNumberLabel);
        panel.add(rightButton);

        updateNavigationState();
    }

    @Override
    protected void attachListeners() {
        leftButton.addActionListener(e -> navigatePage(-1));
        rightButton.addActionListener(e -> navigatePage(1));
    }

    /**
     * Navigates to a page by relative offset.
     *
     * @param direction -1 for previous, +1 for next
     */
    private void navigatePage(int direction) {
        int newIndex = currentPageIndex + direction;
        List<Page> pages = book.getPages();

        if (pages != null && newIndex >= 0 && newIndex < pages.size()) {
            currentPageIndex = newIndex;
            updateNavigationState();
            onPageChange.accept(currentPageIndex);
            logger.info("Navigated to page {} of book '{}'", currentPageIndex + 1, book.getTitle());
        }
    }

    /**
     * Sets the current page index externally (e.g., from search).
     *
     * @param pageIndex The page index to navigate to
     */
    public void setCurrentPage(int pageIndex) {
        List<Page> pages = book.getPages();
        if (pages != null && pageIndex >= 0 && pageIndex < pages.size()) {
            this.currentPageIndex = pageIndex;
            updateNavigationState();
        }
    }

    /**
     * Gets the current page index.
     *
     * @return Current page index (0-based)
     */
    public int getCurrentPage() {
        return currentPageIndex;
    }

    @Override
    protected void updateUI() {
        updateNavigationState();
    }

    /**
     * Updates navigation button states and page label.
     */
    private void updateNavigationState() {
        List<Page> pages = book.getPages();
        int totalPages = pages != null ? pages.size() : 0;

        leftButton.setEnabled(currentPageIndex > 0);
        rightButton.setEnabled(pages != null && currentPageIndex < totalPages - 1);

        pageNumberLabel.setText(formatPageLabel(currentPageIndex, totalPages));
    }

    /**
     * Formats the page number label.
     *
     * @param current Current page index (0-based)
     * @param total Total number of pages
     * @return Formatted string like "Page 1 of 10"
     */
    private String formatPageLabel(int current, int total) {
        if (total == 0) {
            return "No pages available";
        }
        return String.format("Page %d of %d", current + 1, total);
    }
}
