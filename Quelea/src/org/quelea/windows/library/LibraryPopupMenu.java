/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.windows.library;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.actionlisteners.AddSongActionListener;
import org.quelea.windows.main.actionlisteners.EditSongDBActionListener;
import org.quelea.windows.main.actionlisteners.RemoveSongDBActionListener;

/**
 * The popup menu that displays when someone right clicks on a song in the library.
 * @author Michael
 */
public class LibraryPopupMenu extends ContextMenu {

    private final MenuItem addToSchedule;
    private final MenuItem editDB;
    private final MenuItem removeFromDB;
    private final MenuItem print;

    /**
     * Create and initialise the popup menu.
     */
    public LibraryPopupMenu() {
        addToSchedule = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.add.to.schedule.text"), new ImageView(new Image("file:icons/add.png", 16, 16, false, true)));
        addToSchedule.setOnAction(new AddSongActionListener());
        editDB = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.edit.song.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
        editDB.setOnAction(new EditSongDBActionListener());
        removeFromDB = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.remove.song.text"), new ImageView(new Image("file:icons/removedb.png", 16, 16, false, true)));
        removeFromDB.setOnAction(new RemoveSongDBActionListener());
        print = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.print.song.text"), new ImageView(new Image("file:icons/fileprint.png", 16, 16, false, true)));
        print.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                Song song = Application.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue();
                if(song != null) {
//                    int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("print.chords.question"), LabelGrabber.INSTANCE.getLabel("printing.options.text"), JOptionPane.YES_NO_OPTION);
//                    song.setPrintChords(result == JOptionPane.YES_OPTION);
//                    Printer.getInstance().print(song);
                }
            }
        });

        getItems().add(addToSchedule);
        getItems().add(editDB);
        getItems().add(removeFromDB);
        getItems().add(print);
    }
}
