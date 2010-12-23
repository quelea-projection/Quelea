
package org.quelea.windows.main.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The tools menu on the menu bar.
 * @author Michael
 */
public class ToolsMenu extends JMenu {

    private final JMenuItem options;
    private final JMenuItem qspImport;
    private final JMenuItem ssImport;

    /**
     * Create a new tools menu.
     */
    public ToolsMenu() {
        super("Tools");
        JMenu importMenu = new JMenu("Import songs");
        qspImport = new JMenuItem("Quelea song pack...");
        qspImport.setEnabled(false);
        importMenu.add(qspImport);
        ssImport = new JMenuItem("Survivor songbook...");
        ssImport.setEnabled(false);
        importMenu.add(ssImport);
        add(importMenu);
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

    /**
     * Get the import quelea song pack menu item.
     * @return the import quelea song pack menu item.
     */
    public JMenuItem getQSPImport() {
        return qspImport;
    }

    /**
     * Get the import survivor songbook menu item.
     * @return the import survivor songbook menu item.
     */
    public JMenuItem getSSImport() {
        return ssImport;
    }

}
