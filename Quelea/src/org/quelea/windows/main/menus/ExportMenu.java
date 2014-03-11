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

import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.importexport.SelectExportedSongsDialog;

/**
 * Quelea's export menu.
 * <p>
 * @author Michael
 */
public class ExportMenu extends Menu {

    private final MenuItem qspItem;

    /**
     * Create the export menu.
     */
    public ExportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("export.heading"), new ImageView(new Image("file:icons/right.png", 16, 16, false, true)));

        qspItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), new ImageView(new Image("file:icons/logo16.png", 16, 16, false, true)));
        qspItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                final List<SongDisplayable> songs = Arrays.asList(SongManager.get().getSongs());
                //TODO: Determine if number of songs is above some threshold, then display warning that Quelea might be unresponsive while dialog is built.
                SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(songs);
                dialog.showAndWait(); //This line is what takes the time for a large number of songs.
            }
        });
        getItems().add(qspItem);
    }

}
