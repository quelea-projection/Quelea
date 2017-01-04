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
import org.quelea.data.bible.BibleManager;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.EditSongScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.EditThemeScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.EditTimerActionHandler;
import org.quelea.windows.main.actionhandlers.EditTimerThemeActionHandler;
import org.quelea.windows.main.actionhandlers.SelectTranslationsActionHandler;
import org.quelea.windows.main.actionhandlers.SwitchBibleVersionActionHandler;

/**
 * The popup menu that displays when an item in the schedule is right-clicked.
 *
 * @author Michael
 */
public class SchedulePopupMenu extends ContextMenu {

    private final MenuItem editSong = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final MenuItem editTheme = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.theme.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
    private final MenuItem changeBibleVersion = new MenuItem(LabelGrabber.INSTANCE.getLabel("change.bible.version.text"), new ImageView(new Image("file:icons/bible.png", 16, 16, false, true)));
    private final MenuItem translationChoice = new MenuItem(LabelGrabber.INSTANCE.getLabel("choose.translations.text"));
    private final MenuItem editTimer = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.timer.text"), new ImageView(new Image("file:icons/timer-dark.png", 16, 16, false, true)));
    private final MenuItem editTimerTheme = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.theme.text"), new ImageView(new Image("file:icons/theme.png", 16, 16, false, true)));
    

    public SchedulePopupMenu(Displayable item) {
        getItems().add(new MenuItem("placeholder")); //TODO: Investigate why this is required
        setOnShowing((WindowEvent event) -> {
            ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
            boolean singleSelect = sl.getSelectionModel().getSelectedItems().size() == 1;
            boolean allSongs = true;
            boolean allBibles = true;
            for (Displayable d : sl.getSelectionModel().getSelectedItems()) {
                if (!(d instanceof SongDisplayable)) {
                    allSongs = false;
                }
                if (!(d instanceof BiblePassage)) {
                    allBibles = false;
                }
            }
            if (!singleSelect && allSongs && !(item instanceof SongDisplayable)) {
                singleSelect = true;
            }
            if (!singleSelect && allBibles && !(item instanceof BiblePassage)) {
                singleSelect = true;
            }
            getItems().clear();

            if (singleSelect) {
                if (item instanceof BiblePassage) {
                    editTheme.setOnAction(new EditThemeScheduleActionHandler((BiblePassage) item));
                    getItems().addAll(editTheme);
                    if (BibleManager.get().getBibles().length > 1) {
                        getItems().addAll(changeBibleVersion);
                    }
                } else if (item instanceof SongDisplayable) {
                    getItems().addAll(editSong, translationChoice);
                } else if (item instanceof TimerDisplayable) {
                    getItems().addAll(editTimer, editTimerTheme);
                } else {
                    getItems().addAll(editTheme);
                }
            } else {
                if (allSongs) {
                    getItems().addAll(editTheme);
//                    getItems().addAll(editTheme, translationChoice); //TODO: Do we want to add multiple-translation option as well?
                } else if (allBibles) {
                    getItems().addAll(editTheme);
                    if (BibleManager.get().getBibles().length > 1) {
                        getItems().addAll(changeBibleVersion);
                    }
                } else {
                    getItems().addAll(editTheme);
                }
            }
            if (getItems().isEmpty()) {
                event.consume();
            }
        });

        editSong.setOnAction(new EditSongScheduleActionHandler());
        translationChoice.setOnAction(new SelectTranslationsActionHandler());
        editTimer.setOnAction(new EditTimerActionHandler());
        editTimerTheme.setOnAction(new EditTimerThemeActionHandler());
        changeBibleVersion.setOnAction(new SwitchBibleVersionActionHandler());
    }

}
