package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for SQL security operations.
 * Provides methods to safely handle user input in SQL queries.
 */
public class SQLSecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SQLSecurityUtil.class);

    /**
     * Escapes special characters in a LIKE pattern to prevent unintended wildcard behavior.
     * Escapes the following characters:
     * - % (wildcard for any sequence of characters)
     * - _ (wildcard for any single character)
     * - \ (escape character itself)
     *
     * @param input The user input to escape
     * @return The escaped string safe for use in LIKE patterns
     */
    public static String escapeLikePattern(String input) {
        if (input == null) {
            return null;
        }

        // Escape backslash first to avoid double-escaping
        String escaped = input.replace("\\", "\\\\");

        // Escape percent and underscore
        escaped = escaped.replace("%", "\\%");
        escaped = escaped.replace("_", "\\_");

        logger.debug("Escaped LIKE pattern: '{}' -> '{}'", input, escaped);

        return escaped;
    }

    /**
     * Creates a LIKE pattern for searching with the specified match mode.
     * Automatically escapes special characters in the search text.
     *
     * @param searchText The text to search for
     * @param matchMode The type of matching to perform
     * @return A safe LIKE pattern
     */
    public static String createLikePattern(String searchText, LikeMatchMode matchMode) {
        if (searchText == null) {
            throw new IllegalArgumentException("Search text cannot be null");
        }

        String escaped = escapeLikePattern(searchText);

        return switch (matchMode) {
            case EXACT -> escaped;
            case STARTS_WITH -> escaped + "%";
            case ENDS_WITH -> "%" + escaped;
            case CONTAINS -> "%" + escaped + "%";
        };
    }

    /**
     * Enum defining different LIKE pattern matching modes.
     */
    public enum LikeMatchMode {
        /** Exact match (no wildcards) */
        EXACT,
        /** Matches strings starting with the search text */
        STARTS_WITH,
        /** Matches strings ending with the search text */
        ENDS_WITH,
        /** Matches strings containing the search text anywhere */
        CONTAINS
    }

    /**
     * Validates that a search text is not excessively long to prevent DoS attacks.
     *
     * @param searchText The search text to validate
     * @param maxLength Maximum allowed length
     * @throws IllegalArgumentException if the search text is too long
     */
    public static void validateSearchText(String searchText, int maxLength) {
        if (searchText == null) {
            throw new IllegalArgumentException("Search text cannot be null");
        }

        if (searchText.length() > maxLength) {
            throw new IllegalArgumentException(
                String.format("Search text too long: %d characters (max: %d)",
                             searchText.length(), maxLength));
        }
    }

    /**
     * Validates and prepares search text for safe use in SQL LIKE queries.
     * Combines length validation and pattern escaping.
     *
     * @param searchText The raw search text from user input
     * @param maxLength Maximum allowed length
     * @param matchMode The matching mode to use
     * @return A safe, escaped LIKE pattern
     * @throws IllegalArgumentException if validation fails
     */
    public static String prepareSafeLikePattern(String searchText, int maxLength, LikeMatchMode matchMode) {
        validateSearchText(searchText, maxLength);
        return createLikePattern(searchText, matchMode);
    }
}
