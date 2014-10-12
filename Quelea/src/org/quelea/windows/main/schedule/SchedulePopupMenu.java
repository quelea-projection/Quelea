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
import javafx.stage.WindowEvent;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.EditSongScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.EditThemeScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.SelectTranslationsActionHandler;

/**
 * The popup menu that displays when an item in the schedule is right-clicked.
 *
 * @author Michael
 */
public class SchedulePopupMenu extends ContextMenu {

    private final MenuItem editSong = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final MenuItem editTheme = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.theme.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final MenuItem translationChoice = new MenuItem(LabelGrabber.INSTANCE.getLabel("choose.translations.text"));

    public SchedulePopupMenu() {
        getItems().add(new MenuItem("placeholder")); //TODO: Investigate why this is required
        setOnShowing((WindowEvent event) -> {
            ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
            boolean singleSelect = sl.getSelectionModel().getSelectedItems().size() == 1;
            boolean allSongs = true;
            for (Displayable d : sl.getSelectionModel().getSelectedItems()) {
                if(!(d instanceof SongDisplayable)) {
                    allSongs = false;
                    break;
                }
            }
            getItems().clear();
            
            if (singleSelect) {
                if (sl.getSelectionModel().getSelectedItem() instanceof BiblePassage) {
                    getItems().addAll(editTheme);
                } else if (sl.getSelectionModel().getSelectedItem() instanceof SongDisplayable) {
                    getItems().addAll(editSong, translationChoice);
                }
            }
            else {
                if(allSongs) {
                    getItems().addAll(editTheme);
//                    getItems().addAll(editTheme, translationChoice); //TODO: Do we want to add multiple-translation option as well?
                }
                else {
                    getItems().addAll(editTheme);
                }
            }
            if(getItems().isEmpty()) {
                event.consume();
            }
        });
        
        editSong.setOnAction(new EditSongScheduleActionHandler());
        translationChoice.setOnAction(new SelectTranslationsActionHandler());
        editTheme.setOnAction(new EditThemeScheduleActionHandler());
    }

}
