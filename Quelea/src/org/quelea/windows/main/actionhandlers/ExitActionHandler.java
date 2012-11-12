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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.QueleaApp;
import org.quelea.Schedule;
import org.quelea.ScheduleSaver;
import org.quelea.data.displayable.Displayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;

/**
 * The exit action listener - called when the user requests they wish to exit
 * Quelea.
 * <p/>
 * @author Michael
 */
public class ExitActionHandler implements EventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean cancel = false;

    /**
     * Call this method when the event is fired.
     */
    @Override
    public void handle(ActionEvent t) {
        exit();
    }

    /**
     * Process the necessary logic to cleanly exit from Quelea.
     */
    public void exit() {
        LOGGER.log(Level.INFO, "exit() called");
        Schedule schedule = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule();
        if(!schedule.isEmpty() && schedule.isModified()) {
            cancel = false;
            Dialog d = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("save.before.exit.title"), LabelGrabber.INSTANCE.getLabel("save.before.exit.text")).addYesButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    //Save schedule
                    new ScheduleSaver().saveSchedule(false);
                }
            }).addNoButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    //Don't do anything
                }
            }).addCancelButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    //Don't exit
                    cancel = true;
                }
            }).build();
            d.showAndWait();
            if(cancel) {
                return; //Don't exit
            }
        }
        
        LOGGER.log(Level.INFO, "Hiding main window...");
        QueleaApp.get().getMainWindow().hide();
        LOGGER.log(Level.INFO, "Cleaning up displayables before exiting..");
        for(Object obj : QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get()) {
            Displayable d = (Displayable) obj;
            LOGGER.log(Level.INFO, "Cleaning up {0}", d.getClass());
            d.dispose();
        }
        System.exit(0);
    }
}
