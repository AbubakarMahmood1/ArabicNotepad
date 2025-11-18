package util;

import config.Environment;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for resolving environment-specific resource paths.
 *
 * <p>Locates configuration files in environment-specific subdirectories
 * (development/, testing/, production/, remote/) on the classpath.</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see config.BaseConfig
 * @since 1.0
 */
public class ResourcePathResolver {
    private static final Logger logger = LoggerFactory.getLogger(ResourcePathResolver.class);
    
    public static String getPath(Environment env, String filename) {
        String fullPath = env.name().toLowerCase() + "/" + filename;

        try {
            URL resource = ResourcePathResolver.class.getClassLoader().getResource(fullPath);

            if (resource != null) {
                return fullPath;
            } else {
                logger.warn("Resource not found in classpath: {}", fullPath);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error resolving resource path for {}: {}", fullPath, e.getMessage(), e);
            return null;
        }
    }
}