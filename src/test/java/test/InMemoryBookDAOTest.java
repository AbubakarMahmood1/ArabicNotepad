package test;


import dto.Book;
import dto.Page;
import config.DBConfig;
import dao.InMemoryBookDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryBookDAOTest {

    private InMemoryBookDAO inMemoryBookDAO;
    private DBConfig mockDBConfig;

    @BeforeEach
    void setUp() {
        inMemoryBookDAO = new InMemoryBookDAO();
        mockDBConfig = mock(DBConfig.class);
        inMemoryBookDAO.clear(); // Ensure clean state before each test
    }

    @Test
    @DisplayName("Database Connection Check")
    void testDatabaseConnection() {
        assertTrue(inMemoryBookDAO.isDatabaseConnected(), "Database should always be connected in InMemoryBookDAO");
        assertTrue(inMemoryBookDAO.connect(mockDBConfig), "Connection should always succeed");
    }

    @Test
    @DisplayName("Add Book - Successful Addition")
    void testAddBook_Success() {
        Book book = createTestBook("Test Book", "test-hash");
        
        boolean result = inMemoryBookDAO.addBook(book, false);
        
        assertTrue(result, "Book should be added successfully");
        assertEquals(1, book.getId(), "Book should be assigned an ID");
    }

    @Test
    @DisplayName("Add Book - Duplicate Hash Prevention")
    void testAddBook_DuplicateHash() {
        Book book1 = createTestBook("Book 1", "unique-hash");
        Book book2 = createTestBook("Book 2", "unique-hash");
        
        assertTrue(inMemoryBookDAO.addBook(book1, false), "First book should be added");
        assertFalse(inMemoryBookDAO.addBook(book2, false), "Book with duplicate hash should not be added");
    }

    @Test
    @DisplayName("Get Book By Name - Existing Book")
    void testGetBookByName_Exists() {
        Book book = createTestBook("Existing Book", "hash-1");
        inMemoryBookDAO.addBook(book, false);
        
        Book retrievedBook = inMemoryBookDAO.getBookByName("Existing Book");
        
        assertNotNull(retrievedBook, "Book should be retrieved by name");
        assertEquals("Existing Book", retrievedBook.getTitle());
    }

    @Test
    @DisplayName("Get Book By Name - Case Insensitive")
    void testGetBookByName_CaseInsensitive() {
        Book book = createTestBook("Mixed Case Book", "hash-2");
        inMemoryBookDAO.addBook(book, false);
        
        Book retrievedBook = inMemoryBookDAO.getBookByName("mixed case book");
        
        assertNotNull(retrievedBook, "Book should be retrieved case-insensitively");
    }

    @Test
    @DisplayName("Get Book By Name - Non-Existing Book")
    void testGetBookByName_NotExists() {
        Book retrievedBook = inMemoryBookDAO.getBookByName("Non-Existent Book");
        
        assertNull(retrievedBook, "Non-existent book should return null");
    }

    @Test
    @DisplayName("Update Book - Successful Update")
    void testUpdateBook_Success() {
        Book book = createTestBook("Original Book", "hash-3");
        inMemoryBookDAO.addBook(book, false);
        
        book.setTitle("Updated Book Title");
        boolean updateResult = inMemoryBookDAO.updateBook(book);
        
        assertTrue(updateResult, "Book should be updated successfully");
        assertEquals("Updated Book Title", 
            inMemoryBookDAO.getBookByName("Updated Book Title").getTitle());
    }

    @Test
    @DisplayName("Update Book - Non-Existing Book")
    void testUpdateBook_NonExisting() {
        Book book = createTestBook("Non-Existing Book", "hash-4");
        book.setId(999); // Set an ID that doesn't exist
        
        boolean updateResult = inMemoryBookDAO.updateBook(book);
        
        assertFalse(updateResult, "Update should fail for non-existing book");
    }

    @Test
    @DisplayName("Delete Book - Successful Deletion")
    void testDeleteBook_Success() {
        Book book = createTestBook("Book to Delete", "hash-5");
        inMemoryBookDAO.addBook(book, false);
        
        boolean deleteResult = inMemoryBookDAO.deleteBook("Book to Delete");
        
        assertTrue(deleteResult, "Book should be deleted successfully");
        assertNull(inMemoryBookDAO.getBookByName("Book to Delete"), 
            "Deleted book should not be retrievable");
    }

    @Test
    @DisplayName("Delete Book - Non-Existing Book")
    void testDeleteBook_NonExisting() {
        boolean deleteResult = inMemoryBookDAO.deleteBook("Non-Existent Book");
        
        assertFalse(deleteResult, "Deleting non-existing book should return false");
    }

    @Test
    @DisplayName("Add Page to Book")
    void testAddPage_Success() {
        Book book = createTestBook("Book for Pages", "hash-6");
        inMemoryBookDAO.addBook(book, false);
        
        Page page = new Page();
        page.setContent("Test Page Content");
        page.setPageNumber(1);
        
        boolean addPageResult = inMemoryBookDAO.addPage(book.getId(), page);
        
        assertTrue(addPageResult, "Page should be added successfully");
    }

    @Test
    @DisplayName("Get Pages by Book Title")
    void testGetPagesByBookTitle() {
        Book book = createTestBook("Book with Pages", "hash-7");
        inMemoryBookDAO.addBook(book, false);
        
        Page page1 = new Page();
        page1.setContent("Page 1 Content");
        page1.setPageNumber(1);
        page1.setBookId(book.getId());
        
        Page page2 = new Page();
        page2.setContent("Page 2 Content");
        page2.setPageNumber(2);
        page2.setBookId(book.getId());
        
        inMemoryBookDAO.addPage(book.getId(), page1);
        inMemoryBookDAO.addPage(book.getId(), page2);
        
        List<Page> pages = inMemoryBookDAO.getPagesByBookTitle("Book with Pages");
        
        assertEquals(2, pages.size(), "Should retrieve all pages for the book");
    }

    @Test
    @DisplayName("Search Books by Content")
    void testSearchBooksByContent() {
        Book book = createTestBook("Search Book", "hash-8");
        Page page = new Page();
        page.setContent("This is a test content with searchable text");
        book.setPages(List.of(page));
        inMemoryBookDAO.addBook(book, false);
        
        List<String> searchResults = inMemoryBookDAO.searchBooksByContent("searchable");
        
        assertFalse(searchResults.isEmpty(), "Search should return results");
        assertTrue(searchResults.get(0).contains("Search Book"), 
            "Search result should contain book title");
    }

    // Helper method to create a test book
    private Book createTestBook(String title, String hash) {
        Book book = new Book();
        book.setTitle(title);
        book.setHash(hash);
        return book;
    }
}