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
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * An action listener that needs to check whether to clear the schedule before
 * doing so. Common examples include creating a new schedule or opening a
 * schedule - anything that clears the current content.
 *
 * @author Michael
 */
public abstract class ClearingEventHandler implements EventHandler<ActionEvent> {

    private boolean yes = false;

    /**
     * Confirm whether it's ok to clear the current schedule.
     *
     * @return true if this is ok, false otherwise.
     */
    public boolean confirmClear() {
        MainPanel mainpanel = QueleaApp.get().getMainWindow().getMainPanel();
        if (mainpanel.getSchedulePanel().getScheduleList().isEmpty()) {
            return true;
        }
        yes = true;
        if(mainpanel.getSchedulePanel().getScheduleList().getSchedule().isModified()) {
            final Dialog dialog = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.label"), LabelGrabber.INSTANCE.getLabel("schedule.clear.text")).addYesButton(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                }
            }).addNoButton(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    yes = false;
                }
            }).build();
            dialog.showAndWait();
        }
        return yes;
    }
}
