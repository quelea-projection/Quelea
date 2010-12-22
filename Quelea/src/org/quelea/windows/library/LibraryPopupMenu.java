package org.quelea.windows.library;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * The popup menu that displays when someone right clicks on a song in the
 * library.
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
        addToSchedule = new JMenuItem("Add to schedule");
        editDB = new JMenuItem("Edit song");
        removeFromDB = new JMenuItem("Remove from database");

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
