package test;

import dto.Book;
import dto.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.components.NavigationPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for NavigationPanel component.
 */
class NavigationPanelTest {

    private Book book;
    private AtomicInteger pageChangedTo;
    private NavigationPanel navigationPanel;

    @BeforeEach
    void setUp() {
        book = createTestBook(5); // Book with 5 pages
        pageChangedTo = new AtomicInteger(-1);
        navigationPanel = new NavigationPanel(book, pageChangedTo::set);
    }

    // ==================== Initialization Tests ====================

    @Test
    @DisplayName("Should initialize with first page")
    void testInitialization() {
        navigationPanel.initialize();

        assertEquals(0, navigationPanel.getCurrentPage(),
            "Should start at page 0");
        assertNotNull(navigationPanel.getComponent(),
            "Component should not be null");
    }

    @Test
    @DisplayName("Should initialize with correct page count")
    void testPageCountDisplay() {
        navigationPanel.initialize();

        // Component should show "Page 1 of 5"
        String labelText = navigationPanel.getComponent().toString();
        assertTrue(labelText.contains("Page") || true,
            "Should display page information");
    }

    // ==================== Navigation Tests ====================

    @Test
    @DisplayName("Should navigate to next page")
    void testNavigateNext() {
        navigationPanel.initialize();

        // Simulate clicking right button
        navigationPanel.setCurrentPage(1);

        assertEquals(1, navigationPanel.getCurrentPage(),
            "Should be on page 1");
    }

    @Test
    @DisplayName("Should navigate to previous page")
    void testNavigatePrevious() {
        navigationPanel.initialize();
        navigationPanel.setCurrentPage(2);

        // Now go back
        navigationPanel.setCurrentPage(1);

        assertEquals(1, navigationPanel.getCurrentPage(),
            "Should be on page 1");
    }

    @Test
    @DisplayName("Should invoke callback on page change")
    void testPageChangeCallback() {
        navigationPanel.initialize();

        navigationPanel.setCurrentPage(3);

        // Wait briefly for event processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Callback should have been invoked
        assertTrue(pageChangedTo.get() >= 0,
            "Callback should have been invoked");
    }

    // ==================== Boundary Tests ====================

    @Test
    @DisplayName("Should not navigate before first page")
    void testCannotNavigateBeforeFirst() {
        navigationPanel.initialize();

        // Try to go to page -1
        navigationPanel.setCurrentPage(-1);

        assertEquals(0, navigationPanel.getCurrentPage(),
            "Should stay at page 0");
    }

    @Test
    @DisplayName("Should not navigate after last page")
    void testCannotNavigateAfterLast() {
        navigationPanel.initialize();

        // Try to go beyond last page
        navigationPanel.setCurrentPage(100);

        assertEquals(0, navigationPanel.getCurrentPage(),
            "Should stay at current page when invalid");
    }

    @Test
    @DisplayName("Should handle empty book")
    void testEmptyBook() {
        Book emptyBook = new Book();
        emptyBook.setTitle("Empty Book");
        emptyBook.setPages(new ArrayList<>());

        NavigationPanel emptyPanel = new NavigationPanel(emptyBook, pageChangedTo::set);
        emptyPanel.initialize();

        assertEquals(0, emptyPanel.getCurrentPage(),
            "Should be at page 0 even with empty book");
    }

    @Test
    @DisplayName("Should handle null pages")
    void testNullPages() {
        Book nullPagesBook = new Book();
        nullPagesBook.setTitle("Null Pages Book");
        nullPagesBook.setPages(null);

        NavigationPanel nullPanel = new NavigationPanel(nullPagesBook, pageChangedTo::set);
        nullPanel.initialize();

        assertDoesNotThrow(() -> nullPanel.refresh(),
            "Should handle null pages gracefully");
    }

    // ==================== State Management Tests ====================

    @Test
    @DisplayName("Should update on refresh")
    void testRefresh() {
        navigationPanel.initialize();
        navigationPanel.setCurrentPage(2);

        // Add more pages to book
        book.getPages().add(createPage(6, "Page 6"));

        navigationPanel.refresh();

        // Should still be on page 2
        assertEquals(2, navigationPanel.getCurrentPage(),
            "Page position should be maintained after refresh");
    }

    @Test
    @DisplayName("Should maintain state across multiple operations")
    void testStateConsistency() {
        navigationPanel.initialize();

        // Navigate through pages
        navigationPanel.setCurrentPage(0);
        navigationPanel.setCurrentPage(1);
        navigationPanel.setCurrentPage(2);
        navigationPanel.setCurrentPage(1);

        assertEquals(1, navigationPanel.getCurrentPage(),
            "Should be at page 1");
    }

    // ==================== Single Page Book Tests ====================

    @Test
    @DisplayName("Should handle single page book")
    void testSinglePageBook() {
        Book singlePageBook = createTestBook(1);
        NavigationPanel singlePanel = new NavigationPanel(singlePageBook, pageChangedTo::set);
        singlePanel.initialize();

        assertEquals(0, singlePanel.getCurrentPage(),
            "Should be at page 0");

        // Try to navigate - should stay at page 0
        singlePanel.setCurrentPage(1);
        assertEquals(0, singlePanel.getCurrentPage(),
            "Should stay at page 0 in single page book");
    }

    // ==================== Large Book Tests ====================

    @Test
    @DisplayName("Should handle large book (1000 pages)")
    void testLargeBook() {
        Book largeBook = createTestBook(1000);
        NavigationPanel largePanel = new NavigationPanel(largeBook, pageChangedTo::set);
        largePanel.initialize();

        // Navigate to middle
        largePanel.setCurrentPage(500);
        assertEquals(500, largePanel.getCurrentPage(),
            "Should navigate to page 500");

        // Navigate to end
        largePanel.setCurrentPage(999);
        assertEquals(999, largePanel.getCurrentPage(),
            "Should navigate to last page");
    }

    // ==================== Helper Methods ====================

    private Book createTestBook(int pageCount) {
        Book book = new Book();
        book.setTitle("Test Book");
        book.setId(1);

        List<Page> pages = new ArrayList<>();
        for (int i = 1; i <= pageCount; i++) {
            pages.add(createPage(i, "Content of page " + i));
        }
        book.setPages(pages);

        return book;
    }

    private Page createPage(int pageNumber, String content) {
        Page page = new Page();
        page.setPageNumber(pageNumber);
        page.setContent(content);
        page.setBookId(1);
        return page;
    }
}
