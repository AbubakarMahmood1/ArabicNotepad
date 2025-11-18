package common;
import java.rmi.Remote;
import java.rmi.RemoteException;
import dto.Book;
import dto.Page;
import java.util.List;
import ui.ArabicNotepadClient;

/**
 * Remote Method Invocation (RMI) interface for distributed book management operations.
 *
 * <p>This interface extends {@link Remote} to enable remote clients to access the
 * ArabicNotepad application over a network. It provides the same functionality as
 * {@link bl.BookFacade} but with RMI support, allowing users to manage books from
 * different machines.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Network-transparent book CRUD operations</li>
 *   <li>Remote text analysis and transliteration</li>
 *   <li>Client registration for server-push notifications</li>
 *   <li>Connection health monitoring (ping)</li>
 *   <li>Distributed multi-user access to shared book repository</li>
 * </ul>
 *
 * <p><b>Architecture:</b></p>
 * <pre>
 * ┌─────────────────┐         RMI/Network        ┌──────────────────┐
 * │  Remote Client  │ ←──────────────────────→ │  Server (Stub)   │
 * │ (RemoteBookUI)  │   RemoteBookFacade API     │ (BookFacadeImpl) │
 * └─────────────────┘                            └──────────────────┘
 * </pre>
 *
 * <p><b>Network Protocol:</b><br>
 * RMI uses Java serialization for method calls. All parameters (Book, Page)
 * must be {@link java.io.Serializable}. The default RMI registry runs on
 * port 1099.</p>
 *
 * <p><b>Exception Handling:</b><br>
 * All methods throw {@link RemoteException} which may occur due to:</p>
 * <ul>
 *   <li>Network failures (connection lost, timeout)</li>
 *   <li>Serialization errors (non-serializable objects)</li>
 *   <li>Server unavailability (crashed, restarting)</li>
 *   <li>Registry issues (name not bound, port conflicts)</li>
 * </ul>
 *
 * <p><b>Example Usage (Client Side):</b></p>
 * <pre>{@code
 * // Connect to remote server
 * Registry registry = LocateRegistry.getRegistry("server.example.com", 1099);
 * RemoteBookFacade remoteFacade = (RemoteBookFacade) registry.lookup("BookService");
 *
 * // Use remote methods
 * List<Book> books = remoteFacade.getBookList(null);
 * Book book = remoteFacade.getBookByName("My Arabic Notes");
 * String analysis = remoteFacade.performAnalysis(book, "TF-IDF");
 *
 * // Register for notifications
 * remoteFacade.registerClient(myClient);
 * }</pre>
 *
 * <p><b>Security Considerations:</b></p>
 * <ul>
 *   <li>RMI traffic is not encrypted by default (use SSL/TLS for production)</li>
 *   <li>No built-in authentication (implement custom security policy)</li>
 *   <li>Clients can only modify books they authored (write privilege check)</li>
 * </ul>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see common.RemoteBookFacadeImpl
 * @see bl.BookFacade
 * @see java.rmi.Remote
 * @since 1.0
 */
public interface RemoteBookFacade extends Remote {

    /**
     * Retrieves all books from the remote server.
     *
     * <p>Returns a complete list of all books accessible on the server,
     * including all pages and metadata.</p>
     *
     * @param filepath currently unused (reserved for future path-filtering feature)
     * @return list of all books; never null but may be empty
     * @throws RemoteException if network communication fails or server is unavailable
     */
    List<Book> getBookList(String filepath) throws RemoteException;

    /**
     * Retrieves a specific book by title from the remote server.
     *
     * @param value the exact title of the book to retrieve
     * @return the book with matching title, or {@code null} if not found
     * @throws RemoteException if network communication fails
     */
    Book getBookByName(String value) throws RemoteException;

    /**
     * Inserts a new book into the remote database.
     *
     * <p>The book's author ID is automatically set to the current user
     * if not already specified.</p>
     *
     * @param book the book to insert; must be serializable
     * @throws RemoteException if network communication fails or serialization fails
     */
    void insertBook(Book book) throws RemoteException;

    /**
     * Updates an existing book on the remote server.
     *
     * <p><b>Permission Check:</b> Only the book's author can update it.
     * Unauthorized updates will fail.</p>
     *
     * @param book the book with updated content; must be serializable
     * @throws RemoteException if network communication fails
     */
    void updateBook(Book book) throws RemoteException;

    /**
     * Deletes a book from the remote server.
     *
     * <p><b>Permission Check:</b> Only the book's author can delete it.</p>
     *
     * @param value the title of the book to delete
     * @throws RemoteException if network communication fails
     */
    void deleteBook(String value) throws RemoteException;

