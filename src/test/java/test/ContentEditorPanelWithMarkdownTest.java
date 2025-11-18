package test;

import bl.BookFacade;
import dto.Book;
import dto.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ui.components.ContentEditorPanelWithMarkdown;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ContentEditorPanelWithMarkdown component.
 * Tests edit/preview mode switching, markdown rendering, and all base functionality.
 */
class ContentEditorPanelWithMarkdownTest {

    @Mock
    private BookFacade mockFacade;

    private Book testBook;
    private ContentEditorPanelWithMarkdown editor;
    private boolean contentChangedCalled;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testBook = createTestBook("Test Book");
        contentChangedCalled = false;
        editor = new ContentEditorPanelWithMarkdown(testBook, mockFacade, this::onContentChanged);
    }

    private void onContentChanged() {
        contentChangedCalled = true;
    }

    private Book createTestBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setId(1);
        book.setIdauthor("Author");
        book.setPages(new ArrayList<>());
        return book;
    }

    private Page createTestPage(String content) {
        Page page = new Page();
        page.setContent(content);
        page.setPageNumber(1);
        page.setBookId(1);
        return page;
    }

    // ==================== Initialization Tests ====================

    @Test
    @DisplayName("Should initialize without errors")
    void testInitialization() {
        assertDoesNotThrow(() -> editor.initialize(),
            "Initialization should not throw exceptions");
    }

    @Test
    @DisplayName("Should return component after initialization")
    void testGetComponent() {
        editor.initialize();
        JPanel component = editor.getComponent();

        assertNotNull(component, "Component should not be null");
        assertTrue(component instanceof JPanel, "Should return JPanel");
    }

    @Test
    @DisplayName("Should start in edit mode")
    void testStartsInEditMode() {
        editor.initialize();

        assertFalse(editor.isPreviewMode(), "Should start in edit mode");
    }

    // ==================== Mode Switching Tests ====================

    @Test
    @DisplayName("Should switch to preview mode")
    void testSwitchToPreviewMode() {
        editor.initialize();
        editor.setText("# Test");

        editor.setPreviewMode(true);

        assertTrue(editor.isPreviewMode(), "Should be in preview mode");
    }

    @Test
    @DisplayName("Should switch back to edit mode")
    void testSwitchBackToEditMode() {
        editor.initialize();
        editor.setPreviewMode(true);

        editor.setPreviewMode(false);

        assertFalse(editor.isPreviewMode(), "Should be in edit mode");
    }

    @Test
    @DisplayName("Should handle multiple mode switches")
    void testMultipleModeSwitches() {
        editor.initialize();

        assertDoesNotThrow(() -> {
            editor.setPreviewMode(true);
            editor.setPreviewMode(false);
            editor.setPreviewMode(true);
            editor.setPreviewMode(false);
        }, "Should handle multiple mode switches");
    }

    @Test
    @DisplayName("Should toggle mode with same value")
    void testToggleSameValue() {
        editor.initialize();

        assertDoesNotThrow(() -> {
            editor.setPreviewMode(false); // Already false
            editor.setPreviewMode(false); // Still false
        }, "Should handle setting same mode value");
    }

    // ==================== Text Operations Tests ====================

    @Test
    @DisplayName("Should set and get text")
    void testSetAndGetText() {
        editor.initialize();
        String testText = "# Hello World\n\nThis is a test";

        editor.setText(testText);
        String retrieved = editor.getText();

        assertEquals(testText, retrieved, "Should retrieve the same text that was set");
    }

    @Test
    @DisplayName("Should handle empty text")
    void testEmptyText() {
        editor.initialize();

        editor.setText("");
        String retrieved = editor.getText();

        assertEquals("", retrieved, "Should handle empty text");
    }

    @Test
    @DisplayName("Should preserve text when switching modes")
    void testPreserveTextOnModeSwitch() {
        editor.initialize();
        String testText = "# Markdown Content";
        editor.setText(testText);

        editor.setPreviewMode(true);
        editor.setPreviewMode(false);

        assertEquals(testText, editor.getText(),
            "Text should be preserved when switching modes");
    }

    // ==================== Selection Tests ====================

    @Test
    @DisplayName("Should get selected text")
    void testGetSelectedText() {
        editor.initialize();
        editor.setText("Hello World");

        // Note: Can't actually select text in headless test, but should not throw
        assertDoesNotThrow(() -> editor.getSelectedText(),
            "Should not throw when getting selected text");
    }

    @Test
    @DisplayName("Should replace selection")
    void testReplaceSelection() {
        editor.initialize();
        editor.setText("Hello World");

        assertDoesNotThrow(() -> editor.replaceSelection("Replacement"),
            "Should not throw when replacing selection");
    }

    @Test
    @DisplayName("Should handle null selection replacement")
    void testNullSelectionReplacement() {
        editor.initialize();
        editor.setText("Hello World");

        assertDoesNotThrow(() -> editor.replaceSelection(null),
            "Should handle null replacement");
    }

    // ==================== Page Loading Tests ====================

    @Test
    @DisplayName("Should load page with content")
    void testLoadPageWithContent() {
        testBook.getPages().add(createTestPage("# Page Content"));
        editor.initialize();

        assertDoesNotThrow(() -> editor.loadPage(0),
            "Should load page without errors");
        assertEquals("# Page Content", editor.getText(),
            "Should load page content");
    }

    @Test
    @DisplayName("Should load empty page")
    void testLoadEmptyPage() {
        testBook.getPages().add(createTestPage(""));
        editor.initialize();

        assertDoesNotThrow(() -> editor.loadPage(0),
            "Should load empty page without errors");
    }

    @Test
    @DisplayName("Should handle loading non-existent page")
    void testLoadNonExistentPage() {
        editor.initialize();

        assertDoesNotThrow(() -> editor.loadPage(999),
            "Should handle loading non-existent page");
    }

    @Test
    @DisplayName("Should load multiple pages sequentially")
    void testLoadMultiplePages() {
        testBook.getPages().add(createTestPage("Page 1"));
        testBook.getPages().add(createTestPage("Page 2"));
        testBook.getPages().add(createTestPage("Page 3"));
        editor.initialize();

        editor.loadPage(0);
        assertEquals("Page 1", editor.getText());

        editor.loadPage(1);
        assertEquals("Page 2", editor.getText());

        editor.loadPage(2);
        assertEquals("Page 3", editor.getText());
    }

    // ==================== Markdown Preview Tests ====================

    @Test
    @DisplayName("Should render markdown in preview mode")
    void testMarkdownPreview() {
        editor.initialize();
        editor.setText("# Title\n\nThis is **bold**");

        assertDoesNotThrow(() -> editor.setPreviewMode(true),
            "Should render markdown in preview mode");
    }

    @Test
    @DisplayName("Should handle preview with empty content")
    void testPreviewEmptyContent() {
        editor.initialize();
        editor.setText("");

        assertDoesNotThrow(() -> editor.setPreviewMode(true),
            "Should handle preview with empty content");
    }

    @Test
    @DisplayName("Should handle preview with complex markdown")
    void testPreviewComplexMarkdown() {
        editor.initialize();
        String markdown = "# Title\n\n" +
                         "- Item 1\n" +
                         "- Item 2\n\n" +
                         "```\ncode\n```\n\n" +
                         "> Quote";
        editor.setText(markdown);

        assertDoesNotThrow(() -> editor.setPreviewMode(true),
            "Should handle preview with complex markdown");
    }

    @Test
    @DisplayName("Should handle preview with Arabic text")
    void testPreviewArabicText() {
        editor.initialize();
        editor.setText("# عنوان\n\nنص عربي");

        assertDoesNotThrow(() -> editor.setPreviewMode(true),
            "Should handle preview with Arabic text");
    }

    // ==================== Content Update Tests ====================

    @Test
    @DisplayName("Should trigger content change callback")
    void testContentChangeCallback() throws InterruptedException {
        when(mockFacade.updateBook(any(Book.class))).thenReturn(true);
        testBook.getPages().add(createTestPage("Initial"));
        editor.initialize();
        editor.loadPage(0);

        contentChangedCalled = false;
        editor.setText("Modified content");

        // Wait for debounce timer (2+ seconds)
        Thread.sleep(2500);

        assertTrue(contentChangedCalled, "Content change callback should be triggered");
    }

    @Test
    @DisplayName("Should update book after debounce delay")
    void testDebounceUpdate() throws InterruptedException {
        when(mockFacade.updateBook(any(Book.class))).thenReturn(true);
        testBook.getPages().add(createTestPage("Initial"));
        editor.initialize();
        editor.loadPage(0);

        editor.setText("Modified");

        // Wait for debounce
        Thread.sleep(2500);

        verify(mockFacade, atLeastOnce()).updateBook(testBook);
    }

    @Test
    @DisplayName("Should debounce multiple rapid changes")
    void testDebounceMultipleChanges() throws InterruptedException {
        when(mockFacade.updateBook(any(Book.class))).thenReturn(true);
        testBook.getPages().add(createTestPage("Initial"));
        editor.initialize();
        editor.loadPage(0);

        // Simulate rapid typing
        editor.setText("A");
        Thread.sleep(100);
        editor.setText("AB");
        Thread.sleep(100);
        editor.setText("ABC");

        // Wait for debounce
        Thread.sleep(2500);

        // Should only update once after final change
        verify(mockFacade, atMost(2)).updateBook(testBook);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle null book")
    void testNullBook() {
        assertThrows(NullPointerException.class,
            () -> new ContentEditorPanelWithMarkdown(null, mockFacade, this::onContentChanged),
            "Should throw exception for null book");
    }

    @Test
    @DisplayName("Should handle null facade")
    void testNullFacade() {
        assertThrows(NullPointerException.class,
            () -> new ContentEditorPanelWithMarkdown(testBook, null, this::onContentChanged),
            "Should throw exception for null facade");
    }

    @Test
    @DisplayName("Should handle null callback")
    void testNullCallback() {
        assertDoesNotThrow(
            () -> new ContentEditorPanelWithMarkdown(testBook, mockFacade, null),
            "Should handle null callback");
    }

    @Test
    @DisplayName("Should handle book with null pages")
    void testBookWithNullPages() {
        testBook.setPages(null);
        editor.initialize();

        assertDoesNotThrow(() -> editor.loadPage(0),
            "Should handle book with null pages");
    }

    @Test
    @DisplayName("Should handle very long text")
    void testVeryLongText() {
        editor.initialize();
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longText.append("Line ").append(i).append("\n");
        }

        assertDoesNotThrow(() -> editor.setText(longText.toString()),
            "Should handle very long text");
    }

    // ==================== Lifecycle Tests ====================

    @Test
    @DisplayName("Should dispose without errors")
    void testDispose() {
        editor.initialize();
        editor.setText("Content");

        assertDoesNotThrow(() -> editor.dispose(),
            "Should dispose without errors");
    }

    @Test
    @DisplayName("Should dispose before initialization")
    void testDisposeBeforeInit() {
        assertDoesNotThrow(() -> editor.dispose(),
            "Should handle dispose before initialization");
    }

    @Test
    @DisplayName("Should dispose in preview mode")
    void testDisposeInPreviewMode() {
        editor.initialize();
        editor.setPreviewMode(true);

        assertDoesNotThrow(() -> editor.dispose(),
            "Should dispose in preview mode without errors");
    }

    @Test
    @DisplayName("Should handle multiple dispose calls")
    void testMultipleDispose() {
        editor.initialize();

        assertDoesNotThrow(() -> {
            editor.dispose();
            editor.dispose();
        }, "Should handle multiple dispose calls");
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Should work with complete workflow")
    void testCompleteWorkflow() {
        testBook.getPages().add(createTestPage("# Initial"));
        when(mockFacade.updateBook(any(Book.class))).thenReturn(true);
        editor.initialize();

        // Load page
        editor.loadPage(0);
        assertEquals("# Initial", editor.getText());

        // Edit content
        editor.setText("# Modified\n\nNew content");

        // Switch to preview
        editor.setPreviewMode(true);
        assertTrue(editor.isPreviewMode());

        // Switch back to edit
        editor.setPreviewMode(false);
        assertFalse(editor.isPreviewMode());

        // Content should be preserved
        assertEquals("# Modified\n\nNew content", editor.getText());

        // Dispose
        assertDoesNotThrow(() -> editor.dispose());
    }

    @Test
    @DisplayName("Should handle rapid mode switching with content changes")
    void testRapidModeSwitchingWithChanges() {
        editor.initialize();

        assertDoesNotThrow(() -> {
            editor.setText("# Content 1");
            editor.setPreviewMode(true);
            editor.setPreviewMode(false);
            editor.setText("# Content 2");
            editor.setPreviewMode(true);
            editor.setPreviewMode(false);
            editor.setText("# Content 3");
        }, "Should handle rapid mode switching with changes");
    }
}
