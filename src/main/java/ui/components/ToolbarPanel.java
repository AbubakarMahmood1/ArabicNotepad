package ui.components;

import dto.Book;
import bl.BookFacade;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Toolbar panel component providing book manipulation actions.
 *
 * Responsibilities:
 * - Export book functionality
 * - Transliterate selected text
 * - Analyze selected word
 * - Provide action buttons in a consistent layout
 */
public class ToolbarPanel extends BaseUIComponent {

    private final Book book;
    private final BookFacade bookFacade;
    private final Supplier<String> getSelectedText;
    private final java.util.function.Consumer<String> replaceSelection;
    private final Component parentComponent;

    private JButton exportButton;
    private JButton transliterateButton;
    private JButton analyzeWordButton;

    /**
     * Creates a toolbar panel.
     *
     * @param book The book being edited
     * @param bookFacade Facade for book operations
     * @param getSelectedText Supplier that returns currently selected text
     * @param replaceSelection Consumer that replaces selected text
     * @param parentComponent Parent component for dialogs
     */
    public ToolbarPanel(
        Book book,
        BookFacade bookFacade,
        Supplier<String> getSelectedText,
        java.util.function.Consumer<String> replaceSelection,
        Component parentComponent
    ) {
        super();
        this.book = book;
        this.bookFacade = bookFacade;
        this.getSelectedText = getSelectedText;
        this.replaceSelection = replaceSelection;
        this.parentComponent = parentComponent;
    }

    @Override
    protected void setupLayout() {
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    @Override
    protected void buildUI() {
        exportButton = createButton("Export", "Export book to file");
        transliterateButton = createButton("Transliterate", "Transliterate selected text");
        analyzeWordButton = createButton("Analyze Word", "Analyze selected word");

        panel.add(exportButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(transliterateButton);
        panel.add(analyzeWordButton);
    }

    @Override
    protected void attachListeners() {
        exportButton.addActionListener(e -> handleExport());
        transliterateButton.addActionListener(e -> handleTransliterate());
        analyzeWordButton.addActionListener(e -> handleAnalyzeWord());
    }

    /**
     * Creates a styled button.
     *
     * @param text Button text
     * @param tooltip Tooltip text
     * @return Configured JButton
     */
    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    /**
     * Handles book export action.
     */
    private void handleExport() {
        try {
            if (bookFacade.isDatabaseConnected()) {
                bookFacade.exportBook(book.getTitle());
            } else {
                bookFacade.exportBook(book);
            }

            JOptionPane.showMessageDialog(
                parentComponent,
                "Book exported successfully!",
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE
            );

            logger.info("Exported book '{}'", book.getTitle());

        } catch (Exception e) {
            logger.error("Error exporting book '{}'", book.getTitle(), e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error exporting book: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Handles transliterate action on selected text.
     */
    private void handleTransliterate() {
        String selectedText = getSelectedText.get();

        if (selectedText == null || selectedText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Please select text to transliterate.",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE
            );
            logger.warn("No text selected for transliteration in book '{}'", book.getTitle());
            return;
        }

        try {
            String transliterated = bookFacade.transliterate(selectedText);

            if (transliterated != null && !transliterated.equals(selectedText)) {
                replaceSelection.accept(transliterated);
                logger.info("Transliterated selected text in book '{}'", book.getTitle());

                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Text transliterated successfully!",
                    "Transliteration Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "No transliteration changes made.",
                    "Transliteration",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception e) {
            logger.error("Error transliterating text in book '{}'", book.getTitle(), e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error transliterating text: " + e.getMessage(),
                "Transliteration Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Handles word analysis on selected text.
     */
    private void handleAnalyzeWord() {
        String selectedWord = getSelectedText.get();

        if (selectedWord == null || selectedWord.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Please select a word to analyze.",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE
            );
            logger.warn("No word selected for analysis in book '{}'", book.getTitle());
            return;
        }

        try {
            String analysisResult = bookFacade.analyzeWord(selectedWord.trim());

            if (analysisResult != null) {
                // Create a scrollable text area for long analysis results
                JTextArea textArea = new JTextArea(analysisResult);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setRows(10);
                textArea.setColumns(40);

                JScrollPane scrollPane = new JScrollPane(textArea);

                JOptionPane.showMessageDialog(
                    parentComponent,
                    scrollPane,
                    "Word Analysis: " + selectedWord,
                    JOptionPane.INFORMATION_MESSAGE
                );

                logger.info("Analyzed word '{}' in book '{}'", selectedWord, book.getTitle());
            } else {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "No analysis available for: " + selectedWord,
                    "Analysis Result",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception e) {
            logger.error("Error analyzing word '{}' in book '{}'", selectedWord, book.getTitle(), e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error analyzing word: " + e.getMessage(),
                "Analysis Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