    /**
     * Imports book(s) from a file or directory on the server's file system.
     *
     * <p><b>Note:</b> The path is relative to the server's file system,
     * not the client's. This method is typically used by server administrators.</p>
     *
     * @param path absolute path on server to book file or directory
     * @throws RemoteException if network communication fails
     */
    void importBook(String path) throws RemoteException;

    /**
     * Exports a book from the remote server to the server's local storage.
     *
     * <p>Creates a JSON file on the server's file system. The file location
     * is determined by server configuration.</p>
     *
     * @param title the title of the book to export
     * @return {@code true} if export succeeded, {@code false} if book not found
     * @throws RemoteException if network communication fails
     */
    boolean exportBook(String title) throws RemoteException;

    /**
     * Exports a book object to the server's local storage.
     *
     * @param book the book to export; must be serializable
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws RemoteException if network communication fails or serialization fails
     */
    boolean exportBook(Book book) throws RemoteException;

    /**
     * Transliterates Arabic text to Roman (Latin) script on the remote server.
     *
     * <p>Useful for clients that don't have transliteration libraries installed locally.</p>
     *
     * @param arabictext the Arabic text to transliterate
     * @return the transliterated text in Latin script
     * @throws RemoteException if network communication fails
     */
    String transliterate(String arabictext) throws RemoteException;

    /**
     * Searches for books containing the specified text across the remote database.
     *
     * @param searchText the text to search for (case-sensitive)
     * @return list of book titles containing the search text; never null
     * @throws RemoteException if network communication fails
     */
    List<String> searchBooksByContent(String searchText) throws RemoteException;

    /**
     * Adds a new page to an existing book on the remote server.
     *
     * @param title the title of the book to add the page to
     * @param page  the page to add; must be serializable
     * @throws RemoteException if network communication fails or serialization fails
     */
    void addPageByBookTitle(String title, Page page) throws RemoteException;

    /**
     * Performs text analysis on a book using server-side analyzers.
     *
     * <p><b>Supported Methods:</b> "Paper", "PMI", "PKL", "TF-IDF"</p>
     *
     * <p>Server-side analysis is beneficial because:</p>
     * <ul>
     *   <li>Server has more computational resources</li>
     *   <li>Analysis libraries don't need to be on client</li>
     *   <li>Results can be cached on server</li>
     * </ul>
     *
     * @param book           the book to analyze; must be serializable
     * @param analysisMethod the analysis method (case-sensitive)
     * @return formatted analysis results as a string
     * @throws RemoteException if network communication fails or method is unknown
     */
    String performAnalysis(Book book, String analysisMethod) throws RemoteException;

    /**
     * Analyzes a single word using server-side linguistic tools.
     *
     * @param selectedWord the word to analyze
     * @return analysis results as a formatted string
     * @throws RemoteException if network communication fails
     */
    String analyzeWord(String selectedWord) throws RemoteException;

    /**
     * Checks if the remote server's database connection is active.
     *
     * <p>Clients can use this to adapt UI behavior (e.g., show "Server Offline" message).</p>
     *
     * @return {@code true} if server's database is connected, {@code false} otherwise
     * @throws RemoteException if network communication fails
     */
    boolean isDatabaseConnected() throws RemoteException;

    /**
     * Registers a client for server-push notifications.
     *
     * <p>After registration, the server can send notifications to the client about:</p>
     * <ul>
     *   <li>Book updates by other users</li>
     *   <li>System messages</li>
     *   <li>Server shutdowns</li>
     * </ul>
     *
     * <p><b>Note:</b> Client must also be an RMI remote object to receive callbacks.</p>
     *
     * @param client the client to register; must be a remote object
     * @throws RemoteException if network communication fails
     */
    void registerClient(ArabicNotepadClient client) throws RemoteException;

    /**
     * Pings the server to check connection health.
     *
     * <p>Lightweight method for testing if the server is reachable.
     * Clients can use this for connection monitoring and automatic reconnection.</p>
     *
     * @return {@code true} if server is reachable and responsive
     * @throws RemoteException if network communication fails (indicates server unreachable)
     */
    boolean ping() throws RemoteException;

    /**
     * Checks if a book with the specified hash exists on the remote server.
     *
     * <p>Used for duplicate detection before importing books to the server.</p>
     *
     * @param hash the hash string to check
     * @return {@code true} if a book with this hash exists, {@code false} otherwise
     * @throws RemoteException if network communication fails
     */
    public boolean isHashExists(String hash) throws RemoteException;
}
