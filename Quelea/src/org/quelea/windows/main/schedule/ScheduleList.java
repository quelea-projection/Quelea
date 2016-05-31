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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import javafx.scene.control.SelectionMode;
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
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.Utils;
import org.quelea.windows.library.Constraint;
import org.quelea.windows.library.DisplayableListCell;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.main.actionhandlers.AddPdfActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler;
import org.quelea.windows.multimedia.VLCWindow;
import org.quelea.windows.stage.StageDrawer;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * <p/>
 * @author Michael
 */
public class ScheduleList extends StackPane {

    private ListView<Displayable> listView;
    private Schedule schedule;
    private Rectangle markerRect;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private ArrayList<ListCell<Displayable>> cells = new ArrayList<>();
    private int localDragIndex = -1;
    private Displayable tempDisp = null;

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
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        getChildren().add(listView);
        markerRect = new Rectangle(200, 3, Color.GRAY);
        markerRect.setVisible(false);
        getChildren().add(markerRect);
        markerRect.toFront();
        Callback<ListView<Displayable>, ListCell<Displayable>> callback = new Callback<ListView<Displayable>, ListCell<Displayable>>() {
            @Override
            public ListCell<Displayable> call(ListView<Displayable> p) {

                final ListCell<Displayable> listCell = new ListCell<Displayable>() {
                    @Override
                    public void updateItem(Displayable item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(new ScheduleListNode(item));
                            setText(null);
                        }
                        if (item instanceof SongDisplayable || item instanceof BiblePassage || item instanceof TimerDisplayable) {
                            setContextMenu(new SchedulePopupMenu(item));
                        }
                    }
                };
                cells.add(listCell);
                listCell.setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {

                        if (listCell.getItem() != null) {
                            localDragIndex = listCell.getIndex();
                            Dragboard db = listCell.startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            if (listCell.getItem() instanceof SongDisplayable) {
                                content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, listCell.getItem());
                            } else {
                                content.putString("tempdisp");
                                tempDisp = listCell.getItem();
                            }

                            db.setContent(content);
                            event.consume();
                        }
                    }
                });
                listCell.setOnDragEntered(new EventHandler<DragEvent>() {

                    @Override
                    public void handle(DragEvent event) {
                        int size = listView.getItems().size();
                        if (listCell.isEmpty()) {
                            if (event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null
                                    || event.getDragboard().getString() != null) {
                                for (ListCell<Displayable> cell : cells) {
                                    if (cell.isVisible() && cell.getIndex() == size) {
                                        markerRect.setTranslateX(cell.getLayoutX() + cell.getTranslateX());
                                        markerRect.setTranslateY(cell.getLayoutY() + cell.getTranslateY());
                                        markerRect.setVisible(true);
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (event.getDragboard().getString() != null) {
                                if (event.getDragboard().getString().equals("tempdisp")) {
                                    markerRect.setTranslateX(listCell.getLayoutX() + listCell.getTranslateX());
                                    markerRect.setTranslateY(listCell.getLayoutY() + listCell.getTranslateY());
                                    markerRect.setVisible(true);
                                } else {
                                    if (listCell.getItem() instanceof SongDisplayable) {
                                        listCell.setStyle("-fx-background-color: #99cccc;");
                                    } else {
                                        markerRect.setTranslateX(listCell.getLayoutX() + listCell.getTranslateX());
                                        markerRect.setTranslateY(listCell.getLayoutY() + listCell.getTranslateY());
                                        markerRect.setVisible(true);
                                    }
                                }
                            } else if (event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
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
                        if (event.getDragboard().getString() != null || event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                            event.acceptTransferModes(TransferMode.ANY);
                        }
                    }
                });
                listCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        dragDropped(event, listCell);
                    }
                });
                return listCell;
            }
        };
        listView.setCellFactory(DisplayableListCell.<Displayable>forListView(null, callback, new Constraint<Displayable>() {
            @Override
            public boolean isTrue(Displayable d) {
                return d instanceof SongDisplayable || d instanceof BiblePassage || d instanceof TimerDisplayable;
            }
        }));
        listView.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getString() != null || event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                    event.acceptTransferModes(TransferMode.ANY);
                }
                if (event.getDragboard().hasFiles()) {
                    event.getDragboard().getFiles().stream().filter((file) -> (Utils.fileIsImage(file) || Utils.fileIsVideo(file) || file.getPath().matches("(.*)(pdf|ppt|pptx)") && !file.isDirectory())).forEach((file) -> {
                        event.acceptTransferModes(TransferMode.ANY);
                    });
                }
            }
        });
        listView.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                dragDropped(event, null);
            }
        });
        schedule = new Schedule();
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCharacter().equals(" ")) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().requestFocus();
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().selectFirstLyric();
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                }
            }
        });
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.DELETE) {
                    new RemoveScheduleItemActionHandler().handle(null);
                }
            }
        });
    }

    public void add(Displayable displayable) {
        if (!Platform.isFxApplicationThread()) {
            LOGGER.log(Level.WARNING, "Not on the platform thread!", new RuntimeException("DEBUG EX"));
        }
        listView.itemsProperty().get().add(displayable);
    }

    private void dragDropped(DragEvent event, ListCell<Displayable> listCell) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            db.getFiles().stream().forEach((file) -> {
                if (Utils.fileIsImage(file)) {
                    add(new ImageDisplayable(file));
                } else if (Utils.fileIsVideo(file)) {
                    add(new VideoDisplayable(file.getPath()));
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.getVidBlankImage(file); //Cache preview image
                        }
                    }.start();
                } else if (file.getPath().matches("(.*)(ppt|pptx)")) {
                    List<File> presentation = new ArrayList<>();
                    presentation.add(file);
                    new AddPowerpointActionHandler().addPresentation(presentation);
                } else if (file.getPath().contains(".pdf")) {
                    List<File> pdf = new ArrayList<>();
                    pdf.add(file);
                    new AddPdfActionHandler().addPDF(pdf);
                }
            });
        }
        if (listCell != null) {
            listCell.setStyle("-fx-border-color: rgb(0, 0, 0);-fx-border-width: 0,0,0,0;");
        }
        String imageLocation = event.getDragboard().getString();
        boolean useTempDisp = false;
        if (imageLocation != null) {
            useTempDisp = imageLocation.equals("tempdisp");
            if (!Utils.isInDir(QueleaProperties.get().getImageDir(), new File(imageLocation)) && !useTempDisp) {
                try {
                    Utils.copyFile(new File(imageLocation), new File(QueleaProperties.get().getImageDir(), new File(imageLocation).getName()));
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't copy image file", ex);
                }
            }
            boolean isSong = true;
            isSong = !(listCell == null || listCell.isEmpty());
            if (isSong) {
                if (!(listCell.getItem() instanceof TextDisplayable)) {
                    isSong = false;
                }
            }
            if (!isSong && !useTempDisp) {
                ImageDisplayable img = new ImageDisplayable(new File(imageLocation));
                useTempDisp = true;
                tempDisp = img;
            } else {
                if (useTempDisp) {

                    //potentially check if the displayable is an image and then add it to theme of song.
                    //Was not added due to the thought that it could get confusing if someone added image to 
                    //schedule and then it all of a sudden disappeared if they were trying to rearrange the schedule.
                } else {
                    Displayable d = listCell.getItem();
                    if (d instanceof TextDisplayable) {
                        TextDisplayable textDisplayable = (TextDisplayable) d;
                        ThemeDTO theme = textDisplayable.getTheme();
                        SerializableDropShadow dropShadow = theme.getShadow();
                        if (dropShadow == null || (dropShadow.getColor().equals(Color.WHITE) && dropShadow.getOffsetX() == 0 && dropShadow.getOffsetY() == 0)) {
                            dropShadow = new SerializableDropShadow(Color.BLACK, 3, 3, 2, 0, true);
                        }
                        ThemeDTO newTheme = new ThemeDTO(theme.getSerializableFont(), theme.getFontPaint(), theme.getTranslateSerializableFont(), theme.getTranslateFontPaint(), new ImageBackground(new File(imageLocation).getName()), dropShadow, theme.getSerializableFont().isBold(), theme.getSerializableFont().isItalic(), theme.getTranslateSerializableFont().isBold(), theme.getTranslateSerializableFont().isItalic(), theme.getTextPosition(), theme.getTextAlignment());
                        for (TextSection section : textDisplayable.getSections()) {
                            section.setTheme(newTheme);
                        }
                        textDisplayable.setTheme(newTheme);
                        if (d instanceof SongDisplayable) {
                            SongDisplayable sd = (SongDisplayable) d;
                            Utils.updateSongInBackground(sd, true, false);
                        }
                        if (QueleaProperties.get().getPreviewOnImageUpdate()) {
                            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getListView().getSelectionModel().clearSelection();
                            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getListView().getSelectionModel().select(d);
                        }
                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().refresh();
                    }
                }
            }
        }
        if (event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) instanceof SongDisplayable || useTempDisp) {
            Displayable displayable;
            if (event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) instanceof SongDisplayable) {
                displayable = (SongDisplayable) event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT);
            } else {
                displayable = tempDisp;
                tempDisp = null;
            }
            if (displayable != null) {
                if (listCell == null || listCell.getIndex() != localDragIndex) {
                    if (localDragIndex > -1) {
                        getItems().remove(localDragIndex);
                        localDragIndex = -1;
                    }
                    if (listCell == null || listCell.isEmpty()) {
                        add(displayable);
                        listView.getSelectionModel().clearSelection();
                        listView.getSelectionModel().selectLast();
                    } else {
                        listView.itemsProperty().get().add(listCell.getIndex(), displayable);
                        listView.getSelectionModel().clearSelection();
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
        }
        event.consume();
    }

    /**
     * Get the current schedule in use on this list.
     * <p/>
     * @return the current schedule in use on this list.
     */
    public Schedule getSchedule() {
        boolean equal = true;
        if (listView.itemsProperty().get().size() == schedule.getSize()) {
            for (int i = 0; i < listView.itemsProperty().get().size(); i++) {
                Displayable displayable = listView.itemsProperty().get().get(i);
                if (displayable != null && !displayable.equals(schedule.getDisplayable(i))) {
                    equal = false;
                }
            }
        } else {
            equal = false;
        }
        if (equal) {
            return schedule;
        }
        schedule.clear();
        for (int i = 0; i < listView.itemsProperty().get().size(); i++) {
            schedule.add(listView.itemsProperty().get().get(i));
        }
        return schedule;
    }

    /**
     * Get the list view on this schedule list.
     *
     * @return the list view on this schedule list.
     */
    public ListView<Displayable> getListView() {
        return listView;
    }

    /**
     * Erase everything in the current schedule and set the contents of this
     * list to the current schedule.
     * <p/>
     * @param schedule the schedule.
     */
    public void setSchedule(Schedule schedule) {
        clearSchedule();
        for (Displayable displayable : schedule) {
            if (displayable instanceof SongDisplayable) {
                ((SongDisplayable) displayable).matchID();
            }
            listView.itemsProperty().get().add(displayable);
        }
        this.schedule = schedule;
    }

    /**
     * Refresh the display of the items in the schedule list.
     *
     * @param song the song of which the display should be refreshed in the
     * listview.
     */
    public void refreshSong(SongDisplayable song) {
        ObservableList<Displayable> itemp = listView.itemsProperty().get();
        int selectedIndex = listView.selectionModelProperty().get().getSelectedIndex();
        int index = itemp.indexOf(song);
        if (index != -1) {
            itemp.set(index, new SongDisplayable("", ""));
            itemp.set(index, song);
        }
        listView.getSelectionModel().clearSelection();
        listView.selectionModelProperty().get().select(selectedIndex);
    }

    /**
     * Clear the current schedule without warning.
     */
    public void clearSchedule() {
        listView.itemsProperty().get().clear();
        schedule.setFile(null);
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
        if (selectedIndex != -1) {
            Displayable d = listView.selectionModelProperty().get().getSelectedItem();
            Displayable live = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable();
            if ((d == live || listView.getItems().size() == 1) && QueleaProperties.get().getClearLiveOnRemove()) {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().removeDisplayable();
                VLCWindow.INSTANCE.stop();
                WordDrawer drawer;
                if (QueleaApp.get().getProjectionWindow().getCanvas().isStageView()) {
                    drawer = new StageDrawer();
                } else {
                    drawer = new LyricDrawer();
                }
                drawer.setCanvas(QueleaApp.get().getProjectionWindow().getCanvas());
                drawer.setTheme(ThemeDTO.DEFAULT_THEME);
            }
            Displayable preview = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if (d == preview) {
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().removeDisplayable();
            }
            if (d == null) {
                LOGGER.log(Level.WARNING, "Tried to remove null from schedule?");
            } else {
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
        if (selectedIndex == -1) { //Nothing selected
            return;
        }
        if (direction == Direction.UP && selectedIndex > 0) {
            Collections.swap(listView.itemsProperty().get(), selectedIndex, selectedIndex - 1);
            listView.getSelectionModel().clearSelection();
            listView.selectionModelProperty().get().select(selectedIndex - 1);
        }
        if (direction == Direction.DOWN && selectedIndex < listView.itemsProperty().get().size() - 1) {
            Collections.swap(listView.itemsProperty().get(), selectedIndex, selectedIndex + 1);
            listView.getSelectionModel().clearSelection();
            listView.selectionModelProperty().get().select(selectedIndex + 1);
        }
        requestFocus();
    }
}
