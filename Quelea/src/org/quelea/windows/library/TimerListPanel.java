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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.timer.TimerIO;

/**
 * The panel displayed on the library to select the list of videos..
 * <p/>
 * @author Ben
 */
public class TimerListPanel extends BorderPane {

    private static final String BORDER_STYLE_SELECTED = "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_DESELECTED = "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private final ListView<TimerDisplayable> timerList;
    private String dir;
    private Thread updateThread;

    /**
     * Create a new video list panel.
     * <p/>
     * @param dir the directory to use.
     */
    public TimerListPanel(String dir) {
        this.dir = dir;
        timerList = new ListView<>();
        timerList.setOnDragOver((DragEvent t) -> {
            t.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });
        timerList.setOnDragDropped((DragEvent t) -> {
            if (t.getGestureSource() == null) {
                Clipboard cb = t.getDragboard();
                if (cb.hasFiles()) {
                    List<File> files = cb.getFiles();
                    for (File f : files) {
                        if (Utils.fileIsTimer(f) && !f.isDirectory()) {
                            try {
                                Files.copy(f.getAbsoluteFile().toPath(), Paths.get(getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                            } catch (IOException ex) {
                                LoggerUtils.getLogger().log(Level.WARNING, "Could not copy file into TimerPanel through system drag and drop.", ex);
                            }
                            updateTimers();
                        }
                    }
                }
            }
        });
        Callback<ListView<TimerDisplayable>, ListCell<TimerDisplayable>> callback = (ListView<TimerDisplayable> p) -> {
            final TextFieldListCell<TimerDisplayable> cell = new TextFieldListCell<>(new StringConverter<TimerDisplayable>() {
                @Override
                public String toString(TimerDisplayable timer) {
                    return timer.getName();
                }

                @Override
                public TimerDisplayable fromString(String string) {
                    //Implementation not needed.
                    return null;
                }
            });
//                cell.setOnDragDetected(new EventHandler<MouseEvent>() {
//
//                    @Override
//                    public void handle(MouseEvent event) {
//                        TimerDisplayable displayable = cell.getItem();
//                        if (displayable != null) {
//                            Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
//                            ClipboardContent content = new ClipboardContent();
//                            content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, displayable);
//                            db.setContent(content);
//                        }
//                        event.consume();
//                    }
//                });
            return cell;
        };

        timerList.setOnMouseClicked((MouseEvent t) -> {
            if (t.getClickCount() == 2) {
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(getSelectedValue());
            }
        });

        timerList.setCellFactory(callback);
        updateTimers();
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(timerList);
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
     * Refresh the contents of this video list panel.
     */
    public void refresh() {
        updateTimers();
    }

    /**
     * Add the files.
     * <p/>
     */
    private void updateTimers() {
        timerList.getItems().clear();
        final File[] files = new File(dir).listFiles();
        if (updateThread != null && updateThread.isAlive()) {
            return;
        }
        updateThread = new Thread() {
            @Override
            public void run() {
                if (files != null) {
                    for (final File file : files) {
                        Platform.runLater(() -> {
                            TimerDisplayable td = TimerIO.timerFromFile(file);
                            td.setName(file.getName().substring(0, file.getName().length() - 4) + " (" + td.secondsToTime(td.getSeconds()) + ")");
                            timerList.getItems().add(td);
                        });

//                    if (Utils.fileIsTimer(file) && !file.isDirectory()) {
//                        Platform.runLater(() -> {
//                            final HBox viewBox = new HBox();
//                            Image im = Utils.getVidBlankImage(file);
//                            try {
//                                BufferedImage bi = FrameGrab.getFrame(file, 0);
//                                BufferedImage resized = new BufferedImage(160, 90, bi.getType());
//                                Graphics2D g = resized.createGraphics();
//                                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                                g.drawImage(bi, 0, 0, 160, 90, 0, 0, bi.getWidth(), bi.getHeight(), null);
//                                g.dispose();
//                                im = SwingFXUtils.toFXImage(resized, null);
//                            } catch (Exception e) {
//                                LoggerUtils.getLogger().log(Level.INFO, "Could not resize library video image");
//                            }
//                            final ImageView view = new ImageView(im);
//                            view.setPreserveRatio(true);
//                            view.setFitWidth(160);
//                            view.setFitHeight(90);
//                            view.setOnMouseClicked((MouseEvent t) -> {
//                                if (t.getButton() == MouseButton.PRIMARY && t.getClickCount() > 1) {
//                                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(new VideoDisplayable(file.getAbsolutePath()));
//                                } else if (t.getButton() == MouseButton.SECONDARY) {
//                                    ContextMenu removeMenu = new ContextMenu();
//                                    MenuItem removeItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("remove.video.text"));
//                                    removeItem.setOnAction((ActionEvent t1) -> {
//                                        final boolean[] reallyDelete = new boolean[]{false};
//                                        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.video.title"),
//                                                LabelGrabber.INSTANCE.getLabel("delete.video.confirmation")).addYesButton((ActionEvent t2) -> {
//                                                    reallyDelete[0] = true;
//                                                }).addNoButton((ActionEvent t3) -> {
//                                                }).build().showAndWait();
//                                        if (reallyDelete[0]) {
//                                            file.delete();
//                                            timerList.getChildren().remove(viewBox);
//                                        }
//                                    });
//                                    removeMenu.getItems().add(removeItem);
//                                    removeMenu.show(view, t.getScreenX(), t.getScreenY());
//                                }
//                            });
////                            view.setOnDragDetected((MouseEvent t) -> {
////                                Dragboard db = startDragAndDrop(TransferMode.ANY);
////                                ClipboardContent content = new ClipboardContent();
////                                content.putString(file.getAbsolutePath());
////                                db.setContent(content);
////                                t.consume();
////                            });
//                            viewBox.getChildren().add(view);
//                            setupHover(viewBox);
//                            timerList.getChildren().add(viewBox);
//                            });
//                    }
                    }
                }
            }
        };
        updateThread.start();
    }
//
//    private void setupHover(final Node view) {
//        view.setStyle(BORDER_STYLE_DESELECTED);
//        view.setOnMouseEntered((MouseEvent t) -> {
//            view.setStyle(BORDER_STYLE_SELECTED);
//        });
//        view.setOnMouseExited((MouseEvent t) -> {
//            view.setStyle(BORDER_STYLE_DESELECTED);
//        });
//    }

    public void changeDir(File absoluteFile) {
        dir = absoluteFile.getAbsolutePath();
    }

    private TimerDisplayable getSelectedValue() {
        return timerList.selectionModelProperty().get().getSelectedItem();
    }
}
