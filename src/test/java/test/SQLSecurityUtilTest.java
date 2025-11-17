package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.SQLSecurityUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for SQLSecurityUtil security features.
 * Tests SQL injection prevention, LIKE pattern escaping, and input validation.
 */
class SQLSecurityUtilTest {

    // ==================== LIKE Pattern Escaping Tests ====================

    @Test
    @DisplayName("Escape LIKE pattern - escape wildcard characters")
    void testEscapeLikePattern_EscapesWildcards() {
        String input = "test%value_here";
        String escaped = SQLSecurityUtil.escapeLikePattern(input);

        assertEquals("test\\%value\\_here", escaped,
            "Should escape % and _ wildcards");
    }

    @Test
    @DisplayName("Escape LIKE pattern - escape backslash")
    void testEscapeLikePattern_EscapesBackslash() {
        String input = "test\\value";
        String escaped = SQLSecurityUtil.escapeLikePattern(input);

        assertEquals("test\\\\value", escaped,
            "Should escape backslash first to prevent double-escaping issues");
    }

    @Test
    @DisplayName("Escape LIKE pattern - handle empty string")
    void testEscapeLikePattern_HandlesEmpty() {
        String empty = "";
        String escaped = SQLSecurityUtil.escapeLikePattern(empty);

        assertEquals("", escaped, "Should handle empty string");
    }

    @Test
    @DisplayName("Escape LIKE pattern - handle string with no special chars")
    void testEscapeLikePattern_PlainString() {
        String plain = "normaltext";
        String escaped = SQLSecurityUtil.escapeLikePattern(plain);

        assertEquals("normaltext", escaped,
            "Should not modify string without special characters");
    }

    @Test
    @DisplayName("Escape LIKE pattern - complex injection attempt")
    void testEscapeLikePattern_ComplexInjection() {
        String injection = "%' OR '1'='1";
        String escaped = SQLSecurityUtil.escapeLikePattern(injection);

        assertEquals("\\%' OR '1'='1", escaped,
            "Should escape wildcard but preserve other SQL syntax (handled by prepared statements)");
        assertTrue(escaped.startsWith("\\%"), "Should neutralize leading wildcard");
    }

    // ==================== Search Text Validation Tests ====================

