package config;

import java.nio.file.Paths;
import util.ResourcePathResolver;

public class DBConfig extends BaseConfig {

    private static final String ENV_DB_USERNAME = "DB_USERNAME";
    private static final String ENV_DB_PASSWORD = "DB_PASSWORD";
    private static final String ENV_DB_URL = "DB_URL";

    public DBConfig(Environment env) {
        super(getInternalPath(env), getExternalPath());
    }

    private static String getInternalPath(Environment env) {
        return ResourcePathResolver.getPath(env, "db.properties");
    }


    private static String getExternalPath() {
        return Paths.get(System.getenv("APPDATA"), "ArabicNotepad", "config", "db.properties").toString();
    }

    /**
     * Gets a property value, with environment variable override support.
     * For sensitive properties (username, password, url), checks environment variables first.
     *
     * @param key The property key
     * @return The property value from environment variable or properties file
     * @throws IllegalStateException if the property is not found in either location
     */
    @Override
    public String getProperty(String key) {
        // Check environment variables first for sensitive credentials
        String envValue = getFromEnvironment(key);
        if (envValue != null) {
            logger.debug("Using environment variable for property: {}", key);
            return envValue;
        }

        // Fall back to properties file
        String value = properties.getProperty(key);

        // For sensitive properties, warn if using properties file in production
        if (isSensitiveProperty(key) && value != null &&
            EnvironmentManager.getCurrentEnvironment() == Environment.PRODUCTION) {
            logger.warn("Using properties file for sensitive property '{}' in PRODUCTION. " +
                       "Consider using environment variables instead.", key);
        }

        if (value == null) {
            logger.error("Property '{}' not found in environment variables or properties file.", key);
            throw new IllegalStateException("Missing property: " + key);
        }

        return value;
    }

    /**
     * Checks if a property key is sensitive and should be externalized.
     */
    private boolean isSensitiveProperty(String key) {
        return key.equals("username") || key.equals("password") || key.equals("url");
    }

    /**
     * Gets a property value from environment variables based on the key.
     * Maps property keys to their corresponding environment variable names.
     *
     * @param key The property key
     * @return The environment variable value, or null if not set
     */
    private String getFromEnvironment(String key) {
        String envVarName = switch (key) {
            case "username" -> ENV_DB_USERNAME;
            case "password" -> ENV_DB_PASSWORD;
            case "url" -> ENV_DB_URL;
            default -> null;
        };

        if (envVarName != null) {
            String value = System.getenv(envVarName);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }

        return null;
    }
}
