package org.quelea.windows.main;

import java.awt.event.KeyEvent;
import javax.swing.*;
import org.quelea.utils.Utils;

/**
 * The popup menu that displays when a song in the schedule is right-clicked.
 * @author Michael
 */
public class ScheduleSongPopupMenu extends JPopupMenu {

    private final JMenuItem editSong;

    /**
     * Create a new schedule popup menu
     */
    public ScheduleSongPopupMenu() {
        editSong = new JMenuItem("Edit song", Utils.getImageIcon("icons/edit.png", 16, 16));
        editSong.setMnemonic(KeyEvent.VK_E);
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
