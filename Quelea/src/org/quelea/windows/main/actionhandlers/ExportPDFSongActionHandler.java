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
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import static org.quelea.services.importexport.OpenLyricsExporter.LOGGER;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.print.SongPDFPrinter;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;

/**
 * An event handler that exports the currently selected song to a PDF file.
 *
 * @author Michael
 */
public class ExportPDFSongActionHandler implements EventHandler<ActionEvent> {
    
    private boolean exportTranslations;

    @Override
    public void handle(ActionEvent t) {
        MainWindow mainWindow = QueleaApp.get().getMainWindow();
        final LibrarySongList songList = mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        final SongDisplayable song = songList.getListView().itemsProperty().get().get(songList.getListView().getSelectionModel().getSelectedIndex());
        if (song == null) {
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            if (QueleaProperties.get().getLastDirectory() != null) {
                fileChooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
            }
            fileChooser.getExtensionFilters().add(FileFilters.PDF_GENERIC);
            File file = fileChooser.showSaveDialog(QueleaApp.get().getMainWindow());
            if (file != null) {
                QueleaProperties.get().setLastDirectory(file.getParentFile());
                if (song.hasChords()) {
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("printing.options.text"), LabelGrabber.INSTANCE.getLabel("print.chords.question")).addYesButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            song.setPrintChords(true);
                        }
                    }).addNoButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            song.setPrintChords(false);
                        }
                    }).build().showAndWait();
                }
                exportTranslations = false;
                if (!song.getTranslations().isEmpty()) {
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("translation.export.heading"), LabelGrabber.INSTANCE.getLabel("include.translations.question")).addYesButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            exportTranslations = true;
                        }
                    }).addNoButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            exportTranslations = true;
                        }
                    }).build().showAndWait();
                }
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }
                SongPDFPrinter.INSTANCE.print(song, file, exportTranslations);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't export song as PDF", ex);
        }
    }

}
