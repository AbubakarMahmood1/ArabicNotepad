package bl;

import dao.BookDAO;
import dto.Book;

import java.util.List;
import dto.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link BookFacade} interface.
 *
 * <p>This class serves as the primary entry point for all business logic operations
 * in the ArabicNotepad application. It delegates operations to {@link BookService}
 * while providing a simplified, clean API for UI layers and remote clients.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Delegates all business logic to BookService</li>
 *   <li>Provides a clean facade pattern implementation</li>
 *   <li>Handles environment-aware operations (database vs local storage)</li>
 *   <li>Integrates text analysis and transliteration features</li>
 * </ul>
 *
 * <p><b>Architecture Pattern:</b> Facade Pattern<br>
 * This class hides the complexity of BookService and provides a simple interface
 * for clients. It's used by both local UI ({@code BookUIRefactored}) and remote
 * RMI implementation ({@code RemoteBookFacadeImpl}).</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * BookDAO dao = new MySQLBookDAO();
 * BookFacade facade = new BookFacadeImpl(dao);
 *
 * // Create and insert a new book
 * Book book = new Book();
 * book.setTitle("My Arabic Notes");
 * book.setIdauthor("user123");
 * facade.insertBook(book);
 *
 * // Search for books
 * List<String> results = facade.searchBooksByContent("مرحبا");
 *
 * // Perform analysis
 * String analysis = facade.performAnalysis(book, "TF-IDF");
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see BookFacade
 * @see BookService
 * @since 1.0
 */
public class BookFacadeImpl implements BookFacade {

    private static final Logger logger = LoggerFactory.getLogger(BookFacadeImpl.class);
    private final BookService bookService;

    /**
     * Constructs a new BookFacadeImpl with the specified data access object.
     *
     * <p>This constructor initializes the underlying BookService with the provided
     * BookDAO implementation. The DAO determines the storage backend (database,
     * local storage, or in-memory).</p>
     *
     * @param bookDAO the data access object for book persistence operations;
     *                must not be null
     * @throws NullPointerException if bookDAO is null
     */
    public BookFacadeImpl(BookDAO bookDAO) {
        this.bookService = new BookService(bookDAO);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Delegates to {@link BookService#importBook(String)}, which handles both
     * single file and directory imports with batch processing (50 books per batch).</p>
     */
    @Override
    public void importBook(String path) {
        bookService.importBook(path);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Automatically sets the author ID from the current user configuration if not
     * already set on the book object.</p>
     */
    @Override
    public void insertBook(Book book) {
        bookService.insertEmptyBookIntoDB(book);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Note:</b> The path parameter is ignored in this
     * implementation; all books are retrieved from the configured BookDAO.</p>
     */
    @Override
    public List<Book> getBookList(String path) {
        return bookService.getBookListFromDB();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Searches the database by book title and returns the complete book with all pages.</p>
     */
    @Override
    public Book getBookByName(String value) {
        return bookService.getBookByName(value);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Environment-Aware Behavior:</b></p>
     * <ul>
     *   <li>If database is connected: Updates book in database</li>
     *   <li>If database is disconnected: Exports book to local storage instead</li>
     * </ul>
     *
     * <p>This allows the application to continue functioning when the database
     * is unavailable by falling back to file-based storage.</p>
     */
    @Override
    public void updateBook(Book book) {
        if (bookService.isDatabaseConnected()) {
            bookService.updateBook(book);
        } else {
            bookService.exportBook(book);
        }

    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Environment-Aware Behavior:</b></p>
     * <ul>
     *   <li>If database is connected: Deletes book from database by title</li>
     *   <li>If database is disconnected with book path: Deletes from local storage</li>
     *   <li>If database is disconnected without path: Logs error and does nothing</li>
     * </ul>
     *
     * <p><b>Error Handling:</b> If deletion fails in offline mode, an error is logged.</p>
     */
    @Override
    public void deleteBook(String value) {
        if (bookService.isDatabaseConnected()) {
            bookService.deleteBook(value);
        } else if (bookService.getBookPath() != null) {
            bookService.deleteBook(bookService.getBookPath());
        } else {
            logger.error("Database disconnected, cannot delete book: {}", value);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Exports the book to local storage as a JSON file in the configured books directory.</p>
     */
    @Override
    public boolean exportBook(Book book) {
        return bookService.exportBook(book);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Retrieves the book from database by title, then exports it to local storage.</p>
     */
    @Override
    public boolean exportBook(String title) {
        return bookService.exportBook(title);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Uses {@link util.TransliterationUtil} for Arabic-to-Latin transliteration
     * based on standard romanization rules.</p>
     */
    @Override
    public String transliterate(String arabictext) {
        return bookService.translateToRomanEnglish(arabictext);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Uses {@link util.WordAnalyzer} to provide linguistic analysis of individual words.</p>
     */
    @Override
    public String analyzeWord(String selectedWord) {
        return bookService.analyzeWord(selectedWord);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Performs full-text search across all books and pages in the database.</p>
     */
    @Override
    public List<String> searchBooksByContent(String searchText) {
        return bookService.searchBooksByContent(searchText);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * First retrieves the book by title, then adds the page to that book.
     * If the book is not found, logs an error and does nothing.</p>
     *
     * @param title the title of the book to add the page to
     * @param page  the page to add
     */
    @Override
    public void addPageByBookTitle(String title, Page page) {
        Book book = bookService.getBookByName(title);
        if (book != null) {
            bookService.addPage(title, page);
        } else {
            logger.error("Book not found with title: {}", title);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Delegates to appropriate analyzer based on method name:
     * "Paper" (QualityPhrasesMiner), "PMI", "PKL", or "TF-IDF".</p>
     */
    @Override
    public String performAnalysis(Book book, String analysisMethod) {
        return bookService.performAnalysis(book, analysisMethod);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Checks if the underlying BookDAO can connect to the database.</p>
     */
    @Override
    public boolean isDatabaseConnected() {
        return bookService.isDatabaseConnected();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation Details:</b><br>
     * Checks the database for an existing book with the specified hash
     * to prevent duplicate imports.</p>
     */
    @Override
    public boolean isHashExists(String hash) {
        return bookService.isHashExists(hash);
    }
}
