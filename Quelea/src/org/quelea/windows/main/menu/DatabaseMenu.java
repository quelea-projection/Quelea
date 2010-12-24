package org.quelea.windows.main.menu;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The database menu.
 * @author Michael
 */
public class DatabaseMenu extends JMenu {

    private final JMenuItem newSong;
    private final JMenuItem exportSongs;

    /**
     * Create a new database menu.
     */
    public DatabaseMenu() {
        super("Database");
        newSong = new JMenuItem("New song...");
        newSong.setMnemonic(KeyEvent.VK_N);
        add(newSong);
        exportSongs = new JMenuItem("Export songs...");
        exportSongs.setMnemonic(KeyEvent.VK_E);
        exportSongs.setEnabled(false);
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
     * Get the "new song" menu item.
     * @return the "new song" menu item.
     */
    public JMenuItem getNewSong() {
        return newSong;
    }

}
