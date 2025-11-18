package config;

/**
 * Enumeration of application runtime environments.
 *
 * <p>This enum defines the different modes in which the ArabicNotepad application
 * can run, each with different configuration profiles and behaviors.</p>
 *
 * <p><b>Environment Types:</b></p>
 * <ul>
 *   <li><b>DEVELOPMENT:</b> Default environment for local development with debug logging</li>
 *   <li><b>TESTING:</b> Test environment with in-memory database and isolated test data</li>
 *   <li><b>PRODUCTION:</b> Production environment with optimized settings and MySQL database</li>
 *   <li><b>REMOTE:</b> Client mode connecting to remote RMI server</li>
 * </ul>
 *
 * <p><b>Selection Mechanism:</b><br>
 * The environment is determined by {@link EnvironmentManager#getCurrentEnvironment()},
 * which checks (in order): system.properties file, system property {@code app.env},
 * environment variable {@code app.env}, defaults to DEVELOPMENT.</p>
 *
 * <p><b>Configuration Impact:</b><br>
 * Each environment loads different configuration files (db.properties, local.properties, etc.)
 * with settings appropriate for that environment.</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see EnvironmentManager
 * @see ConfigurationManager
 * @since 1.0
 */
public enum Environment {
    DEVELOPMENT,
    TESTING,
    PRODUCTION,
    REMOTE
}
