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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;

import java.util.List;

/**
 * Action listener that removes the selected song from the database.
 * <p/>
 *
 * @author Michael
 */
public class RemoveSongDBActionHandler implements EventHandler<ActionEvent> {

    private boolean yes = false;

    /**
     * Remove the selected song from the database.
     * <p/>
     *
     * @param t the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        MainWindow mainWindow = QueleaApp.get().getMainWindow();
        final LibrarySongList songList = mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        List<Integer> indices = songList.getListView().getSelectionModel().getSelectedIndices();
        if (indices.isEmpty()) {
            return;
        }

        String confirmRemoveQuestion;
        if (indices.size() == 1) {
            final SongDisplayable song = songList.getListView().itemsProperty().get().get(indices.get(0));
            confirmRemoveQuestion = LabelGrabber.INSTANCE.getLabel("confirm.remove.question").replace("$1", song.getTitle());
        } else {
            confirmRemoveQuestion = LabelGrabber.INSTANCE.getLabel("confirm.remove.bulk.question").replace("$1", Integer.toString(indices.size()));
        }

        yes = false;
        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.remove.text"),
                        confirmRemoveQuestion)
                .addYesButton(t1 -> yes = true).addNoButton(t12 -> {
                }).build().showAndWait();
        if (yes) {
            songList.setLoading(true);
            new Thread(() -> {
                ObservableList<SongDisplayable> items = songList.getListView().getSelectionModel().getSelectedItems();
                if (!SongManager.get().removeSongs(items)) {
                    Platform.runLater(() -> Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.text"), LabelGrabber.INSTANCE.getLabel("error.removing.song.db")));
                } else {
                    items.forEach(s -> s.setID(-1));
                }
                Platform.runLater(() -> {
                    songList.getListView().itemsProperty().get().removeAll(items);
                    songList.setLoading(false);
                });
            }).start();
        }
    }
}
