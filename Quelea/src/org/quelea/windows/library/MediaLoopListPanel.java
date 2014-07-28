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
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.mediaLoop.MediaLoopManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.DatabaseListener;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.PreviewPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.mediaLoop.mediaLoopCreator.MediaLoopCreatorWindow;

/**
 * The panel displayed on the library to select the list of media loops.
 * <p/>
 * @author Greg
 */
public class MediaLoopListPanel extends BorderPane {

    private static final String BORDER_STYLE_SELECTED = "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_DESELECTED = "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private final TilePane mediaLoopList;
    private Thread updateThread;
    private ObservableList<MediaLoopDisplayable> allMediaLoops;
    private MediaLoopDisplayable currentDisplayable;

    /**
     * Create a new media loop list panel.
     * <p/>
     */
    public MediaLoopListPanel() {
        mediaLoopList = new TilePane();
        mediaLoopList.setAlignment(Pos.CENTER);
        mediaLoopList.setHgap(15);
        mediaLoopList.setVgap(15);
        mediaLoopList.setOrientation(Orientation.HORIZONTAL);
        mediaLoopList.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent t) {
                t.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        mediaLoopList.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent t) {

                if (t.getGestureSource() == null) {
                    Clipboard cb = t.getDragboard();
                    if (cb.hasFiles()) {
                        List<File> files = cb.getFiles();

                        MediaLoopCreatorWindow mediaLoopCreatorWindow = QueleaApp.get().getMainWindow().getMediaLoopCreatorWindow();
                        mediaLoopCreatorWindow.resetNewMediaLoop();
                        mediaLoopCreatorWindow.getMediaLoopEditorPanel().addFiles(files);
                        mediaLoopCreatorWindow.showAndWait();

                    }
                }
            }
        });
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(mediaLoopList);
        setCenter(scroll);
        new Thread() {
            public void run() {
                refresh();
            }
        }.start();

        MediaLoopManager.get().registerDatabaseListener(new DatabaseListener() {

            @Override
            public void databaseChanged() {
                refresh();
            }
        });

    }

    /**
     * Refresh the contents of this image list panel.
     */
    public void refresh() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                updateMediaLoops();
            }
        });

    }

    /**
     * Add the files.
     * <p/>
     */
    private void updateMediaLoops() {
        mediaLoopList.getChildren().clear();
 
        allMediaLoops = FXCollections.observableArrayList(MediaLoopManager.get().getMediaLoops());
       
        if (updateThread != null && updateThread.isAlive()) {
            return;
        }
        updateThread = new Thread() {
            @Override
            public void run() {
                if (allMediaLoops == null) {
                    return;
                }
                for (final MediaLoopDisplayable mediaLoop : allMediaLoops) {
                    final VBox viewBox = new VBox();
                    final ImageView view = new ImageView(mediaLoop.getImage());
                    view.setPreserveRatio(true);
                    view.setFitWidth(160);
                    view.setFitHeight(90);

                    viewBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent t) {
                            currentDisplayable = mediaLoop;
                            if (t.getButton() == MouseButton.PRIMARY) {

                            } else if (t.getButton() == MouseButton.SECONDARY) {

                                ContextMenu removeMenu = new ContextMenu();

                                MenuItem addToSchedule = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.add.to.schedule.text"), new ImageView(new Image("file:icons/add.png", 16, 16, false, true)));
                                addToSchedule.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent t) {
                                        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(mediaLoop);
                                    }
                                });
                                MenuItem preview = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.preview.mediaLoop.text"), new ImageView(new Image("file:icons/prev.png", 16, 16, false, true)));
                                preview.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent t) {
                                        LibraryPanel libraryPanel = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel();
                                        PreviewPanel prevPanel = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel();
                                        prevPanel.setDisplayable(mediaLoop, 0);
                                    }
                                });
                                MenuItem editDB = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.edit.mediaLoop.text"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
                                editDB.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent t) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                MediaLoopCreatorWindow mediaLoopEditWindow = QueleaApp.get().getMainWindow().getMediaLoopCreatorWindow();
                                                if (mediaLoop != null) {
                                                    mediaLoopEditWindow.resetEditMediaLoop(mediaLoop);
                                                    mediaLoopEditWindow.showAndWait();

                                                    mediaLoopEditWindow.toFront();
                                                    refresh();
                                                }
                                            }
                                        });
                                    }
                                });
                                MenuItem removeItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("remove.mediaLoop.text"), new ImageView(new Image("file:icons/removedb.png", 16, 16, false, true)));;
                                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent t) {
                                        final boolean[] reallyDelete = new boolean[]{false};
                                        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.mediaLoop.title"),
                                                LabelGrabber.INSTANCE.getLabel("delete.mediaLoop.confirmation"))
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
                                        if (reallyDelete[0]) {
                                            MediaLoopManager.get().removeMediaLoop(mediaLoop);
                                            mediaLoopList.getChildren().remove(viewBox);
                                        }
                                    }
                                });

                                removeMenu.getItems().add(addToSchedule);
                                removeMenu.getItems().add(preview);
                                removeMenu.getItems().add(editDB);
                                removeMenu.getItems().add(removeItem);
                                removeMenu.show(view, t.getScreenX(), t.getScreenY());
                            }
                        }
                    });

                    viewBox.setOnDragDetected(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent t) {
                            Dragboard db = startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            content.put(MediaLoopDisplayable.MEDIA_LOOP_DISPLAYABLE_FORMAT, mediaLoop);
                            db.setContent(content);
                            t.consume();
                        }
                    });
                    viewBox.getChildren().add(view);
                    viewBox.getChildren().add(new Label(mediaLoop.getPreviewText()));
                    viewBox.setAlignment(Pos.CENTER);
                    viewBox.setFillWidth(true);
                    setupHover(viewBox);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            mediaLoopList.getChildren().add(viewBox);
                        }
                    });
                }
            }

        };
        updateThread.start();
    }

    /**
     * setup what happens when the mouse hovers over
     *
     * @param view the node which the mouse would be hovering over
     */
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

    /**
     * get all the media loops represented in this panel
     *
     * @return the media loops as an observable list.
     */
    public ObservableList<MediaLoopDisplayable> getAllMediaLoops() {
        return allMediaLoops;
    }

    /**
     *
     * get the current MediaLoopDisplayable
     *
     * @return the current displayable
     */
    public MediaLoopDisplayable getCurrentDisplayable() {
        return currentDisplayable;
    }
}
