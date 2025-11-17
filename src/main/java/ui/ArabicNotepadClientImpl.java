package ui;

import java.io.Serializable;
import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArabicNotepadClientImpl implements ArabicNotepadClient, Serializable {
    private static final long serialVersionUID = -8213028181954831062L;
    private static final Logger logger = LoggerFactory.getLogger(ArabicNotepadClientImpl.class);

    @Override
    public void onRegisterClient(boolean result) throws RemoteException {
        if (result) {
            logger.info("Client registered successfully on the server");
        } else {
            logger.warn("Client registration failed");
        }
    }
}
 