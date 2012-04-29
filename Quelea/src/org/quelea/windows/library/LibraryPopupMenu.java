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
package org.quelea.windows.library;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;
import org.quelea.print.Printer;
import org.quelea.utils.Utils;

/**
 * The popup menu that displays when someone right clicks on a song in the library.
 * @author Michael
 */
public class LibraryPopupMenu extends JPopupMenu {

    private final JMenuItem addToSchedule;
    private final JMenuItem editDB;
    private final JMenuItem removeFromDB;
    private final JMenuItem print;

    /**
     * Create and initialise the popup menu.
     */
    public LibraryPopupMenu() {
        addToSchedule = new JMenuItem(LabelGrabber.INSTANCE.getLabel("library.add.to.schedule.text"), Utils.getImageIcon("icons/add.png", 16, 16));
        addToSchedule.setMnemonic(KeyEvent.VK_A);
        editDB = new JMenuItem(LabelGrabber.INSTANCE.getLabel("library.edit.song.text"), Utils.getImageIcon("icons/edit.png", 16, 16));
        editDB.setMnemonic(KeyEvent.VK_E);
        removeFromDB = new JMenuItem(LabelGrabber.INSTANCE.getLabel("library.remove.song.text"), Utils.getImageIcon("icons/removedb.png", 16, 16));
        removeFromDB.setMnemonic(KeyEvent.VK_R);
        print = new JMenuItem(LabelGrabber.INSTANCE.getLabel("library.print.song.text"), Utils.getImageIcon("icons/fileprint.png", 16, 16));
        print.setMnemonic(KeyEvent.VK_P);

        print.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Song song = Application.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue();
                if (song != null) {
                    int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("print.chords.question"), LabelGrabber.INSTANCE.getLabel("printing.options.text"), JOptionPane.YES_NO_OPTION);
                    song.setPrintChords(result == JOptionPane.YES_OPTION);
                    Printer.getInstance().print(song);
                }
            }
        });

        add(addToSchedule);
        add(editDB);
        add(removeFromDB);
        add(print);
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
