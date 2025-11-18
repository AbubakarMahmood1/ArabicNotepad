package dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object representing a book in the ArabicNotepad application.
 *
 * <p>A book is the primary entity in the system, containing metadata (title, author, hash)
 * and a collection of pages with textual content. Books can be persisted to various data
 * stores (database, local storage, in-memory) and support operations like search, import/export,
 * and text analysis.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Unique identifier (id) for database persistence</li>
 *   <li>Title for book identification and display</li>
 *   <li>Content hash for duplicate detection</li>
 *   <li>Author identifier for attribution</li>
 *   <li>Collection of pages containing the actual content</li>
 *   <li>Serializable for RMI and file-based persistence</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Create a new book
 * Book book = new Book();
 * book.setTitle("My Arabic Notes");
 * book.setIdauthor("author123");
 *
 * // Add pages
 * List<Page> pages = new ArrayList<>();
 * Page page1 = new Page();
 * page1.setContent("مرحبا بكم");
 * pages.add(page1);
 * book.setPages(pages);
 *
 * // Save via facade
 * bookFacade.insertBook(book);
 * }</pre>
 *
 * @author ArabicNotepad Team
 * @version 2.0
 * @since 1.0
 * @see Page
 * @see bl.BookFacade
 */
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this book in the database.
     * Set by the database upon insertion.
     */
    private int id;

    /**
     * The title of the book.
     * Used for identification, display, and search operations.
     */
    private String title;

    /**
     * Content hash for duplicate detection.
     * Calculated based on the book's content to identify duplicates.
     */
    private String hash;

    /**
     * Author identifier.
     * Links this book to an author entity or username.
     */
    private String idauthor;

    /**
     * Collection of pages containing the book's content.
     * Each page represents a section of text that can be edited independently.
     */
    private List<Page> pages;

    /**
     * Default constructor.
     * Creates an empty book with no properties set.
     */
    public Book() {}

    /**
     * Full constructor with all properties.
     *
     * @param id Unique identifier
     * @param title Book title
     * @param hash Content hash
     * @param idauthor Author identifier
     * @param pages List of pages
     */
    public Book(int id, String title, String hash, String idauthor, List<Page> pages) {
        this.id = id;
        this.title = title;
        this.hash = hash;
        this.idauthor = idauthor;
        this.pages = pages;
    }

    /**
     * Gets the unique identifier of this book.
     *
     * @return the book ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this book.
     *
     * @param id the book ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the title of this book.
     *
     * @return the book title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this book.
     *
     * @param title the book title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the content hash of this book.
     *
     * @return the content hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the content hash of this book.
     *
     * @param hash the content hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Gets the author identifier of this book.
     *
     * @return the author ID
     */
    public String getIdauthor() {
        return idauthor;
    }

    /**
     * Sets the author identifier of this book.
     *
     * @param idauthor the author ID to set
     */
    public void setIdauthor(String idauthor) {
        this.idauthor = idauthor;
    }

    /**
     * Gets the list of pages in this book.
     *
     * @return the list of pages, may be null if not loaded
     */
    public List<Page> getPages() {
        return pages;
    }

    /**
     * Sets the list of pages in this book.
     *
     * @param pages the list of pages to set
     */
    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    /**
     * Returns a string representation of this book.
     * Includes ID, title, hash, author, and page count.
     *
     * @return string representation of the book
     */
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", hash='" + hash + '\'' +
                ", idauthor='" + idauthor + '\'' +
                ", pages=" + (pages != null ? pages.size() : 0) +
                '}';
    }

    /**
     * Compares this book to another object for equality.
     * Two books are considered equal if they have the same ID and title.
     *
     * @param o the object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return id == book.id && Objects.equals(title, book.title);
    }

    /**
     * Generates a hash code for this book.
     * Based on ID and title fields.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
