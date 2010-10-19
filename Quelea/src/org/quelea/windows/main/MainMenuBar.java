package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * The main menu bar that's displayed at the top of the main Quelea window.
 * @author Michael
 */
public class MainMenuBar extends JMenuBar {

    /**
     * Create a new main menu bar and initialise all the components.
     */
    public MainMenuBar() {
        addFileMenu();
    }

    /**
     * Add the file menu to the menu bar.
     */
    private void addFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(exit);
        add(fileMenu);
    }

}
