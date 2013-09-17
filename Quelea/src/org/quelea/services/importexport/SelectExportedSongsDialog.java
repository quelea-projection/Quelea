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
import java.util.Arrays;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.db.SongManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SongPack;

/**
 * A dialog used for selecting the songs to be put in the song pack after
 * export.
 * <p/>
 * @author Michael
 */
public class SelectExportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new exported songs dialog.
     * <p/>
     * @param owner the owner of the dialog.
     */
    public SelectExportedSongsDialog() {
        super(new String[]{
            LabelGrabber.INSTANCE.getLabel("select.export.songs.line1"),
            LabelGrabber.INSTANCE.getLabel("select.export.songs.line2")
        }, LabelGrabber.INSTANCE.getLabel("add.text"), LabelGrabber.INSTANCE.getLabel("add.to.songpack.question"));

        setSongs(Arrays.asList(SongManager.get().getSongs()), null, false);


        getAddButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                final String extension = QueleaProperties.get().getSongPackExtension();
                FileChooser chooser = getChooser();
                File file = chooser.showSaveDialog(SelectExportedSongsDialog.this);
                if(file != null) {
                    if(!file.getName().endsWith("." + extension)) {
                        file = new File(file.getAbsoluteFile() + "." + extension);
                    }
                    if(file.exists()) {
                        final File theFile = file;
                        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("overwrite.text"), file.getName() + " " + LabelGrabber.INSTANCE.getLabel("already.exists.overwrite.label")).addYesButton(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                writeSongPack(theFile);
                            }
                        }).addNoButton(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                            }
                        }).build().showAndWait();
                    }
                    else {
                        writeSongPack(file);
                    }
                }
            }
        });
        showAndWait();
    }

    /**
     * Get the JFileChooser to be used.
     * <p/>
     * @return the song pack JFileChooser.
     */
    private FileChooser getChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.SONG_PACK);
        return chooser;
    }

    /**
     * Write the song pack to the specified file, closing the window when done.
     * <p/>
     * @param file the file to write the song pack to.
     */
    private void writeSongPack(final File file) {
        final SongPack pack = new SongPack();
        getAddButton().setDisable(true);
        pack.addSongs(getSelectedSongs());
        pack.writeToFile(file);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                hide();
                getAddButton().setDisable(false);
            }
        });
    }
}
