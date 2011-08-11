package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;
import org.quelea.windows.library.LibrarySongList;

/**
 *
 * @author Michael
 */
public class RemoveSongDBActionListener implements ActionListener {
    
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
        songList.getModel().removeElement(song);
    }
    
}
