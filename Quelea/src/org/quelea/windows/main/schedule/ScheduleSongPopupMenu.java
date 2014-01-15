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
package org.quelea.windows.main.schedule;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;

/**
 * The popup menu that displays when a song in the schedule is right-clicked.
 * @author Michael
 */
public class ScheduleSongPopupMenu extends ContextMenu {

    private final MenuItem editSong;

    /**
     * Create a new schedule popup menu
     */
    public ScheduleSongPopupMenu() {
        editSong = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
        getItems().add(editSong);
    }

    /**
     * Get the edit song button.
     * @return the edit song button.
     */
    public MenuItem getEditSongButton() {
        return editSong;
    }

}
