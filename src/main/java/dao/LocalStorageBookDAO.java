package dao;

import dto.Book;
import dto.Page;
import config.ConfigurationManager;
import config.LocalConfig;
import config.DBConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;
import util.PathSecurityUtil;

/**
 * File-based implementation of {@link BookDAO} for offline and backup storage.
 *
 * <p>This DAO persists books as text files (.txt) or markdown files (.md) in the local
 * file system. It serves as both a fallback mechanism when the database is unavailable
 * and an import/export tool for book data.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>Offline Capability:</b> Works without database connection</li>
 *   <li><b>Human-Readable Format:</b> Books stored as plain text/markdown</li>
 *   <li><b>Security:</b> Validates file paths to prevent traversal attacks</li>
 *   <li><b>Auto-Pagination:</b> Splits content into pages (20 lines per page)</li>
 *   <li><b>Metadata Preservation:</b> Stores author ID in first line</li>
 * </ul>
 *
 * <p><b>File Format:</b></p>
 * <pre>
 * **idauthor**: user123
 * First page content here
 * ...up to 20 lines...
 *
 * Second page content here
 * ...up to 20 lines...
 * </pre>
 *
 * <p><b>Storage Locations:</b></p>
 * <ul>
 *   <li><b>Normal Mode:</b> Uses {@code LocalConfig.getStoragePath()} from configuration</li>
 *   <li><b>Database Down:</b> Uses {@code LocalConfig.getCurrentPath()} as primary, falls back to storage path</li>
 * </ul>
 *
 * <p><b>Security Measures:</b></p>
 * <ul>
 *   <li>Validates book titles using {@link PathSecurityUtil#validateBookTitle(String)}</li>
 *   <li>Creates files using {@link PathSecurityUtil#createSafeFile(File, String, String)}</li>
 *   <li>Prevents path traversal attacks (e.g., "../../../etc/passwd")</li>
 *   <li>Rejects malicious filenames (null bytes, control characters)</li>
 * </ul>
 *
 * <p><b>Pagination Algorithm:</b><br>
 * Content is automatically split into pages of {@value #MAX_LINES_PER_PAGE} lines each.
 * Empty lines are preserved for formatting but don't count toward the line limit.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * BookDAO localDAO = new LocalStorageBookDAO();
 *
 * // Import books from directory
 * List<Book> books = localDAO.getAllBooks("/path/to/books/");
 *
 * // Export a book to local storage
 * Book book = createMyBook();
 * localDAO.addBook(book, true); // true = database is down
 *
 * // Read single book
 * Book book = localDAO.getBookByName("/path/to/books/MyBook.txt");
 * }</pre>
 *
 * <p><b>Limitations:</b></p>
 * <ul>
 *   <li>Search operations not implemented (returns null)</li>
 *   <li>Page operations not implemented (individual page add/delete)</li>
 *   <li>Hash duplicate detection not implemented (always returns false)</li>
 * </ul>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see BookDAO
 * @see MySQLBookDAO
 * @see InMemoryBookDAO
 * @see PathSecurityUtil
 * @since 1.0
 */
public class LocalStorageBookDAO implements BookDAO {

    private static final int MAX_LINES_PER_PAGE = 20;
    private static final Logger logger = LoggerFactory.getLogger(LocalStorageBookDAO.class);

    private LocalConfig localConfig;

    /**
     * Constructs a new LocalStorageBookDAO with configuration from the ConfigurationManager.
     *
     * <p>Loads the {@link LocalConfig} to determine storage paths for book files.</p>
     */
    public LocalStorageBookDAO() {

        ConfigurationManager configManager = ConfigurationManager.getInstance();
        try {
            this.localConfig = configManager.getLocalConfig();
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(LocalStorageBookDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<Book> getAllBooks(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        List<Book> books = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Book book = new Book();
                    List<Page> pages = new ArrayList<>();
                    String firstLine;

                    try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
                        firstLine = reader.readLine();

                        if (isIdAuthor(firstLine)) {
                            book.setIdauthor(firstLine.substring("**idauthor**: ".length()).trim());
                        } else {
                            logger.info("First line does not indicate idauthor: {}", firstLine);
                            book.setIdauthor(null);
                        }

                        String line;
                        int pageNumber = 1;
                        StringBuilder contentBuilder = new StringBuilder();
                        int lineCount = 0;

                        while ((line = reader.readLine()) != null) {
                            if (!line.isEmpty()) {
                                contentBuilder.append(line).append("\n");
                                lineCount++;

                                if (lineCount >= MAX_LINES_PER_PAGE) {
                                    Page page = new Page();
                                    page.setPageNumber(pageNumber++);
                                    page.setContent(contentBuilder.toString().trim());
                                    pages.add(page);
                                    contentBuilder.setLength(0);
                                    lineCount = 0;
                                }
                            } else {
                                if (contentBuilder.length() > 0) {
                                    contentBuilder.append("\n");
                                }
                            }
                        }

                        if (contentBuilder.length() > 0) {
                            Page page = new Page();
                            page.setPageNumber(pageNumber++);
                            page.setContent(contentBuilder.toString().trim());
                            pages.add(page);
                        }

                        book.setPages(pages);
                    } catch (IOException e) {
                        logger.error("Error reading book from file: {}", file.getAbsolutePath(), e);
                        continue;
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid date format in file: {}", file.getAbsolutePath(), e);
                        continue;
                    }

                    book.setTitle(file.getName().replace(".txt", ""));
                    book.setHash(FileUtil.calculateSHA256(getAllPagesContent(pages)));
                    books.add(book);
                }
            }
        } else {
            logger.warn("No files found in the directory: {}", path);
        }

