package bl;

import config.ConfigurationManager;
import config.Environment;
import config.UserConfig;
import dao.BookDAO;
import dao.LocalStorageBookDAO;
import dto.Book;
import dto.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Core service layer providing comprehensive business logic for book management operations.
 *
 * <p>This class serves as the central orchestrator for all book-related operations in the
 * ArabicNotepad application. It coordinates between multiple data sources (database and local
 * storage), manages text analysis operations, handles user permissions, and provides
 * environment-aware fallback mechanisms.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Book CRUD operations with permission checking</li>
 *   <li>Batch import/export with duplicate detection</li>
 *   <li>Environment-aware operations (database vs local storage)</li>
 *   <li>Text analysis integration (TF-IDF, PMI, PKL, quality phrases)</li>
 *   <li>Arabic transliteration and word analysis</li>
 *   <li>Full-text search across all books</li>
 * </ul>
 *
 * <p><b>Design Patterns:</b></p>
 * <ul>
 *   <li><b>Service Layer Pattern:</b> Encapsulates business logic</li>
 *   <li><b>Lazy Initialization:</b> Analyzers are created on-demand</li>
 *   <li><b>Batch Processing:</b> Imports 50 books at a time for efficiency</li>
 *   <li><b>Fallback Strategy:</b> Switches to local storage when database unavailable</li>
 * </ul>
 *
 * <p><b>Multi-Environment Support:</b><br>
 * The service adapts its behavior based on database connectivity:</p>
 * <pre>
 * Database Connected:    Database Disconnected:
 * - CRUD in MySQL        - CRUD in local files
 * - Network-based        - File-based operations
 * - Multi-user access    - Single-user access
 * </pre>
 *
 * <p><b>Performance Optimizations:</b></p>
 * <ul>
 *   <li>Batch import processing (50 books per batch)</li>
 *   <li>Lazy analyzer initialization (singletons)</li>
 *   <li>Hash-based duplicate detection (prevents re-imports)</li>
 *   <li>Efficient logging at appropriate levels</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * BookDAO dao = new MySQLBookDAO();
 * BookService service = new BookService(dao);
 *
 * // Import books from directory
 * service.importBook("/path/to/books/");
 *
 * // Get all books
 * List<Book> books = service.getBookListFromDB();
 *
 * // Perform TF-IDF analysis
 * String analysis = service.performAnalysis(book, "TF-IDF");
 *
 * // Check write privileges
 * boolean canWrite = service.hasWritePrivileges("My Book");
 * if (canWrite) {
 *     service.updateBook(book);
 * }
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see BookFacadeImpl
 * @see BookDAO
 * @see LocalStorageBookDAO
 * @since 1.0
 */
public class BookService {

    private static final int BATCH_SIZE = 50;
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookDAO bookDAO;
    private final LocalStorageBookDAO localStorageBookDAO;
    private final ConfigurationManager configManager;
    private UserConfig userConfig;
    private final String userId;
    private Environment currentEnvironment;

    private QualityPhrasesMiner qualityPhrasesMiner;
    private PMIAnalyzer pmiAnalyzer;
    private PKLAnalyzer pklAnalyzer;
    private TFIDFAnalyzer tfidfAnalyzer;
    private WordAnalyzer wordAnalyzer;
    private TransliterationUtil transliterationUtil;

