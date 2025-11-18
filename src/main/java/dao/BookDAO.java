package dao;

import dto.Book;
import config.DBConfig;
import java.util.List;
import dto.Page;

/**
 * Data Access Object interface for book and page persistence operations.
 *
 * <p>This interface defines the contract for all data access implementations,
 * whether database-based (MySQL), file-based (LocalStorage), or in-memory.
 * It provides a unified API for CRUD operations on books and pages, abstracting
 * away the details of the underlying storage mechanism.</p>
 *
 * <p><b>Implementations:</b></p>
 * <ul>
 *   <li>{@link MySQLBookDAO} - Database persistence with MySQL</li>
 *   <li>{@link LocalStorageBookDAO} - File-based persistence</li>
 *   <li>{@link InMemoryBookDAO} - In-memory storage for testing</li>
 * </ul>
 *
 * <p><b>Core Operations:</b></p>
 * <ul>
 *   <li>Book CRUD (Create, Read, Update, Delete)</li>
 *   <li>Page management within books</li>
 *   <li>Content search across all books</li>
 *   <li>Hash-based duplicate detection</li>
 *   <li>Database connection management</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Create DAO instance via factory
 * BookDAO dao = BookDAOFactory.createDAO("MySQL");
 *
 * // Connect to database
 * dao.connect(dbConfig);
 *
 * // Perform operations
 * List<Book> books = dao.getAllBooks(null);
 * dao.addBook(newBook, false);
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 2.0
 * @since 1.0
 * @see MySQLBookDAO
 * @see LocalStorageBookDAO
 * @see InMemoryBookDAO
 * @see BookDAOFactory
 */
public interface BookDAO {

    /**
     * Retrieves all books from the data store.
     *
     * @param path Optional path parameter (implementation-specific, may be null)
     * @return List of all books, or empty list if none found
     */
    List<Book> getAllBooks(String path);

    /**
     * Retrieves a book by its title.
     *
     * @param name The title of the book to retrieve
     * @return The book with the specified title, or null if not found
     */
    Book getBookByName(String name);

    /**
     * Adds a new book to the data store.
     *
     * @param book The book to add (must not be null)
     * @param isDbDown Flag indicating if database is unavailable (for fallback logic)
     * @return true if book was added successfully, false otherwise
     */
    public boolean addBook(Book book, boolean isDbDown);

    /**
     * Updates an existing book in the data store.
     *
     * @param book The book with updated information (must not be null)
     * @return true if update succeeded, false otherwise
     */
    boolean updateBook(Book book);

    /**
     * Deletes a book from the data store by its title.
     *
     * @param title The title of the book to delete
     * @return true if deletion succeeded, false otherwise
     */
    boolean deleteBook(String title);

    /**
     * Checks if a content hash already exists in the data store.
     *
     * <p>Used for duplicate detection before adding new books.</p>
     *
     * @param hash The hash string to check
     * @return true if hash exists, false otherwise
     */
    boolean isHashExists(String hash);

    /**
     * Establishes a connection to the data store.
     *
     * @param dbConfig Configuration object containing connection parameters
     * @return true if connection succeeded, false otherwise
     */
    boolean connect(DBConfig dbConfig);

    /**
     * Checks if the data store connection is active.
     *
     * @return true if connected and accessible, false otherwise
     */
    public boolean isDatabaseConnected();

    /**
     * Searches for books containing the specified text in their content.
     *
     * <p>Performs a full-text search across all books and pages.</p>
     *
     * @param searchText The text to search for
     * @return List of search results with book titles and matching sentences,
     *         or empty list if no matches found
     */
    public List<String> searchBooksByContent(String searchText);

    /**
     * Adds a new page to a book.
     *
     * @param bookId The ID of the book to add the page to
     * @param page The page to add (must not be null)
     * @return true if page was added successfully, false otherwise
     */
    public boolean addPage(int bookId, Page page);

    /**
     * Retrieves all pages for a book by its title.
     *
     * @param title The title of the book
     * @return List of pages ordered by page number, or empty list if none found
     */
    public List<Page> getPagesByBookTitle(String title);

    /**
     * Deletes all pages associated with a book.
     *
     * @param title The title of the book whose pages should be deleted
     */
    public void deletePagesByBookTitle(String title);
}
