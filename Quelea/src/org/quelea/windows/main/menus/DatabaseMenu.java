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
package org.quelea.windows.main.menus;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.QueleaApp;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.actionhandlers.EditSongDBActionHandler;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveSongDBActionHandler;
import org.quelea.windows.main.actionhandlers.ViewTagsActionHandler;

/**
 * Quelea's database menu.
 * @author Michael
 */
public class DatabaseMenu extends Menu {

    private final MenuItem newSongItem;
    private final MenuItem editSongItem;
    private final MenuItem deleteSongItem;
    private final MenuItem tagsItem;
    
    private final ImportMenu importMenu;
    private final ExportMenu exportMenu;

    /**
     * Create the database menu.
     */
    public DatabaseMenu() {
        super(LabelGrabber.INSTANCE.getLabel("database.heading"));

        newSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("new.song.button"), new ImageView(new Image("file:icons/newsong.png", 16, 16, false, true)));
        newSongItem.setOnAction(new NewSongActionHandler());
        getItems().add(newSongItem);

        editSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.button"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
        editSongItem.setOnAction(new EditSongDBActionHandler());
        editSongItem.setDisable(true);
        getItems().add(editSongItem);

        deleteSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("delete.song.button"), new ImageView(new Image("file:icons/remove 2.png", 16, 16, false, true)));
        deleteSongItem.setOnAction(new RemoveSongDBActionHandler());
        deleteSongItem.setDisable(true);
        getItems().add(deleteSongItem);

        tagsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("tags.button"), new ImageView(new Image("file:icons/tag.png", 16, 16, false, true)));
        tagsItem.setOnAction(new ViewTagsActionHandler());
        getItems().add(tagsItem);

        final LibrarySongList libraryList = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();

        libraryList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {

            @Override
            public void changed(ObservableValue<? extends Song> ov, Song t, Song t1) {
                checkEditDeleteItems(editSongItem, deleteSongItem);
            }
        });
        libraryList.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                checkEditDeleteItems(editSongItem, deleteSongItem);
            }
        });
        
        getItems().add(new SeparatorMenuItem());
        importMenu = new ImportMenu();
        getItems().add(importMenu);
        exportMenu = new ExportMenu();
        getItems().add(exportMenu);
    }

    /**
     * Check whether the edit / delete buttons should be set to enabled or not.
     */
    private void checkEditDeleteItems(MenuItem editSongButton, MenuItem deleteSongButton) {
        final LibrarySongList libraryList = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        if(!libraryList.isFocused()) {
            deleteSongButton.setDisable(true);
            editSongButton.setDisable(true);
            return;
        }
        if(libraryList.getSelectedValue()==null) {
            deleteSongButton.setDisable(true);
            editSongButton.setDisable(true);
        }
        else {
            deleteSongButton.setDisable(false);
            editSongButton.setDisable(false);
        }
    }
}
