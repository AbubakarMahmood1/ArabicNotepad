package ui.components;

import dto.Book;
import common.RemoteBookFacade;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.function.Supplier;

/**
 * Toolbar panel for remote book operations.
 * Handles RemoteException appropriately for RMI calls.
 */
public class RemoteToolbarPanel extends BaseUIComponent {

    private final Book book;
    private final RemoteBookFacade remoteFacade;
    private final Supplier<String> getSelectedText;
    private final java.util.function.Consumer<String> replaceSelection;
    private final Component parentComponent;

    private JButton exportButton;
    private JButton transliterateButton;
    private JButton analyzeWordButton;

    public RemoteToolbarPanel(
        Book book,
        RemoteBookFacade remoteFacade,
        Supplier<String> getSelectedText,
        java.util.function.Consumer<String> replaceSelection,
        Component parentComponent
    ) {
        super();
        this.book = book;
        this.remoteFacade = remoteFacade;
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

    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    private void handleExport() {
        try {
            if (remoteFacade.isDatabaseConnected()) {
                remoteFacade.exportBook(book.getTitle());
            } else {
                remoteFacade.exportBook(book);
            }

            JOptionPane.showMessageDialog(
                parentComponent,
                "Book exported successfully!",
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            logger.info("Exported book '{}' (remote)", book.getTitle());

        } catch (RemoteException e) {
            logger.error("Remote error exporting book '{}'", book.getTitle(), e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error exporting book: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

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
            String transliterated = remoteFacade.transliterate(selectedText);

            if (transliterated != null && !transliterated.equals(selectedText)) {
                replaceSelection.accept(transliterated);
                logger.info("Transliterated selected text in book '{}' (remote)", book.getTitle());

                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Text transliterated successfully!",
                    "Transliteration Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (RemoteException e) {
            logger.error("Remote error transliterating text in book '{}'", book.getTitle(), e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error transliterating text: " + e.getMessage(),
                "Transliteration Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

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
            String analysisResult = remoteFacade.analyzeWord(selectedWord.trim());

            if (analysisResult != null) {
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

                logger.info("Analyzed word '{}' in book '{}' (remote)", selectedWord, book.getTitle());
            }

        } catch (RemoteException e) {
            logger.error("Remote error analyzing word '{}' in book '{}'", selectedWord, book.getTitle(), e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error analyzing word: " + e.getMessage(),
                "Analysis Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
