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
package org.quelea.windows.main.actionhandlers;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.QueleaApp;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.languages.LabelGrabber;

/**
 * The action listener for adding a DVD.
 * @author Michael
 */
public class AddDVDActionHandler implements EventHandler<ActionEvent> {

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
            Dialog.showError(LabelGrabber.INSTANCE.getLabel("no.dvd.heading"), LabelGrabber.INSTANCE.getLabel("no.dvd.error"));
        }
        else {
            VideoDisplayable displayable = new VideoDisplayable(file, VideoDisplayable.VideoType.DVD);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
    }
    
}
