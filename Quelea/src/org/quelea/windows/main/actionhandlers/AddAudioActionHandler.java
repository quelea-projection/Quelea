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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.services.utils.FileFilters;
import org.quelea.windows.main.QueleaApp;

/**
 * The action handler for adding a video.
 * @author Michael
 */
public class AddAudioActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(FileFilters.AUDIOS);
        File file = fileChooser.showOpenDialog(QueleaApp.get().getMainWindow());
        if(file != null) {
            AudioDisplayable displayable = new AudioDisplayable(file);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
    }
    
}
