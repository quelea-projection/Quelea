/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.actionhandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.library.LibraryPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.SchedulePanel;

/**
 * The action listener for adding a song, called when something fires off an
 * action that adds a song from the library to the schedule.
 * <p/>
 * @author Michael
 */
public class AddSongActionHandler implements EventHandler<ActionEvent> {
    
    private final boolean updateInDB;
    
    public AddSongActionHandler(boolean updateInDB) {
        this.updateInDB = updateInDB;
    }

    /**
     * Get the current selected song from the library to the schedule.
     * <p/>
     * @param t the event.
     */
    @Override
    public void handle(ActionEvent t) {
        LibraryPanel libraryPanel = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel();
        SchedulePanel schedulePanel = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel();
        SongDisplayable song = libraryPanel.getLibrarySongPanel().getSongList().getSelectedValue();
        if(QueleaProperties.get().getSongOverflow() || !updateInDB) {
            song = new SongDisplayable(song);
        }
        if(!updateInDB) {
            song.setID(-1);
            song.setNoDBUpdate();
        }
        cacheVidPreview(song);
        schedulePanel.getScheduleList().add(song);
        libraryPanel.getLibrarySongPanel().getSearchBox().clear();
    }

    private void cacheVidPreview(SongDisplayable song) {
        if(song != null && song.getSections()!=null && song.getSections().length > 0 && song.getSections()[0] != null && song.getSections()[0].getTheme() != null && song.getSections()[0].getTheme().getBackground() instanceof VideoBackground) {
            final VideoBackground background = (VideoBackground) song.getSections()[0].getTheme().getBackground();
            new Thread() {
                @Override
                public void run() {
                    Utils.getVidBlankImage(background.getVideoFile()); //cache it
                }
            }.start();
        }
    }
}
