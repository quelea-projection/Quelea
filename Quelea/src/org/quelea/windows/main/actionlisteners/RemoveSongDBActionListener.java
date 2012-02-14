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
package org.quelea.windows.main.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.SortedListModel;
import org.quelea.displayable.Song;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.MainWindow;

/**
 * Action listener that removes the selected song from the database.
 * @author Michael
 */
public class RemoveSongDBActionListener implements ActionListener {
    
    /**
     * Remove the selected song from the database.
     * @param e the action event.
     */
    public void actionPerformed(ActionEvent e) {
        MainWindow mainWindow = Application.get().getMainWindow();
        LibrarySongList songList = mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        Song song = songList.getModel().getElementAt(songList.getSelectedIndex());
        if (song == null) {
            return;
        }
        int confirmResult = JOptionPane.showConfirmDialog(mainWindow, "Really remove \"" + song.getTitle() + "\" from the database? This action cannnot be undone.", "Confirm remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmResult == JOptionPane.NO_OPTION) {
            return;
        }
        if (!SongDatabase.get().removeSong(song)) {
            JOptionPane.showMessageDialog(mainWindow, "There was an error removing the song from the database.", "Error", JOptionPane.ERROR_MESSAGE, null);
        }
        song.setID(-1);
        ListModel<Song> model = songList.getModel();
        if(model instanceof SortedListModel) {
            ((SortedListModel<Song>)songList.getModel()).removeElement(song);
        }
        if(model instanceof DefaultListModel) {
            ((DefaultListModel<Song>)songList.getModel()).removeElement(song);
        }
        else {
            throw new RuntimeException("Couldn't remove song, list model is unknown type: " + model.getClass());
        }
    }
    
}
