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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The image panel in the library.
 * <p/>
 * @author Michael
 */
public class LibraryImagePanel extends BorderPane {

    private final ImageListPanel imagePanel;
    private ToolBar toolbar;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new library image panel.
     */
    public LibraryImagePanel() {
        imagePanel = new ImageListPanel(QueleaProperties.get().getImageDir().getName());
        setCenter(imagePanel);
        toolbar = new ToolBar();

        Button addButton = new Button("", new ImageView(new Image("file:icons/add.png")));
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.images.panel")));
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(FileFilters.IMAGES);
                chooser.setInitialDirectory(QueleaProperties.get().getImageDir().getAbsoluteFile());
                List<File> files = chooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());
                if(files != null) {
                    final boolean[] refresh = new boolean[]{false};
                    for(final File f : files) {
                        try {
                            final Path sourceFile = f.getAbsoluteFile().toPath();

                            if(new File(imagePanel.getDir(), f.getName()).exists()) {
                                Dialog d = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.overwrite.title"), f.getName() + "\n" + LabelGrabber.INSTANCE.getLabel("confirm.overwrite.text"))
                                        .addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.replace.button"), new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent t) {
                                                try {
                                                    Files.delete(Paths.get(imagePanel.getDir(), f.getName()));
                                                    Files.copy(sourceFile, Paths.get(imagePanel.getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                                                    refresh[0] = true;
                                                }
                                                catch(IOException e) {
                                                    LOGGER.log(Level.WARNING, "Could not delete or copy file back into directory.", e);
                                                }
                                            }
                                        }).addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.continue.button"), new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent t) {
                                                // DO NOTHING
                                            }
                                        }).build();
                                d.showAndWait();
                            }
                            else {
                                Files.copy(sourceFile, Paths.get(imagePanel.getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                                refresh[0] = true;
                            }
                        }
                        catch(IOException ex) {
                            LOGGER.log(Level.WARNING, "Could not copy file into ImagePanel from FileChooser selection", ex);
                        }
                    }
                    if(refresh[0]) {
                        imagePanel.refresh();
                    }
                }
            }
        });
        HBox toolbarBox = new HBox();
        toolbar.setOrientation(Orientation.VERTICAL);
        toolbarBox.getChildren().add(toolbar);
        Utils.setToolbarButtonStyle(addButton);
        toolbar.getItems().add(addButton);
        setLeft(toolbarBox);
    }

    /**
     * Get the image list panel.
     * <p/>
     * @return the image list panel.
     */
    public ImageListPanel getImagePanel() {
        return imagePanel;
    }
}
