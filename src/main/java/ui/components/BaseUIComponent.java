package ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Abstract base class for UI components providing common functionality.
 * Implements the Template Method pattern for component lifecycle.
 */
public abstract class BaseUIComponent implements UIComponent {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected JPanel panel;
    protected boolean initialized = false;

    protected BaseUIComponent() {
        this.panel = new JPanel();
    }

    @Override
    public JComponent getComponent() {
        if (!initialized) {
            initialize();
        }
        return panel;
    }

    @Override
    public void initialize() {
        if (initialized) {
            logger.warn("Component already initialized: {}", getClass().getSimpleName());
            return;
        }

        logger.debug("Initializing component: {}", getClass().getSimpleName());
        setupLayout();
        buildUI();
        attachListeners();
        initialized = true;
    }

    @Override
    public void refresh() {
        if (!initialized) {
            logger.warn("Cannot refresh uninitialized component: {}", getClass().getSimpleName());
            return;
        }
        logger.debug("Refreshing component: {}", getClass().getSimpleName());
        updateUI();
    }

    /**
     * Sets up the layout manager for the panel.
     * Override to customize layout.
     */
    protected abstract void setupLayout();

    /**
     * Builds the UI components and adds them to the panel.
     * Override to define the component's structure.
     */
    protected abstract void buildUI();

    /**
     * Attaches event listeners to UI elements.
     * Override to add custom behavior.
     */
    protected void attachListeners() {
        // Default: no listeners
    }

    /**
     * Updates the UI when refresh() is called.
     * Override to define refresh behavior.
     */
    protected void updateUI() {
        // Default: no update logic
    }
}
