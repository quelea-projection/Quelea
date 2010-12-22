package org.quelea.windows.main.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The database menu.
 * @author Michael
 */
public class DatabaseMenu extends JMenu {

    private final JMenuItem newSong;
    private final JMenuItem qspImport;
    private final JMenuItem ssImport;
    private final JMenuItem exportSongs;

    /**
     * Create a new database menu.
     */
    public DatabaseMenu() {
        super("Database");
        newSong = new JMenuItem("New song...");
        add(newSong);
        JMenu importMenu = new JMenu("Import songs");
        qspImport = new JMenuItem("Quelea song pack...");
        qspImport.setEnabled(false);
        importMenu.add(qspImport);
        ssImport = new JMenuItem("Survivor songbook...");
        ssImport.setEnabled(false);
        importMenu.add(ssImport);
        add(importMenu);
        exportSongs = new JMenuItem("Export songs...");
        add(exportSongs);
    }

    /**
     * Get the "export songs" menu item.
     * @return the "export songs" menu item.
     */
    public JMenuItem getExportSongs() {
        return exportSongs;
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

    /**
     * Get the "new song" menu item.
     * @return the "new song" menu item.
     */
    public JMenuItem getNewSong() {
        return newSong;
    }

}
