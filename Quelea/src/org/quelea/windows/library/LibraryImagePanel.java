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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.quelea.QueleaApp;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;

/**
 * The image panel in the library.
 *
 * @author Michael
 */
public class LibraryImagePanel extends BorderPane {

    private final ImageListPanel imagePanel;
    private HBox northPanel;
    private ObservableList<NewFile> list = FXCollections.observableArrayList();

    /**
     * Create a new library image panel.
     */
    public LibraryImagePanel() {
        imagePanel = new ImageListPanel("img");
        setCenter(imagePanel);
        northPanel = new HBox();
        Button refreshButton = new Button("", new ImageView(new Image("file:icons/green_refresh128.png", 22, 22, true, true)));
        refreshButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("refresh.images.panel")));
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                imagePanel.refresh();
            }
        });
        
        Button addButton = new Button("", new ImageView(new Image("file:icons/add.png", 22, 22, true, true)));
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.images.panel")));
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(FileFilters.IMAGES);
                chooser.setInitialDirectory(QueleaProperties.get().getImageDir());
                List<File> files = chooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());
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
        
        final ObservableList<NewFile> list = FXCollections.observableArrayList();
        list.addAll(getComboBoxOptions());
        
        ComboBox cb = new ComboBox(list);
        cb.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("select.folder.images.panel")));
        cb.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                NewFile nf = (NewFile) t1;
                if(nf.getFile() == null) {
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setInitialDirectory(QueleaProperties.get().getImageDir());
                    File f = chooser.showDialog(QueleaApp.get().getMainWindow());
                    if(f != null && f.isDirectory()) {
                        imagePanel.changeDir(f.getAbsolutePath());
                        list.add(list.size()-1, new NewFile(f));
                        }
                    else {
                        // Throw error?
                    }
                }
                else {
                    imagePanel.changeDir(nf.getFile().getAbsolutePath());
                }
                imagePanel.refresh();
            }
        });
        cb.setMinWidth(250.0);
        northPanel.getChildren().add(cb);
        northPanel.getChildren().add(addButton);
        northPanel.getChildren().add(refreshButton);
        northPanel.alignmentProperty().setValue(Pos.CENTER);

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

    private ObservableList<NewFile> getComboBoxOptions() {
        list.add(new NewFile(new File(System.getProperty("user.home"))));
        list.add(new NewFile(QueleaProperties.get().getImageDir()));
        list.add(new NewFile(null));
        return list;
    }

    private static class NewFile {

        private File file;
        
        public NewFile(File f) {
            file = f;
        }
        
        @Override
        public String toString() {
            if(file==null) { 
                return "Select...";
            }
            else {
                return file.getName();
            }
        }
        
        public void set(File f) {
            file = f;
        }
        
        public File getFile() {
            return file;
        }
    }
}
