package org.quelea.windows.main.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The file menu on the menu bar.
 * @author Michael
 */
public class FileMenu extends JMenu {

    private final JMenuItem newSchedule;
    private final JMenuItem openSchedule;
    private final JMenuItem saveSchedule;
    private final JMenuItem saveScheduleAs;

    /**
     * Create a new file menu.
     */
    public FileMenu() {
        super("File");
        newSchedule = new JMenuItem("New");
        add(newSchedule);
        openSchedule = new JMenuItem("Open");
        add(openSchedule);
        saveSchedule = new JMenuItem("Save");
        add(saveSchedule);
        saveScheduleAs = new JMenuItem("Save as...");
        add(saveScheduleAs);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(exit);
    }

    /**
     * Get the new schedule menu item.
     * @return the new schedule menu item.
     */
    public JMenuItem getNewSchedule() {
        return newSchedule;
    }

    /**
     * Get the open schedule menu item.
     * @return the open schedule menu item.
     */
    public JMenuItem getOpenSchedule() {
        return openSchedule;
    }

    /**
     * Get the save schedule menu item.
     * @return the save schedule menu item.
     */
    public JMenuItem getSaveSchedule() {
        return saveSchedule;
    }

    /**
     * Get the save as schedule menu item.
     * @return the save as schedule menu item.
     */
    public JMenuItem getSaveScheduleAs() {
        return saveScheduleAs;
    }

}
