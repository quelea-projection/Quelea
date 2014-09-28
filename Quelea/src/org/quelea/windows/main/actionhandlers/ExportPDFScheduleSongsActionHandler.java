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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.importexport.PDFExporter;
import static org.quelea.services.importexport.PDFExporter.LOGGER;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.windows.main.QueleaApp;

/**
 * An event handler that exports the current schedule to a PDF file.
 *
 * @author Michael
 */
public class ExportPDFScheduleSongsActionHandler implements EventHandler<ActionEvent> {

    private boolean printChords;
    private final HashSet<String> names = new HashSet<>();

    @Override
    public void handle(ActionEvent t) {
        Schedule schedule = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(FileFilters.ZIP);
        File file = fileChooser.showSaveDialog(QueleaApp.get().getMainWindow());
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".zip")) {
                file = new File(file.getAbsolutePath() + ".zip");
            }
            Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("printing.options.text"), LabelGrabber.INSTANCE.getLabel("print.chords.export.question")).addYesButton(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    printChords = true;
                }
            }).addNoButton(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    printChords = false;
                }
            }).build().showAndWait();
            names.clear();
            try {
                try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                    for (int i = 0; i < schedule.getSize(); i++) {
                        Displayable d = schedule.getDisplayable(i);
                        if (!(d instanceof SongDisplayable)) {
                            continue;
                        }
                        SongDisplayable song = (SongDisplayable) d;
                        String name = song.getTitle() + ".pdf";
                        while (names.contains(name)) {
                            name = PDFExporter.incrementExtension(name);
                        }
                        names.add(name);
                        out.putNextEntry(new ZipEntry(name));
                        out.write(PDFExporter.getPDF(song, printChords));
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't export PDF songs", ex);
            }
        }
    }

}
