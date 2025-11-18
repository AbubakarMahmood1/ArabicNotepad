package common;

import bl.BookFacade;
import java.rmi.server.UnicastRemoteObject;
import dto.Book;
import dto.Page;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.ArabicNotepadClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * RMI server implementation of {@link RemoteBookFacade} with thread pool concurrency.
 *
 * <p>This class extends {@link UnicastRemoteObject} to enable Remote Method Invocation (RMI)
 * and wraps a local {@link BookFacade} instance, delegating all operations to it while
 * providing network transparency and concurrent request handling.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>Thread Pool:</b> Fixed thread pool ({@value #MAX_THREAD_POOL} threads) for concurrent client handling</li>
 *   <li><b>Async Operations:</b> Write operations (insert, update, delete) execute asynchronously</li>
 *   <li><b>Sync Operations:</b> Read operations (get, search) block until completion</li>
 *   <li><b>Client Management:</b> Maintains list of registered clients for notifications</li>
 *   <li><b>Connection Monitoring:</b> Ping endpoint for health checks</li>
 * </ul>
 *
 * <p><b>Concurrency Model:</b></p>
 * <pre>
 * Write Operations (async):     Read Operations (sync):
 * - insertBook()                 - getBookList()
 * - updateBook()                 - getBookByName()
 * - deleteBook()                 - exportBook()
 * - importBook()                 - searchBooksByContent()
 * - addPageByBookTitle()         - performAnalysis()
 *                                - transliterate()
 *                                - analyzeWord()
 * </pre>
 *
 * <p><b>Thread Pool Configuration:</b><br>
 * Uses a fixed thread pool with {@value #MAX_THREAD_POOL} threads. This limits concurrent
 * operations to prevent server overload but may queue requests if all threads are busy.</p>
 *
 * <p><b>Error Handling:</b><br>
 * All operations catch {@link ExecutionException} and {@link InterruptedException} from
 * thread pool execution and convert them to {@link RemoteException} for RMI protocol.</p>
 *
 * <p><b>Example Usage (Server Side):</b></p>
 * <pre>{@code
 * // Create local facade
 * BookDAO dao = new MySQLBookDAO(dbConfig);
 * BookFacade facade = new BookFacadeImpl(dao);
 *
 * // Wrap in RMI implementation
 * RemoteBookFacade remoteFacade = new RemoteBookFacadeImpl(facade);
 *
 * // Register with RMI registry
 * Registry registry = LocateRegistry.createRegistry(1099);
 * registry.rebind("BookService", remoteFacade);
 *
 * // Server is now ready for remote clients
 * System.out.println("Server started on port 1099");
 *
 * // Shutdown hook
 * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
 *     ((RemoteBookFacadeImpl) remoteFacade).shutdownThreadPool();
 * }));
 * }</pre>
 *
 * <p><b>Lifecycle Management:</b></p>
 * <ul>
 *   <li><b>Startup:</b> Constructor creates thread pool and exports RMI object</li>
 *   <li><b>Runtime:</b> Thread pool handles concurrent client requests</li>
 *   <li><b>Shutdown:</b> Call {@link #shutdownThreadPool()} to gracefully stop (60s timeout)</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. The thread pool handles concurrent
 * access, but {@code registeredClients} list modification is not synchronized (lazy init only).</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see RemoteBookFacade
 * @see BookFacade
 * @see UnicastRemoteObject
 * @since 1.0
 */
public class RemoteBookFacadeImpl extends UnicastRemoteObject implements RemoteBookFacade {

    private final BookFacade bookFacade;
    private List<ArabicNotepadClient> registeredClients = null;
    private static final Logger logger = LoggerFactory.getLogger(RemoteBookFacadeImpl.class);
    private final ExecutorService threadPool;
    private final int MAX_THREAD_POOL = 10;

    /**
     * Constructs a new RemoteBookFacadeImpl wrapping a local BookFacade.
     *
     * <p>Initializes the RMI infrastructure and creates a fixed thread pool
     * for handling concurrent client requests.</p>
     *
     * @param bookFacade the local facade to delegate operations to; must not be null
     * @throws RemoteException if RMI export fails
     */
    public RemoteBookFacadeImpl(BookFacade bookFacade) throws RemoteException {
        super();
        this.bookFacade = bookFacade;
        this.threadPool = Executors.newFixedThreadPool(MAX_THREAD_POOL);
    }

    @Override
public List<Book> getBookList(String filepath) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.getBookList(filepath)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in getBookList: {}", e.getMessage(), e);
        throw new RemoteException("Error while retrieving book list", e);
    }
}


    @Override
    public Book getBookByName(String value) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.getBookByName(value)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in getBookByName: {}", e.getMessage(), e);
        throw new RemoteException("Error while retrieving book by name", e);
    }
}

    @Override
    public void insertBook(Book book) throws RemoteException {
        threadPool.execute(() -> bookFacade.insertBook(book));
    }

    @Override
    public void updateBook(Book book) throws RemoteException {
        threadPool.execute(() -> bookFacade.updateBook(book));
    }

    @Override
    public void deleteBook(String title) throws RemoteException {
        threadPool.execute(() -> bookFacade.deleteBook(title));
    }

    @Override
    public void importBook(String path) throws RemoteException {
        threadPool.execute(() -> bookFacade.importBook(path));
    }

    @Override
