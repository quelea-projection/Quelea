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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.quelea.Application;
import org.quelea.Schedule;
import org.quelea.ScheduleSaver;
import org.quelea.displayable.Displayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.LoggerUtils;

/**
 * The exit action listener - called when the user requests they wish to exit
 * Quelea.
 *
 * @author Michael
 */
public class ExitActionListener implements ActionListener {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Call this method when the event is fired.
     *
     * @param ae the actionevent. May be null (not used.)
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        exit();
    }

    /**
     * Process the necessary logic to cleanly exit from Quelea.
     */
    private void exit() {
        LOGGER.log(Level.INFO, "exit() called");
        Schedule schedule = Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule();
        if(!schedule.isEmpty() && schedule.isModified()) {
            int val = JOptionPane.showConfirmDialog(Application.get().getMainWindow(),
                    LabelGrabber.INSTANCE.getLabel("save.before.exit.text"),
                    LabelGrabber.INSTANCE.getLabel("save.before.exit.title"),
                    JOptionPane.YES_NO_CANCEL_OPTION);
            switch(val) {
                case JOptionPane.YES_OPTION:
                    new ScheduleSaver().saveSchedule(false);
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.CANCEL_OPTION: //Don't exit
                    return;
            }
        }
        LOGGER.log(Level.INFO, "Cleaning up displayables before exiting..");
        for(Object obj : Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().toArray()) {
            Displayable d = (Displayable)obj;
            LOGGER.log(Level.INFO, "Cleaning up {0}", d.getClass());
            d.dispose();
        }
        System.exit(0);
    }
}
