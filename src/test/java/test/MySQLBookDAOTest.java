package test;

import config.DBConfig;
import dao.MySQLBookDAO;
import dto.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MySQLBookDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private DBConfig mockDbConfig;

    private MySQLBookDAO bookDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        when(mockDbConfig.getProperty("url")).thenReturn("jdbc:mysql://localhost:3306/testdb");
        when(mockDbConfig.getProperty("username")).thenReturn("testuser");
        when(mockDbConfig.getProperty("password")).thenReturn("testpassword");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        bookDAO = new MySQLBookDAO(mockDbConfig);
        bookDAO.setConnection( mockConnection);
    }

    @Test
    void testIsDatabaseConnected_Connected() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean isConnected = bookDAO.isDatabaseConnected();

        assertTrue(isConnected);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testIsDatabaseConnected_NotConnected() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException());

        boolean isConnected = bookDAO.isDatabaseConnected();

        assertFalse(isConnected);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testAddBook_Success() throws SQLException {
        Book book = new Book();
        book.setTitle("Test Book");
        book.setHash("hash123");
        book.setIdauthor("1");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        boolean result = bookDAO.addBook(book, false);

        assertTrue(result);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testGetAllBooks() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("idbook")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("Book 1", "Book 2");
        when(mockResultSet.getString("hash")).thenReturn("hash1", "hash2");
        when(mockResultSet.getString("idauthor")).thenReturn("author1", "author2");

        List<Book> books = bookDAO.getAllBooks("");

        assertNotNull(books);
        assertEquals(2, books.size());
        assertEquals("Book 1", books.get(0).getTitle());
        assertEquals("Book 2", books.get(1).getTitle());
    }

    @Test
    void testGetBookByName_Found() throws SQLException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("idbook")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Book");
        when(mockResultSet.getString("hash")).thenReturn("hash123");
        when(mockResultSet.getString("idauthor")).thenReturn("1");

        Book book = bookDAO.getBookByName("Test Book");

        assertNotNull(book);
        assertEquals("Test Book", book.getTitle());
    }

    @Test
    void testGetBookByName_NotFound() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        Book book = bookDAO.getBookByName("Non-existent Book");

        assertNull(book);
    }

    @Test
    void testDeleteBook_Success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = bookDAO.deleteBook("Test Book");

        assertTrue(result);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testDeleteBook_NotFound() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = bookDAO.deleteBook("Non-existent Book");

        assertFalse(result);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testSearchBooksByContent_Found() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("Book 1");
        when(mockResultSet.getString("content")).thenReturn("This is a test content.");

        List<String> results = bookDAO.searchBooksByContent("test");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).contains("Book 1"));
    }

    @Test
    void testSearchBooksByContent_NotFound() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        List<String> results = bookDAO.searchBooksByContent("non-existent");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
