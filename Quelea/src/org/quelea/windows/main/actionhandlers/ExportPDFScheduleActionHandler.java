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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.quelea.data.Schedule;
import static org.quelea.services.importexport.OpenLyricsExporter.LOGGER;
import org.quelea.services.print.SchedulePDFPrinter;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * An event handler that exports the current schedule to a PDF file.
 *
 * @author Michael
 */
public class ExportPDFScheduleActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {
        Schedule schedule = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule();
        try {
            FileChooser fileChooser = new FileChooser();
            if (QueleaProperties.get().getLastDirectory() != null) {
                fileChooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
            }
            fileChooser.getExtensionFilters().add(FileFilters.PDF_GENERIC);
            File file = fileChooser.showSaveDialog(QueleaApp.get().getMainWindow());
            if (file != null) {
                QueleaProperties.get().setLastDirectory(file.getParentFile());
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }
                new SchedulePDFPrinter().print(schedule, file);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't export schedule as pdf", ex);
        }
    }

}
