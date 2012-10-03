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

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javax.swing.JOptionPane;
import org.quelea.Application;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.languages.LabelGrabber;

/**
 * The action listener for adding a DVD.
 * @author Michael
 */
public class AddDVDActionListener implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {
        File[] arr = File.listRoots();
        File file = null;
        for(File f : arr) {
            if(f.getUsableSpace() == 0 && f.getTotalSpace() > 0) {
                file = f;
            }
        }
        if(file == null) {
//            JOptionPane.showMessageDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("no.dvd.error"), LabelGrabber.INSTANCE.getLabel("no.dvd.heading"), JOptionPane.ERROR_MESSAGE);
        }
        else {
            VideoDisplayable displayable = new VideoDisplayable(file, VideoDisplayable.VideoType.DVD);
            Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
    }
    
}
