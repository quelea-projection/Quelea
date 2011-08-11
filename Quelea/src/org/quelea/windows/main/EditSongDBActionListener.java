package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 *
 * @author Michael
 */
public class EditSongDBActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        SongEntryWindow songEntryWindow = Application.get().getMainWindow().getSongEntryWindow();
        songEntryWindow.setLocationRelativeTo(songEntryWindow.getOwner());
        songEntryWindow.resetEditSong(Application.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue());
        songEntryWindow.setVisible(true);
    }
}
