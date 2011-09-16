/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

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
