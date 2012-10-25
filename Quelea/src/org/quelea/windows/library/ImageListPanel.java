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
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;
import org.quelea.QueleaApp;
import org.quelea.displayable.ImageDisplayable;
import org.quelea.utils.Utils;

/**
 * The panel displayed on the library to select the list of images.
 * <p/>
 * @author Michael
 */
public class ImageListPanel extends BorderPane {

    private final TilePane imageList;
    private String dir;

    /**
     * Create a new image list panel.
     * <p/>
     * @param dir the directory to use.
     */
    public ImageListPanel(String dir) {
        this.dir = dir;
        imageList = new TilePane();
        imageList.setHgap(10);
        imageList.setVgap(10);
        imageList.setOrientation(Orientation.HORIZONTAL);
        updateImages();
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(imageList);
        setCenter(scroll);
    }

    /**
     * Change the panel to display a new directory.
     * <p/>
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
        updateImages();
    }

    /**
     * Add the files to the given model.
     * <p/>
     * @param model the model to add files to.
     */
    private void updateImages() {
        imageList.getChildren().clear();
        final File[] files = new File(dir).listFiles();
        final List<ImageView> views = FXCollections.observableArrayList();
        final Thread runner = new Thread() {
            @Override
            public void run() {
                for(final File file : files) {
                    if(Utils.fileIsImage(file) && !file.isDirectory()) {
                        ImageView view = new ImageView(new Image("file:" + file, 72, 72, false, true));
                        view.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent t) {
                                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(new ImageDisplayable(file));
                            }
                        });
                        setupHover(view);
                        views.add(view);
                    }
                }
            }
        };
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                imageList.getChildren().addAll(views);
            }
        });
        runner.start();
    }

    private void setupHover(final ImageView view) {
        final double SECONDS = 0.2;
        final double SCALE = 2;

        view.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(view.getScaleX() == 1) {
                    view.toFront();
                    final Timeline timeline = new Timeline();
                    timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(view.scaleXProperty(), 1)));
                    timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(view.scaleYProperty(), 1)));
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(SECONDS), new KeyValue(view.scaleXProperty(), SCALE)));
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(SECONDS), new KeyValue(view.scaleYProperty(), SCALE)));
                    timeline.play();
                }
            }
        });
        view.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                view.toFront();
                final Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(view.scaleXProperty(), view.getScaleX())));
                timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(view.scaleYProperty(), view.getScaleY())));
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(SECONDS), new KeyValue(view.scaleXProperty(), 1)));
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(SECONDS), new KeyValue(view.scaleYProperty(), 1)));
                timeline.play();
            }
        });
    }
}
