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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.javafx.dialog.Dialog;
import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel displayed on the library to select the list of videos..
 * <p/>
 * @author Ben
 */
public class VideoListPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final String BORDER_STYLE_SELECTED = "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_DESELECTED = "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private final TilePane videoList;
    private String dir;
    private Thread updateThread;
    private static final Image BLANK = new Image("file:icons/vid preview.png");

    /**
     * Create a new video list panel.
     * <p/>
     * @param dir the directory to use.
     */
    public VideoListPanel(String dir) {
        this.dir = dir;
        videoList = new TilePane();
        videoList.setAlignment(Pos.CENTER);
        videoList.setHgap(15);
        videoList.setVgap(15);
        videoList.setOrientation(Orientation.HORIZONTAL);
        videoList.setOnDragOver((DragEvent t) -> {
            t.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });
        videoList.setOnDragDropped((DragEvent t) -> {
            if (t.getGestureSource() == null) {
                Clipboard cb = t.getDragboard();
                if (cb.hasFiles()) {
                    List<File> files = cb.getFiles();
                    for (File f : files) {
                        if (Utils.fileIsVideo(f) && !f.isDirectory()) {
                            try {
                                Files.copy(f.getAbsoluteFile().toPath(), Paths.get(getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                            } catch (IOException ex) {
                                LoggerUtils.getLogger().log(Level.WARNING, "Could not copy file into VideoPanel through system drag and drop.", ex);
                            }
                            updateVideos();
                        }
                    }
                }
            }
        });
        updateVideos();
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(videoList);
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
        updateVideos();
    }

    /**
     * Add the files.
     * <p/>
     */
    private void updateVideos() {
        videoList.getChildren().clear();
        final File[] files = new File(dir).listFiles();
        if (updateThread != null && updateThread.isAlive()) {
            return;
        }
        ExecutorService previewService = Executors.newSingleThreadExecutor();
        updateThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (Utils.fileIsVideo(file) && !file.isDirectory()) {
                        final ImageView view = new ImageView();
                        final Label fileLabel = new Label(trim17(file.getName()));
                        previewService.submit(() -> {
                            try {
                                LOGGER.log(Level.INFO, "Grabbing frame for {0}", file);

                                try {
                                    FileChannelWrapper ch = NIOUtils.readableFileChannel(file.getAbsolutePath());
                                    MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
                                    DemuxerTrack video_track = demuxer.getVideoTrack();
                                    int totalSeconds = (int)video_track.getMeta().getTotalDuration();
                                    int hours = totalSeconds / 3600;
                                    int minutes = (totalSeconds % 3600) / 60;
                                    int seconds = totalSeconds % 60;
                                    DecimalFormat formatter = new DecimalFormat("00");
                                    Platform.runLater(() -> {
                                        fileLabel.setText(fileLabel.getText() + " - " + formatter.format(hours) + ":" + formatter.format(minutes) + ":" + formatter.format(seconds));
                                    });
                                } catch (Exception ex) {
                                    LOGGER.log(Level.INFO, "Couldn't get video length", ex);
                                }

                                BufferedImage bi = AWTFrameGrab.getFrame(file, 0);
                                BufferedImage resized = new BufferedImage(160, 90, bi.getType());
                                Graphics2D g = resized.createGraphics();
                                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                g.drawImage(bi, 0, 0, 160, 90, 0, 0, bi.getWidth(), bi.getHeight(), null);
                                g.dispose();
                                bi.flush();
                                Platform.runLater(() -> {
                                    view.setImage(SwingFXUtils.toFXImage(resized, null));
                                });
                                LOGGER.log(Level.INFO, "Grabbed frame for {0}", file);
                                resized.flush();
                            } catch (Exception e) {
                                LOGGER.log(Level.INFO, "Could not resize library video image", e);
                                Platform.runLater(() -> {
                                    view.setImage(BLANK);
                                });
                            }
                        });
                        if (i == files.length - 1) {
                            previewService.shutdown();
                        }
                        Platform.runLater(() -> {
                            final VBox viewBox = new VBox();
                            viewBox.setAlignment(Pos.CENTER);
                            view.setPreserveRatio(true);
                            view.setFitWidth(160);
                            view.setFitHeight(90);
                            view.setOnMouseClicked((MouseEvent t) -> {
                                if (t.getButton() == MouseButton.PRIMARY && t.getClickCount() > 1) {
                                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(new VideoDisplayable(file.getAbsolutePath()));
                                } else if (t.getButton() == MouseButton.SECONDARY) {
                                    ContextMenu removeMenu = new ContextMenu();
                                    MenuItem removeItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("remove.video.text"));
                                    removeItem.setOnAction((ActionEvent t1) -> {
                                        final boolean[] reallyDelete = new boolean[]{false};
                                        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.video.title"),
                                                LabelGrabber.INSTANCE.getLabel("delete.video.confirmation")).addYesButton((ActionEvent t2) -> {
                                            reallyDelete[0] = true;
                                        }).addNoButton((ActionEvent t3) -> {
                                        }).build().showAndWait();
                                        if (reallyDelete[0]) {
                                            file.delete();
                                            videoList.getChildren().remove(viewBox);
                                        }
                                    });
                                    removeMenu.getItems().add(removeItem);
                                    removeMenu.show(view, t.getScreenX(), t.getScreenY());
                                }
                            });
//                            view.setOnDragDetected((MouseEvent t) -> {
//                                Dragboard db = startDragAndDrop(TransferMode.ANY);
//                                ClipboardContent content = new ClipboardContent();
//                                content.putString(file.getAbsolutePath());
//                                db.setContent(content);
//                                t.consume();
//                            });
                            viewBox.getChildren().add(view);
                            viewBox.getChildren().add(fileLabel);
                            setupHover(viewBox, file.getName());
                            videoList.getChildren().add(viewBox);
                        });
                    }
                }
            }
        };
        updateThread.start();
    }

    private void setupHover(final Node view, String fileName) {
        Tooltip tt = new Tooltip(fileName);
        view.setStyle(BORDER_STYLE_DESELECTED);
        view.setOnMouseEntered((MouseEvent t) -> {
            view.setStyle(BORDER_STYLE_SELECTED);
            Bounds b = view.localToScreen(view.getLayoutBounds());
            tt.show(view, b.getMaxX(), b.getMinY());
        });
        view.setOnMouseExited((MouseEvent t) -> {
            view.setStyle(BORDER_STYLE_DESELECTED);
            tt.hide();
        });
    }

    public void changeDir(File absoluteFile) {
        dir = absoluteFile.getAbsolutePath();
    }
    
    private static String trim17(String toTrim) {
        if(toTrim.length()>17) {
            return toTrim.substring(0,16) + "..";
        }
        return toTrim;
    }
}
