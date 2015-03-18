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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.ImageManager;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel displayed on the library to select the list of images.
 * <p/>
 * @author Michael
 */
public class ImageListPanel extends BorderPane {

    private static final String BORDER_STYLE_SELECTED = "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_DESELECTED = "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private final TilePane imageList;
    private String dir;
    private Thread updateThread;

    /**
     * Create a new image list panel.
     * <p/>
     * @param dir the directory to use.
     */
    public ImageListPanel(String dir) {
        this.dir = dir;
        imageList = new TilePane();
        imageList.setAlignment(Pos.CENTER);
        imageList.setHgap(15);
        imageList.setVgap(15);
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
                if(t.getGestureSource() == null) {
                    Clipboard cb = t.getDragboard();
                    if(cb.hasFiles()) {
                        List<File> files = cb.getFiles();
                        for(File f : files) {
                            if(Utils.fileIsImage(f) && !f.isDirectory()) {
                                try {
                                    Files.copy(f.getAbsoluteFile().toPath(), Paths.get(getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                                }
                                catch(IOException ex) {
                                    LoggerUtils.getLogger().log(Level.WARNING, "Could not copy file into ImagePanel through system drag and drop.", ex);
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
     * <p/>
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
        if(updateThread != null && updateThread.isAlive()) {
            return;
        }
        updateThread = new Thread() {
            @Override
            public void run() {
                for(final File file : files) {
                    if(Utils.fileIsImage(file) && !file.isDirectory()) {
                        final HBox viewBox = new HBox();
                        final ImageView view = new ImageView(ImageManager.INSTANCE.getImage(file.toURI().toString(), 160, 90, false));
                        view.setPreserveRatio(true);
                        view.setFitWidth(160);
                        view.setFitHeight(90);
                        view.setOnMouseClicked((MouseEvent t) -> {
                            if(t.getButton() == MouseButton.PRIMARY && t.getClickCount() > 1) {
                                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(new ImageDisplayable(file));
                            }
                            else if(t.getButton() == MouseButton.SECONDARY) {
                                ContextMenu removeMenu = new ContextMenu();
                                MenuItem removeItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("remove.image.text"));
                                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent t) {
                                        final boolean[] reallyDelete = new boolean[]{false};
                                        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.image.title"),
                                                LabelGrabber.INSTANCE.getLabel("delete.image.confirmation"))
                                                .addYesButton(new EventHandler<ActionEvent>() {
                                                    @Override
                                                    public void handle(ActionEvent t) {
                                                        reallyDelete[0] = true;
                                                    }
                                                })
                                                .addNoButton(new EventHandler<ActionEvent>() {
                                                    @Override
                                                    public void handle(ActionEvent t) {
                                                    }
                                                }).build().showAndWait();
                                        if(reallyDelete[0]) {
                                            file.delete();
                                            imageList.getChildren().remove(viewBox);
                                        }
                                    }
                                });
                                removeMenu.getItems().add(removeItem);
                                removeMenu.show(view, t.getScreenX(), t.getScreenY());
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
                        viewBox.getChildren().add(view);
                        setupHover(viewBox);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                imageList.getChildren().add(viewBox);
                            }
                        });
                    }
                }
            }
        };
        updateThread.start();
    }

    private void setupHover(final Node view) {
        view.setStyle(BORDER_STYLE_DESELECTED);
        view.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                view.setStyle(BORDER_STYLE_SELECTED);
            }
        });
        view.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                view.setStyle(BORDER_STYLE_DESELECTED);
            }
        });
    }

    public void changeDir(File absoluteFile) {
        dir = absoluteFile.getAbsolutePath();
    }
}