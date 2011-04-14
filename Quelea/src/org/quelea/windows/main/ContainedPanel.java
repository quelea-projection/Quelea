package org.quelea.windows.main;

import javax.swing.JPanel;

/**
 * A panel that's contained within the live / preview panels.
 * @author Michael
 */
public abstract class ContainedPanel extends JPanel {

    /**
     * Focus on the panel.
     */
    public abstract void focus();

    /**
     * Clear the panel.
     */
    public abstract void clear();

}
