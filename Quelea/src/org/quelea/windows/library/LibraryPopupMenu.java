package org.quelea.windows.library;

import java.awt.event.KeyEvent;
import javax.swing.*;
import org.quelea.utils.Utils;

/**
 * The popup menu that displays when someone right clicks on a song in the library.
 * @author Michael
 */
public class LibraryPopupMenu extends JPopupMenu {

    private final JMenuItem addToSchedule;
    private final JMenuItem editDB;
    private final JMenuItem removeFromDB;

    /**
     * Create and initialise the popup menu.
     */
    public LibraryPopupMenu() {
        addToSchedule = new JMenuItem("Add to schedule", Utils.getImageIcon("icons/add.png", 16, 16));
        addToSchedule.setMnemonic(KeyEvent.VK_A);
        editDB = new JMenuItem("Edit song", Utils.getImageIcon("icons/edit.png", 16, 16));
        editDB.setMnemonic(KeyEvent.VK_E);
        removeFromDB = new JMenuItem("Remove from database", Utils.getImageIcon("icons/remove.png", 16, 16));
        removeFromDB.setMnemonic(KeyEvent.VK_R);

        add(addToSchedule);
        add(editDB);
        add(removeFromDB);
    }

    /**
     * Get the add to schedule button in the popup menu.
     * @return the add to schedule button.
     */
    public JMenuItem getAddToScheduleButton() {
        return addToSchedule;
    }

    /**
     * Get the edit button in the popup menu.
     * @return the edit button.
     */
    public JMenuItem getEditDBButton() {
        return editDB;
    }

    /**
     * Get the remove from db button in the popup menu.
     * @return the remove from db button.
     */
    public JMenuItem getRemoveFromDBButton() {
        return removeFromDB;
    }

}
