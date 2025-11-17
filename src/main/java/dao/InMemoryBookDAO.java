package dao;

import config.DBConfig;
import dto.Book;
import dto.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public void clear() {
        books.clear();
        pages.clear();
        bookIdCounter = 1;
        pageIdCounter = 1;
    }
}
