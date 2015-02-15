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
package org.quelea.windows.main.actionhandlers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;

/**
 * Action listener that removes the selected song from the database.
 * <p/>
 * @author Michael
 */
public class RemoveSongDBActionHandler implements EventHandler<ActionEvent> {

    private boolean yes = false;

    /**
     * Remove the selected song from the database.
     * <p/>
     * @param t the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        MainWindow mainWindow = QueleaApp.get().getMainWindow();
        final LibrarySongList songList = mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        int index = songList.getListView().getSelectionModel().getSelectedIndex();
        if(index == -1) {
            return;
        }
        final SongDisplayable song = songList.getListView().itemsProperty().get().get(index);
        if(song == null) {
            return;
        }
        yes = false;
        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.remove.text"),
                LabelGrabber.INSTANCE.getLabel("confirm.remove.question").replace("$1", song.getTitle()))
                .addYesButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                yes = true;
            }
        }).addNoButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
            }
        }).build().showAndWait();
        if(yes) {
            songList.setLoading(true);
            new Thread() {
                @Override
                public void run() {
                    if(!SongManager.get().removeSong(song)) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.text"), LabelGrabber.INSTANCE.getLabel("error.removing.song.db"));
                            }
                        });
                    }
                    else {
                        song.setID(-1);
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                songList.getListView().itemsProperty().get().remove(song);
                                songList.setLoading(false);
                            }
                        });
                    }
                }
            }.start();
        }
    }
}
