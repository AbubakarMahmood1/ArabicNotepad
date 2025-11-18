package dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object representing a page within a book.
 *
 * <p>A page is a unit of content within a book, containing textual data that can
 * include Arabic or Latin script, markdown formatting, and various text elements.
 * Pages are ordered sequentially within a book and can be edited, searched, and
 * analyzed independently.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Unique identifier (id) for database persistence</li>
 *   <li>Book association (bookId) for relational integrity</li>
 *   <li>Page number for ordering and navigation</li>
 *   <li>Text content supporting Unicode (Arabic/Latin)</li>
 *   <li>Serializable for RMI and file-based operations</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Create a new page
 * Page page = new Page();
 * page.setBookId(1);
 * page.setPageNumber(1);
 * page.setContent("# Title\n\nمرحبا بكم في المفكرة العربية");
 *
 * // Add to book
 * bookFacade.addPageByBookTitle("My Book", page);
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 2.0
 * @since 1.0
 * @see Book
 * @see bl.BookFacade
 */
public class Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this page in the database.
     * Set by the database upon insertion.
     */
    private int id;

    /**
     * Foreign key reference to the parent book.
     * Links this page to its containing book.
     */
    private int bookId;

    /**
     * Sequential number of this page within its book.
     * Used for ordering and navigation (1-based indexing).
     */
    private int pageNumber;

    /**
     * The textual content of this page.
     * Supports Unicode characters including Arabic script,
     * and may contain markdown formatting.
     */
    private String content;

    /**
     * Default constructor.
     * Creates an empty page with no properties set.
     */
    public Page() {}

    /**
     * Full constructor with all properties.
     *
     * @param id Unique identifier
     * @param bookId Parent book ID
     * @param pageNumber Sequential page number
     * @param content Text content of the page
     */
    public Page(int id, int bookId, int pageNumber, String content) {
        this.id = id;
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.content = content;
    }

    /**
     * Gets the unique identifier of this page.
     *
     * @return the page ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this page.
     *
     * @param id the page ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the ID of the parent book.
     *
     * @return the book ID this page belongs to
     */
    public int getBookId() {
        return bookId;
    }

    /**
     * Sets the ID of the parent book.
     *
     * @param bookId the book ID to set
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets the sequential number of this page.
     *
     * @return the page number (1-based)
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Sets the sequential number of this page.
     *
     * @param pageNumber the page number to set (should be 1-based)
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Gets the textual content of this page.
     *
     * @return the page content, may be null or empty
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the textual content of this page.
     *
     * @param content the content to set (supports Unicode/Arabic)
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns a string representation of this page.
     * Includes ID, bookId, pageNumber, and content length.
     *
     * @return string representation of the page
     */
    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", contentLength=" + (content != null ? content.length() : 0) +
                '}';
    }

    /**
     * Compares this page to another object for equality.
     * Two pages are considered equal if they have the same ID, bookId, and pageNumber.
     *
     * @param o the object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page page = (Page) o;
        return id == page.id && bookId == page.bookId && pageNumber == page.pageNumber;
    }

    /**
     * Generates a hash code for this page.
     * Based on ID, bookId, and pageNumber fields.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, bookId, pageNumber);
    }
}
