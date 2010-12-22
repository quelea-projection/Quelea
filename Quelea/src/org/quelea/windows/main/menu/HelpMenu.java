package org.quelea.windows.main.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The help menu.
 * @author Michael
 */
public class HelpMenu extends JMenu {

    private final JMenuItem about;

    /**
     * Create a new help menu
     */
    public HelpMenu() {
        super("Help");
        about = new JMenuItem("About...");
        add(about);
    }

    /**
     * Get the about menu item.
     * @return the about menu item.
     */
    public JMenuItem getAbout() {
        return about;
    }
}
