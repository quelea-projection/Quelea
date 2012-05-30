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
import org.quelea.windows.main.ScheduleList;

/**
 * Action listener to remove an item from the schedule.
 * @author Michael
 */
public class RemoveScheduleItemActionListener implements ActionListener {

    /**
     * Remove the currently selected item from the schedule.
     * @param e the action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        ScheduleList scheduleList = Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
        int selectedIndex = scheduleList.getSelectedIndex();
        scheduleList.removeCurrentItem();
        if (selectedIndex == scheduleList.getModel().getSize()) {
            selectedIndex--;
        }
        if (selectedIndex >= 0) {
            scheduleList.setSelectedIndex(selectedIndex);
        }
    }
}
