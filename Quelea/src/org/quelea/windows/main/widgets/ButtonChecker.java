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
package org.quelea.windows.main.widgets;

import javafx.scene.control.MenuItem;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;

/**
 * Responsible for checking, enabling, disabling etc. buttons.
 *
 * @author Michael
 */
public class ButtonChecker {

    public static final ButtonChecker INSTANCE = new ButtonChecker();

    /**
     * Only private.
     */
    private ButtonChecker() {
    }
    
    /**
     * Check whether the edit or remove buttons should be set to enabled or
     * disabled.
     *
     * @param editSongButton the edit button to check.
     * @param removeSongButton the remove button to check.
     */
    public void checkEditRemoveButtons(MenuItem editSongButton, MenuItem removeSongButton) {
        final MainPanel mainPanel = QueleaApp.get().getMainWindow().getMainPanel();
        final ScheduleList scheduleList = mainPanel.getSchedulePanel().getScheduleList();
        if(!scheduleList.focusedProperty().get()) {
            editSongButton.setDisable(true);
            removeSongButton.setDisable(true);
            return;
        }
        if(scheduleList.getSelectionModel().getSelectedIndex() == -1) {
            editSongButton.setDisable(true);
            removeSongButton.setDisable(true);
        }
        else {
            if(scheduleList.getSelectionModel().getSelectedItem() instanceof SongDisplayable) {
                editSongButton.setDisable(false);
            }
            else {
                editSongButton.setDisable(true);
            }
            removeSongButton.setDisable(false);
        }
    }

    /**
     * Check whether the add to schedule button should be set enabled or
     * disabled.
     *
     * @param addSongButton the button to check.
     */
    public void checkAddButton(MenuItem addSongButton) {
        final MainPanel mainPanel = QueleaApp.get().getMainWindow().getMainPanel();
        final LibrarySongList songList = mainPanel.getLibraryPanel().getLibrarySongPanel().getSongList();
        if(!songList.focusedProperty().get()) {
            addSongButton.setDisable(true);
            return;
        }
        if(songList.getListView().getSelectionModel().getSelectedIndex() == -1) {
            addSongButton.setDisable(true);
        }
        else {
            addSongButton.setDisable(false);
        }
    }
}
