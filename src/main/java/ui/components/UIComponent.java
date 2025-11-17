package ui.components;

import javax.swing.*;

/**
 * Base interface for UI components in the ArabicNotepad application.
 * All UI components should implement this interface to ensure consistent behavior.
 *
 * Design Pattern: Component Pattern
 * - Promotes separation of concerns
 * - Enables reusability across different UI contexts
 * - Simplifies testing and maintenance
 */
public interface UIComponent {

    /**
     * Gets the Swing component to be added to the parent container.
     *
     * @return The JPanel or other Swing component representing this UI component
     */
    JComponent getComponent();

    /**
     * Initializes the component with necessary setup.
     * Called once during component creation.
     */
    void initialize();

    /**
     * Updates the component's state based on current data.
     * Called when the underlying data changes.
     */
    void refresh();

    /**
     * Cleans up resources when the component is no longer needed.
     * Override if the component holds resources that need explicit cleanup.
     */
    default void dispose() {
        // Default: no cleanup needed
    }
}
