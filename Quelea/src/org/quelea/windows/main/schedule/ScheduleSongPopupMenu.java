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
import org.quelea.windows.main.actionhandlers.EditSongScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.SelectTranslationsActionHandler;

/**
 * The popup menu that displays when a song in the schedule is right-clicked.
 *
 * @author Michael
 */
public class ScheduleSongPopupMenu extends ContextMenu {

    private final MenuItem editSong = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final MenuItem translationChoice = new MenuItem(LabelGrabber.INSTANCE.getLabel("choose.translations.text"));

    public ScheduleSongPopupMenu() {
        editSong.setOnAction(new EditSongScheduleActionHandler());
        translationChoice.setOnAction(new SelectTranslationsActionHandler());
        getItems().addAll(editSong, translationChoice);
    }

}
