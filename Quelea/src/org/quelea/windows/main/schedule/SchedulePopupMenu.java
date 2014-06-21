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
 *
 * @author Michael
 */
public class SchedulePopupMenu extends ContextMenu {

    private final static MenuItem editSong = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final static MenuItem editBible = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.bible.passage.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final static MenuItem translationChoice = new MenuItem(LabelGrabber.INSTANCE.getLabel("choose.translations.text"));

    /**
     * Create a new schedule popup menu
     * 
     */
    public static SchedulePopupMenu getSongPopup() {
        SchedulePopupMenu spm = new SchedulePopupMenu();
        spm.getItems().addAll(editSong, translationChoice);
        return spm;
    }
    
    /**
     * Create a new schedule popup menu
     * 
     */
    public static SchedulePopupMenu getBiblePopup() {
        SchedulePopupMenu spm = new SchedulePopupMenu();
        spm.getItems().add(editBible);
        return spm;
    }
    
    /**
     * Get the edit song button.
     *
     * @return the edit song button.
     */
    public static MenuItem getEditSongButton() {
        return editSong;
    }
    
     /**
     * Get the edit bible passage button.
     *
     * @return the edit bible button.
     */
    public static MenuItem getEditBibleButton() {
        return editBible;
    }

    /**
     * Get the translation choice button.
     *
     * @return the translation choice button.
     */
    public static MenuItem getTranslationChoice() {
        return translationChoice;
    }

}
