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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.RemoveTimerActionHandler;
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
                    return timer.getName() + " (" + timer.secondsToTime(timer.getSeconds()) + ")";
                }

                @Override
                public TimerDisplayable fromString(String string) {
                    //Implementation not needed.
                    return null;
                }
            });
            return cell;
        };

        timerList.setOnMouseClicked((MouseEvent t) -> {
            if (t.getClickCount() == 2) {
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(getSelectedValue());
            }
        });

        ContextMenu removeMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("remove.timer.text"), new ImageView(new Image("file:icons/removedb.png", 16, 16, false, false)));
        removeItem.setOnAction(new RemoveTimerActionHandler());
        removeMenu.getItems().add(removeItem);
        timerList.setCellFactory(DisplayableListCell.forListView(removeMenu, callback, null));

        updateTimers();
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(timerList);
        setCenter(scroll);
    }

    /**
     * Returns the absolute path of the currently selected directory
     * <p/>
     * @return Returns the directory path
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
                            if (td != null) {
                                timerList.getItems().add(td);
                            }
                        });
                    }
                }
            }
        };
        updateThread.start();
    }

    public void changeDir(File absoluteFile) {
        dir = absoluteFile.getAbsolutePath();
    }

    private TimerDisplayable getSelectedValue() {
        return timerList.selectionModelProperty().get().getSelectedItem();
    }

    public ListView<TimerDisplayable> getListView() {
        return timerList;
    }
}
