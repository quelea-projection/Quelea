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

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.DVDDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 * The action handler responsible for letting the user add a DVD to the
 * schedule.
 * <p>
 * @author Michael
 */
public class AddDVDActionHandler implements EventHandler<ActionEvent> {
    
    private Dialog warningDialog;

    @Override
    public void handle(ActionEvent t) {
        String dvdLocation = getLocation();
        if(dvdLocation == null) {
            warningDialog = new Dialog.Builder().create()
                    .setWarningIcon()
                    .setMessage(LabelGrabber.INSTANCE.getLabel("no.dvd.error"))
                    .setTitle(LabelGrabber.INSTANCE.getLabel("no.dvd.heading"))
                    .addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            warningDialog.hide();
                        }
                    })
                    .build();
            warningDialog.showAndWait();
        }
        else {
            DVDDisplayable displayable = new DVDDisplayable(dvdLocation);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
    }

    /**
     * Get the location of the DVD, or null if no DVD can be found.
     * @return the DVD location.
     */
    private String getLocation() {
        FileSystem fs = FileSystems.getDefault();
        for(Path rootPath : fs.getRootDirectories()) {
            try {
                FileStore store = Files.getFileStore(rootPath);
                if(store.type().toLowerCase().contains("udf")) {
                    return rootPath.toString();
                }
            }
            catch(IOException ex) {
                //Never mind
            }
        }
        return null;
    }

}
