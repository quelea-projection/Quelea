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
package org.quelea.services.importexport;

import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * A dialog used for selecting the songs to be put in the song pack after
 * export.
 * <p/>
 * @author Michael
 */
public class SelectExportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new exported songs dialog.
     * @param songs the songs to display in this dialog.
     * @param exporter the exporter to use.
     */
    public SelectExportedSongsDialog(List<SongDisplayable> songs, final Exporter exporter) {
        super(new String[]{
            LabelGrabber.INSTANCE.getLabel("select.export.songs.line1"),
            LabelGrabber.INSTANCE.getLabel("select.export.songs.line2")
        }, LabelGrabber.INSTANCE.getLabel("add.text"), LabelGrabber.INSTANCE.getLabel("add.to.songpack.question"));

        setSongs(songs, null, false);

        getAddButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                final String extension = exporter.getStrExtension();
                FileChooser chooser = exporter.getChooser();
                if (QueleaProperties.get().getLastDirectory() != null) {
                    chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
                }
                File file = chooser.showSaveDialog(SelectExportedSongsDialog.this);
                if(file != null) {
                    QueleaProperties.get().setLastDirectory(file.getParentFile());
                    QueleaProperties.get().setLastDirectory(file.getParentFile());
                    if(!file.getName().endsWith("." + extension)) {
                        file = new File(file.getAbsoluteFile() + "." + extension);
                    }
                    if(file.exists()) {
                        final File theFile = file;
                        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("overwrite.text"), file.getName() + " " + LabelGrabber.INSTANCE.getLabel("already.exists.overwrite.label")).addYesButton(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                getAddButton().setDisable(true);
                                exporter.exportSongs(theFile, getSelectedSongs());
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        hide();
                                        getAddButton().setDisable(false);
                                    }
                                });
                            }
                        }).addNoButton(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                            }
                        }).build().showAndWait();
                    }
                    else {
                        getAddButton().setDisable(true);
                        exporter.exportSongs(file, getSelectedSongs());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                hide();
                                getAddButton().setDisable(false);
                            }
                        });
                    }
                }
            }
        });
    }
}
