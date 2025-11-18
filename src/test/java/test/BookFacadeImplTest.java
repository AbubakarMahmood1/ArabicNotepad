package test;

import bl.BookFacade;
import bl.BookFacadeImpl;
import dao.BookDAO;
import dto.Book;
import dto.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for BookFacadeImpl.
 * Uses Mockito to mock DAO layer.
 */
class BookFacadeImplTest {

    @Mock
    private BookDAO mockDAO;

    private BookFacade bookFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookFacade = new BookFacadeImpl(mockDAO);
    }

    // ==================== Book CRUD Tests ====================

    @Test
    @DisplayName("Should add book successfully")
    void testAddBook() {
        Book book = createTestBook("Test Book");
        when(mockDAO.addBook(any(Book.class))).thenReturn(true);

        boolean result = bookFacade.addBook(book);

        assertTrue(result, "Should add book successfully");
        verify(mockDAO, times(1)).addBook(book);
    }

    @Test
    @DisplayName("Should fail to add null book")
    void testAddNullBook() {
        boolean result = bookFacade.addBook(null);

        assertFalse(result, "Should not add null book");
        verify(mockDAO, never()).addBook(any());
    }

    @Test
    @DisplayName("Should get all books")
    void testGetAllBooks() {
        List<Book> books = createTestBookList(5);
        when(mockDAO.getAllBooks()).thenReturn(books);

        List<Book> result = bookFacade.getAllBooks();

        assertNotNull(result, "Result should not be null");
        assertEquals(5, result.size(), "Should return 5 books");
        verify(mockDAO, times(1)).getAllBooks();
    }

    @Test
    @DisplayName("Should get book by name")
    void testGetBookByName() {
        Book book = createTestBook("My Book");
        when(mockDAO.getBookByName("My Book")).thenReturn(book);

        Book result = bookFacade.getBookByName("My Book");

        assertNotNull(result, "Book should not be null");
        assertEquals("My Book", result.getTitle(), "Should return correct book");
        verify(mockDAO, times(1)).getBookByName("My Book");
    }

    @Test
    @DisplayName("Should return null for non-existent book")
    void testGetNonExistentBook() {
        when(mockDAO.getBookByName("Non Existent")).thenReturn(null);

        Book result = bookFacade.getBookByName("Non Existent");

        assertNull(result, "Should return null for non-existent book");
    }

    @Test
    @DisplayName("Should update book")
    void testUpdateBook() {
        Book book = createTestBook("Updated Book");
        when(mockDAO.updateBook(any(Book.class))).thenReturn(true);

        boolean result = bookFacade.updateBook(book);

        assertTrue(result, "Should update book successfully");
        verify(mockDAO, times(1)).updateBook(book);
    }

    @Test
    @DisplayName("Should delete book")
    void testDeleteBook() {
        when(mockDAO.deleteBook("Book to Delete")).thenReturn(true);

        boolean result = bookFacade.deleteBook("Book to Delete");

        assertTrue(result, "Should delete book successfully");
        verify(mockDAO, times(1)).deleteBook("Book to Delete");
    }

    // ==================== Page CRUD Tests ====================

    @Test
    @DisplayName("Should add page by book title")
    void testAddPageByBookTitle() {
        Page page = createTestPage(1, "Page content");
        when(mockDAO.addPageByBookTitle(anyString(), any(Page.class))).thenReturn(true);

        boolean result = bookFacade.addPageByBookTitle("Test Book", page);

        assertTrue(result, "Should add page successfully");
        verify(mockDAO, times(1)).addPageByBookTitle("Test Book", page);
    }

    @Test
    @DisplayName("Should get pages by book title")
    void testGetPagesByBookTitle() {
        List<Page> pages = createTestPageList(3);
        when(mockDAO.getPagesByBookTitle("Test Book")).thenReturn(pages);

        List<Page> result = bookFacade.getPagesByBookTitle("Test Book");

        assertNotNull(result, "Pages should not be null");
        assertEquals(3, result.size(), "Should return 3 pages");
        verify(mockDAO, times(1)).getPagesByBookTitle("Test Book");
    }

    @Test
    @DisplayName("Should delete pages by book title")
    void testDeletePagesByBookTitle() {
        doNothing().when(mockDAO).deletePagesByBookTitle("Test Book");

        assertDoesNotThrow(() -> bookFacade.deletePagesByBookTitle("Test Book"),
            "Should delete pages without error");
        verify(mockDAO, times(1)).deletePagesByBookTitle("Test Book");
    }

    // ==================== Search Tests ====================

    @Test
    @DisplayName("Should search books by content")
    void testSearchBooksByContent() {
        List<String> searchResults = List.of(
            "Title: Book1, Sentence: Found text here",
            "Title: Book2, Sentence: Found text there"
        );
        when(mockDAO.searchBooksByContent("test")).thenReturn(searchResults);

        List<String> result = bookFacade.searchBooksByContent("test");

        assertNotNull(result, "Results should not be null");
        assertEquals(2, result.size(), "Should return 2 results");
        verify(mockDAO, times(1)).searchBooksByContent("test");
    }

    @Test
    @DisplayName("Should return empty list for no search results")
    void testSearchWithNoResults() {
        when(mockDAO.searchBooksByContent("nonexistent")).thenReturn(new ArrayList<>());

        List<String> result = bookFacade.searchBooksByContent("nonexistent");

        assertNotNull(result, "Results should not be null");
        assertTrue(result.isEmpty(), "Should return empty list");
    }

    // ==================== Database Connection Tests ====================

    @Test
    @DisplayName("Should check database connection")
    void testIsDatabaseConnected() {
        when(mockDAO.isDatabaseConnected()).thenReturn(true);

        boolean result = bookFacade.isDatabaseConnected();

        assertTrue(result, "Should be connected");
        verify(mockDAO, times(1)).isDatabaseConnected();
    }

    @Test
    @DisplayName("Should handle disconnected database")
    void testDatabaseDisconnected() {
        when(mockDAO.isDatabaseConnected()).thenReturn(false);

        boolean result = bookFacade.isDatabaseConnected();

        assertFalse(result, "Should be disconnected");
    }

    // ==================== Export Tests ====================

    @Test
    @DisplayName("Should export book by title")
    void testExportBookByTitle() {
        when(mockDAO.isDatabaseConnected()).thenReturn(true);

        assertDoesNotThrow(() -> bookFacade.exportBook("Test Book"),
            "Should export book without error");
    }

    @Test
    @DisplayName("Should export book object")
    void testExportBookObject() {
        Book book = createTestBook("Export Book");

        assertDoesNotThrow(() -> bookFacade.exportBook(book),
            "Should export book object without error");
    }

    // ==================== Transliteration Tests ====================

    @Test
    @DisplayName("Should transliterate text")
    void testTransliterate() {
        String arabicText = "كتاب";

        String result = bookFacade.transliterate(arabicText);

        assertNotNull(result, "Result should not be null");
        // Actual transliteration logic would be tested here
    }

    @Test
    @DisplayName("Should handle null transliteration")
    void testTransliterateNull() {
        String result = bookFacade.transliterate(null);

        // Should handle null gracefully
        assertTrue(result == null || result.isEmpty(),
            "Should handle null input");
    }

    @Test
    @DisplayName("Should handle empty transliteration")
    void testTransliterateEmpty() {
        String result = bookFacade.transliterate("");

        assertTrue(result == null || result.isEmpty(),
            "Should handle empty input");
    }

    // ==================== Word Analysis Tests ====================

    @Test
    @DisplayName("Should analyze word")
    void testAnalyzeWord() {
        String word = "test";

        String result = bookFacade.analyzeWord(word);

        assertNotNull(result, "Analysis result should not be null");
        // Actual analysis logic would be tested here
    }

    @Test
    @DisplayName("Should handle null word analysis")
    void testAnalyzeNullWord() {
        String result = bookFacade.analyzeWord(null);

        assertTrue(result == null || result.isEmpty(),
            "Should handle null word");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle empty book list")
    void testEmptyBookList() {
        when(mockDAO.getAllBooks()).thenReturn(new ArrayList<>());

        List<Book> result = bookFacade.getAllBooks();

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Should return empty list");
    }

    @Test
    @DisplayName("Should handle DAO exceptions gracefully")
    void testDAOException() {
        when(mockDAO.addBook(any())).thenThrow(new RuntimeException("Database error"));

        Book book = createTestBook("Error Book");

        assertDoesNotThrow(() -> {
            boolean result = bookFacade.addBook(book);
            assertFalse(result, "Should return false on exception");
        }, "Should handle DAO exceptions gracefully");
    }

    @Test
    @DisplayName("Should handle concurrent operations")
    void testConcurrentOperations() throws InterruptedException {
        when(mockDAO.getAllBooks()).thenReturn(createTestBookList(10));

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                bookFacade.getAllBooks();
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        verify(mockDAO, times(5)).getAllBooks();
    }

    // ==================== Helper Methods ====================

    private Book createTestBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setId(1);
        book.setIdauthor("Author1");
        book.setPages(createTestPageList(3));
        return book;
    }

    private List<Book> createTestBookList(int count) {
        List<Book> books = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            books.add(createTestBook("Book " + i));
        }
        return books;
    }

    private Page createTestPage(int pageNumber, String content) {
        Page page = new Page();
        page.setPageNumber(pageNumber);
        page.setContent(content);
        page.setBookId(1);
        return page;
    }

    private List<Page> createTestPageList(int count) {
        List<Page> pages = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            pages.add(createTestPage(i, "Content of page " + i));
        }
        return pages;
    }
}
