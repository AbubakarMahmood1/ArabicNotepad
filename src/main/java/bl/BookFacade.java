package bl;

import dto.Book;
import java.util.List;
import dto.Page;

/**
 * Main business logic facade for the ArabicNotepad application.
 *
 * <p>This interface provides a simplified API for managing books, pages, and performing
 * various text analysis operations. It acts as the primary entry point for all business
 * logic operations, abstracting away the complexity of the underlying data access and
 * processing layers.</p>
 *
 * <p><b>Core Operations:</b></p>
 * <ul>
 *   <li>Book CRUD operations (create, read, update, delete)</li>
 *   <li>Page management within books</li>
 *   <li>Content search across all books</li>
 *   <li>Import/Export functionality</li>
 *   <li>Arabic text transliteration</li>
 *   <li>Text analysis (TF-IDF, PMI, PKL)</li>
 *   <li>Word-level analysis</li>
 *   <li>Database connectivity checks</li>
 *   <li>Content hash verification</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * BookFacade facade = new BookFacadeImpl();
 *
 * // Get all books
 * List<Book> books = facade.getBookList(null);
 *
 * // Search for content
 * List<String> results = facade.searchBooksByContent("search term");
 *
 * // Transliterate Arabic text
 * String transliterated = facade.transliterate("مرحبا");
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 2.0
 * @since 1.0
 * @see BookFacadeImpl
 * @see Book
 * @see Page
 */
public interface BookFacade {

    /**
     * Retrieves a list of all books from the specified data source.
     *
     * @param filepath Path to the data source (may be null for default source)
     * @return List of all books, or empty list if none found
     */
    List<Book> getBookList(String filepath);

    /**
     * Retrieves a specific book by its title.
     *
     * @param value The title of the book to retrieve
     * @return The book with the specified title, or null if not found
     */
    Book getBookByName(String value);

    /**
     * Inserts a new book into the data store.
     *
     * @param book The book to insert (must not be null)
     * @throws IllegalArgumentException if book is null
     */
    void insertBook(Book book);

    /**
     * Updates an existing book in the data store.
     *
     * @param book The book with updated information (must not be null)
     * @throws IllegalArgumentException if book is null
     */
    void updateBook(Book book);

    /**
     * Deletes a book from the data store by its title.
     *
     * @param value The title of the book to delete
     */
    void deleteBook(String value);

    /**
     * Imports a book from an external file.
     *
     * @param path The file path to import from (must be a valid file path)
     * @throws IllegalArgumentException if path is invalid
     */
    void importBook(String path);

    /**
     * Exports a book by its title to an external file.
     *
     * @param title The title of the book to export
     * @return true if export succeeded, false otherwise
     */
    boolean exportBook(String title);

    /**
     * Exports a book object to an external file.
     *
     * @param book The book to export (must not be null)
     * @return true if export succeeded, false otherwise
     * @throws IllegalArgumentException if book is null
     */
    boolean exportBook(Book book);

    /**
     * Transliterates Arabic text to Latin characters.
     *
     * <p>Converts Arabic script to a romanized representation using
     * standard transliteration rules.</p>
     *
     * @param arabictext The Arabic text to transliterate
     * @return The transliterated text in Latin characters, or empty string if input is empty
     */
    String transliterate(String arabictext);

    /**
     * Searches for books containing the specified text in their content.
     *
     * <p>Performs a full-text search across all books and pages, returning
     * matching sentences with book titles.</p>
     *
     * @param searchText The text to search for
     * @return List of search results in format "Title: [book], Sentence: [matching text]",
     *         or empty list if no matches found
     */
    List<String> searchBooksByContent(String searchText);

    /**
     * Adds a new page to a book identified by its title.
     *
     * @param title The title of the book to add the page to
     * @param page The page to add (must not be null)
     * @throws IllegalArgumentException if title or page is null
     */
    void addPageByBookTitle(String title, Page page);

    /**
     * Performs text analysis on a book using the specified analysis method.
     *
     * <p><b>Supported Analysis Methods:</b></p>
     * <ul>
     *   <li>"TFIDF" - Term Frequency-Inverse Document Frequency analysis</li>
     *   <li>"PMI" - Pointwise Mutual Information analysis</li>
     *   <li>"PKL" - Phrase Key Likelihood analysis</li>
     * </ul>
     *
     * @param book The book to analyze (must not be null)
     * @param analysisMethod The analysis method to use ("TFIDF", "PMI", or "PKL")
     * @return Analysis results as formatted string, or error message if analysis fails
     * @throws IllegalArgumentException if book is null or analysisMethod is invalid
     */
    String performAnalysis(Book book, String analysisMethod);

    /**
     * Analyzes a single word for linguistic properties.
     *
     * <p>Provides detailed analysis of the selected word including frequency,
     * context, and linguistic features.</p>
     *
     * @param selectedWord The word to analyze
     * @return Analysis results as formatted string, or empty string if word is empty
     */
    String analyzeWord(String selectedWord);

    /**
     * Checks if the database connection is active.
     *
     * @return true if database is connected and accessible, false otherwise
     */
    boolean isDatabaseConnected();

    /**
     * Checks if a content hash already exists in the database.
     *
     * <p>Used for duplicate detection and content verification.</p>
     *
     * @param hash The hash string to check
     * @return true if hash exists, false otherwise
     */
    public boolean isHashExists(String hash);
}
