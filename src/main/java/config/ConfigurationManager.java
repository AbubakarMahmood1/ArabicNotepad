package config;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Singleton configuration manager for environment-aware application settings.
 *
 * <p>This class manages all configuration objects ({@link DBConfig}, {@link LocalConfig},
 * {@link UserConfig}) and provides centralized access to settings based on the current
 * environment (LOCAL or REMOTE). It extends {@link UnicastRemoteObject} to support
 * remote access via RMI.</p>
 *
 * <p><b>Design Patterns:</b></p>
 * <ul>
 *   <li><b>Singleton:</b> Only one instance per JVM, accessed via {@code getInstance()}</li>
 *   <li><b>Strategy Pattern:</b> Different configurations loaded based on environment</li>
 *   <li><b>Lazy Initialization:</b> Instance created on first access</li>
 * </ul>
 *
 * <p><b>Supported Environments:</b></p>
 * <ul>
 *   <li><b>LOCAL:</b> Standalone mode with local database/file storage</li>
 *   <li><b>REMOTE:</b> Client mode connecting to remote RMI server</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> All public methods are {@code synchronized} to ensure
 * thread-safe singleton initialization and environment switching.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Initialize once at application startup
 * Environment env = EnvironmentManager.getCurrentEnvironment();
 * ConfigurationManager configMgr = ConfigurationManager.getInstance(env);
 *
 * // Access configurations
 * DBConfig dbConfig = configMgr.getDbConfig();
 * String dbUrl = dbConfig.getProperty("url");
 *
 * UserConfig userConfig = configMgr.getUserConfig();
 * String userId = userConfig.getUserId();
 *
 * LocalConfig localConfig = configMgr.getLocalConfig();
 * String storagePath = localConfig.getStoragePath();
 *
 * // Later in application (no environment parameter needed)
 * ConfigurationManager configMgr2 = ConfigurationManager.getInstance();
 * }</pre>
 *
 * <p><b>Environment Switching:</b><br>
 * If {@code getInstance(Environment)} is called with a different environment than
 * the current one, configurations are reloaded for the new environment.</p>
 *
 * <p><b>RMI Support:</b><br>
 * Extends {@link UnicastRemoteObject} to allow remote clients to access configurations
 * via the {@link ConfigurationManagerRemote} interface.</p>
 *
 * @author ArabicNotepad Team
 * @version 1.0
 * @see DBConfig
 * @see LocalConfig
 * @see UserConfig
 * @see Environment
 * @since 1.0
 */
public class ConfigurationManager extends UnicastRemoteObject implements ConfigurationManagerRemote {
    private static ConfigurationManager instance;
    private DBConfig dbConfig;
    private LocalConfig localConfig;
    private UserConfig userConfig;
    private Environment currentEnvironment;

    /**
     * Private constructor for singleton pattern.
     *
     * @param env the environment to load configurations for
     * @throws RemoteException if RMI export fails
     */
    private ConfigurationManager(Environment env) throws RemoteException {
        loadConfigurations(env);
    }

    /**
     * Returns the singleton instance, creating it if necessary.
     *
     * <p>If the instance doesn't exist, creates a new one for the specified environment.
     * If the instance exists but has a different environment, reloads configurations
     * for the new environment.</p>
     *
     * <p><b>Thread Safety:</b> This method is synchronized to prevent race conditions
     * during initialization.</p>
     *
     * @param env the environment to configure for (LOCAL or REMOTE)
     * @return the singleton ConfigurationManager instance
     * @throws RemoteException if RMI export fails during initialization
     */
    public static synchronized ConfigurationManager getInstance(Environment env) throws RemoteException {
        if (instance == null) {
            instance = new ConfigurationManager(env);
        } else if (instance.currentEnvironment != env) {
            instance.loadConfigurations(env);
        }
        return instance;
    }

    /**
     * Returns the singleton instance without environment parameter.
     *
     * <p>This method should be called after {@link #getInstance(Environment)} has been
     * called at least once to initialize the singleton.</p>
     *
     * @return the singleton ConfigurationManager instance
     * @throws IllegalStateException if {@code getInstance(Environment)} hasn't been called yet
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigurationManager is not initialized. Call getInstance(Environment) first.");
        }
        return instance;
    }

    /**
     * Loads all configuration objects for the specified environment.
     *
     * <p>Creates new instances of {@link DBConfig}, {@link LocalConfig}, and
     * {@link UserConfig} with settings appropriate for the given environment.</p>
     *
     * @param env the environment to load configurations for
     */
    private void loadConfigurations(Environment env) {
        this.currentEnvironment = env;
        this.dbConfig = new DBConfig(env);
        this.localConfig = new LocalConfig(env);
        this.userConfig = new UserConfig(env);
    }

    @Override
    public Environment getCurrentEnvironment() throws RemoteException {
        return currentEnvironment;
    }

    @Override
    public DBConfig getDbConfig() throws RemoteException {
        return dbConfig;
    }

    @Override
    public LocalConfig getLocalConfig() throws RemoteException {
        return localConfig;
    }

    @Override
    public UserConfig getUserConfig() throws RemoteException {
        return userConfig;
    }
}
