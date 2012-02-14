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
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.windows.library.LibraryPanel;
import org.quelea.windows.main.SchedulePanel;

/**
 * The action listener for adding a song, called when something fires off an 
 * action that adds a song from the library to the schedule.
 * @author Michael
 */
public class AddSongActionListener implements ActionListener {

    /**
     * Get the current selected song from the library to the schedule.
     * @param e the event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryPanel libraryPanel = Application.get().getMainWindow().getMainPanel().getLibraryPanel();
        SchedulePanel schedulePanel = Application.get().getMainWindow().getMainPanel().getSchedulePanel();
        Song song = libraryPanel.getLibrarySongPanel().getSongList().getSelectedValue();
        schedulePanel.getScheduleList().getModel().addElement(song);
    }
}
