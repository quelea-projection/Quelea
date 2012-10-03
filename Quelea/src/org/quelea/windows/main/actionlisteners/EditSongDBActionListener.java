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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.quelea.Application;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * Called when the current song in the library should be edited.
 * @author Michael
 */
public class EditSongDBActionListener implements EventHandler<ActionEvent> {

    /**
     * Edit the currently selected song in the library.
     * @param e the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                SongEntryWindow songEntryWindow = Application.get().getMainWindow().getSongEntryWindow();
                songEntryWindow.resetEditSong(Application.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue());
                songEntryWindow.show();
                songEntryWindow.toFront();
            }
        });
        
    }
}
