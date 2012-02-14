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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.MainPanel;

/**
 * The open schedule action listener.
 * @author Michael
 */
public class OpenScheduleActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ae) {
        if(confirmClear()) {
            JFileChooser chooser = Utils.getScheduleFileChooser();
            if(chooser.showOpenDialog(Application.get().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                Application.get().openSchedule(chooser.getSelectedFile());
            }
        }
    }

    /**
     * Confirm whether it's ok to clear the current schedule.
     *
     * @return true if this is ok, false otherwise.
     */
    private boolean confirmClear() {
        MainPanel mainpanel = Application.get().getMainWindow().getMainPanel();
        if(mainpanel.getSchedulePanel().getScheduleList().isEmpty()) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("schedule.clear.text"), LabelGrabber.INSTANCE.getLabel("confirm.label"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
        if(result == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }
}
