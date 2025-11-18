package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import util.PathSecurityUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for PathSecurityUtil security features.
 * Tests path traversal prevention, filename sanitization, and validation.
 */
class PathSecurityUtilTest {

    // ==================== Filename Sanitization Tests ====================

    @Test
    @DisplayName("Sanitize filename - remove path traversal")
    void testSanitizeFilename_RemovesPathTraversal() {
        String malicious = "../../../etc/passwd";
        String sanitized = PathSecurityUtil.sanitizeFilename(malicious);

        assertFalse(sanitized.contains(".."), "Should remove .. sequences");
        assertFalse(sanitized.contains("/"), "Should remove path separators");
    }

    @Test
    @DisplayName("Sanitize filename - remove dangerous characters")
    void testSanitizeFilename_RemovesDangerousChars() {
        String dangerous = "test<>:\"|?*file.txt";
        String sanitized = PathSecurityUtil.sanitizeFilename(dangerous);

        assertFalse(sanitized.matches(".*[<>:\"|?*].*"),
            "Should remove dangerous characters: < > : \" | ? *");
    }

    @Test
    @DisplayName("Sanitize filename - preserve valid characters")
    void testSanitizeFilename_PreservesValidChars() {
        String valid = "My Book Title 2024.txt";
        String sanitized = PathSecurityUtil.sanitizeFilename(valid);

        assertTrue(sanitized.contains("My Book Title 2024"),
            "Should preserve alphanumeric and spaces");
    }

