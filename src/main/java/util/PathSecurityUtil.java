package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for secure file path validation and sanitization.
 * Prevents path traversal attacks and ensures file operations stay within allowed directories.
 */
public class PathSecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(PathSecurityUtil.class);

    // Maximum allowed filename length
    private static final int MAX_FILENAME_LENGTH = 255;

    // Characters that are not allowed in filenames
    private static final String INVALID_FILENAME_CHARS = "[/\\\\:*?\"<>|\\x00-\\x1F]";

    /**
     * Sanitizes a filename by removing or replacing invalid characters.
     * Prevents path traversal attacks by removing directory separators and special characters.
     *
     * @param filename The filename to sanitize
     * @return A safe filename
     * @throws IllegalArgumentException if the filename is null, empty, or cannot be sanitized
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // Remove leading/trailing whitespace
        String sanitized = filename.trim();

        // Remove path traversal sequences
        sanitized = sanitized.replaceAll("\\.\\.", "");

        // Remove invalid characters (including path separators)
        sanitized = sanitized.replaceAll(INVALID_FILENAME_CHARS, "_");

        // Remove leading/trailing dots and spaces (problematic on Windows)
        sanitized = sanitized.replaceAll("^[.\\s]+|[.\\s]+$", "");

        // Ensure filename is not empty after sanitization
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Filename becomes empty after sanitization: " + filename);
        }

        // Check length
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            logger.warn("Filename too long, truncating from {} to {} characters",
                       sanitized.length(), MAX_FILENAME_LENGTH);
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
        }

        // Reject reserved names on Windows
        String upperName = sanitized.toUpperCase();
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
                                  "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2",
                                  "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

        for (String reserved : reservedNames) {
            if (upperName.equals(reserved) || upperName.startsWith(reserved + ".")) {
                sanitized = "_" + sanitized;
                break;
            }
        }

        if (!sanitized.equals(filename)) {
            logger.debug("Filename sanitized: '{}' -> '{}'", filename, sanitized);
        }

        return sanitized;
    }

    /**
     * Validates that a file path is within an allowed directory.
     * Prevents path traversal attacks by ensuring the canonical path is within the base directory.
     *
     * @param baseDirectory The allowed base directory
     * @param filePath The file path to validate
     * @return true if the path is safe, false otherwise
     */
    public static boolean isPathWithinDirectory(File baseDirectory, File filePath) {
        try {
            String canonicalBase = baseDirectory.getCanonicalPath();
            String canonicalFile = filePath.getCanonicalPath();

            // Check if the file path starts with the base directory path
            boolean isWithin = canonicalFile.startsWith(canonicalBase);

            if (!isWithin) {
                logger.warn("Security violation: File path '{}' is outside allowed directory '{}'",
                           canonicalFile, canonicalBase);
            }

            return isWithin;
        } catch (IOException e) {
            logger.error("Error validating path security", e);
            return false;
        }
    }

    /**
     * Creates a safe file object within a base directory using a sanitized filename.
     * Combines sanitization and path validation for secure file creation.
     *
     * @param baseDirectory The base directory
     * @param filename The filename (will be sanitized)
     * @param extension The file extension (optional, can be null)
     * @return A safe File object
     * @throws IllegalArgumentException if the filename is invalid or path traversal is detected
     * @throws SecurityException if the resulting path is outside the base directory
     */
    public static File createSafeFile(File baseDirectory, String filename, String extension) {
        // Sanitize the filename
        String safeFilename = sanitizeFilename(filename);

        // Add extension if provided
        if (extension != null && !extension.isEmpty()) {
            if (!extension.startsWith(".")) {
                extension = "." + extension;
            }
            safeFilename = safeFilename + extension;
        }

        // Create file object
        File file = new File(baseDirectory, safeFilename);

        // Validate that the file is within the base directory
        if (!isPathWithinDirectory(baseDirectory, file)) {
            throw new SecurityException(
                String.format("Path traversal detected: filename '%s' would create file outside base directory",
                             filename));
        }

        return file;
    }

    /**
     * Validates a book title for safe use as a filename.
     * Checks length and content constraints.
     *
     * @param title The book title to validate
     * @throws IllegalArgumentException if the title is invalid
     */
    public static void validateBookTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be null or empty");
        }

        if (title.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Book title too long: %d characters (max: %d)",
                             title.length(), MAX_FILENAME_LENGTH));
        }

        // Check for path traversal attempts
        if (title.contains("..") || title.contains("/") || title.contains("\\")) {
            throw new IllegalArgumentException(
                "Book title contains invalid characters (path separators or traversal sequences)");
        }
    }
}