    @Test
    @DisplayName("Validate search text - accept valid input")
    void testValidateSearchText_AcceptsValid() {
        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText("test", 100),
            "Should accept normal text");

        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText("كلمة عربية", 100),
            "Should accept unicode text");

        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText("test123", 100),
            "Should accept alphanumeric");
    }

    @Test
    @DisplayName("Validate search text - reject null input")
    void testValidateSearchText_RejectsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.validateSearchText(null, 100),
            "Should reject null input");
    }

    @Test
    @DisplayName("Validate search text - reject empty input")
    void testValidateSearchText_RejectsEmpty() {
        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.validateSearchText("", 100),
            "Should reject empty string");

        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.validateSearchText("   ", 100),
            "Should reject whitespace-only string");
    }

    @Test
    @DisplayName("Validate search text - enforce length limit")
    void testValidateSearchText_EnforcesLengthLimit() {
        String tooLong = "a".repeat(600);

        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.validateSearchText(tooLong, 500),
            "Should reject text exceeding maxLength");
    }

    @Test
    @DisplayName("Validate search text - accept at length limit")
    void testValidateSearchText_AcceptsAtLimit() {
        String atLimit = "a".repeat(500);

        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText(atLimit, 500),
            "Should accept text at exactly maxLength");
    }

    // ==================== Prepared LIKE Pattern Tests ====================

    @Test
    @DisplayName("Prepare safe LIKE pattern - CONTAINS mode")
    void testPrepareSafeLikePattern_ContainsMode() {
        String input = "test";
        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            input, 100, SQLSecurityUtil.LikeMatchMode.CONTAINS);

        assertEquals("%test%", pattern,
            "CONTAINS mode should wrap with wildcards");
    }

    @Test
    @DisplayName("Prepare safe LIKE pattern - STARTS_WITH mode")
    void testPrepareSafeLikePattern_StartsWithMode() {
        String input = "test";
        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            input, 100, SQLSecurityUtil.LikeMatchMode.STARTS_WITH);

        assertEquals("test%", pattern,
            "STARTS_WITH mode should add trailing wildcard only");
    }

    @Test
    @DisplayName("Prepare safe LIKE pattern - ENDS_WITH mode")
    void testPrepareSafeLikePattern_EndsWithMode() {
        String input = "test";
        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            input, 100, SQLSecurityUtil.LikeMatchMode.ENDS_WITH);

        assertEquals("%test", pattern,
            "ENDS_WITH mode should add leading wildcard only");
    }

    @Test
    @DisplayName("Prepare safe LIKE pattern - EXACT mode")
    void testPrepareSafeLikePattern_ExactMode() {
        String input = "test";
        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            input, 100, SQLSecurityUtil.LikeMatchMode.EXACT);

        assertEquals("test", pattern,
            "EXACT mode should not add wildcards");
    }

    @Test
    @DisplayName("Prepare safe LIKE pattern - escapes malicious input")
    void testPrepareSafeLikePattern_EscapesMaliciousInput() {
        String malicious = "%admin%";
        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            malicious, 100, SQLSecurityUtil.LikeMatchMode.CONTAINS);

        assertEquals("%\\%admin\\%%", pattern,
            "Should escape existing wildcards before adding pattern wildcards");
        assertFalse(pattern.matches(".*[^\\\\]%[^\\\\].*"),
            "All unescaped % should be pattern wildcards only");
    }

    @Test
    @DisplayName("Prepare safe LIKE pattern - validates input first")
    void testPrepareSafeLikePattern_ValidatesInput() {
        String tooLong = "a".repeat(600);

        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.prepareSafeLikePattern(
                tooLong, 500, SQLSecurityUtil.LikeMatchMode.CONTAINS),
            "Should validate input before processing");

        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.prepareSafeLikePattern(
                null, 100, SQLSecurityUtil.LikeMatchMode.CONTAINS),
            "Should reject null input");
    }

    // ==================== SQL Injection Prevention Tests ====================

    @Test
    @DisplayName("Security - wildcard injection prevention")
    void testSecurity_WildcardInjection() {
        // Attacker tries to match all records with % wildcard
        String attack = "%";
        String safe = SQLSecurityUtil.prepareSafeLikePattern(
            attack, 100, SQLSecurityUtil.LikeMatchMode.EXACT);

        assertEquals("\\%", safe,
            "Should escape standalone wildcard");

        // When used in SQL: WHERE content LIKE '\\%' ESCAPE '\\'
        // This will match literal '%' character, not all records
    }

    @Test
    @DisplayName("Security - LIKE injection with OR clause")
    void testSecurity_LikeInjectionWithOR() {
        // Attacker tries: %' OR '1'='1
        String attack = "%' OR '1'='1";
        String safe = SQLSecurityUtil.escapeLikePattern(attack);

        assertTrue(safe.startsWith("\\%"),
            "Should escape leading wildcard");
        // The ' characters are handled by PreparedStatement parameterization
        // Our job is just to escape LIKE metacharacters
    }

    @Test
    @DisplayName("Security - underscore wildcard exploitation")
    void testSecurity_UnderscoreWildcard() {
        // _ matches any single character, could be used for data extraction
        String attack = "admin_";
        String safe = SQLSecurityUtil.escapeLikePattern(attack);

        assertEquals("admin\\_", safe,
            "Should escape underscore to match literal character");
    }

    @Test
    @DisplayName("Security - backslash escape sequence bypass")
    void testSecurity_BackslashBypass() {
        // Attacker tries to use backslash to escape our escaping
        String attack = "\\%test";
        String safe = SQLSecurityUtil.escapeLikePattern(attack);

        assertEquals("\\\\\\%test", safe,
            "Should escape backslash first, then wildcard");
        // Result: \\\\\\% means literal \\ followed by literal %
    }

    @Test
    @DisplayName("Security - DoS prevention via length limit")
    void testSecurity_DoSPrevention() {
        // Extremely long search text could cause DoS
        String dos = "a".repeat(10000);

        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.validateSearchText(dos, 500),
            "Should prevent DoS with length limits");
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Integration - complete LIKE query protection")
    void testIntegration_CompleteLikeQueryProtection() {
        // Simulate real-world usage
        String userInput = "%admin%' OR '1'='1";

        // Step 1: Validate
        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.validateSearchText(userInput, 20),
            "Should reject if exceeds length limit");

        // With appropriate length limit:
        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText(userInput, 100),
            "Should accept if within length limit");

        // Step 2: Prepare safe pattern
        String safePattern = SQLSecurityUtil.prepareSafeLikePattern(
            userInput, 100, SQLSecurityUtil.LikeMatchMode.CONTAINS);

        // Step 3: Verify pattern is safe
        assertTrue(safePattern.contains("\\%"),
            "Wildcards should be escaped");
        assertTrue(safePattern.startsWith("%"),
            "Should have pattern wildcard at start (CONTAINS mode)");
        assertTrue(safePattern.endsWith("%"),
            "Should have pattern wildcard at end (CONTAINS mode)");

        // When used: WHERE content LIKE ? ESCAPE '\\'
        // PreparedStatement sets parameter to: %\%admin\%' OR '1'='1%
        // This safely searches for the literal string "%admin%' OR '1'='1"
    }

    @Test
    @DisplayName("Integration - Unicode search safety")
    void testIntegration_UnicodeSearch() {
        String arabicSearch = "كتاب";

        // Should handle unicode without issues
        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText(arabicSearch, 100));

        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            arabicSearch, 100, SQLSecurityUtil.LikeMatchMode.CONTAINS);

        assertTrue(pattern.contains("كتاب"),
            "Should preserve unicode characters");
        assertEquals("%كتاب%", pattern,
            "Should correctly format unicode search");
    }

    @Test
    @DisplayName("Integration - empty search handling")
    void testIntegration_EmptySearchHandling() {
        // Empty searches should be rejected early
        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.prepareSafeLikePattern(
                "", 100, SQLSecurityUtil.LikeMatchMode.CONTAINS),
            "Should reject empty search text");

        assertThrows(IllegalArgumentException.class,
            () -> SQLSecurityUtil.prepareSafeLikePattern(
                "   ", 100, SQLSecurityUtil.LikeMatchMode.CONTAINS),
            "Should reject whitespace-only search text");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Edge case - multiple consecutive wildcards")
    void testEdgeCase_MultipleWildcards() {
        String input = "%%__%%";
        String escaped = SQLSecurityUtil.escapeLikePattern(input);

        assertEquals("\\%\\%\\_\\_\\%\\%", escaped,
            "Should escape all wildcard characters");
    }

    @Test
    @DisplayName("Edge case - mixed special characters")
    void testEdgeCase_MixedSpecialChars() {
        String input = "\\%_test_\\%";
        String escaped = SQLSecurityUtil.escapeLikePattern(input);

        assertEquals("\\\\\\%\\_test\\_\\\\\\%", escaped,
            "Should escape backslashes before wildcards");
    }

    @Test
    @DisplayName("Edge case - single character search")
    void testEdgeCase_SingleCharacter() {
        String single = "a";

        assertDoesNotThrow(() -> SQLSecurityUtil.validateSearchText(single, 100));

        String pattern = SQLSecurityUtil.prepareSafeLikePattern(
            single, 100, SQLSecurityUtil.LikeMatchMode.EXACT);

        assertEquals("a", pattern, "Should handle single character search");
    }
}