    /**
     * Constructs a new BookService with the specified data access object.
     *
     * <p>This constructor initializes the service with both database and local storage
     * DAOs, loads user configuration, and prepares the environment for operations.
     * Text analyzers are lazily initialized on first use for better performance.</p>
     *
     * <p><b>Initialization Steps:</b></p>
     * <ol>
     *   <li>Store the provided BookDAO (database or in-memory)</li>
     *   <li>Create LocalStorageBookDAO for file operations</li>
     *   <li>Load ConfigurationManager singleton</li>
     *   <li>Retrieve current environment (LOCAL/REMOTE)</li>
     *   <li>Load user configuration and extract user ID</li>
     *   <li>Log initialization completion</li>
     * </ol>
     *
     * @param bookDAO the data access object for primary storage operations;
     *                typically {@link dao.MySQLBookDAO} for database access or
     *                {@link dao.InMemoryBookDAO} for testing
     * @throws NullPointerException if bookDAO is null
     */
    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
        this.localStorageBookDAO = new LocalStorageBookDAO();
        this.configManager = ConfigurationManager.getInstance();
        try {
            this.currentEnvironment = configManager.getCurrentEnvironment();
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(BookService.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.userConfig = configManager.getUserConfig();
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(BookService.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.userId = userConfig.getUserId();

        logger.info("Initializing BookService in {} environment.", currentEnvironment);
    }
    
     private QualityPhrasesMiner getQualityPhrasesMiner() {
        if (qualityPhrasesMiner == null) {
            qualityPhrasesMiner = QualityPhrasesMiner.getInstance();
        }
        return qualityPhrasesMiner;
    }

    private PMIAnalyzer getPmiAnalyzer() {
        if (pmiAnalyzer == null) {
            pmiAnalyzer = PMIAnalyzer.getInstance();
        }
        return pmiAnalyzer;
    }

    private PKLAnalyzer getPklAnalyzer() {
        if (pklAnalyzer == null) {
            pklAnalyzer = PKLAnalyzer.getInstance();
        }
        return pklAnalyzer;
    }

    private TFIDFAnalyzer getTfidfAnalyzer() {
        if (tfidfAnalyzer == null) {
            tfidfAnalyzer = TFIDFAnalyzer.getInstance();
        }
        return tfidfAnalyzer;
    }

    private WordAnalyzer getWordAnalyzer() {
        if (wordAnalyzer == null) {
            wordAnalyzer = WordAnalyzer.getInstance();
        }
        return wordAnalyzer;
    }

    private TransliterationUtil getTransliterationUtil() {
        if (transliterationUtil == null) {
            transliterationUtil = TransliterationUtil.getInstance();
        }
        return transliterationUtil;
    }


    private void addBookWithLogging(Book book) {
        if (bookDAO.addBook(book, true)) {
            logger.info("Successfully added book to DB: {}", book.getTitle());
        } else {
            logger.warn("Failed to add book to DB: {}", book.getTitle());
        }
    }

    /**
     * Checks if the current user has write privileges for a specified book.
     *
     * <p>Write privileges are determined by comparing the current user's ID
     * (from configuration) with the book's author ID. Only the book's author
     * can modify or delete it.</p>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Prevent unauthorized edits in multi-user environments</li>
     *   <li>Protect book integrity in remote RMI scenarios</li>
     *   <li>Enable UI to show/hide edit buttons based on permissions</li>
     * </ul>
     *
     * @param bookTitle the title of the book to check permissions for
     * @return {@code true} if current user is the book's author, {@code false} otherwise
     */
    public boolean hasWritePrivileges(String bookTitle) {
        Book book = bookDAO.getBookByName(bookTitle);

        if (book == null) {
            logger.warn("No book found with title: {}", bookTitle);
            return false;
        }

        boolean hasPrivileges = userId.equals(book.getIdauthor());
        if (hasPrivileges) {
            logger.info("User '{}' has write privileges for the book '{}'.", userId, bookTitle);
        } else {
            logger.warn("User '{}' does NOT have write privileges for the book '{}'.", userId, bookTitle);
        }
        return hasPrivileges;
    }

    /**
     * Imports one or more books from a file or directory into the database.
     *
     * <p>This method intelligently handles both single-file and directory imports:</p>
     * <ul>
     *   <li><b>Single File:</b> Imports one book from JSON file</li>
     *   <li><b>Directory:</b> Recursively imports all books from directory,
     *       processing them in batches of 50 for efficiency</li>
     * </ul>
     *
     * <p><b>Duplicate Prevention:</b><br>
     * Books are checked against existing database records using hash comparison.
     * Only books with new hashes are imported; duplicates are logged and skipped.</p>
     *
     * <p><b>Batch Processing:</b><br>
     * Directory imports process books in batches of 50 to balance memory usage
     * and database transaction efficiency.</p>
     *
     * <p><b>Author Assignment:</b><br>
     * If a book lacks an author ID, the current user's ID is automatically assigned.</p>
     *
     * @param path the absolute path to a book file or directory containing books;
     *             must be a valid file system path
     * @see #importBooksInBatches(List)
     * @see #processSingleBook(Book)
     */
    public void importBook(String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            List<Book> books = localStorageBookDAO.getAllBooks(path);
            if (books == null || books.isEmpty()) {
                logger.warn("No books found in local storage for the directory: {}", path);
                return;
            }
            importBooksInBatches(books);
        } else if (file.isFile()) {
            Book book = localStorageBookDAO.getBookByName(file.getAbsolutePath());
            if (book != null) {
                processSingleBook(book);
            } else {
                logger.warn("No book found in local storage with name: {}", file.getName());
            }
        } else {
            logger.warn("Invalid path provided: {}", path);
        }
    }

    
    private void importBooksInBatches(List<Book> books) {
        List<Book> batch = new ArrayList<>();

        for (Book book : books) {
            try {
                setAuthorIdIfNecessary(book);
                batch.add(book);

                if (batch.size() == BATCH_SIZE) {
                    processBatch(batch);
                    batch.clear();
                }
            } catch (Exception e) {
                logger.error("Error processing book: {}", book.getTitle(), e);
            }
        }

        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }

    
    private void processBatch(List<Book> batch) {
        logger.info("Processing batch of size: {}", batch.size());

        for (Book book : batch) {
            if (!isHashExists(book.getHash()))
            {
                addBookWithLogging(book);
            } else {
                logger.info("Book already exists in DB, skipping: {}", book.getTitle());
            }
        }
    }
    
    /**
     * Checks if a book with the specified hash already exists in the database.
     *
     * <p>This method is used for duplicate detection during import operations.
     * Each book has a unique hash generated from its content, preventing
     * the same book from being imported multiple times.</p>
     *
     * @param hash the hash string to check for existence
     * @return {@code true} if a book with this hash exists, {@code false} otherwise
     */
    public boolean isHashExists(String hash)
    {
        return bookDAO.isHashExists(hash);
    }


    private void processSingleBook(Book book) {
        try {
            setAuthorIdIfNecessary(book);
            if (!bookDAO.isHashExists(book.getHash())) {
                addBookWithLogging(book);
            } else {
                logger.info("Book already exists in DB, skipping: {}", book.getTitle());
            }
        } catch (Exception e) {
            logger.error("Failed to import book: {}", book.getTitle(), e);
        }
    }

    
    private void setAuthorIdIfNecessary(Book book) {
        if (book.getIdauthor() == null || book.getIdauthor().isEmpty()) {
            book.setIdauthor(userId);
            logger.debug("Set author ID for book '{}': {}", book.getTitle(), userId);
        }
    }

    /**
     * Inserts a new book into the database.
     *
     * <p>This method creates a new book entry in the database. If the book
     * does not have an author ID set, the current user's ID is automatically
     * assigned as the author.</p>
     *
     * <p><b>Auto-Assignment:</b> Author ID is set from user configuration if missing.</p>
     *
     * @param book the book to insert; may have empty pages list
     */
    public void insertEmptyBookIntoDB(Book book) {
        setAuthorIdIfNecessary(book);
        addBookWithLogging(book);
    }

    /**
     * Retrieves all books from the database.
     *
     * <p>Returns a complete list of all books with their associated pages.
     * Useful for displaying book libraries, generating reports, or
     * performing bulk analysis operations.</p>
     *
     * @return list of all books in the database; never null but may be empty
     */
    public List<Book> getBookListFromDB() {
        List<Book> books = bookDAO.getAllBooks(null);
        if (books.isEmpty()) {
            logger.info("No books found in the database.");
        } else {
            logger.info("Retrieved {} books from the database.", books.size());
        }
        return books;
    }

    /**
     * Retrieves a specific book by its title.
     *
     * <p>Searches the database for a book with the exact title match
     * and returns the complete book object including all pages.</p>
     *
     * @param title the exact title of the book to retrieve
     * @return the book with matching title, or {@code null} if not found
     */
    public Book getBookByName(String title) {
        Book book = bookDAO.getBookByName(title);
        if (book == null) {
            logger.warn("No book found with title: {}", title);
        } else {
            logger.info("Retrieved book from DB: {}", book.getTitle());
        }
        return book;
    }

    /**
     * Deletes a book from storage (database or local file).
     *
     * <p><b>Smart Deletion Strategy:</b></p>
     * <ul>
     *   <li>If value is an existing file path: Deletes from local storage</li>
     *   <li>Otherwise: Treats as book title and deletes from database</li>
     * </ul>
     *
     * <p>This dual-mode behavior allows the method to work in both
     * database and offline file-based modes.</p>
     *
     * @param value either a file path (for local storage) or book title (for database)
     * @return {@code true} if deletion succeeded, {@code false} otherwise
     */
     public boolean deleteBook(String value) {
        File file = new File(value);

        if (file.exists() && file.isFile()) {
            boolean deleted = localStorageBookDAO.deleteBook(value);
            if (deleted) {
                logger.info("Deleted book from local storage: {}", value);
            } else {
                logger.warn("Failed to delete book from local storage: {}", value);
            }
            return deleted;
        } else {
            boolean deleted = bookDAO.deleteBook(value);
            if (deleted) {
                logger.info("Deleted book from DB: {}", value);
            } else {
                logger.warn("Failed to delete book from DB: {}", value);
            }
            return deleted;
        }
    }
    
    /**
     * Exports a book from the database to local storage as a JSON file.
     *
     * <p>Retrieves the book by title from the database, then saves it
     * to the local file system in JSON format. Useful for creating backups
     * or enabling offline access to books.</p>
     *
     * @param bookTitle the title of the book to export
     * @return {@code true} if export succeeded, {@code false} if book not found or export failed
     */
    public boolean exportBook(String bookTitle) {
        Book book = bookDAO.getBookByName(bookTitle);
        if (book == null) {
            logger.warn("No book found in SQL DB with title: {}", bookTitle);
            return false;
        }

        boolean exported = localStorageBookDAO.addBook(book, !isDatabaseConnected());
        if (exported) {
            logger.info("Successfully exported book to local storage: {}", book.getTitle());
        } else {
            logger.warn("Failed to export book to local storage: {}", book.getTitle());
        }
        return exported;
    }

    /**
     * Exports a book object to local storage as a JSON file.
     *
     * <p>Saves the provided book to the file system without retrieving it
     * from the database first. Useful when you already have the book object
     * in memory.</p>
     *
     * @param book the book to export
     * @return {@code true} if export succeeded, {@code false} otherwise
     */
    public boolean exportBook(Book book) {
        boolean exported = localStorageBookDAO.addBook(book, !isDatabaseConnected());
        if (exported) {
            logger.info("Successfully exported book to local storage: {}", book.getTitle());
        } else {
            logger.warn("Failed to export book to local storage: {}", book.getTitle());
        }
        return exported;
    }

    /**
     * Updates an existing book in the database.
     *
     * <p>Saves all changes to the book and its pages back to the database.
     * The book is identified by its ID, and all fields (including pages)
     * are updated.</p>
     *
     * @param book the book with updated content
     * @return {@code true} if update succeeded, {@code false} otherwise
     */
    public boolean updateBook(Book book) {
        boolean updated = bookDAO.updateBook(book);
        if (updated) {
            logger.info("Book '{}' was updated successfully.", book.getTitle());
        } else {
            logger.error("Failed to update book '{}'.", book.getTitle());
        }
        return updated;
    }

    /**
     * Checks if the database connection is currently active.
     *
     * <p>Used throughout the service to determine whether to use database
     * operations or fall back to local file storage. Essential for
     * environment-aware behavior.</p>
     *
     * @return {@code true} if database is reachable, {@code false} otherwise
     */
    public boolean isDatabaseConnected() {
        boolean connected = bookDAO.isDatabaseConnected();
        if (connected) {
            logger.info("Database connection is active.");
        } else {
            logger.warn("Database connection is inactive.");
        }
        return connected;
    }

    /**
     * Transliterates Arabic text to Roman (Latin) script.
     *
     * <p>Converts Arabic characters to their Latin equivalents using
     * standard romanization rules. Useful for search, indexing, and
     * enabling non-Arabic speakers to work with Arabic text.</p>
     *
     * @param arabicText the Arabic text to transliterate
     * @return the transliterated text in Latin script
     */
     public String translateToRomanEnglish(String arabicText) {
        String translated = getTransliterationUtil().translateToRomanEnglish(arabicText);
        logger.debug("Translated Arabic to Roman English: {}", translated);
        return translated;
    }

    /**
     * Searches for books containing the specified text in their content.
     *
     * <p>Performs full-text search across all books and pages in the database.
     * Returns the titles of books that contain the search text anywhere in
     * their pages.</p>
     *
     * @param searchText the text to search for (case-sensitive)
     * @return list of book titles containing the search text; never null
     */
    public List<String> searchBooksByContent(String searchText) {
        List<String> results = bookDAO.searchBooksByContent(searchText);
        logger.info("Found {} books matching the search text '{}'.", results.size(), searchText);
        return results;
    }

    /**
     * Searches for books by title.
     *
     * <p><b>Note:</b> Current implementation delegates to content search.
     * This may be a legacy method or placeholder for future title-specific search.</p>
     *
     * @param searchText the title text to search for
     * @return list of book titles matching the search; never null
     */
    public List<String> searchBooksByTitle(String searchText)
    {
        List<String> results = bookDAO.searchBooksByContent(searchText);
        logger.info("Found {} books matchning the search text '{}.", results.size(), searchText);
        return results;
    }

    /**
     * Adds a new page to an existing book.
     *
     * <p>Appends a new page to the book identified by title. The page is
     * added to the database and associated with the book's ID.</p>
     *
     * @param title the title of the book to add the page to
     * @param page  the page to add
     * @return {@code true} if page was added successfully, {@code false} if book not found
     */
    public boolean addPage(String title, Page page) {
        Book book = getBookByName(title);
        if (book != null) {
            boolean added = bookDAO.addPage(book.getId(), page);
            if (added) {
                logger.info("Added page to book '{}'.", title);
            } else {
                logger.warn("Failed to add page to book '{}'.", title);
            }
            return added;
        } else {
            logger.warn("Cannot add page. Book '{}' does not exist.", title);
            return false;
        }
    }

    /**
     * Retrieves the current book path from configuration.
     *
     * <p>Returns the path where books are stored in local file mode.
     * Used primarily in offline scenarios or when switching between
     * database and file-based storage.</p>
     *
     * @return the current book storage path, or {@code null} if not set
     */
    public String getBookPath() {
        String currentPath = null;
        try {
            currentPath = configManager.getLocalConfig().getCurrentPath();
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(BookService.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.debug("Retrieved current path from config: {}", currentPath);
        return currentPath;
    }

    /**
     * Performs text analysis on a book using the specified analysis method.
     *
     * <p><b>Supported Analysis Methods:</b></p>
     * <ul>
     *   <li><b>"Paper"</b> - Quality Phrases Mining (extracts meaningful phrases)</li>
     *   <li><b>"PMI"</b> - Pointwise Mutual Information (word association analysis)</li>
     *   <li><b>"PKL"</b> - Phrase Key Likelihood (statistical phrase analysis)</li>
     *   <li><b>"TF-IDF"</b> - Term Frequency-Inverse Document Frequency (keyword extraction)</li>
     * </ul>
     *
     * <p>All analyzers are lazy-loaded singletons for efficiency. Results are
     * returned as formatted strings suitable for display in the UI.</p>
     *
     * @param book           the book to analyze
     * @param analysisMethod the analysis method to use (case-sensitive)
     * @return formatted analysis results as a string
     * @throws IllegalArgumentException if analysisMethod is not recognized
     */
     public String performAnalysis(Book book, String analysisMethod) {
        logger.info("Starting analysis '{}' for book '{}'.", analysisMethod, book.getTitle());
        String result;

        switch (analysisMethod) {
            case "Paper" -> result = getQualityPhrasesMiner().mineQualityPhrases(book);
            case "PMI" -> result = getPmiAnalyzer().calculatePMI(book);
            case "PKL" -> result = getPklAnalyzer().calculatePKL(book);
            case "TF-IDF" -> result = getTfidfAnalyzer().calculateTFIDF(book);
            default -> {
                logger.error("Unknown analysis method: {}", analysisMethod);
                throw new IllegalArgumentException("Unknown analysis method: " + analysisMethod);
            }
        }
        logger.info("Completed analysis '{}' for book '{}'.", analysisMethod, book.getTitle());
        return result;
    }

    /**
     * Analyzes a single word for linguistic information.
     *
     * <p>Provides detailed analysis of the word including morphological,
     * grammatical, or statistical properties depending on the WordAnalyzer
     * implementation.</p>
     *
     * @param word the word to analyze
     * @return analysis results as a formatted string
     */
    public String analyzeWord(String word) {
        return getWordAnalyzer().analyzeWord(word);
    }
}
