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
import javafx.stage.FileChooser;
import org.quelea.QueleaApp;
import org.quelea.services.utils.FileFilters;

/**
 * The open schedule action listener.
 * @author Michael
 */
public class OpenScheduleActionHandler extends ClearingEventHandler {

    @Override
    public void handle(javafx.event.ActionEvent t) {
        if(confirmClear()) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(FileFilters.SCHEDULE);
            File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
            if(file!=null) {
                QueleaApp.get().openSchedule(file);
            }
        }
    }

}
