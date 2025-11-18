package dao;

import config.ConfigurationManager;
import config.DBConfig;
import config.Environment;
import config.EnvironmentManager;
import java.rmi.RemoteException;

/**
 * Factory class for creating the appropriate {@link BookDAO} implementation.
 *
 * <p>This factory implements the Factory Pattern to abstract the creation of DAO
 * instances based on configuration. It reads the database type from {@link DBConfig}
 * and instantiates the corresponding DAO implementation.</p>
 *
 * <p><b>Supported Database Types:</b></p>
 * <ul>
 *   <li><b>"mysql"</b> → {@link MySQLBookDAO} - Production database storage</li>
 *   <li><b>"test"</b> → {@link InMemoryBookDAO} - In-memory storage for testing</li>
 *   <li><b>"mongodb"</b> → Not implemented (throws {@link UnsupportedOperationException})</li>
 * </ul>
 *
 * <p><b>Configuration Source:</b><br>
 * The factory reads from {@code DBConfig.getProperty("type")} which is typically
 * loaded from {@code db.properties} or environment-specific configuration files.</p>
 *
 * <p><b>Initialization:</b><br>
 * The factory uses a static initializer block to load {@link DBConfig} once when
 * the class is first referenced. This ensures configuration is loaded before any
 * DAO instances are created.</p>
 *
 * <p><b>Design Pattern:</b> Factory Pattern<br>
 * Benefits:</p>
 * <ul>
 *   <li>Decouples DAO selection from business logic</li>
 *   <li>Centralizes DAO creation logic</li>
 *   <li>Simplifies switching between implementations (dev vs prod)</li>
 *   <li>Enables easy testing with InMemoryBookDAO</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // In production (db.properties: type=mysql)
 * BookDAO dao = BookDAOFactory.createBookDAO();
 * // Returns: MySQLBookDAO instance
 *
 * // In testing (db.properties: type=test)
 * BookDAO dao = BookDAOFactory.createBookDAO();
 * // Returns: InMemoryBookDAO instance
 *
 * // Use the DAO without knowing implementation
 * List<Book> books = dao.getAllBooks(null);
 * }</pre>
 *
 * <p><b>Error Handling:</b></p>
 * <ul>
 *   <li>Static initialization failure → {@link RuntimeException} (application won't start)</li>
 *   <li>Unsupported database type → {@link UnsupportedOperationException}</li>
 *   <li>Missing configuration → Handled by ConfigurationManager (throws RemoteException)</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Static initialization is thread-safe. The {@code createBookDAO}
 * method creates new instances on each call (not a singleton).</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see BookDAO
 * @see MySQLBookDAO
 * @see InMemoryBookDAO
 * @see DBConfig
 * @since 1.0
 */
public class BookDAOFactory {

    private static final DBConfig dbConfig;

    static {
        try {
            Environment env = EnvironmentManager.getCurrentEnvironment();
            dbConfig = ConfigurationManager.getInstance(env).getDbConfig();
        } catch (RemoteException e) {
            throw new RuntimeException("Error initializing ConfigurationManager", e);
        }
    }

    /**
     * Creates and returns a new {@link BookDAO} instance based on configuration.
     *
     * <p>Reads the database type from {@code DBConfig.getProperty("type")} and
     * instantiates the corresponding DAO implementation:</p>
     * <ul>
     *   <li>{@code "mysql"} → {@link MySQLBookDAO}</li>
     *   <li>{@code "test"} → {@link InMemoryBookDAO}</li>
     *   <li>{@code "mongodb"} → Throws {@link UnsupportedOperationException}</li>
     *   <li>Other values → Throws {@link UnsupportedOperationException}</li>
     * </ul>
     *
     * <p><b>Note:</b> This method creates a new instance on each call. It does not
     * return a singleton. If you need a single shared instance, wrap this in your
     * own singleton pattern.</p>
     *
     * @return a new {@link BookDAO} instance configured for the current environment
     * @throws UnsupportedOperationException if database type is not supported
     */
    public static BookDAO createBookDAO() {
        String dbType = dbConfig.getProperty("type");
        if ("mysql".equalsIgnoreCase(dbType)) {
            return new MySQLBookDAO(dbConfig);
        } else if ("mongodb".equalsIgnoreCase(dbType)) {
            // return new MongoDBBookDAO(dbConfig);
            throw new UnsupportedOperationException("MongoDB support is not implemented yet.");
        }
        else if("test".equalsIgnoreCase(dbType))
        {
            return new InMemoryBookDAO();
        }
        else {
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }
    }
}
