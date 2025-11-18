package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.MarkdownParser;
import util.MarkdownParser.MarkdownDocument;
import util.MarkdownParser.MarkdownElement;
import util.MarkdownParser.MarkdownElementType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MarkdownParser.
 * Tests all supported markdown syntax and edge cases.
 */
class MarkdownParserTest {

    private MarkdownParser parser;

    @BeforeEach
    void setUp() {
        parser = new MarkdownParser();
    }

    // ==================== Header Tests ====================

    @Test
    @DisplayName("Should parse H1 header")
    void testH1Header() {
        String markdown = "# Header 1";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(1, doc.getElementCount());
        MarkdownElement element = doc.getElements().get(0);
        assertEquals(MarkdownElementType.HEADER, element.getType());
        assertEquals("Header 1", element.getContent());
        assertEquals(1, element.getLevel());
    }

    @Test
    @DisplayName("Should parse all header levels (H1-H6)")
    void testAllHeaderLevels() {
        String markdown = "# H1\n## H2\n### H3\n#### H4\n##### H5\n###### H6";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(6, doc.getElementCount());
        for (int i = 0; i < 6; i++) {
            MarkdownElement element = doc.getElements().get(i);
            assertEquals(MarkdownElementType.HEADER, element.getType());
            assertEquals(i + 1, element.getLevel());
        }
    }

    @Test
    @DisplayName("Should convert headers to HTML")
    void testHeadersToHTML() {
        String markdown = "# Title\n## Subtitle";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<h2>Subtitle</h2>"));
    }

    // ==================== Paragraph Tests ====================

    @Test
    @DisplayName("Should parse simple paragraph")
    void testSimpleParagraph() {
        String markdown = "This is a paragraph.";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(1, doc.getElementCount());
        assertEquals(MarkdownElementType.PARAGRAPH, doc.getElements().get(0).getType());
        assertEquals("This is a paragraph.", doc.getElements().get(0).getContent());
    }

    @Test
    @DisplayName("Should handle multi-line paragraphs")
    void testMultiLineParagraph() {
        String markdown = "Line 1\nLine 2\nLine 3";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(1, doc.getElementCount());
        assertEquals(MarkdownElementType.PARAGRAPH, doc.getElements().get(0).getType());
        assertTrue(doc.getElements().get(0).getContent().contains("Line 1"));
        assertTrue(doc.getElements().get(0).getContent().contains("Line 2"));
    }

    @Test
    @DisplayName("Should separate paragraphs with blank lines")
    void testMultipleParagraphs() {
        String markdown = "Paragraph 1\n\nParagraph 2";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(2, doc.getElementCount());
        assertEquals("Paragraph 1", doc.getElements().get(0).getContent());
        assertEquals("Paragraph 2", doc.getElements().get(1).getContent());
    }

    // ==================== Emphasis Tests ====================

    @Test
    @DisplayName("Should convert bold to HTML")
    void testBold() {
        String markdown = "This is **bold** text";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<strong>bold</strong>"));
    }

    @Test
    @DisplayName("Should convert italic to HTML")
    void testItalic() {
        String markdown = "This is *italic* text";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<em>italic</em>"));
    }

    @Test
    @DisplayName("Should convert bold-italic to HTML")
    void testBoldItalic() {
        String markdown = "This is ***bold italic*** text";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<strong><em>bold italic</em></strong>"));
    }

    @Test
    @DisplayName("Should handle multiple emphasis in same line")
    void testMultipleEmphasis() {
        String markdown = "**bold** and *italic* and ***both***";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<em>italic</em>"));
        assertTrue(html.contains("<strong><em>both</em></strong>"));
    }

    // ==================== List Tests ====================

    @Test
    @DisplayName("Should parse unordered list")
    void testUnorderedList() {
        String markdown = "- Item 1\n- Item 2\n- Item 3";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(3, doc.getElementCount());
        for (MarkdownElement element : doc.getElements()) {
            assertEquals(MarkdownElementType.LIST_ITEM, element.getType());
        }
    }

    @Test
    @DisplayName("Should parse ordered list")
    void testOrderedList() {
        String markdown = "1. First\n2. Second\n3. Third";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(3, doc.getElementCount());
        for (MarkdownElement element : doc.getElements()) {
            assertEquals(MarkdownElementType.ORDERED_LIST_ITEM, element.getType());
        }
    }

    @Test
    @DisplayName("Should convert lists to HTML")
    void testListsToHTML() {
        String markdown = "- Item 1\n- Item 2";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>Item 1</li>"));
        assertTrue(html.contains("<li>Item 2</li>"));
        assertTrue(html.contains("</ul>"));
    }

    @Test
    @DisplayName("Should handle different list markers")
    void testDifferentListMarkers() {
        String markdown1 = "- Item";
        String markdown2 = "* Item";
        String markdown3 = "+ Item";

        assertTrue(parser.parseToHTML(markdown1).contains("<ul>"));
        assertTrue(parser.parseToHTML(markdown2).contains("<ul>"));
        assertTrue(parser.parseToHTML(markdown3).contains("<ul>"));
    }

    // ==================== Link and Image Tests ====================

