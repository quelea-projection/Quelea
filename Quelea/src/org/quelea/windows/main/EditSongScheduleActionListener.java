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
package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * Called when the current song in the schedule should be edited.
 * @author Michael
 */
public class EditSongScheduleActionListener implements ActionListener {

    /**
     * Edit the currently selected song in the library.
     * @param e the action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SongEntryWindow songEntryWindow = Application.get().getMainWindow().getSongEntryWindow();
        songEntryWindow.setLocationRelativeTo(songEntryWindow.getOwner());
        songEntryWindow.resetEditSong((Song) Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSelectedValue());
        songEntryWindow.setVisible(true);
    }
}