public boolean exportBook(String title) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.exportBook(title)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while exporting book", e);
    }
}

@Override
    public boolean exportBook(Book book) throws RemoteException {
       try {
        return threadPool.submit(() -> bookFacade.exportBook(book)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while exporting book", e);
    }
    }

@Override
public String transliterate(String arabictext) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.transliterate(arabictext)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in transliterate: {}", e.getMessage(), e);
        throw new RemoteException("Error while transliterating text", e);
    }
}

@Override
public List<String> searchBooksByContent(String searchText) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.searchBooksByContent(searchText)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in searchBooksByContent: {}", e.getMessage(), e);
        throw new RemoteException("Error while searching books by content", e);
    }
}

@Override
public boolean isDatabaseConnected() throws RemoteException {
    try {
        return threadPool.submit(bookFacade::isDatabaseConnected).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in isDatabaseConnected: {}", e.getMessage(), e);
        throw new RemoteException("Error while checking database connection", e);
    }
}

    

    @Override
    public void addPageByBookTitle(String title, Page page) throws RemoteException {
        threadPool.execute(() -> bookFacade.addPageByBookTitle(title, page));
    }

    @Override
    public String performAnalysis(Book book, String analysisMethod) throws RemoteException  {
        try {
         return threadPool.submit(() -> bookFacade.performAnalysis(book, analysisMethod)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while analyzing the book", e);
    }
        
        
    }

    @Override
    public String analyzeWord(String selectedWord) throws RemoteException {
        try {
         return threadPool.submit(() -> bookFacade.analyzeWord(selectedWord)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while analyzing the word", e);
    }  
    }

    @Override
    public void registerClient(ArabicNotepadClient client) throws RemoteException {
        threadPool.execute(() -> {
            if (registeredClients == null) {
                registeredClients = new ArrayList<>();
            }
            registeredClients.add(client);
            try {
                client.onRegisterClient(true);
            } catch (RemoteException e) {
                logger.error("Error registering client", e);
            }
        });
    }

    @Override
    public boolean ping() throws RemoteException {
        logger.info("Ping received from client");
        return true;
    }
    /**
     * Gracefully shuts down the thread pool with a 60-second timeout.
     *
     * <p>This method should be called when the server is shutting down to ensure
     * all pending operations complete and threads are properly terminated.</p>
     *
     * <p><b>Shutdown Process:</b></p>
     * <ol>
     *   <li>Initiates orderly shutdown (no new tasks accepted)</li>
     *   <li>Waits up to 60 seconds for existing tasks to complete</li>
     *   <li>If timeout expires, forces immediate shutdown with {@code shutdownNow()}</li>
     *   <li>If interrupted during wait, forces shutdown and restores interrupt status</li>
     * </ol>
     *
     * <p><b>Best Practice:</b> Call this in a JVM shutdown hook:</p>
     * <pre>{@code
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     *     remoteFacade.shutdownThreadPool();
     *     System.out.println("Server shutdown complete");
     * }));
     * }</pre>
     */
    public void shutdownThreadPool() {
        threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
}

    @Override
    public boolean isHashExists(String hash) throws RemoteException {      
        try {
            return threadPool.submit(() -> bookFacade.isHashExists(hash)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in exportBook: {}", e.getMessage(), e);
            throw new RemoteException("Error while analyzing the word", e);
        }
    }

}