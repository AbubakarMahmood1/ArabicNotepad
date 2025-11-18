package server;

import bl.BookFacadeImpl;
import common.RemoteBookFacadeImpl;
import config.ConfigurationManager;
import config.ConfigurationManagerRemote;
import config.Environment;
import config.EnvironmentManager;
import dao.BookDAOFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main RMI server application for ArabicNotepad.
 *
 * <p>This class starts an RMI registry and registers the {@link RemoteBookFacadeImpl}
 * service, allowing remote clients to connect and perform book management operations
 * over the network.</p>
 *
 * <p><b>Server Architecture:</b></p>
 * <pre>
 * 1. Detect environment (DEVELOPMENT/PRODUCTION/etc.)
 * 2. Initialize ConfigurationManager
 * 3. Create BookDAO via factory (MySQL/InMemory based on config)
 * 4. Create local BookFacade wrapping DAO
 * 5. Create RemoteBookFacade wrapping local facade
 * 6. Start RMI registry on port 1099
 * 7. Register ConfigurationManager and RemoteBookFacade
 * 8. Install shutdown hook for graceful termination
 * </pre>
 *
 * <p><b>RMI Registry Bindings:</b></p>
 * <ul>
 *   <li><b>"ConfigurationManager"</b> → {@link ConfigurationManagerRemote}</li>
 *   <li><b>"RemoteBookFacade"</b> → {@link common.RemoteBookFacade}</li>
 * </ul>
 *
 * <p><b>Network Configuration:</b></p>
 * <ul>
 *   <li><b>Port:</b> 1099 (default RMI registry port)</li>
 *   <li><b>Protocol:</b> Java RMI over TCP/IP</li>
 *   <li><b>Max Concurrent Clients:</b> 10 (thread pool limit)</li>
 * </ul>
 *
 * <p><b>Graceful Shutdown:</b><br>
 * The server installs a JVM shutdown hook that triggers graceful thread pool
 * termination, allowing in-flight operations to complete (60s timeout).</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Start server (defaults to DEVELOPMENT environment)
 * java -jar arabic-notepad-server.jar
 *
 * // Start in PRODUCTION mode
 * java -Dapp.env=PRODUCTION -jar arabic-notepad-server.jar
 *
 * // Or set environment in system.properties:
 * app.env=PRODUCTION
 * }</pre>
 *
 * <p><b>Client Connection:</b></p>
 * <pre>{@code
 * // Client code to connect:
 * Registry registry = LocateRegistry.getRegistry("server-host", 1099);
 * RemoteBookFacade facade = (RemoteBookFacade) registry.lookup("RemoteBookFacade");
 * List<Book> books = facade.getBookList(null);
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see RemoteBookFacadeImpl
 * @see ConfigurationManager
 * @see BookDAOFactory
 * @since 1.0
 */
public class BookServer {
    private static final Logger logger = LoggerFactory.getLogger(BookServer.class);

    /**
     * Main entry point for the RMI server.
     *
     * <p>Initializes the server, starts the RMI registry, and registers remote services.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Environment env = EnvironmentManager.getCurrentEnvironment();
        ConfigurationManagerRemote configManager = null;
        try {
            configManager = ConfigurationManager.getInstance(env);
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(BookServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            var bookDAO = BookDAOFactory.createBookDAO();
            var localFacade = new BookFacadeImpl(bookDAO);
            var remoteFacade = new RemoteBookFacadeImpl(localFacade);
            
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ConfigurationManager", configManager);
            logger.info("Server started and ConfigurationManager bound to registry.");
            registry.rebind("RemoteBookFacade", remoteFacade);
            logger.info("Server started and RemoteBookFacade bound to registry on port 1099");

             Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                remoteFacade.shutdownThreadPool();
                logger.info("Server shutting down gracefully");
            }));
        } catch (RemoteException e) {
            logger.error("Server error: {}", e.getMessage(), e);
        }
    }
}