        return books;
    }

    @Override
    public Book getBookByName(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            logger.warn("File does not exist or is not a file: {}", path);
            return null;
        }

        Book book = new Book();
        List<Page> pages = new ArrayList<>();
        StringBuilder contentBuilder = new StringBuilder();
        String firstLine;

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            firstLine = reader.readLine();

            if (isIdAuthor(firstLine)) {
                book.setIdauthor(firstLine.substring("**idauthor**: ".length()).trim());
            } else {
                logger.info("First line does not indicate idauthor: {}", firstLine);
                book.setIdauthor(null);
            }

            String line;
            int pageNumber = 1;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    contentBuilder.append(line).append(System.lineSeparator());
                    lineCount++;

                    if (lineCount >= MAX_LINES_PER_PAGE) {
                        Page page = new Page();
                        page.setPageNumber(pageNumber++);
                        page.setContent(contentBuilder.toString().trim());
                        pages.add(page);
                        contentBuilder.setLength(0);
                        lineCount = 0;
                    }
                } else {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append(System.lineSeparator());
                    }
                }
            }

            if (contentBuilder.length() > 0) {
                Page page = new Page();
                page.setPageNumber(pageNumber++);
                page.setContent(contentBuilder.toString().trim());
                pages.add(page);
            }

            book.setPages(pages);
        } catch (IOException e) {
            logger.error("Error reading book from file: {}", path, e);
            return null;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid date format in file: {}", path, e);
            return null;
        }

        book.setTitle(file.getName().replace(".txt", ""));
        book.setHash(FileUtil.calculateSHA256(getAllPagesContent(pages)));

        return book;
    }

    @Override
    public boolean addBook(Book book, boolean isDbDown) {
        if (book == null || book.getTitle() == null || book.getIdauthor() == null || book.getIdauthor().isEmpty()) {
            logger.warn("Invalid book details provided for storage.");
            return false;
        }

        // Validate and sanitize book title for security
        try {
            PathSecurityUtil.validateBookTitle(book.getTitle());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid book title for file storage: {}", book.getTitle(), e);
            return false;
        }

        String storagePath;
        if (isDbDown) {
            storagePath = localConfig.getCurrentPath();
            if (storagePath == null || storagePath.isEmpty()) {
                storagePath = localConfig.getStoragePath();
            }
        } else {
            storagePath = localConfig.getStoragePath();
        }

        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Use secure file creation to prevent path traversal
        File bookFile;
        try {
            bookFile = PathSecurityUtil.createSafeFile(directory, book.getTitle(), "md");
        } catch (SecurityException | IllegalArgumentException e) {
            logger.error("Security violation when creating file for book: {}", book.getTitle(), e);
            return false;
        }

        try (FileWriter writer = new FileWriter(bookFile, false)) {
            writer.write("**idauthor**: " + book.getIdauthor() + "\n");

            for (Page page : book.getPages()) {
                writer.write(page.getContent() + "\n\n");
            }
        } catch (IOException e) {
            logger.error("Error writing book to local storage: {}", book.getTitle(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateBook(Book book) {
        // Validate and sanitize book title for security
        try {
            PathSecurityUtil.validateBookTitle(book.getTitle());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid book title for file update: {}", book.getTitle(), e);
            return false;
        }

        File directory = new File(localConfig.getStoragePath());

        // Use secure file creation to prevent path traversal
        File bookFile;
        try {
            bookFile = PathSecurityUtil.createSafeFile(directory, book.getTitle(), "md");
        } catch (SecurityException | IllegalArgumentException e) {
            logger.error("Security violation when accessing file for book: {}", book.getTitle(), e);
            return false;
        }

        if (bookFile.exists()) {
            try (FileWriter writer = new FileWriter(bookFile, false)) {
                writer.write("**idauthor**: " + book.getIdauthor() + "\n");

                for (Page page : book.getPages()) {
                    writer.write(page.getContent() + "\n\n");
                }

                logger.info("Successfully updated book in local storage: {}", book.getTitle());
                return true;
            } catch (IOException e) {
                logger.error("Error updating book in local storage: {}", book.getTitle(), e);
                return false;
            }
        } else {
            logger.warn("Book file does not exist. Attempting to add book: {}", book.getTitle());
            boolean added = addBook(book, true);
            if (added) {
                logger.info("Book added successfully as it did not exist: {}", book.getTitle());
            } else {
                logger.error("Failed to add book as it did not exist: {}", book.getTitle());
            }
            return added;
        }
    }

    @Override
    public boolean deleteBook(String path) {
        File bookFile = new File(path);
        if (bookFile.exists()) {
            if (bookFile.delete()) {
                logger.info("Book deleted successfully at path: {}", path);
                return true;
            } else {
                logger.error("Failed to delete book at path: {}", path);
                return false;
            }
        } else {
            logger.warn("Book file does not exist for deletion at path: {}", path);
            return false;
        }
    }

    @Override
    public boolean isHashExists(String hash) {
        return false;
    }

    @Override
    public boolean connect(DBConfig dbConfig) {
        return true;
    }

    @Override
    public boolean isDatabaseConnected() {
        return false;
    }

    private boolean isIdAuthor(String line) {
        return line.startsWith("**idauthor**: ");
    }

    @Override
    public List<String> searchBooksByContent(String searchText) {
        return null;
    }

    @Override
    public boolean addPage(int bookId, Page page) {
        return false;
    }

    @Override
    public List<Page> getPagesByBookTitle(String title) {
        return null;
    }

    @Override
    public void deletePagesByBookTitle(String title) {
        
    }

    private String getAllPagesContent(List<Page> pages) {
        StringBuilder sb = new StringBuilder();
        for (Page page : pages) {
            sb.append(page.getContent()).append("\n");
        }
        return sb.toString();
    }
}
