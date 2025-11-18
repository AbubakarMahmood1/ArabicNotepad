package dao;

import config.DBConfig;
import dto.Book;
import dto.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of {@link BookDAO} for testing and development purposes.
 *
 * <p>This DAO stores all books and pages in memory using {@link HashMap} collections,
 * making it ideal for unit testing, integration testing, and development environments
 * where database setup is impractical or unnecessary.</p>
 *
 * <p><b>Key Characteristics:</b></p>
 * <ul>
 *   <li><b>Volatile Storage:</b> All data is lost when application terminates</li>
 *   <li><b>Fast Operations:</b> No I/O overhead, instant CRUD operations</li>
 *   <li><b>Thread-Unsafe:</b> Not suitable for concurrent access without synchronization</li>
 *   <li><b>No Persistence:</b> Data exists only in JVM heap memory</li>
 * </ul>
 *
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Unit testing business logic without database dependencies</li>
 *   <li>Integration testing with predictable data state</li>
 *   <li>Development and debugging without MySQL setup</li>
 *   <li>Proof-of-concept and prototyping</li>
 * </ul>
 *
 * <p><b>Data Structure:</b></p>
 * <pre>
 * books: Map&lt;Integer, Book&gt;    (bookId → Book object)
 * pages: Map&lt;Integer, Page&gt;    (pageId → Page object)
 * </pre>
 *
 * <p><b>ID Generation:</b><br>
 * Book and page IDs are generated using simple counters (bookIdCounter, pageIdCounter).
 * IDs start at 1 and increment sequentially. Not suitable for distributed systems.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // In test setup
 * BookDAO dao = new InMemoryBookDAO();
 *
 * // Add test books
 * Book book = new Book();
 * book.setTitle("Test Book");
 * book.setIdauthor("testuser");
 * dao.addBook(book, false);
 *
 * // Verify in test
 * List<Book> books = dao.getAllBooks(null);
 * assertEquals(1, books.size());
 *
 * // Clean up between tests
 * ((InMemoryBookDAO) dao).clear();
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is <b>NOT</b> thread-safe. For concurrent access,
 * wrap operations in synchronization blocks or use {@link java.util.concurrent.ConcurrentHashMap}.</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see BookDAO
 * @see MySQLBookDAO
 * @see LocalStorageBookDAO
 * @since 1.0
 */
public final class InMemoryBookDAO implements BookDAO {

    private final Map<Integer, Book> books = new HashMap<>();
    private final Map<Integer, Page> pages = new HashMap<>();
    private int bookIdCounter = 1;
    private int pageIdCounter = 1;

    @Override
    public boolean isDatabaseConnected() {
        return true;
    }

    @Override
    public boolean connect(DBConfig dbConfig) {
        return true;
    }

    
    @Override
    public List<Book> getAllBooks(String path) {
        return new ArrayList<>(books.values());
    }

    @Override
    public Book getBookByName(String title) {
    if (title == null) {
        return null;
    }
    return books.values().stream()
            .filter(book -> title.equalsIgnoreCase(book.getTitle()))
            .findFirst()
            .orElse(null);
}

    @Override
    public boolean addBook(Book book, boolean isDbDown) {
   if (isHashExists(book.getHash())) {
        return false;
    }
    
    book.setId(bookIdCounter++);
    books.put(book.getId(), book);
    if (book.getPages() != null) {
        for (Page page : book.getPages()) {
            page.setBookId(book.getId());
            addPage(book.getId(), page);
        }
    }
    return true;
}
    @Override
    public boolean updateBook(Book book) {
        if (books.containsKey(book.getId())) {
            books.put(book.getId(), book);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteBook(String title) {
    Book book = getBookByName(title);
    if (book != null) {
        books.remove(book.getId());
        deletePagesByBookTitle(book.getTitle());
        return true;
    }
    return false;
    }


    @Override
    public void deletePagesByBookTitle(String title) {
    Book book = getBookByName(title);
    if (book != null) {
        pages.entrySet().removeIf(entry -> entry.getValue().getBookId() == book.getId());
    }
}



    @Override
    public boolean isHashExists(String hash) {
        return books.values().stream().anyMatch(book -> book.getHash().equals(hash));
    }

    @Override
    public boolean addPage(int bookId, Page page) {
        page.setId(pageIdCounter++);
        pages.put(page.getId(), page);
        return true;
    }

    @Override
    public List<Page> getPagesByBookTitle(String title) {
    Book book = getBookByName(title);
    if (book == null) {
        return new ArrayList<>();
    }
    return pages.values().stream()
            .filter(page -> page.getBookId() == book.getId())
            .toList();
    }

    @Override
    public List<String> searchBooksByContent(String searchText) {
    List<String> results = new ArrayList<>();
    String searchTextLower = searchText.toLowerCase(); // Convert search text to lowercase
    
    for (Book book : books.values()) {
        if (book.getPages() == null) {
            continue;
        }
        for (Page page : book.getPages()) {
            if (page.getContent() != null && page.getContent().toLowerCase().contains(searchTextLower)) {
                results.add("Title: " + book.getTitle() + ", Page: " + page.getPageNumber() + ", Content: " + page.getContent());
            }
        }
    }
    return results;
    }
    /**
     * Clears all data from the in-memory storage.
     *
     * <p>This method removes all books and pages from the HashMaps and resets
     * the ID counters back to 1. Useful for cleaning up between test cases to
     * ensure test isolation.</p>
     *
     * <p><b>Use Case:</b> Call this in {@code @AfterEach} or {@code @Before} test methods
     * to reset the DAO to a clean state.</p>
     *
     * <p><b>Warning:</b> All data is permanently lost. This cannot be undone.</p>
     */
    public void clear() {
        books.clear();
        pages.clear();
        bookIdCounter = 1;
        pageIdCounter = 1;
    }
}