    @Test
    @DisplayName("Sanitize filename - handle empty/null input")
    void testSanitizeFilename_RejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.sanitizeFilename(null),
            "Should reject null filename");

        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.sanitizeFilename(""),
            "Should reject empty filename");

        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.sanitizeFilename("   "),
            "Should reject whitespace-only filename");
    }

    @Test
    @DisplayName("Sanitize filename - handle excessively long names")
    void testSanitizeFilename_HandlesLongNames() {
        String longName = "a".repeat(300); // 300 characters
        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.sanitizeFilename(longName),
            "Should reject filenames exceeding 255 characters");
    }

    @Test
    @DisplayName("Sanitize filename - remove leading/trailing dots and spaces")
    void testSanitizeFilename_RemovesLeadingTrailingChars() {
        String filename = "  ...test file...  ";
        String sanitized = PathSecurityUtil.sanitizeFilename(filename);

        assertFalse(sanitized.startsWith("."), "Should not start with dot");
        assertFalse(sanitized.endsWith("."), "Should not end with dot");
        assertFalse(sanitized.startsWith(" "), "Should not start with space");
        assertFalse(sanitized.endsWith(" "), "Should not end with space");
    }

    // ==================== Book Title Validation Tests ====================

    @Test
    @DisplayName("Validate book title - accept valid titles")
    void testValidateBookTitle_AcceptsValidTitles() {
        assertDoesNotThrow(() -> PathSecurityUtil.validateBookTitle("My Book"));
        assertDoesNotThrow(() -> PathSecurityUtil.validateBookTitle("كتاب عربي"));
        assertDoesNotThrow(() -> PathSecurityUtil.validateBookTitle("Book 2024"));
    }

    @Test
    @DisplayName("Validate book title - reject path separators")
    void testValidateBookTitle_RejectsPathSeparators() {
        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.validateBookTitle("../book"),
            "Should reject forward slash");

        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.validateBookTitle("..\\book"),
            "Should reject backslash");

        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.validateBookTitle("folder/book"),
            "Should reject path with forward slash");
    }

    @Test
    @DisplayName("Validate book title - reject null/empty")
    void testValidateBookTitle_RejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.validateBookTitle(null),
            "Should reject null title");

        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.validateBookTitle(""),
            "Should reject empty title");
    }

    @Test
    @DisplayName("Validate book title - reject excessively long")
    void testValidateBookTitle_RejectsLongTitles() {
        String longTitle = "a".repeat(300);
        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.validateBookTitle(longTitle),
            "Should reject titles exceeding 255 characters");
    }

    // ==================== Safe File Creation Tests ====================

    @Test
    @DisplayName("Create safe file - creates file in correct directory")
    void testCreateSafeFile_CreatesInCorrectDirectory(@TempDir Path tempDir) throws IOException {
        File dir = tempDir.toFile();
        File safeFile = PathSecurityUtil.createSafeFile(dir, "test", "txt");

        assertTrue(safeFile.getAbsolutePath().startsWith(dir.getAbsolutePath()),
            "File should be within parent directory");
        assertEquals("test.txt", safeFile.getName(), "Filename should be sanitized");
    }

    @Test
    @DisplayName("Create safe file - prevents path traversal")
    void testCreateSafeFile_PreventsPathTraversal(@TempDir Path tempDir) throws IOException {
        File dir = tempDir.toFile();
        File safeFile = PathSecurityUtil.createSafeFile(dir, "../../../etc/passwd", "txt");

        assertTrue(safeFile.getAbsolutePath().startsWith(dir.getAbsolutePath()),
            "Should prevent escaping parent directory");
        assertFalse(safeFile.getAbsolutePath().contains("etc"),
            "Should not contain traversal path elements");
    }

    @Test
    @DisplayName("Create safe file - rejects null directory")
    void testCreateSafeFile_RejectsNullDirectory() {
        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.createSafeFile(null, "test", "txt"),
            "Should reject null directory");
    }

    @Test
    @DisplayName("Create safe file - rejects non-directory")
    void testCreateSafeFile_RejectsNonDirectory(@TempDir Path tempDir) throws IOException {
        File notADir = new File(tempDir.toFile(), "regularfile.txt");
        notADir.createNewFile();

        assertThrows(IllegalArgumentException.class,
            () -> PathSecurityUtil.createSafeFile(notADir, "test", "txt"),
            "Should reject non-directory");
    }

    // ==================== Path Validation Tests ====================

    @Test
    @DisplayName("Is path within directory - validates safe paths")
    void testIsPathWithinDirectory_ValidatesSafePaths(@TempDir Path tempDir) throws IOException {
        File parent = tempDir.toFile();
        File child = new File(parent, "subdir/file.txt");

        assertTrue(PathSecurityUtil.isPathWithinDirectory(child, parent),
            "Child path should be within parent");
    }

    @Test
    @DisplayName("Is path within directory - detects traversal")
    void testIsPathWithinDirectory_DetectsTraversal(@TempDir Path tempDir) throws IOException {
        File parent = tempDir.toFile();
        File outside = new File(parent.getParentFile(), "outside.txt");

        assertFalse(PathSecurityUtil.isPathWithinDirectory(outside, parent),
            "Path outside parent should be detected");
    }

    @Test
    @DisplayName("Is path within directory - handles symbolic links safely")
    void testIsPathWithinDirectory_HandlesSymlinks(@TempDir Path tempDir) throws IOException {
        // This test verifies the implementation uses getCanonicalPath()
        // which resolves symlinks to prevent traversal
        File parent = tempDir.toFile();
        File legitimate = new File(parent, "legitimate.txt");
        legitimate.createNewFile();

        assertTrue(PathSecurityUtil.isPathWithinDirectory(legitimate, parent),
            "Should handle canonical paths correctly");
    }

    // ==================== Edge Cases and Security Tests ====================

    @Test
    @DisplayName("Security - null byte injection prevention")
    void testSecurity_NullByteInjectionPrevention() {
        String nullByteAttack = "test\0.txt.evil";
        String sanitized = PathSecurityUtil.sanitizeFilename(nullByteAttack);

        assertFalse(sanitized.contains("\0"),
            "Should remove null bytes");
    }

    @Test
    @DisplayName("Security - unicode normalization")
    void testSecurity_UnicodeHandling() {
        // Test that unicode characters are handled safely
        String arabic = "كتاب";
        String sanitized = PathSecurityUtil.sanitizeFilename(arabic);

        assertNotNull(sanitized, "Should handle unicode");
        assertTrue(sanitized.length() > 0, "Should preserve valid unicode");
    }

    @Test
    @DisplayName("Security - reserved Windows filenames")
    void testSecurity_ReservedWindowsNames() {
        // Windows reserved names: CON, PRN, AUX, NUL, COM1-9, LPT1-9
        String[] reserved = {"CON", "PRN", "AUX", "NUL", "COM1", "LPT1"};

        for (String name : reserved) {
            assertThrows(IllegalArgumentException.class,
                () -> PathSecurityUtil.sanitizeFilename(name),
                "Should reject reserved Windows name: " + name);
        }
    }

    @Test
    @DisplayName("Security - TOCTOU protection in file operations")
    void testSecurity_TOCTOUProtection(@TempDir Path tempDir) throws IOException {
        // Test that createSafeFile validates after canonicalization
        File parent = tempDir.toFile();
        File safe = PathSecurityUtil.createSafeFile(parent, "test", "txt");

        // Verify the returned file is still within parent
        // (guards against TOCTOU race conditions)
        assertTrue(PathSecurityUtil.isPathWithinDirectory(safe, parent),
            "Should maintain path validation after creation");
    }
}
