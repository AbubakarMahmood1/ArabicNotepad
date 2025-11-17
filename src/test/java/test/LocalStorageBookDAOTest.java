package test;

import config.ConfigurationManager;
import config.Environment;
import config.EnvironmentManager;
import dao.LocalStorageBookDAO;
import dto.Book;
import dto.Page;
import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalStorageBookDAOTest {

    private LocalStorageBookDAO bookDAO;
    private Path tempDir;

    @BeforeAll
    void setUp() throws Exception {
        Environment env = EnvironmentManager.getCurrentEnvironment();
        try {
            ConfigurationManager.getInstance(env);
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(BookServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        bookDAO = new LocalStorageBookDAO();
        tempDir = Files.createTempDirectory("test_books");
    }

    @AfterAll
    void tearDown() throws Exception {
        Files.walk(tempDir)
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    @Order(1)
    void testAddBook() {
        Book book = createSampleBook("SampleBook", "**idauthor**: Author123");
        boolean isAdded = bookDAO.addBook(book, true);

        assertTrue(isAdded, "Book should be added successfully.");
        File bookFile = tempDir.resolve("SampleBook.md").toFile();
        assertTrue(bookFile.exists(), "Book file should exist.");
    }

    @Test
    @Order(2)
    void testGetBookByName() {
        String bookPath = tempDir.resolve("SampleBook.md").toString();
        Book book = bookDAO.getBookByName(bookPath);

        assertNotNull(book, "Book should be retrieved successfully.");
        assertEquals("SampleBook", book.getTitle(), "Book title should match.");
        assertEquals("Author123", book.getIdauthor(), "Book idauthor should match.");
        assertEquals(1, book.getPages().size(), "Book should have one page.");
    }

    @Test
    @Order(3)
    void testUpdateBook() {
        Book book = createSampleBook("SampleBook", "**idauthor**: UpdatedAuthor");
        boolean isUpdated = bookDAO.updateBook(book);

        assertTrue(isUpdated, "Book should be updated successfully.");
        Book updatedBook = bookDAO.getBookByName(tempDir.resolve("SampleBook.md").toString());
        assertEquals("UpdatedAuthor", updatedBook.getIdauthor(), "Updated idauthor should match.");
    }

    @Test
    @Order(4)
    void testDeleteBook() {
        String bookPath = tempDir.resolve("SampleBook.md").toString();
        boolean isDeleted = bookDAO.deleteBook(bookPath);

        assertTrue(isDeleted, "Book should be deleted successfully.");
        File bookFile = new File(bookPath);
        assertFalse(bookFile.exists(), "Book file should no longer exist.");
    }

    @Test
    @Order(5)
    void testGetAllBooks() {
        Book book1 = createSampleBook("Book1", "**idauthor**: Author1");
        Book book2 = createSampleBook("Book2", "**idauthor**: Author2");
        bookDAO.addBook(book1, true);
        bookDAO.addBook(book2, true);

        List<Book> books = bookDAO.getAllBooks(tempDir.toString());

        assertEquals(2, books.size(), "Two books should be retrieved.");
        assertEquals("Book1", books.get(0).getTitle(), "First book title should match.");
        assertEquals("Author1", books.get(0).getIdauthor(), "First book idauthor should match.");
        assertEquals("Book2", books.get(1).getTitle(), "Second book title should match.");
        assertEquals("Author2", books.get(1).getIdauthor(), "Second book idauthor should match.");
    }

    private Book createSampleBook(String title, String idAuthorLine) {
        Book book = new Book();
        book.setTitle(title);
        book.setIdauthor(idAuthorLine.substring("**idauthor**: ".length()).trim());
        
        List<Page> pages = new ArrayList<>();
        Page page = new Page();
        page.setPageNumber(1);
        page.setContent("This is a sample page content.");
        pages.add(page);

        book.setPages(pages);
        return book;
    }
}
