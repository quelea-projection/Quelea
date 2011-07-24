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
public class EditSongScheduleActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        SongEntryWindow songEntryWindow = Application.get().getMainWindow().getSongEntryWindow();
        songEntryWindow.setLocationRelativeTo(songEntryWindow.getOwner());
        songEntryWindow.resetEditSong((Song) Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSelectedValue());
        songEntryWindow.setVisible(true);
    }
}
