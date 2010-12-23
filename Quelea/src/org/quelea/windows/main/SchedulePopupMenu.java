package org.quelea.windows.main;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * The popup menu that displays when a song in the schedule is right-clicked.
 * @author Michael
 */
public class SchedulePopupMenu extends JPopupMenu {

    private final JMenuItem editSong;

    /**
     * Create a new schedule popup menu
     */
    public SchedulePopupMenu() {
        editSong = new JMenuItem("Edit song");
        add(editSong);
    }

    /**
     * Get the edit song button.
     * @return the edit song button.
     */
    public JMenuItem getEditSongButton() {
        return editSong;
    }

}
