/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javax.swing.JOptionPane;
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.MainPanel;

/**
 * An action listener that needs to check whether to clear the schedule before
 * doing so. Common examples include creating a new schedule or opening a 
 * schedule - anything that clears the current content.
 * @author Michael
 */
public abstract class ClearingActionListener implements EventHandler<ActionEvent> {

    /**
     * Confirm whether it's ok to clear the current schedule.
     *
     * @return true if this is ok, false otherwise.
     */
    public boolean confirmClear() {
        MainPanel mainpanel = Application.get().getMainWindow().getMainPanel();
        if(mainpanel.getSchedulePanel().getScheduleList().isEmpty()) {
            return true;
        }
//        int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("schedule.clear.text"), LabelGrabber.INSTANCE.getLabel("confirm.label"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
//        if(result == JOptionPane.YES_OPTION) {
//            return true;
//        }
        return false;
    }
}
