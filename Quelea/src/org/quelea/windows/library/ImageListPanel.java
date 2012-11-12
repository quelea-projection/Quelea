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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.services.watcher.ImageFileWatcher;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel displayed on the library to select the list of images.
 * <p/>
 * @author Michael
 */
public class ImageListPanel extends BorderPane {

    private final TilePane imageList;
    private String dir;
    private Thread t;

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
        imageList.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent t) {
                t.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        imageList.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent t) {
                if (t.getGestureSource() == null) {
                    Clipboard cb = Clipboard.getSystemClipboard();
                    if (cb.hasFiles()) {
                        List<File> files = cb.getFiles();
                        for (File f : files) {
                            if (Utils.fileIsImage(f) && !f.isDirectory()) {
                                try {
                                    Files.copy(f.getAbsoluteFile().toPath(), Paths.get(getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
        updateImages();
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(imageList);
        setCenter(scroll);
    }

    /**
     * Returns the absolute path of the currently selected directory
     *
     */
    public String getDir() {
        return dir;
    }

    /**
     * Refresh the contents of this image list panel.
     */
    public void refresh() {
        updateImages();
    }

    /**
     * Add the files.
     * <p/>
     */
    private void updateImages() {
        imageList.getChildren().clear();
        final File[] files = new File(dir).listFiles();
        new Thread() {
            @Override
            public void run() {
                for (final File file : files) {
                    if (Utils.fileIsImage(file) && !file.isDirectory()) {
                        final ImageView view = new ImageView(new Image("file:" + file, 72, 72, false, true));
                        view.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent t) {
                                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(new ImageDisplayable(file));
                            }
                        });
                        view.setOnDragDetected(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent t) {
                                Dragboard db = startDragAndDrop(TransferMode.ANY);
                                ClipboardContent content = new ClipboardContent();
                                content.putString(file.getAbsolutePath());
                                db.setContent(content);
                                t.consume();
                            }
                        });
                        //setupHover(view);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                imageList.getChildren().add(view);
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private void setupHover(final ImageView view) {
        final double SECONDS = 0.2;
        final double SCALE = 2;

        view.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (view.getScaleX() == 1) {
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

    public void changeDir(File absoluteFile) {
        dir = absoluteFile.getAbsolutePath();
        ImageFileWatcher.get().changeDir(absoluteFile);
    }
}