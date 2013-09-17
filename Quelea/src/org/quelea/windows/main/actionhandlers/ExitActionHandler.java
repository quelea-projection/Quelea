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
import javafx.event.Event;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.data.Schedule;
import org.quelea.data.ScheduleSaver;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SceneInfo;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;

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
        exit(t);
    }

    /**
     * Process the necessary logic to cleanly exit from Quelea.
     */
    public void exit(Event t) {
        LOGGER.log(Level.INFO, "exit() called");
        MainWindow mainWindow = QueleaApp.get().getMainWindow();
        t.consume();
        Schedule schedule = mainWindow.getMainPanel().getSchedulePanel().getScheduleList().getSchedule();
        if(!schedule.isEmpty() && schedule.isModified()) {
            cancel = false;
            Dialog d = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("save.before.exit.title"), LabelGrabber.INSTANCE.getLabel("save.before.exit.text")).addYesButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    //Save schedule
                   cancel = !(new ScheduleSaver().saveSchedule(false));
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
        LOGGER.log(Level.INFO, "Saving window position...");
        QueleaProperties.get().setSceneInfo(new SceneInfo(mainWindow.getX(), mainWindow.getY(), mainWindow.getWidth(), mainWindow.getHeight()));
        LOGGER.log(Level.INFO, "Hiding main window...");
        mainWindow.hide();
        LOGGER.log(Level.INFO, "Cleaning up displayables before exiting..");
        for(Object obj : mainWindow.getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get()) {
            Displayable d = (Displayable) obj;
            LOGGER.log(Level.INFO, "Cleaning up {0}", d.getClass());
            d.dispose();
        }

        LOGGER.log(Level.INFO, "Try close OOfice if opened");
        OOUtils.closeOOApp();
        System.exit(0);
    }
}
