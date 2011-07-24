package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.windows.library.LibraryPanel;

/**
 *
 * @author Michael
 */
public class AddSongActionListener implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryPanel libraryPanel = Application.get().getMainWindow().getMainPanel().getLibraryPanel();
        SchedulePanel schedulePanel = Application.get().getMainWindow().getMainPanel().getSchedulePanel();
        Song song = (Song) libraryPanel.getLibrarySongPanel().getSongList().getSelectedValue();
        ((DefaultListModel) schedulePanel.getScheduleList().getModel()).addElement(song);
    }
    
}
