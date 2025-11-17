package test;

import bl.BookService;
import config.ConfigurationManager;
import config.Environment;
import config.EnvironmentManager;
import config.UserConfig;
import dao.BookDAO;
import dao.LocalStorageBookDAO;
import dto.Book;
import dto.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookDAO mockBookDAO;

    @Mock
    private ConfigurationManager mockConfigManager;

    @Mock
    private UserConfig mockUserConfig;

    @Mock
    private LocalStorageBookDAO mockLocalStorageBookDAO;

    private BookService bookService;

    @BeforeEach
    void setUp() throws RemoteException {
        MockitoAnnotations.openMocks(this);

        Environment env = EnvironmentManager.getCurrentEnvironment();
        try {
            ConfigurationManager.getInstance(env);
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(BookServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        when(mockConfigManager.getCurrentEnvironment()).thenReturn(Environment.TESTING);
        when(mockConfigManager.getUserConfig()).thenReturn(mockUserConfig);
        when(mockUserConfig.getUserId()).thenReturn("testUser");

        bookService = new BookService(mockBookDAO);
    }

    @Test
    void testHasWritePrivileges_UserIsAuthor_ReturnsTrue() {
        Book book = new Book();
        book.setIdauthor("testUser");
        book.setTitle("Test Book");

        when(mockBookDAO.getBookByName("Test Book")).thenReturn(book);

        assertTrue(bookService.hasWritePrivileges("Test Book"));
    }

    @Test
    void testHasWritePrivileges_UserNotAuthor_ReturnsFalse() {
        Book book = new Book();
        book.setIdauthor("otherUser");
        book.setTitle("Test Book");

        when(mockBookDAO.getBookByName("Test Book")).thenReturn(book);

        assertFalse(bookService.hasWritePrivileges("Test Book"));
    }

    @Test
    void testImportBook_Directory_Success() {
        String dirPath = "/path/to/books";
        List<Book> books = new ArrayList<>();
        Book book1 = new Book();
        book1.setTitle("Book 1");
        book1.setHash("hash1");
        books.add(book1);

        when(mockLocalStorageBookDAO.getAllBooks(dirPath)).thenReturn(books);
        when(mockBookDAO.isHashExists("hash1")).thenReturn(false);

        bookService.importBook(dirPath);

        verify(mockBookDAO).addBook(book1, true);
    }

    @Test
    void testImportBook_SingleFile_Success() {
        String filePath = "/path/to/book.txt";
        File mockFile = mock(File.class);
        when(mockFile.isFile()).thenReturn(true);
        when(mockFile.getAbsolutePath()).thenReturn(filePath);

        Book book = new Book();
        book.setTitle("Single Book");
        book.setHash("uniqueHash");

        when(mockLocalStorageBookDAO.getBookByName(filePath)).thenReturn(book);
        when(mockBookDAO.isHashExists("uniqueHash")).thenReturn(false);

        bookService.importBook(filePath);

        verify(mockBookDAO).addBook(book, true);
    }

    @Test
    void testGetBookListFromDB_Success() {
        List<Book> expectedBooks = new ArrayList<>();
        Book book1 = new Book();
        book1.setTitle("Book 1");
        expectedBooks.add(book1);

        when(mockBookDAO.getAllBooks(null)).thenReturn(expectedBooks);

        List<Book> retrievedBooks = bookService.getBookListFromDB();

        assertEquals(expectedBooks, retrievedBooks);
    }

    @Test
    void testGetBookByName_Exists() {
        Book expectedBook = new Book();
        expectedBook.setTitle("Test Book");

        when(mockBookDAO.getBookByName("Test Book")).thenReturn(expectedBook);

        Book retrievedBook = bookService.getBookByName("Test Book");

        assertEquals(expectedBook, retrievedBook);
    }

    @Test
    void testDeleteBook_FromDB_Success() {
        String bookTitle = "Book to Delete";

        when(mockBookDAO.deleteBook(bookTitle)).thenReturn(true);

        assertTrue(bookService.deleteBook(bookTitle));
    }

    @Test
    void testDeleteBook_FromLocalStorage_Success() {
        String filePath = "/path/to/book.txt";
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.isFile()).thenReturn(true);

        when(mockLocalStorageBookDAO.deleteBook(filePath)).thenReturn(true);

        assertTrue(bookService.deleteBook(filePath));
    }

    @Test
    void testExportBook_Success() {
        Book book = new Book();
        book.setTitle("Book to Export");

        when(mockBookDAO.getBookByName("Book to Export")).thenReturn(book);
        when(mockLocalStorageBookDAO.addBook(book, true)).thenReturn(true);

        assertTrue(bookService.exportBook("Book to Export"));
    }

    @Test
    void testUpdateBook_Success() {
        Book book = new Book();
        book.setTitle("Book to Update");

        when(mockBookDAO.updateBook(book)).thenReturn(true);

        assertTrue(bookService.updateBook(book));
    }

    @Test
    void testIsDatabaseConnected() {
        when(mockBookDAO.isDatabaseConnected()).thenReturn(true);

        assertTrue(bookService.isDatabaseConnected());
    }

    @Test
    void testSearchBooksByContent_Success() {
        String searchText = "test search";
        List<String> expectedResults = new ArrayList<>();
        expectedResults.add("Book 1");
        expectedResults.add("Book 2");

        when(mockBookDAO.searchBooksByContent(searchText)).thenReturn(expectedResults);

        List<String> results = bookService.searchBooksByContent(searchText);

        assertEquals(expectedResults, results);
    }

    @Test
    void testAddPage_Success() {
        Book book = new Book();
        book.setId(123);
        book.setTitle("Book for Page");

        Page page = new Page();
        page.setContent("Test Page Content");

        when(mockBookDAO.getBookByName("Book for Page")).thenReturn(book);
        when(mockBookDAO.addPage(book.getId(), page)).thenReturn(true);

        assertTrue(bookService.addPage("Book for Page", page));
    }

    @Test
    void testAddPage_BookNotFound() {
        when(mockBookDAO.getBookByName("Non-existent Book")).thenReturn(null);

        assertFalse(bookService.addPage("Non-existent Book", new Page()));
    }

    @Test
    void testPerformAnalysis_Success() {
        Book book = new Book();
        book.setTitle("Analysis Book");

        String[] methods = {"Paper", "PMI", "PKL", "TF-IDF"};
        for (String method : methods) {
            String result = bookService.performAnalysis(book, method);
            assertNotNull(result);
        }
    }

    @Test
    void testPerformAnalysis_UnknownMethod() {
    Book book = new Book();
    book.setTitle("Analysis Book");
    
    String result = bookService.performAnalysis(book, "Unknown Method");
    
    assertTrue(result.contains("Error") || result.contains("Unknown") || result.startsWith("ERROR:"), 
        "Method should return an error message for unknown analysis method");
    }

    @Test
    void testAnalyzeWord_Success() {
        String word = "testWord";
        String analysisResult = bookService.analyzeWord(word);
        assertNotNull(analysisResult);
    }

}