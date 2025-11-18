package config;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for remote configuration access.
 *
 * <p>Extends {@link java.rmi.Remote} to allow remote clients to access
 * configuration settings from the server. Implemented by
 * {@link ConfigurationManager}.</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see ConfigurationManager
 * @since 1.0
 */
public interface ConfigurationManagerRemote extends Remote {
    Environment getCurrentEnvironment() throws RemoteException;
    DBConfig getDbConfig() throws RemoteException;
    LocalConfig getLocalConfig() throws RemoteException;
    UserConfig getUserConfig() throws RemoteException;
}
