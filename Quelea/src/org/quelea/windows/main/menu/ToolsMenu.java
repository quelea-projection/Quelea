
package org.quelea.windows.main.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The tools menu on the menu bar.
 * @author Michael
 */
public class ToolsMenu extends JMenu {

    private final JMenuItem options;

    /**
     * Create a new tools menu.
     */
    public ToolsMenu() {
        super("Tools");
        options = new JMenuItem("Options");
        add(options);
    }

    /**
     * Get the options menu item.
     * @return the options menu item.
     */
    public JMenuItem getOptions() {
        return options;
    }

}
