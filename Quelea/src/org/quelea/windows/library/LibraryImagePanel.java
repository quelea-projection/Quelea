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
package org.quelea.windows.library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.quelea.utils.FileFilters;
import org.quelea.utils.QueleaProperties;

/**
 * The image panel in the library.
 *
 * @author Michael
 */
public class LibraryImagePanel extends BorderPane {

    private final ImageListPanel imagePanel;
    private HBox northPanel;

    /**
     * Create a new library image panel.
     */
    public LibraryImagePanel() {
        imagePanel = new ImageListPanel("img");
        setCenter(imagePanel);
        northPanel = new HBox();
        Button refreshButton = new Button("", new ImageView(new Image("file:icons/about.png")));
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                imagePanel.refresh();
            }
        });
        refreshButton.setAlignment(Pos.CENTER_RIGHT);
        northPanel.getChildren().add(refreshButton);

        Button addButton = new Button("", new ImageView(new Image("file:icons/add.png")));
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(FileFilters.IMAGES);
                chooser.setInitialDirectory(QueleaProperties.getQueleaUserHome());
                List<File> files = chooser.showOpenMultipleDialog(null);
                if (files != null) {
                    for (File f : files) {
                        try {
                            Files.copy(f.getAbsoluteFile().toPath(), Paths.get(imagePanel.getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        addButton.setAlignment(Pos.CENTER_LEFT);
        northPanel.getChildren().add(addButton);


        HBox.setHgrow(refreshButton, Priority.NEVER);
        setTop(northPanel);

    }

    /**
     * Get the image list panel.
     *
     * @return the image list panel.
     */
    public ImageListPanel getImagePanel() {
        return imagePanel;
    }
}
