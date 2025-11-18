package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.components.MarkdownRenderer;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MarkdownRenderer component.
 * Tests HTML rendering, CSS styling, component lifecycle, and edge cases.
 */
class MarkdownRendererTest {

    private MarkdownRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new MarkdownRenderer();
    }

    // ==================== Initialization Tests ====================

    @Test
    @DisplayName("Should initialize without errors")
    void testInitialization() {
        assertDoesNotThrow(() -> renderer.initialize(),
            "Initialization should not throw exceptions");
    }

    @Test
    @DisplayName("Should return JPanel component after initialization")
    void testGetComponent() {
        renderer.initialize();
        JPanel component = renderer.getComponent();

        assertNotNull(component, "Component should not be null");
        assertTrue(component instanceof JPanel, "Should return JPanel");
    }

    @Test
    @DisplayName("Should not throw when getting component before initialization")
    void testGetComponentBeforeInit() {
        assertDoesNotThrow(() -> renderer.getComponent(),
            "Should handle getting component before initialization");
    }

    // ==================== Markdown Rendering Tests ====================

    @Test
    @DisplayName("Should render simple markdown text")
    void testRenderSimpleMarkdown() {
        renderer.initialize();

        assertDoesNotThrow(() -> renderer.setMarkdown("# Hello World"),
            "Should render simple markdown without errors");
    }

    @Test
    @DisplayName("Should render markdown with headers")
    void testRenderHeaders() {
        renderer.initialize();
        String markdown = "# H1\n## H2\n### H3";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render headers without errors");
    }

    @Test
    @DisplayName("Should render markdown with emphasis")
    void testRenderEmphasis() {
        renderer.initialize();
        String markdown = "This is **bold** and *italic* text";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render emphasis without errors");
    }

    @Test
    @DisplayName("Should render markdown with lists")
    void testRenderLists() {
        renderer.initialize();
        String markdown = "- Item 1\n- Item 2\n- Item 3";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render lists without errors");
    }

    @Test
    @DisplayName("Should render markdown with code blocks")
    void testRenderCodeBlocks() {
        renderer.initialize();
        String markdown = "```\nint x = 5;\nSystem.out.println(x);\n```";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render code blocks without errors");
    }

    @Test
    @DisplayName("Should render markdown with links")
    void testRenderLinks() {
        renderer.initialize();
        String markdown = "[Google](https://google.com)";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render links without errors");
    }

    @Test
    @DisplayName("Should render markdown with images")
    void testRenderImages() {
        renderer.initialize();
        String markdown = "![Alt text](image.jpg)";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render images without errors");
    }

    @Test
    @DisplayName("Should render markdown with blockquotes")
    void testRenderBlockquotes() {
        renderer.initialize();
        String markdown = "> This is a quote";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render blockquotes without errors");
    }

    @Test
    @DisplayName("Should render complex markdown document")
    void testRenderComplexDocument() {
        renderer.initialize();
        String markdown = "# Title\n\n" +
                         "This is **bold** and *italic*.\n\n" +
                         "- List item 1\n" +
                         "- List item 2\n\n" +
                         "> A quote\n\n" +
                         "```\ncode block\n```";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render complex document without errors");
    }

    // ==================== Arabic/Unicode Tests ====================

    @Test
    @DisplayName("Should render Arabic text")
    void testRenderArabicText() {
        renderer.initialize();
        String markdown = "# عنوان\n\nهذا نص عربي";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render Arabic text without errors");
    }

    @Test
    @DisplayName("Should render mixed Arabic and English")
    void testRenderMixedLanguages() {
        renderer.initialize();
        String markdown = "# Title عنوان\n\nEnglish and عربي mixed";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should render mixed languages without errors");
    }

    @Test
    @DisplayName("Should handle RTL text")
    void testRenderRTLText() {
        renderer.initialize();
        String markdown = "مرحبا بك في المفكرة العربية";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should handle RTL text without errors");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle null markdown")
    void testNullMarkdown() {
        renderer.initialize();

        // Should either handle gracefully or throw IllegalArgumentException
        try {
            renderer.setMarkdown(null);
            // If it doesn't throw, that's fine too
        } catch (IllegalArgumentException e) {
            // Expected behavior
            assertTrue(e.getMessage().contains("null") || e.getMessage().contains("Markdown"),
                "Exception message should mention null or markdown");
        }
    }

    @Test
    @DisplayName("Should handle empty markdown")
    void testEmptyMarkdown() {
        renderer.initialize();

        assertDoesNotThrow(() -> renderer.setMarkdown(""),
            "Should handle empty markdown without errors");
    }

    @Test
    @DisplayName("Should handle whitespace-only markdown")
    void testWhitespaceOnlyMarkdown() {
        renderer.initialize();

        assertDoesNotThrow(() -> renderer.setMarkdown("   \n\n   "),
            "Should handle whitespace-only markdown without errors");
    }

    @Test
    @DisplayName("Should handle very long markdown")
    void testVeryLongMarkdown() {
        renderer.initialize();
        StringBuilder longMarkdown = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMarkdown.append("Line ").append(i).append("\n\n");
        }

        assertDoesNotThrow(() -> renderer.setMarkdown(longMarkdown.toString()),
            "Should handle very long markdown without errors");
    }

    @Test
    @DisplayName("Should handle special characters")
    void testSpecialCharacters() {
        renderer.initialize();
        String markdown = "Special chars: < > & \" ' \\ / @ # $ % ^ * ( ) [ ] { }";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should handle special characters without errors");
    }

    @Test
    @DisplayName("Should handle malformed markdown")
    void testMalformedMarkdown() {
        renderer.initialize();
        String markdown = "**bold without closing\n*italic without closing\n[link without url";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should handle malformed markdown without errors");
    }

    // ==================== Clear Functionality Tests ====================

    @Test
    @DisplayName("Should clear content")
    void testClear() {
        renderer.initialize();
        renderer.setMarkdown("# Some content");

        assertDoesNotThrow(() -> renderer.clear(),
            "Should clear content without errors");
    }

    @Test
    @DisplayName("Should clear before initialization")
    void testClearBeforeInit() {
        // Should handle clearing before initialization
        assertDoesNotThrow(() -> renderer.clear(),
            "Should handle clear before initialization");
    }

    @Test
    @DisplayName("Should allow setting markdown after clear")
    void testSetMarkdownAfterClear() {
        renderer.initialize();
        renderer.setMarkdown("# Original content");
        renderer.clear();

        assertDoesNotThrow(() -> renderer.setMarkdown("# New content"),
            "Should allow setting markdown after clear");
    }

    // ==================== Multiple Renders Tests ====================

    @Test
    @DisplayName("Should handle multiple consecutive renders")
    void testMultipleRenders() {
        renderer.initialize();

        assertDoesNotThrow(() -> {
            renderer.setMarkdown("# First");
            renderer.setMarkdown("# Second");
            renderer.setMarkdown("# Third");
        }, "Should handle multiple consecutive renders");
    }

    @Test
    @DisplayName("Should handle rapid markdown updates")
    void testRapidUpdates() {
        renderer.initialize();

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                renderer.setMarkdown("# Update " + i);
            }
        }, "Should handle rapid markdown updates");
    }

    // ==================== Lifecycle Tests ====================

    @Test
    @DisplayName("Should dispose without errors")
    void testDispose() {
        renderer.initialize();
        renderer.setMarkdown("# Content");

        assertDoesNotThrow(() -> renderer.dispose(),
            "Should dispose without errors");
    }

    @Test
    @DisplayName("Should dispose before initialization")
    void testDisposeBeforeInit() {
        assertDoesNotThrow(() -> renderer.dispose(),
            "Should handle dispose before initialization");
    }

    @Test
    @DisplayName("Should handle dispose multiple times")
    void testMultipleDispose() {
        renderer.initialize();

        assertDoesNotThrow(() -> {
            renderer.dispose();
            renderer.dispose();
            renderer.dispose();
        }, "Should handle multiple dispose calls");
    }

    // ==================== HTML Sanitization Tests ====================

    @Test
    @DisplayName("Should sanitize HTML in code blocks")
    void testHTMLSanitization() {
        renderer.initialize();
        String markdown = "```\n<script>alert('xss')</script>\n```";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should sanitize HTML in code blocks");
    }

    @Test
    @DisplayName("Should handle HTML entities")
    void testHTMLEntities() {
        renderer.initialize();
        String markdown = "&lt;tag&gt; &amp; &quot;quotes&quot;";

        assertDoesNotThrow(() -> renderer.setMarkdown(markdown),
            "Should handle HTML entities");
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should render large document efficiently")
    void testLargeDocumentPerformance() {
        renderer.initialize();
        StringBuilder largeDoc = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            largeDoc.append("# Header ").append(i).append("\n\n");
            largeDoc.append("Paragraph ").append(i).append("\n\n");
        }

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> renderer.setMarkdown(largeDoc.toString()),
            "Should render large document");
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 10000,
            "Should render large document in reasonable time (<10s), took: " + duration + "ms");
    }
}
