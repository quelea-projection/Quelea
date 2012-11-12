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

import javafx.application.Platform;
import javafx.event.EventHandler;
import org.quelea.data.db.SongManager;

/**
 * A dialog used for selecting the songs to be entered into the database after they've been imported.
 * @author Michael
 */
public class SelectImportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectImportedSongsDialog() {
        super(new String[]{
                    "The following songs have been imported.",
                    "Select the ones you want to add to the database then hit \"Add\".",
                    "Songs that Quelea thinks are duplicates have been unchecked."
                }, "Add", "Add to database?");

        getAddButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                getAddButton().setDisable(true);
                new Thread() {
                    public void run() {
                        for(int i = 0; i < getSongs().size(); i++) {
                            if(getCheckedColumn().getCellData(i)) {
                                SongManager.get().addSong(getSongs().get(i), false);
                            }
                        }
                        
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                SongManager.get().fireUpdate();
                                hide();
                                getAddButton().setDisable(false);
                            }
                        });
                    }
                }.start();
            }
        });
    }
}
