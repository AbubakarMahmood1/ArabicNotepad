package common;

import bl.BookFacade;
import java.rmi.server.UnicastRemoteObject;
import dto.Book;
import dto.Page;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.ArabicNotepadClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RemoteBookFacadeImpl extends UnicastRemoteObject implements RemoteBookFacade {

    private final BookFacade bookFacade;
    private List<ArabicNotepadClient> registeredClients = null;
    private static final Logger logger = LoggerFactory.getLogger(RemoteBookFacadeImpl.class);
    private final ExecutorService threadPool;
    private final int MAX_THREAD_POOL = 10;

    public RemoteBookFacadeImpl(BookFacade bookFacade) throws RemoteException {
        super();
        this.bookFacade = bookFacade;
        this.threadPool = Executors.newFixedThreadPool(MAX_THREAD_POOL);
    }

    @Override
public List<Book> getBookList(String filepath) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.getBookList(filepath)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in getBookList: {}", e.getMessage(), e);
        throw new RemoteException("Error while retrieving book list", e);
    }
}


    @Override
    public Book getBookByName(String value) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.getBookByName(value)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in getBookByName: {}", e.getMessage(), e);
        throw new RemoteException("Error while retrieving book by name", e);
    }
}

    @Override
    public void insertBook(Book book) throws RemoteException {
        threadPool.execute(() -> bookFacade.insertBook(book));
    }

    @Override
    public void updateBook(Book book) throws RemoteException {
        threadPool.execute(() -> bookFacade.updateBook(book));
    }

    @Override
    public void deleteBook(String title) throws RemoteException {
        threadPool.execute(() -> bookFacade.deleteBook(title));
    }

    @Override
    public void importBook(String path) throws RemoteException {
        threadPool.execute(() -> bookFacade.importBook(path));
    }

    @Override
public boolean exportBook(String title) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.exportBook(title)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while exporting book", e);
    }
}

@Override
    public boolean exportBook(Book book) throws RemoteException {
       try {
        return threadPool.submit(() -> bookFacade.exportBook(book)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while exporting book", e);
    }
    }

@Override
public String transliterate(String arabictext) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.transliterate(arabictext)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in transliterate: {}", e.getMessage(), e);
        throw new RemoteException("Error while transliterating text", e);
    }
}

@Override
public List<String> searchBooksByContent(String searchText) throws RemoteException {
    try {
        return threadPool.submit(() -> bookFacade.searchBooksByContent(searchText)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in searchBooksByContent: {}", e.getMessage(), e);
        throw new RemoteException("Error while searching books by content", e);
    }
}

@Override
public boolean isDatabaseConnected() throws RemoteException {
    try {
        return threadPool.submit(bookFacade::isDatabaseConnected).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in isDatabaseConnected: {}", e.getMessage(), e);
        throw new RemoteException("Error while checking database connection", e);
    }
}

    

    @Override
    public void addPageByBookTitle(String title, Page page) throws RemoteException {
        threadPool.execute(() -> bookFacade.addPageByBookTitle(title, page));
    }

    @Override
    public String performAnalysis(Book book, String analysisMethod) throws RemoteException  {
        try {
         return threadPool.submit(() -> bookFacade.performAnalysis(book, analysisMethod)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while analyzing the book", e);
    }
        
        
    }

    @Override
    public String analyzeWord(String selectedWord) throws RemoteException {
        try {
         return threadPool.submit(() -> bookFacade.analyzeWord(selectedWord)).get();
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error in exportBook: {}", e.getMessage(), e);
        throw new RemoteException("Error while analyzing the word", e);
    }  
    }

    @Override
    public void registerClient(ArabicNotepadClient client) throws RemoteException {
        threadPool.execute(() -> {
            if (registeredClients == null) {
                registeredClients = new ArrayList<>();
            }
            registeredClients.add(client);
            try {
                client.onRegisterClient(true);
            } catch (RemoteException e) {
                logger.error("Error registering client", e);
            }
        });
    }

    @Override
    public boolean ping() throws RemoteException {
        logger.info("Ping received from client");
        return true;
    }
    
    public void shutdownThreadPool() {
        threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
}

    @Override
    public boolean isHashExists(String hash) throws RemoteException {      
        try {
            return threadPool.submit(() -> bookFacade.isHashExists(hash)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in exportBook: {}", e.getMessage(), e);
            throw new RemoteException("Error while analyzing the word", e);
        }
    }

}