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
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;

/**
 * Action listener to remove a song from the schedule.
 * <p/>
 * @author Michael
 */
public class RemoveScheduleItemActionHandler implements EventHandler<ActionEvent> {

    /**
     * Remove the currently selected song from the schedule.
     * <p/>
     * @param t the action event (ignored.)
     */
    @Override
    public void handle(ActionEvent t) {
        ScheduleList scheduleList = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
        int selectedIndex = scheduleList.getSelectionModel().getSelectedIndex();
        scheduleList.removeCurrentItem();
        if(selectedIndex == scheduleList.itemsProperty().get().size()) {
            selectedIndex--;
        }
        if(selectedIndex >= 0) {
            scheduleList.getSelectionModel().select(selectedIndex);
        }
        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().updateScheduleDisplay();
//        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().updateCanvases(); //This stops a playing video when an item is deleted, which isn't what we want.
    }
}
