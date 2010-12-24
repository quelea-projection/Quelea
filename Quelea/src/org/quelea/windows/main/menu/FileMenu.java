package org.quelea.windows.main.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

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
        newSchedule.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK));
        newSchedule.setMnemonic(KeyEvent.VK_N);
        add(newSchedule);
        openSchedule = new JMenuItem("Open");
        openSchedule.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        openSchedule.setMnemonic(KeyEvent.VK_O);
        add(openSchedule);
        saveSchedule = new JMenuItem("Save");
        saveSchedule.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
        saveSchedule.setMnemonic(KeyEvent.VK_S);
        add(saveSchedule);
        saveScheduleAs = new JMenuItem("Save as...");
        saveScheduleAs.setMnemonic(KeyEvent.VK_A);
        add(saveScheduleAs);
        JMenuItem exit = new JMenuItem("Quit");
        exit.setMnemonic(KeyEvent.VK_Q);
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
