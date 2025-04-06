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

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.quelea.data.Background;
import org.quelea.data.ImageBackground;
import org.quelea.data.Schedule;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
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
import org.quelea.utils.SongDisplayableList;
import org.quelea.windows.library.DisplayableListCell;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.main.actionhandlers.AddPdfActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler;
import org.quelea.windows.stage.StageDrawer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * <p/>
 *
 * @author Michael
 */
public class ScheduleList extends StackPane {

    private final ListView<Displayable> listView;
    private Schedule schedule;
    private final Rectangle markerRect;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ArrayList<ListCell<Displayable>> cells = new ArrayList<>();
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
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        getChildren().add(listView);
        markerRect = new Rectangle(200, 3, Color.GRAY);
        markerRect.setVisible(false);
        getChildren().add(markerRect);
        markerRect.toFront();
        Callback<ListView<Displayable>, ListCell<Displayable>> callback = new Callback<>() {
            @Override
            public ListCell<Displayable> call(ListView<Displayable> p) {

                final ListCell<Displayable> listCell = new ListCell<>() {
                    @Override
                    public void updateItem(Displayable item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            ScheduleListNode scheduleListNode = new ScheduleListNode(item);
                            setGraphic(scheduleListNode);
                            setText(null);
                            scheduleListNode.setLive(item.equals(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable()));
                        }
                        if (item instanceof SongDisplayable || item instanceof BiblePassage || item instanceof TimerDisplayable) {
                            setContextMenu(new SchedulePopupMenu(item));
                        }
                    }
                };
                cells.add(listCell);
                listCell.setOnDragDetected(event -> {
                    if (listCell.getItem() != null) {
                        // Get the current index of the cell being dragged, regardless of selection state
                        localDragIndex = listCell.getIndex();
                        
                        // Make sure the item is selected before starting the drag operation
                        if (!listView.getSelectionModel().isSelected(localDragIndex)) {
                            listView.getSelectionModel().clearAndSelect(localDragIndex);
                        }
                        
                        Dragboard db = listCell.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        if (listCell.getItem() instanceof SongDisplayable) {
                            content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, new SongDisplayableList((SongDisplayable) listCell.getItem()));
                        } else {
                            content.putString("tempdisp");
                            tempDisp = listCell.getItem();
                        }
                
                        db.setContent(content);
                        event.consume();
                        db.setDragView(listCell.snapshot(null, null));
                    }
                });
                listCell.setOnDragEntered(event -> {
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
                });
                listCell.setOnDragExited(t -> {
                    listCell.setStyle("");
                    markerRect.setVisible(false);
                });
                listCell.setOnDragOver(event -> {
                    if (event.getDragboard().getString() != null || event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                        event.acceptTransferModes(TransferMode.ANY);
                    }
                });
                listCell.setOnDragDone(event -> {
                    localDragIndex = -1;
                    event.consume();
                });
                listCell.setOnDragDropped(event -> dragDropped(event, listCell));
                return listCell;
            }
        };
        listView.setCellFactory(DisplayableListCell.forListView(null, callback, d -> d instanceof SongDisplayable || d instanceof BiblePassage || d instanceof TimerDisplayable));
        listView.setOnDragOver(event -> {
            if (event.getDragboard().getString() != null || event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            if (event.getDragboard().hasFiles()) {
                event.getDragboard().getFiles().stream().filter((file) -> (Utils.fileIsImage(file) || Utils.fileIsVideo(file) || file.getPath().matches("(.*)(pdf|ppt|pptx)") && !file.isDirectory())).forEach((file) -> {
                    event.acceptTransferModes(TransferMode.ANY);
                });
            }
        });
        listView.setOnDragDropped(event -> dragDropped(event, null));
        schedule = new Schedule();
        setOnKeyTyped(t -> {
            if (t.getCharacter().equals(" ")) {
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().requestFocus();
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().selectFirstLyric();
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
            }
        });
        setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.DELETE) {
                new RemoveScheduleItemActionHandler().handle(null);
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
        if (listCell != null) {
            listCell.setStyle("-fx-border-color: rgb(0, 0, 0);-fx-border-width: 0,0,0,0;");
        }

        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            db.getFiles().forEach((file) -> {
                if (Utils.fileIsImage(file)) {
                    add(new ImageDisplayable(file));
                } else if (Utils.fileIsVideo(file)) {
                    add(new VideoDisplayable(file.getPath()));
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
        } else {
            String dbLocation = db.getString();
            boolean useTempDisp = false;
            if (dbLocation != null) {
                boolean isVideo = Utils.fileIsVideo(new File(dbLocation));
                useTempDisp = dbLocation.equals("tempdisp");
                if (!Utils.isInDir(QueleaProperties.get().getImageDir(), new File(dbLocation)) && !useTempDisp) {
                    try {
                        Utils.copyFile(new File(dbLocation), new File(QueleaProperties.get().getImageDir(), new File(dbLocation).getName()));
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't copy image file", ex);
                    }
                }
                boolean isSong = !(listCell == null || listCell.isEmpty());
                if (isSong) {
                    if (!(listCell.getItem() instanceof TextDisplayable)) {
                        isSong = false;
                    }
                }
                if (!isSong && !useTempDisp) {
                    Displayable visualDisplayable;
                    if(isVideo) {
                        visualDisplayable = new VideoDisplayable(dbLocation);
                    }
                    else {
                        visualDisplayable = new ImageDisplayable(new File(dbLocation));
                    }
                    useTempDisp = true;
                    tempDisp = visualDisplayable;
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
                            Background background;
                            if(isVideo) {
                                background = new VideoBackground(new File(dbLocation).getName(), 0, true);
                            }
                            else {
                                background = new ImageBackground(new File(dbLocation).getName());
                            }

                            ThemeDTO newTheme = new ThemeDTO(theme.getSerializableFont(), theme.getFontPaint(), theme.getTranslateSerializableFont(), theme.getTranslateFontPaint(), background, dropShadow, theme.getSerializableFont().isBold(), theme.getSerializableFont().isItalic(), theme.getTranslateSerializableFont().isBold(), theme.getTranslateSerializableFont().isItalic(), theme.getTextPosition(), theme.getTextAlignment());
                            for (TextSection section : textDisplayable.getSections()) {
                                section.setTheme(newTheme);
                            }
                            textDisplayable.setTheme(newTheme);
                            if (d instanceof SongDisplayable) {
                                SongDisplayable sd = (SongDisplayable) d;
                                if(QueleaProperties.get().getUseDefaultTranslation()) {
                                    String defaultTranslation = QueleaProperties.get().getDefaultTranslationName();
                                    if(defaultTranslation!=null && !defaultTranslation.trim().isEmpty()) {
                                        sd.setCurrentTranslationLyrics(defaultTranslation);
                                    }
                                }
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
            if (db.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) instanceof SongDisplayableList || useTempDisp) {
                List<? extends Displayable> displayables;
                if (db.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) instanceof SongDisplayableList) {
                    List<SongDisplayable> songDisplayables = ((SongDisplayableList) db.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT)).getSongDisplayables();
                    displayables = songDisplayables;
                    for(SongDisplayable song : songDisplayables) {
                        if (!QueleaProperties.get().getDefaultSongDBUpdate()) {
                            song.setID(-1);
                            song.setNoDBUpdate();
                        }
                        if (QueleaProperties.get().getUseDefaultTranslation()) {
                            String defaultTranslation = QueleaProperties.get().getDefaultTranslationName();
                            if (defaultTranslation != null && !defaultTranslation.trim().isEmpty()) {
                                song.setCurrentTranslationLyrics(defaultTranslation);
                            }
                        }
                    }
                } else {
                    displayables = List.of(tempDisp);
                    tempDisp = null;
                }
                if (!displayables.isEmpty()) {
                    for(Displayable d : displayables) {
                        if (listCell == null || listCell.getIndex() != localDragIndex) {
                            if (localDragIndex > -1) {
                                getItems().remove(localDragIndex);
                                localDragIndex = -1;
                            }
                            if (listCell == null || listCell.isEmpty()) {
                                add(d);
                                listView.getSelectionModel().clearSelection();
                                listView.getSelectionModel().selectLast();
                            } else {
                                listView.itemsProperty().get().add(listCell.getIndex(), d);
                                listView.getSelectionModel().clearSelection();
                                listView.getSelectionModel().select(listCell.getIndex());
                            }
                            listView.requestFocus();
                            Platform.runLater(() -> {
                                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(d, 0);
                                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                            });
                        }
                    }
                }
            }
        }
        event.consume();
        localDragIndex = -1;
    }

    /**
     * Get the current schedule in use on this list.
     * <p/>
     *
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
     *
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
     *             listview.
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
     *
     * @return the selection model of the listview.
     */
    public MultipleSelectionModel<Displayable> getSelectionModel() {
        return listView.getSelectionModel();
    }

    /**
     * Get the items property of the underlying list.
     * <p>
     *
     * @return the items property of the underlying list.
     */
    public ObjectProperty<ObservableList<Displayable>> itemsProperty() {
        return listView.itemsProperty();
    }

    /**
     * Return the items of the underlying list.
     * <p>
     *
     * @return the items of the underlying list.
     */
    public ObservableList<Displayable> getItems() {
        return listView.getItems();
    }

    /**
     * Determine whether the schedule list is empty.
     * <p/>
     *
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
     *
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
