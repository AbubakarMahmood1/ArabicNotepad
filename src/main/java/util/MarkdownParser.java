package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown parser for ArabicNotepad.
 * Supports basic markdown syntax including headers, emphasis, lists, links, and code blocks.
 * Designed to work with both Latin and Arabic text.
 *
 * <p><b>Supported Markdown Features:</b></p>
 * <ul>
 *   <li>Headers: # H1, ## H2, ### H3, etc.</li>
 *   <li>Emphasis: *italic*, **bold**, ***bold italic***</li>
 *   <li>Lists: Unordered (-,*,+) and Ordered (1.)</li>
 *   <li>Links: [text](url)</li>
 *   <li>Images: ![alt](url)</li>
 *   <li>Code: `inline code` and ```code blocks```</li>
 *   <li>Blockquotes: > quote</li>
 *   <li>Horizontal rules: ---, ***, ___</li>
 *   <li>Line breaks and paragraphs</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * MarkdownParser parser = new MarkdownParser();
 * String html = parser.parseToHTML("# Hello\n\nThis is **bold** text");
 * MarkdownDocument doc = parser.parse("# Title\n\nParagraph");
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 2.0
 * @since 1.0
 */
public class MarkdownParser {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownParser.class);

    // Regex patterns for markdown elements
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern BOLD_ITALIC_PATTERN = Pattern.compile("\\*\\*\\*(.+?)\\*\\*\\*");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.+?)\\*");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^>\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern UNORDERED_LIST_PATTERN = Pattern.compile("^[-*+]\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("^\\d+\\.\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern HORIZONTAL_RULE_PATTERN = Pattern.compile("^(---|\\*\\*\\*|___)$", Pattern.MULTILINE);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([\\s\\S]*?)```", Pattern.MULTILINE);

    /**
     * Default constructor for MarkdownParser.
     */
    public MarkdownParser() {
        logger.debug("MarkdownParser initialized");
    }

    /**
     * Parses markdown text and returns a structured document representation.
     *
     * @param markdown The markdown text to parse
     * @return A MarkdownDocument containing parsed elements
     * @throws IllegalArgumentException if markdown is null
     */
    public MarkdownDocument parse(String markdown) {
        if (markdown == null) {
            throw new IllegalArgumentException("Markdown text cannot be null");
        }

        logger.debug("Parsing markdown document ({} characters)", markdown.length());

        MarkdownDocument document = new MarkdownDocument();
        String[] lines = markdown.split("\n");

        List<MarkdownElement> elements = new ArrayList<>();
        StringBuilder currentParagraph = new StringBuilder();
        boolean inCodeBlock = false;
        StringBuilder codeBlockContent = new StringBuilder();

        for (String line : lines) {
            // Handle code blocks
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    // End of code block
                    elements.add(new MarkdownElement(MarkdownElementType.CODE_BLOCK, codeBlockContent.toString()));
                    codeBlockContent = new StringBuilder();
                    inCodeBlock = false;
                } else {
                    // Start of code block
                    if (currentParagraph.length() > 0) {
                        elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                        currentParagraph = new StringBuilder();
                    }
                    inCodeBlock = true;
                }
                continue;
            }

            if (inCodeBlock) {
                codeBlockContent.append(line).append("\n");
                continue;
            }

            // Handle headers
            if (line.trim().startsWith("#")) {
                if (currentParagraph.length() > 0) {
                    elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                    currentParagraph = new StringBuilder();
                }

                int level = 0;
                while (level < line.length() && line.charAt(level) == '#') {
                    level++;
                }
                String headerText = line.substring(level).trim();
                elements.add(new MarkdownElement(MarkdownElementType.HEADER, headerText, level));
                continue;
            }

            // Handle unordered lists
            if (line.trim().matches("^[-*+]\\s+.+")) {
                if (currentParagraph.length() > 0) {
                    elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                    currentParagraph = new StringBuilder();
                }
                String listItem = line.trim().substring(2);
                elements.add(new MarkdownElement(MarkdownElementType.LIST_ITEM, listItem));
                continue;
            }

            // Handle ordered lists
            if (line.trim().matches("^\\d+\\.\\s+.+")) {
                if (currentParagraph.length() > 0) {
                    elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                    currentParagraph = new StringBuilder();
                }
                String listItem = line.trim().replaceFirst("^\\d+\\.\\s+", "");
                elements.add(new MarkdownElement(MarkdownElementType.ORDERED_LIST_ITEM, listItem));
                continue;
            }

            // Handle blockquotes
            if (line.trim().startsWith(">")) {
                if (currentParagraph.length() > 0) {
                    elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                    currentParagraph = new StringBuilder();
                }
                String quoteText = line.trim().substring(1).trim();
                elements.add(new MarkdownElement(MarkdownElementType.BLOCKQUOTE, quoteText));
                continue;
            }

            // Handle horizontal rules
            if (line.trim().matches("^(---|\\*\\*\\*|___)$")) {
                if (currentParagraph.length() > 0) {
                    elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                    currentParagraph = new StringBuilder();
                }
                elements.add(new MarkdownElement(MarkdownElementType.HORIZONTAL_RULE, ""));
                continue;
            }

            // Handle blank lines (paragraph breaks)
            if (line.trim().isEmpty()) {
                if (currentParagraph.length() > 0) {
                    elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
                    currentParagraph = new StringBuilder();
                }
                continue;
            }

            // Add to current paragraph
            if (currentParagraph.length() > 0) {
                currentParagraph.append(" ");
            }
            currentParagraph.append(line.trim());
        }

        // Add final paragraph if exists
        if (currentParagraph.length() > 0) {
            elements.add(new MarkdownElement(MarkdownElementType.PARAGRAPH, currentParagraph.toString()));
        }

        document.setElements(elements);
        logger.debug("Parsed {} markdown elements", elements.size());
        return document;
    }

    /**
     * Parses markdown text and converts it directly to HTML.
     *
     * @param markdown The markdown text to parse
     * @return HTML representation of the markdown
     * @throws IllegalArgumentException if markdown is null
     */
    public String parseToHTML(String markdown) {
        if (markdown == null) {
            throw new IllegalArgumentException("Markdown text cannot be null");
        }

        logger.debug("Converting markdown to HTML");
        StringBuilder html = new StringBuilder();
        MarkdownDocument document = parse(markdown);

        boolean inList = false;
        boolean inOrderedList = false;

        for (MarkdownElement element : document.getElements()) {
            switch (element.getType()) {
                case HEADER:
                    if (inList) {
                        html.append("</ul>\n");
                        inList = false;
                    }
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    html.append(String.format("<h%d>%s</h%d>\n",
                        element.getLevel(),
                        processInlineMarkdown(element.getContent()),
                        element.getLevel()));
                    break;

                case PARAGRAPH:
                    if (inList) {
                        html.append("</ul>\n");
                        inList = false;
                    }
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    html.append("<p>").append(processInlineMarkdown(element.getContent())).append("</p>\n");
                    break;

                case LIST_ITEM:
                    if (!inList) {
                        html.append("<ul>\n");
                        inList = true;
                    }
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    html.append("<li>").append(processInlineMarkdown(element.getContent())).append("</li>\n");
                    break;

                case ORDERED_LIST_ITEM:
                    if (!inOrderedList) {
                        html.append("<ol>\n");
                        inOrderedList = true;
                    }
                    if (inList) {
                        html.append("</ul>\n");
                        inList = false;
                    }
                    html.append("<li>").append(processInlineMarkdown(element.getContent())).append("</li>\n");
                    break;

                case BLOCKQUOTE:
                    if (inList) {
                        html.append("</ul>\n");
                        inList = false;
                    }
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    html.append("<blockquote>").append(processInlineMarkdown(element.getContent())).append("</blockquote>\n");
                    break;

                case CODE_BLOCK:
                    if (inList) {
                        html.append("</ul>\n");
                        inList = false;
                    }
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    html.append("<pre><code>").append(escapeHtml(element.getContent())).append("</code></pre>\n");
                    break;

                case HORIZONTAL_RULE:
                    if (inList) {
                        html.append("</ul>\n");
                        inList = false;
                    }
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    html.append("<hr/>\n");
                    break;
            }
        }

        // Close any open lists
        if (inList) {
            html.append("</ul>\n");
        }
        if (inOrderedList) {
            html.append("</ol>\n");
        }

        return html.toString();
    }

    /**
     * Processes inline markdown elements (bold, italic, links, code, etc.).
     *
     * @param text Text containing inline markdown
     * @return Text with inline markdown converted to HTML
     */
    private String processInlineMarkdown(String text) {
        String result = text;

        // Process in order: bold-italic, bold, italic to avoid conflicts
        result = BOLD_ITALIC_PATTERN.matcher(result).replaceAll("<strong><em>$1</em></strong>");
        result = BOLD_PATTERN.matcher(result).replaceAll("<strong>$1</strong>");
        result = ITALIC_PATTERN.matcher(result).replaceAll("<em>$1</em>");

        // Process links and images
        result = IMAGE_PATTERN.matcher(result).replaceAll("<img src=\"$2\" alt=\"$1\" />");
        result = LINK_PATTERN.matcher(result).replaceAll("<a href=\"$2\">$1</a>");

        // Process inline code
        result = INLINE_CODE_PATTERN.matcher(result).replaceAll("<code>$1</code>");

        return result;
    }

    /**
     * Escapes HTML special characters.
     *
     * @param text Text to escape
     * @return Escaped text
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Represents a parsed markdown document.
     */
    public static class MarkdownDocument {
        private List<MarkdownElement> elements = new ArrayList<>();

        public List<MarkdownElement> getElements() {
            return elements;
        }

        public void setElements(List<MarkdownElement> elements) {
            this.elements = elements;
        }

        public int getElementCount() {
            return elements.size();
        }
    }

    /**
     * Represents a single markdown element (header, paragraph, list item, etc.).
     */
    public static class MarkdownElement {
        private final MarkdownElementType type;
        private final String content;
        private final int level; // For headers (1-6)

        public MarkdownElement(MarkdownElementType type, String content) {
            this(type, content, 0);
        }

        public MarkdownElement(MarkdownElementType type, String content, int level) {
            this.type = type;
            this.content = content;
            this.level = level;
        }

        public MarkdownElementType getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Types of markdown elements.
     */
    public enum MarkdownElementType {
        HEADER,
        PARAGRAPH,
        LIST_ITEM,
        ORDERED_LIST_ITEM,
        BLOCKQUOTE,
        CODE_BLOCK,
        HORIZONTAL_RULE
    }
}
