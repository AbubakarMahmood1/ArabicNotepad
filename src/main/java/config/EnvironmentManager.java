package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for detecting and managing the current application environment.
 *
 * <p>This class provides environment detection with multiple fallback mechanisms,
 * checking configuration sources in priority order to determine which {@link Environment}
 * the application should run in.</p>
 *
 * <p><b>Detection Priority (checked in order):</b></p>
 * <ol>
 *   <li>{@code system.properties} file (classpath resource)</li>
 *   <li>System property: {@code app.env}</li>
 *   <li>Environment variable: {@code app.env}</li>
 *   <li>Default: {@link Environment#DEVELOPMENT}</li>
 * </ol>
 *
 * <p><b>Supported Values:</b></p>
 * <ul>
 *   <li>"PRODUCTION" → {@link Environment#PRODUCTION}</li>
 *   <li>"TESTING" → {@link Environment#TESTING}</li>
 *   <li>"REMOTE" → {@link Environment#REMOTE}</li>
 *   <li>"DEVELOPMENT" → {@link Environment#DEVELOPMENT}</li>
 *   <li>Any other value → {@link Environment#DEVELOPMENT} (default)</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Method 1: Via system.properties file
 * // Create src/main/resources/system.properties with:
 * // app.env=PRODUCTION
 *
 * // Method 2: Via system property
 * java -Dapp.env=TESTING -jar arabic-notepad.jar
 *
 * // Method 3: Via environment variable
 * export app.env=REMOTE
 * java -jar arabic-notepad.jar
 *
 * // In code:
 * Environment env = EnvironmentManager.getCurrentEnvironment();
 * System.out.println("Running in " + env + " mode");
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see Environment
 * @see ConfigurationManager
 * @since 1.0
 */
public class EnvironmentManager {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManager.class);
    private static final String ENV_PROPERTY = "app.env";
    private static final String CONFIG_FILE = "system.properties";

    /**
     * Detects and returns the current application environment.
     *
     * <p>Checks multiple sources in priority order:</p>
     * <ol>
     *   <li>system.properties file (classpath)</li>
     *   <li>System property (app.env)</li>
     *   <li>Environment variable (app.env)</li>
     *   <li>Default (DEVELOPMENT)</li>
     * </ol>
     *
     * @return the detected environment, never null; defaults to DEVELOPMENT if not configured
     */
    public static Environment getCurrentEnvironment() {
        String env = getEnvironmentFromFile();

        if (env == null) {
            env = System.getProperty(ENV_PROPERTY);
        }
        if (env == null) {
            env = System.getenv(ENV_PROPERTY);
        }
        if (env != null) {
            return switch (env.toUpperCase()) {
                case "PRODUCTION" -> Environment.PRODUCTION;
                case "TESTING" -> Environment.TESTING;
                case "REMOTE" -> Environment.REMOTE;
                case "DEVELOPMENT" -> Environment.DEVELOPMENT;
                default -> Environment.DEVELOPMENT;
            };
        }
        return Environment.DEVELOPMENT;
    }

    private static String getEnvironmentFromFile() {
    Properties properties = new Properties();
    try (InputStream inputStream = EnvironmentManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
        if (inputStream != null) {
            properties.load(inputStream);
            return properties.getProperty(ENV_PROPERTY);
        } else {
            logger.warn("Could not find {} in the classpath, using default environment", CONFIG_FILE);
        }
    } catch (IOException e) {
        logger.error("Could not load {}: {}", CONFIG_FILE, e.getMessage(), e);
    }
    return null;
}

}