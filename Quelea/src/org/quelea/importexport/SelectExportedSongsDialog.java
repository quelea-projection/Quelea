/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.importexport;

import java.io.File;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javax.swing.SwingWorker;
import org.quelea.utils.FileFilters;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SongPack;

/**
 * A dialog used for selecting the songs to be put in the song pack after export.
 * @author Michael
 */
public class SelectExportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new exported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectExportedSongsDialog() {
        super(new String[]{
                    "The following songs are in the database.",
                    "Select the ones you want to add to the song pack then hit \"Add\"."
                }, "Add", "Add to song pack?");

        getAddButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                final String extension = QueleaProperties.get().getSongPackExtension();
                FileChooser chooser = getChooser();
                File file =chooser.showSaveDialog(SelectExportedSongsDialog.this);
                if(file!=null) {
                    if(!file.getName().endsWith("." + extension)) {
                        file = new File(file.getAbsoluteFile() + "." + extension);
                    }
                    boolean writeFile = true;
                    if(file.exists()) {
//                        int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), file.getName() + " already exists. Overwrite?",
//                                "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
//                        if (result != JOptionPane.YES_OPTION) {
//                            writeFile = false;
//                        }
                    }
                    if(writeFile) {
                        writeSongPack(file);
                    }

                }
            }
        });
    }

    /**
     * Get the JFileChooser to be used.
     * @return the song pack JFileChooser.
     */
    private FileChooser getChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.SONG_PACK);
        return chooser;
    }

    /**
     * Write the song pack to the specified file, closing the window when done.
     * @param file the file to write the song pack to.
     */
    private void writeSongPack(final File file) {
        final SongPack pack = new SongPack();
        getAddButton().setDisable(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                for (int i = 0; i < getSongs().size(); i++) {
                    if ((Boolean) getTable().getValueAt(i, 2)) {
                        pack.addSong(getSongs().get(i));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                pack.writeToFile(file);
                hide();
                getAddButton().setDisable(false);
            }
        };
        worker.execute();
    }
}
