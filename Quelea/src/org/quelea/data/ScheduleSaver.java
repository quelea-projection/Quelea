/* 
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.data;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * Responsible for saving a schedule.
 * @author Michael
 */
public class ScheduleSaver {
    
    private boolean yes = false;

    /**
     * Save the current schedule.
     * @param saveAs true if the file location should be specified, false if the current one should be used.
     */
    public void saveSchedule(boolean saveAs) {
        MainPanel mainpanel = QueleaApp.get().getMainWindow().getMainPanel();
        Schedule schedule = mainpanel.getSchedulePanel().getScheduleList().getSchedule();
        File file = schedule.getFile();
        if (saveAs || file == null) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(FileFilters.SCHEDULE);
            File selectedFile = chooser.showSaveDialog(QueleaApp.get().getMainWindow());
            if (selectedFile != null) {
                String extension = QueleaProperties.get().getScheduleExtension();
                if (!selectedFile.getName().endsWith("." + extension)) {
                    selectedFile = new File(selectedFile.getAbsoluteFile() + "." + extension);
                }
                if (selectedFile.exists()) {
                    yes = false;
                    Dialog confirm = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("overwrite.text"), LabelGrabber.INSTANCE.getLabel("already.exists.overwrite.label")).addYesButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            yes = true;
                        }
                    }).addNoButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                        }
                    }).build();
                    confirm.showAndWait();
                    if (!yes) {
                        selectedFile = null;
                    }
                }
                schedule.setFile(selectedFile);
            }
        }
        if (schedule.getFile() != null) {
            boolean success = schedule.writeToFile();
            if (!success) {
                Dialog.showError(LabelGrabber.INSTANCE.getLabel("cant.save.schedule.title"), LabelGrabber.INSTANCE.getLabel("cant.save.schedule.text"));
            }
        }
    }

}
