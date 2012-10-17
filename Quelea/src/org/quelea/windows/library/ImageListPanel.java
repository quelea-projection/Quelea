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

import org.quelea.displayable.ImageDisplayable;
import java.awt.Component;
import java.io.File;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.quelea.utils.Utils;

/**
 * The panel displayed on the library to select the list of images.
 *
 * @author Michael
 */
public class ImageListPanel extends BorderPane {

    private final ListView<ImageDisplayable> imageList;
    private String dir;

    /**
     * Create a new image list panel.
     *
     * @param dir the directory to use.
     */
    public ImageListPanel(String dir) {
        this.dir = dir;
        imageList = new ListView<>();
        imageList.setCellFactory(new Callback<ListView<ImageDisplayable>, ListCell<ImageDisplayable>>() {

            @Override
            public ListCell<ImageDisplayable> call(ListView<ImageDisplayable> p) {
                return new ImageCell();
            }
        });
        imageList.setOrientation(Orientation.VERTICAL);
        addFiles();
        setCenter(imageList);
    }

    /**
     * Change the panel to display a new directory.
     *
     * @param newDir the new directory.
     */
    public void changeDir(String newDir) {
        dir = newDir;
        refresh();
    }

    /**
     * Refresh the contents of this image list panel.
     */
    public void refresh() {
        addFiles();
    }

    /**
     * Add the files to the given model.
     *
     * @param model the model to add files to.
     */
    private void addFiles() {
        final File[] files = new File(dir).listFiles();
        final ObservableList<ImageDisplayable> images = FXCollections.observableArrayList();
        final Thread runner = new Thread() {

            @Override
            public void run() {
                for(final File file : files) {
                    if(Utils.fileIsImage(file) && !file.isDirectory()) {
                        images.add(new ImageDisplayable(file));
                    }
                }
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        imageList.itemsProperty().set(images);
                    }
                });
            }
        };
        runner.start();
    }

    private static class ImageCell extends ListCell<ImageDisplayable> {
        @Override
        public void updateItem(ImageDisplayable item, boolean empty) {
            super.updateItem(item, empty);
            if(item != null) {
                ImageView iv = new ImageView(new Image("file:"+item.getFile().getAbsolutePath(), 100, 100, false, false));
                setGraphic(iv);
                setText(null);
            }
        }
    }
}
