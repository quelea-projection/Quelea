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
package org.quelea.windows.main.schedule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.quelea.data.ImageBackground;
import org.quelea.data.Schedule;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.library.Constraint;
import org.quelea.windows.library.DisplayableListCell;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * <p/>
 * @author Michael
 */
public class ScheduleList extends StackPane {

    private ListView<Displayable> listView;
    private Schedule schedule;
    private final ScheduleSongPopupMenu popupMenu;
    private Rectangle markerRect;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private ArrayList<ListCell<Displayable>> cells = new ArrayList<>();
    private int localDragIndex = -1;

    /**
     * A direction; either up or down. Used for rearranging the order of items
     * in the service.
     */
    public enum Direction {

        UP, DOWN
    }

    /**
     * Create a new schedule list.
     */
    public ScheduleList() {
        setAlignment(Pos.TOP_LEFT);
        listView = new ListView<>();
        getChildren().add(listView);
        markerRect = new Rectangle(200, 3, Color.GRAY);
        markerRect.setVisible(false);
        getChildren().add(markerRect);
        markerRect.toFront();
        popupMenu = new ScheduleSongPopupMenu();
        Callback<ListView<Displayable>, ListCell<Displayable>> callback = new Callback<ListView<Displayable>, ListCell<Displayable>>() {
            @Override
            public ListCell<Displayable> call(ListView<Displayable> p) {

                final ListCell<Displayable> listCell = new ListCell<Displayable>() {
                    @Override
                    public void updateItem(Displayable item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                            setGraphic(null);
                        }
                        else {
                            setGraphic(item.getPreviewIcon());
                            setText(item.getPreviewText());
                        }
                    }
                };
                cells.add(listCell);
                listCell.setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        localDragIndex = listCell.getIndex();
                        Dragboard db = listCell.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, listCell.getItem());
                        db.setContent(content);
                        event.consume();
                    }
                });
                listCell.setOnDragEntered(new EventHandler<DragEvent>() {

                    @Override
                    public void handle(DragEvent event) {
                        int size = listView.getItems().size();
                        if(listCell.isEmpty()) {
                            if(event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null || event.getDragboard().getString() != null) {
                                for(ListCell<Displayable> cell : cells) {
                                    if(cell.isVisible() && cell.getIndex() == size) {
                                        markerRect.setTranslateX(cell.getLayoutX() + cell.getTranslateX());
                                        markerRect.setTranslateY(cell.getLayoutY() + cell.getTranslateY());
                                        markerRect.setVisible(true);
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            if(event.getDragboard().getString() != null) {
                                if(listCell.getItem() instanceof SongDisplayable) {
                                    listCell.setStyle("-fx-background-color: #99cccc;");
                                }
                            }
                            else if(event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                                markerRect.setTranslateX(listCell.getLayoutX() + listCell.getTranslateX());
                                markerRect.setTranslateY(listCell.getLayoutY() + listCell.getTranslateY());
                                markerRect.setVisible(true);
                            }
                        }
                    }
                });
                listCell.setOnDragExited(new EventHandler<DragEvent>() {

                    @Override
                    public void handle(DragEvent t) {
                        listCell.setStyle("");
                        markerRect.setVisible(false);
                    }
                });
                listCell.setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        if(event.getDragboard().getString() != null || event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                            event.acceptTransferModes(TransferMode.ANY);
                        }
                    }
                });
                listCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        listCell.setStyle("-fx-border-color: rgb(0, 0, 0);-fx-border-width: 0,0,0,0;");
                        String imageLocation = event.getDragboard().getString();
                        if(imageLocation != null) {
                            if(listCell.isEmpty()) {
                                ImageDisplayable img = new ImageDisplayable(new File(imageLocation));
                                add(img);
                            }
                            else {
                                Displayable d = listCell.getItem();
                                if(d instanceof SongDisplayable) {
                                    SongDisplayable songDisplayable = (SongDisplayable) d;
                                    ThemeDTO theme = songDisplayable.getTheme();
                                    ThemeDTO newTheme = new ThemeDTO(theme.getSerializableFont(), theme.getFontPaint(), new ImageBackground(new File(imageLocation).getName()), theme.getShadow(), theme.getSerializableFont().isBold(), theme.getSerializableFont().isItalic(), theme.getTextPosition());
                                    for(TextSection section : songDisplayable.getSections()) {
                                        section.setTheme(newTheme);
                                    }
                                    songDisplayable.setTheme(newTheme);
                                    Utils.updateSongInBackground(songDisplayable, true, false);
                                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().refresh();
                                }
                            }
                        }
                        final SongDisplayable displayable = (SongDisplayable) event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT);
                        if(displayable != null) {
                            if(listCell.getIndex() != localDragIndex) {
                                if(localDragIndex > -1) {
                                    getItems().remove(localDragIndex);
                                    localDragIndex = -1;
                                }
                                if(listCell.isEmpty()) {
                                    add(displayable);
                                    listView.getSelectionModel().selectLast();
                                }
                                else {
                                    listView.itemsProperty().get().add(listCell.getIndex(), displayable);
                                    listView.getSelectionModel().select(listCell.getIndex());
                                }
                                listView.requestFocus();
                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(displayable, 0);
                                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                                    }
                                });
                            }
                        }
                        event.consume();
                    }
                });
                return listCell;
            }
        };
        listView.setCellFactory(DisplayableListCell.<Displayable>forListView(popupMenu, callback, new Constraint<Displayable>() {
            @Override
            public boolean isTrue(Displayable d) {
                return d instanceof SongDisplayable;
            }
        }));
        schedule = new Schedule();
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCharacter().equals(" ")) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().requestFocus();
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().selectFirstLyric();
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                }
            }
        });
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode() == KeyCode.DELETE) {
                    new RemoveScheduleItemActionHandler().handle(null);
                }
            }
        });
    }

    public void add(Displayable displayable) {
        if(!Platform.isFxApplicationThread()) {
            LOGGER.log(Level.WARNING, "Not on the platform thread!", new RuntimeException("DEBUG EX"));
        }
        listView.itemsProperty().get().add(displayable);
    }

    /**
     * Get the current schedule in use on this list.
     * <p/>
     * @return the current schedule in use on this list.
     */
    public Schedule getSchedule() {
        boolean equal = true;
        if(listView.itemsProperty().get().size() == schedule.getSize()) {
            for(int i = 0; i < listView.itemsProperty().get().size(); i++) {
                Displayable displayable = listView.itemsProperty().get().get(i);
                if(displayable != null && !displayable.equals(schedule.getDisplayable(i))) {
                    equal = false;
                }
            }
        }
        else {
            equal = false;
        }
        if(equal) {
            return schedule;
        }
        schedule.clear();
        for(int i = 0; i < listView.itemsProperty().get().size(); i++) {
            schedule.add(listView.itemsProperty().get().get(i));
        }
        return schedule;
    }

    /**
     * Erase everything in the current schedule and set the contents of this
     * list to the current schedule.
     * <p/>
     * @param schedule the schedule.
     */
    public void setSchedule(Schedule schedule) {
        clearSchedule();
        for(Displayable displayable : schedule) {
            if(displayable instanceof SongDisplayable) {
                ((SongDisplayable) displayable).matchID();
            }
            listView.itemsProperty().get().add(displayable);
        }
        this.schedule = schedule;
    }

    /**
     * Clear the current schedule without warning.
     */
    public void clearSchedule() {
        listView.itemsProperty().get().clear();
    }

    /**
     * Get the popup menu on this schedule list.
     * <p/>
     * @return the popup menu.
     */
    public ScheduleSongPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Get the selection model of the underlying list.
     * <p>
     * @return the selection model of the listview.
     */
    public MultipleSelectionModel<Displayable> getSelectionModel() {
        return listView.getSelectionModel();
    }

    /**
     * Get the items property of the underlying list.
     * <p>
     * @return the items property of the underlying list.
     */
    public ObjectProperty<ObservableList<Displayable>> itemsProperty() {
        return listView.itemsProperty();
    }

    /**
     * Return the items of the underlying list.
     * <p>
     * @return the items of the underlying list.
     */
    public ObservableList<Displayable> getItems() {
        return listView.getItems();
    }

    /**
     * Determine whether the schedule list is empty.
     * <p/>
     * @return true if it's empty, false otherwise.
     */
    public boolean isEmpty() {
        return listView.itemsProperty().get().isEmpty();
    }

    /**
     * Remove the currently selected item in the list, or do nothing if there is
     * no selected item.
     */
    public void removeCurrentItem() {
        int selectedIndex = listView.selectionModelProperty().get().getSelectedIndex();
        if(selectedIndex != -1) {
            Displayable d = listView.selectionModelProperty().get().getSelectedItem();
            Displayable live = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable();
            if(d == live && QueleaProperties.get().getClearLiveOnRemove()) {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().removeDisplayable();
                VLCWindow.INSTANCE.stop();
            }
            Displayable preview = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if(d == preview) {
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().removeDisplayable();
            }
            if(d == null) {
                LOGGER.log(Level.WARNING, "Tried to remove null from schedule?");
            }
            else {
                d.dispose();
            }
            listView.itemsProperty().get().remove(selectedIndex);
        }
    }

    /**
     * Move the currently selected item in the list in the specified direction.
     * <p/>
     * @param direction the direction to move the selected item.
     */
    public void moveCurrentItem(Direction direction) {
        int selectedIndex = listView.selectionModelProperty().get().getSelectedIndex();
        if(selectedIndex == -1) { //Nothing selected
            return;
        }
        if(direction == Direction.UP && selectedIndex > 0) {
            listView.selectionModelProperty().get().clearSelection();
            Collections.swap(listView.itemsProperty().get(), selectedIndex, selectedIndex - 1);
            listView.selectionModelProperty().get().select(selectedIndex - 1);
        }
        if(direction == Direction.DOWN && selectedIndex < listView.itemsProperty().get().size() - 1) {
            listView.selectionModelProperty().get().clearSelection();
            Collections.swap(listView.itemsProperty().get(), selectedIndex, selectedIndex + 1);
            listView.selectionModelProperty().get().select(selectedIndex + 1);
        }
        requestFocus();
    }
}
