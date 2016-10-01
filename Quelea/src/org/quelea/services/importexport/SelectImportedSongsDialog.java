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

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * A dialog used for selecting the songs to be entered into the database after
 * they've been imported.
 * <p/>
 * @author Michael
 */
public class SelectImportedSongsDialog extends SelectSongsDialog {

    private StatusPanel statusPanel;

    /**
     * Create a new imported songs dialog.
     */
    public SelectImportedSongsDialog() {
        super(new String[]{
            LabelGrabber.INSTANCE.getLabel("select.imported.songs.line1"),
            LabelGrabber.INSTANCE.getLabel("select.imported.songs.line2"),
            LabelGrabber.INSTANCE.getLabel("select.imported.songs.line3")
        }, LabelGrabber.INSTANCE.getLabel("add.text"), LabelGrabber.INSTANCE.getLabel("add.to.database.question"));

        getAddButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                getAddButton().setDisable(true);
                new Thread() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                hide();
                                getAddButton().setDisable(false);
                                statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("importing.status"));
                                statusPanel.removeCancelButton();
                            }
                        });
                        List<SongDisplayable> songDisplayables = new ArrayList<>();
                        songDisplayables.addAll(getSelectedSongs());
                        SongManager.get().addSong(songDisplayables, false);
                        SongManager.get().fireUpdate();
                        if(statusPanel != null) {
                            statusPanel.done();
                        }
                    }
                }.start();
            }
        });
    }
}
