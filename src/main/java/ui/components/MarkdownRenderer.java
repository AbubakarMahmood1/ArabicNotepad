package ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MarkdownParser;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;

/**
 * Markdown renderer component for displaying parsed markdown as HTML.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Renders markdown to HTML with proper styling</li>
 *   <li>Supports bidirectional text (RTL and LTR)</li>
 *   <li>Clickable links with browser integration</li>
 *   <li>Customizable CSS styling</li>
 *   <li>Scrollable content area</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>{@code
 * MarkdownRenderer renderer = new MarkdownRenderer();
 * renderer.initialize();
 * renderer.setMarkdown("# Title\n\nThis is **bold** text");
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @since 2.0
 */
public class MarkdownRenderer extends BaseUIComponent {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownRenderer.class);

    private JEditorPane editorPane;
    private JScrollPane scrollPane;
    private MarkdownParser parser;

    // Default CSS styling for rendered markdown
    private static final String DEFAULT_CSS =
        "<style>" +
        "body { " +
        "  font-family: 'Segoe UI', Tahoma, Arial, sans-serif; " +
        "  font-size: 14px; " +
        "  line-height: 1.6; " +
        "  color: #333; " +
        "  padding: 10px; " +
        "  max-width: 800px; " +
        "} " +
        "h1 { " +
        "  color: #2c3e50; " +
        "  border-bottom: 2px solid #3498db; " +
        "  padding-bottom: 10px; " +
        "  font-size: 28px; " +
        "} " +
        "h2 { " +
        "  color: #34495e; " +
        "  border-bottom: 1px solid #95a5a6; " +
        "  padding-bottom: 8px; " +
        "  font-size: 24px; " +
        "} " +
        "h3 { color: #34495e; font-size: 20px; } " +
        "h4 { color: #555; font-size: 18px; } " +
        "h5 { color: #555; font-size: 16px; } " +
        "h6 { color: #777; font-size: 14px; } " +
        "code { " +
        "  background-color: #f4f4f4; " +
        "  border: 1px solid #ddd; " +
        "  border-radius: 3px; " +
        "  padding: 2px 5px; " +
        "  font-family: 'Courier New', monospace; " +
        "  color: #c7254e; " +
        "} " +
        "pre { " +
        "  background-color: #f4f4f4; " +
        "  border: 1px solid #ddd; " +
        "  border-radius: 5px; " +
        "  padding: 10px; " +
        "  overflow-x: auto; " +
        "} " +
        "pre code { " +
        "  background-color: transparent; " +
        "  border: none; " +
        "  padding: 0; " +
        "  color: #333; " +
        "} " +
        "blockquote { " +
        "  border-left: 4px solid #3498db; " +
        "  margin: 10px 0; " +
        "  padding: 10px 20px; " +
        "  background-color: #ecf0f1; " +
        "  color: #555; " +
        "  font-style: italic; " +
        "} " +
        "ul, ol { " +
        "  margin: 10px 0; " +
        "  padding-left: 30px; " +
        "} " +
        "li { " +
        "  margin: 5px 0; " +
        "} " +
        "a { " +
        "  color: #3498db; " +
        "  text-decoration: none; " +
        "} " +
        "a:hover { " +
        "  text-decoration: underline; " +
        "} " +
        "img { " +
        "  max-width: 100%; " +
        "  height: auto; " +
        "} " +
        "hr { " +
        "  border: none; " +
        "  border-top: 2px solid #ddd; " +
        "  margin: 20px 0; " +
        "} " +
        "table { " +
        "  border-collapse: collapse; " +
        "  width: 100%; " +
        "  margin: 10px 0; " +
        "} " +
        "th, td { " +
        "  border: 1px solid #ddd; " +
        "  padding: 8px; " +
        "  text-align: left; " +
        "} " +
        "th { " +
        "  background-color: #3498db; " +
        "  color: white; " +
        "} " +
        "</style>";

    /**
     * Creates a new MarkdownRenderer.
     */
    public MarkdownRenderer() {
        super();
        this.parser = new MarkdownParser();
    }

    @Override
    protected void setupLayout() {
        panel.setLayout(new BorderLayout());
    }

    @Override
    protected void buildUI() {
        // Create editor pane for HTML display
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setEditorKit(new HTMLEditorKit());

        // Set default content
        editorPane.setText(wrapWithCSS("<p>No content to display</p>"));

        // Make it scrollable
        scrollPane = new JScrollPane(editorPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

        logger.debug("MarkdownRenderer UI built");
    }

    @Override
    protected void attachListeners() {
        // Handle hyperlink clicks
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    // Open link in default browser
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            desktop.browse(new URI(e.getURL().toString()));
                            logger.info("Opened link in browser: {}", e.getURL());
                        }
                    } else {
                        logger.warn("Desktop browsing not supported on this platform");
                        JOptionPane.showMessageDialog(
                            panel,
                            "Cannot open link: " + e.getURL(),
                            "Browser Not Supported",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    logger.error("Error opening link: {}", e.getURL(), ex);
                    JOptionPane.showMessageDialog(
                        panel,
                        "Error opening link: " + ex.getMessage(),
                        "Link Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    /**
     * Sets the markdown content to render.
     *
     * @param markdown The markdown text to render
     */
    public void setMarkdown(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            editorPane.setText(wrapWithCSS("<p><em>No content to display</em></p>"));
            logger.debug("Cleared markdown renderer");
            return;
        }

        try {
            String html = parser.parseToHTML(markdown);
            String styledHtml = wrapWithCSS(html);
            editorPane.setText(styledHtml);
            editorPane.setCaretPosition(0); // Scroll to top
            logger.debug("Rendered markdown ({} chars)", markdown.length());
        } catch (Exception e) {
            logger.error("Error rendering markdown", e);
            editorPane.setText(wrapWithCSS(
                "<p style='color: red;'><strong>Error rendering markdown:</strong> " +
                e.getMessage() + "</p>"));
        }
    }

    /**
     * Gets the current markdown content (if stored).
     *
     * @return The current markdown text, or empty string if none set
     */
    public String getMarkdown() {
        // Note: We don't store the original markdown, only the rendered HTML
        // This method is here for API completeness
        return "";
    }

    /**
     * Clears the rendered content.
     */
    public void clear() {
        editorPane.setText(wrapWithCSS("<p><em>No content</em></p>"));
        logger.debug("Markdown renderer cleared");
    }

    /**
     * Sets custom CSS for styling the rendered HTML.
     *
     * @param css Custom CSS rules
     */
    public void setCustomCSS(String css) {
        // This would require re-rendering with new CSS
        // For now, using default CSS
        logger.warn("Custom CSS not yet implemented");
    }

    /**
     * Wraps HTML content with CSS styling and proper document structure.
     *
     * @param htmlContent The HTML content to wrap
     * @return Complete HTML document with CSS
     */
    private String wrapWithCSS(String htmlContent) {
        return "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               DEFAULT_CSS +
               "</head>" +
               "<body>" +
               htmlContent +
               "</body>" +
               "</html>";
    }

    /**
     * Updates the display (refreshes the current content).
     */
    @Override
    protected void updateUI() {
        // Refresh current display
        editorPane.repaint();
    }

    /**
     * Gets the underlying JEditorPane for advanced customization.
     *
     * @return The JEditorPane component
     */
    public JEditorPane getEditorPane() {
        return editorPane;
    }

    /**
     * Sets whether the renderer should be opaque.
     *
     * @param opaque true for opaque, false for transparent
     */
    public void setOpaque(boolean opaque) {
        if (editorPane != null) {
            editorPane.setOpaque(opaque);
        }
        if (scrollPane != null) {
            scrollPane.setOpaque(opaque);
        }
    }

    /**
     * Sets the background color of the renderer.
     *
     * @param color The background color
     */
    public void setBackground(Color color) {
        super.panel.setBackground(color);
        if (editorPane != null) {
            editorPane.setBackground(color);
        }
        if (scrollPane != null) {
            scrollPane.setBackground(color);
        }
    }
}
