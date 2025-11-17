package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentManager {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManager.class);
    private static final String ENV_PROPERTY = "app.env";
    private static final String CONFIG_FILE = "system.properties";

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