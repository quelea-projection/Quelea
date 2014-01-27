/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * The action handler for adding a video.
 * <p/>
 * @author Michael
 */
public class QuickInsertActionHandler implements EventHandler<ActionEvent> {

    /**
     * Show the song displayable window and grab the resulting song entered,
     * entering it into the schedule.
     * <p/>
     * @param t the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        SongEntryWindow sew = QueleaApp.get().getMainWindow().getSongEntryWindow();
        sew.resetQuickInsert();
        sew.showAndWait();
        if(!sew.wasCancelled()) {
            SongDisplayable quickSong = sew.getSong();
            quickSong.setQuickInsert();
            if(!quickSong.getTitle().trim().isEmpty()) {
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(quickSong);
            }
        }
    }
}
