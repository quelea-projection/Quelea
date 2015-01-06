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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;

/**
 * Quelea's database menu.
 *
 * @author Michael
 */
public class DatabaseMenu extends Menu {

    private final MenuItem newSongItem;
    private final ImportMenu importMenu;
    private final ExportMenu exportMenu;

    /**
     * Create the database menu.
     */
    public DatabaseMenu() {
        super(LabelGrabber.INSTANCE.getLabel("database.heading"));

        newSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("new.song.button"), new ImageView(new Image("file:icons/newsong.png", 16, 16, false, true)));
        newSongItem.setOnAction(new NewSongActionHandler());
        newSongItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        getItems().add(newSongItem);

        getItems().add(new SeparatorMenuItem());
        importMenu = new ImportMenu();
        getItems().add(importMenu);
        exportMenu = new ExportMenu();
        getItems().add(exportMenu);
    }
}