    @Test
    @DisplayName("Should parse links")
    void testLinks() {
        String markdown = "[Google](https://google.com)";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<a href=\"https://google.com\">Google</a>"));
    }

    @Test
    @DisplayName("Should parse images")
    void testImages() {
        String markdown = "![Alt text](image.jpg)";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<img src=\"image.jpg\" alt=\"Alt text\" />"));
    }

    // ==================== Code Tests ====================

    @Test
    @DisplayName("Should parse inline code")
    void testInlineCode() {
        String markdown = "Use `code` here";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<code>code</code>"));
    }

    @Test
    @DisplayName("Should parse code blocks")
    void testCodeBlocks() {
        String markdown = "```\nint x = 5;\nSystem.out.println(x);\n```";
        MarkdownDocument doc = parser.parse(markdown);

        boolean hasCodeBlock = doc.getElements().stream()
            .anyMatch(e -> e.getType() == MarkdownElementType.CODE_BLOCK);
        assertTrue(hasCodeBlock, "Should contain code block");
    }

    @Test
    @DisplayName("Should escape HTML in code blocks")
    void testCodeBlockHTMLEscape() {
        String markdown = "```\n<script>alert('xss')</script>\n```";
        String html = parser.parseToHTML(markdown);

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }

    // ==================== Blockquote Tests ====================

    @Test
    @DisplayName("Should parse blockquotes")
    void testBlockquotes() {
        String markdown = "> This is a quote";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(1, doc.getElementCount());
        assertEquals(MarkdownElementType.BLOCKQUOTE, doc.getElements().get(0).getType());
    }

    @Test
    @DisplayName("Should convert blockquotes to HTML")
    void testBlockquotesToHTML() {
        String markdown = "> Quote text";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<blockquote>Quote text</blockquote>"));
    }

    // ==================== Horizontal Rule Tests ====================

    @Test
    @DisplayName("Should parse horizontal rules")
    void testHorizontalRules() {
        String markdown1 = "---";
        String markdown2 = "***";
        String markdown3 = "___";

        assertEquals(1, parser.parse(markdown1).getElementCount());
        assertEquals(1, parser.parse(markdown2).getElementCount());
        assertEquals(1, parser.parse(markdown3).getElementCount());
    }

    @Test
    @DisplayName("Should convert horizontal rules to HTML")
    void testHorizontalRulesToHTML() {
        String markdown = "---";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<hr/>"));
    }

    // ==================== Unicode and Arabic Tests ====================

    @Test
    @DisplayName("Should handle Arabic text")
    void testArabicText() {
        String markdown = "# عنوان\n\nهذا نص عربي";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(2, doc.getElementCount());
        assertEquals("عنوان", doc.getElements().get(0).getContent());
        assertEquals("هذا نص عربي", doc.getElements().get(1).getContent());
    }

    @Test
    @DisplayName("Should handle mixed Arabic and English")
    void testMixedLanguages() {
        String markdown = "# Arabic and English\n\nThis is **English** and هذا **عربي**";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("English"));
        assertTrue(html.contains("عربي"));
        assertTrue(html.contains("<strong>"));
    }

    // ==================== Complex Document Tests ====================

    @Test
    @DisplayName("Should parse complex document")
    void testComplexDocument() {
        String markdown = "# Title\n\n" +
                         "Paragraph with **bold** and *italic*.\n\n" +
                         "- List item 1\n" +
                         "- List item 2\n\n" +
                         "> A quote\n\n" +
                         "---\n\n" +
                         "```\ncode block\n```";

        MarkdownDocument doc = parser.parse(markdown);

        assertTrue(doc.getElementCount() >= 6, "Should parse all elements");

        // Verify different element types exist
        long headerCount = doc.getElements().stream()
            .filter(e -> e.getType() == MarkdownElementType.HEADER).count();
        assertTrue(headerCount > 0, "Should have headers");

        long listCount = doc.getElements().stream()
            .filter(e -> e.getType() == MarkdownElementType.LIST_ITEM).count();
        assertTrue(listCount > 0, "Should have list items");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle empty markdown")
    void testEmptyMarkdown() {
        String markdown = "";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(0, doc.getElementCount(), "Empty markdown should produce no elements");
    }

    @Test
    @DisplayName("Should handle null markdown")
    void testNullMarkdown() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse(null),
            "Should throw exception for null markdown");
    }

    @Test
    @DisplayName("Should handle only whitespace")
    void testWhitespaceOnly() {
        String markdown = "   \n\n   \n";
        MarkdownDocument doc = parser.parse(markdown);

        assertEquals(0, doc.getElementCount(), "Whitespace only should produce no elements");
    }

    @Test
    @DisplayName("Should handle very long document")
    void testLongDocument() {
        StringBuilder markdown = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            markdown.append("Line ").append(i).append("\n");
        }

        MarkdownDocument doc = parser.parse(markdown.toString());

        assertTrue(doc.getElementCount() > 0, "Should parse long document");
    }

    @Test
    @DisplayName("Should handle nested emphasis")
    void testNestedEmphasis() {
        String markdown = "This has ***nested*** formatting";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("<strong><em>nested</em></strong>"));
    }

    @Test
    @DisplayName("Should handle special characters in links")
    void testSpecialCharsInLinks() {
        String markdown = "[Link](https://example.com?param=value&other=123)";
        String html = parser.parseToHTML(markdown);

        assertTrue(html.contains("https://example.com?param=value&other=123"));
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should parse large document efficiently")
    void testLargeDocumentPerformance() {
        StringBuilder markdown = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            markdown.append("# Header ").append(i).append("\n\n");
            markdown.append("Paragraph ").append(i).append("\n\n");
        }

        long startTime = System.currentTimeMillis();
        MarkdownDocument doc = parser.parse(markdown.toString());
        long endTime = System.currentTimeMillis();

        assertTrue(doc.getElementCount() > 0, "Should parse large document");
        assertTrue(endTime - startTime < 5000, "Should parse in reasonable time (<5s)");
    }
}
