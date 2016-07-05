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

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The action handler for adding a multimedia file.
 *
 * @author Michael
 */
public class AddVideoActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {
        FileChooser fileChooser = new FileChooser();
        if (QueleaProperties.get().getLastVideoDirectory() != null) {
            fileChooser.setInitialDirectory(QueleaProperties.get().getLastVideoDirectory());
        }
        fileChooser.getExtensionFilters().add(FileFilters.MULTIMEDIA);
        final List<File> files = fileChooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());

        if (files != null) {
            for (File file : files) {
                QueleaProperties.get().setLastVideoDirectory(file.getParentFile());
                VideoDisplayable displayable = new VideoDisplayable(file.getAbsolutePath());
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
                new Thread() {
                    @Override
                    public void run() {
                        Utils.getVidBlankImage(file); //Cache preview image
                    }
                }.start();
            }
        }
    }

}
