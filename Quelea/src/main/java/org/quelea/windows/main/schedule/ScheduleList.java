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

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.css.PseudoClass;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Duration;
import org.quelea.data.Background;
import org.quelea.data.ImageBackground;
import org.quelea.data.Schedule;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.IndexedDisplayable;
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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * <p/>
 *
 * @author Michael
 */
public class ScheduleList extends StackPane {

    /** Height of the gap that opens where a dragged item will land. */
    private static final double DROP_GAP_HEIGHT = 40;
    /** Space between the edge of the gap and the placeholder shown in it. */
    private static final double DROP_SLOT_INSET = 3;
    private static final Duration DROP_GAP_ANIMATION = Duration.millis(130);

    private final ListView<IndexedDisplayable> listView;
    private Schedule schedule;
    private final Region dropSlot;
    private final Set<ListCell<IndexedDisplayable>> cells = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<Node, TranslateTransition> slides = new HashMap<>();
    /** Where a drop would insert, or -1 if it wouldn't insert anything. */
    private int dropIndex = -1;
    /** The text item that a dragged image or video would become the background of. */
    private ListCell<IndexedDisplayable> dropOntoCell = null;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final PseudoClass DROP_TARGET = PseudoClass.getPseudoClass("drop-target");
    private IndexedDisplayable tempDisp = null;
    private IndexedDisplayable displayableThatSeesMouse = null;

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
        dropSlot = new Region();
        dropSlot.getStyleClass().add("schedule-drop-slot");
        dropSlot.setManaged(false);
        dropSlot.setMouseTransparent(true);
        dropSlot.setVisible(false);
        getChildren().add(dropSlot);
        Callback<ListView<IndexedDisplayable>, ListCell<IndexedDisplayable>> callback = new Callback<>() {
            @Override
            public ListCell<IndexedDisplayable> call(ListView<IndexedDisplayable> p) {

                ListCell<IndexedDisplayable> cell = new ListCell<>() {
                    @Override
                    public void updateItem(IndexedDisplayable indexedDisplayable, boolean empty) {
                        super.updateItem(indexedDisplayable, empty);

                        setOnMouseEntered(_ -> {
                            displayableThatSeesMouse = this.getItem();
                        });
                        setOnDragDetected(event -> {
                            recalculateDisplayableIndexes();
                            if (displayableThatSeesMouse != null) {
                                Dragboard db = startDragAndDrop(TransferMode.ANY);
                                ClipboardContent content = new ClipboardContent();
                                if (displayableThatSeesMouse.displayable() instanceof SongDisplayable) {
                                    content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, new SongDisplayableList(displayableThatSeesMouse));
                                } else {
                                    content.putString("tempdisp");
                                    tempDisp = displayableThatSeesMouse;
                                }

                                db.setContent(content);
                                event.consume();
                                ScheduleListNode preview = new ScheduleListNode(displayableThatSeesMouse.displayable());

                                new Scene(new Group(preview));
                                WritableImage dragImage = preview.snapshot(new SnapshotParameters(), null);
                                db.setDragView(dragImage);
                            }
                        });
                        setOnDragDone(e -> {
                            clearDropTarget(false);
                            e.consume();
                        });

                        if (empty || indexedDisplayable == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Displayable item = indexedDisplayable.displayable();
                            ScheduleListNode scheduleListNode = new ScheduleListNode(item);
                            // As wide as the cell, so the theme button stays at its right edge
                            scheduleListNode.prefWidthProperty().bind(Bindings.createDoubleBinding(
                                    () -> Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset()),
                                    widthProperty(), paddingProperty()));
                            setGraphic(scheduleListNode);
                            setText(null);
                            scheduleListNode.setLive(item.equals(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable()));
                            if (item instanceof SongDisplayable || item instanceof BiblePassage || item instanceof TimerDisplayable) {
                                setContextMenu(new SchedulePopupMenu(item));
                            }
                        }
                    }
                };
                cells.add(cell);
                return cell;
            }
        };
        listView.setCellFactory(DisplayableListCell.forListView(null, callback, d -> d.displayable() instanceof SongDisplayable || d.displayable() instanceof BiblePassage || d.displayable() instanceof TimerDisplayable));
        listView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            String location = db.getString();
            boolean canDrop = db.hasFiles()
                    ? db.getFiles().stream().anyMatch(ScheduleList::isSupportedFile)
                    : db.hasContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT)
                    || ("tempdisp".equals(location) && tempDisp != null)
                    || (location != null && isSupportedMediaFile(new File(location)));
            if (canDrop) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            updateDropTarget(event);
        });
        listView.setOnDragExited(event -> clearDropTarget(true));
        listView.setOnDragDropped(this::dragDropped);
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

    /**
     * Deprecated: This method is here for compatibility and should be removed. Displayables may be duplicated
     * in a schedule list, in which case this method won't return a reliable value.
     */
    @Deprecated
    public int indexOf(Displayable displayable) {
        if (displayable == null) return -1;
        for (IndexedDisplayable indexedDisplayable : getItems()) {
            if (indexedDisplayable.displayable().equals(displayable)) {
                return indexedDisplayable.index();
            }
        }
        return -1;
    }

    public void add(Displayable displayable) {
        add(displayable, false);
    }

    public void add(Displayable displayable, boolean select) {
        if (!Platform.isFxApplicationThread()) {
            LOGGER.log(Level.WARNING, "Not on the platform thread!", new RuntimeException("DEBUG EX"));
        }
        IndexedDisplayable idxDisplayable = new IndexedDisplayable(displayable, listView.itemsProperty().getValue().size());
        listView.itemsProperty().get().add(idxDisplayable);
        getSelectionModel().select(idxDisplayable);
    }

    /**
     * Work out what a drop at the position of this event would do, and show
     * it. This is decided here rather than in the cells, and from where the
     * cells are laid out rather than where they are drawn, because the cells
     * slide away from under the pointer as the gap opens.
     */
    private void updateDropTarget(DragEvent event) {
        Dragboard db = event.getDragboard();
        List<ListCell<IndexedDisplayable>> shown = cells.stream()
                .filter(cell -> cell.isVisible() && cell.getParent() != null)
                .sorted(Comparator.comparingInt(ListCell::getIndex))
                .toList();
        String location = db.getString();
        boolean canInsert = db.hasContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT)
                || ("tempdisp".equals(location) && tempDisp != null)
                || (location != null && isSupportedMediaFile(new File(location)));
        if (db.hasFiles() || !canInsert || shown.isEmpty()) {
            clearDropTarget(true);
            return;
        }
        double y = shown.get(0).getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
        ListCell<IndexedDisplayable> over = null;
        int index = -1;
        int afterLast = 0;
        for (ListCell<IndexedDisplayable> cell : shown) {
            if (cell.isEmpty()) {
                break;
            }
            double top = cell.getLayoutY();
            double height = cell.getHeight();
            if (y >= top && y < top + height) {
                over = cell;
            }
            if (index < 0 && y < top + height / 2) {
                index = cell.getIndex();
            }
            afterLast = cell.getIndex() + 1;
        }
        boolean isMedia = location != null && !location.equals("tempdisp");
        if (isMedia && over != null && over.getItem().displayable() instanceof TextDisplayable) {
            closeDropGap(true);
            setDropOntoCell(over);
        } else {
            setDropOntoCell(null);
            openDropGap(index < 0 ? afterLast : index, shown);
        }
    }

    private void setDropOntoCell(ListCell<IndexedDisplayable> cell) {
        if (dropOntoCell != null) {
            dropOntoCell.pseudoClassStateChanged(DROP_TARGET, false);
        }
        dropOntoCell = cell;
        if (cell != null) {
            cell.pseudoClassStateChanged(DROP_TARGET, true);
        }
    }

    /**
     * Slide the items from this index onwards down to make room, and show a
     * placeholder in the space.
     */
    private void openDropGap(int index, List<ListCell<IndexedDisplayable>> shown) {
        boolean alreadyOpen = dropIndex >= 0;
        dropIndex = index;
        for (ListCell<IndexedDisplayable> cell : cells) {
            slideTo(cell, cell.getIndex() >= index ? DROP_GAP_HEIGHT : 0, true);
        }
        ListCell<IndexedDisplayable> anchor = shown.get(shown.size() - 1);
        double top = anchor.getLayoutY() + anchor.getHeight();
        for (ListCell<IndexedDisplayable> cell : shown) {
            if (cell.getIndex() == index) {
                anchor = cell;
                top = cell.getLayoutY();
                break;
            }
        }
        Point2D position = sceneToLocal(anchor.getParent().localToScene(anchor.getLayoutX(), top));
        double y = Math.min(position.getY(), getHeight() - DROP_GAP_HEIGHT) + DROP_SLOT_INSET;
        dropSlot.resize(anchor.getWidth() - 2 * DROP_SLOT_INSET, DROP_GAP_HEIGHT - 2 * DROP_SLOT_INSET);
        dropSlot.setLayoutX(position.getX() + DROP_SLOT_INSET);
        if (alreadyOpen) {
            slideTo(dropSlot, y, true);
        } else {
            dropSlot.setTranslateY(y);
            dropSlot.setVisible(true);
            FadeTransition fade = new FadeTransition(DROP_GAP_ANIMATION, dropSlot);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    private void closeDropGap(boolean animate) {
        dropIndex = -1;
        dropSlot.setVisible(false);
        slideTo(dropSlot, dropSlot.getTranslateY(), false);
        for (ListCell<IndexedDisplayable> cell : cells) {
            slideTo(cell, 0, animate);
        }
    }

    private void clearDropTarget(boolean animate) {
        setDropOntoCell(null);
        closeDropGap(animate);
    }

    private void slideTo(Node node, double y, boolean animate) {
        TranslateTransition slide = slides.get(node);
        if (animate && (slide == null ? node.getTranslateY() == y : slide.getToY() == y)) {
            return;
        }
        if (slide != null) {
            slide.stop();
            slides.remove(node);
        }
        if (!animate) {
            node.setTranslateY(y);
            return;
        }
        TranslateTransition newSlide = new TranslateTransition(DROP_GAP_ANIMATION, node);
        newSlide.setToY(y);
        newSlide.setOnFinished(e -> slides.remove(node));
        slides.put(node, newSlide);
        newSlide.play();
    }

    private void dragDropped(DragEvent event) {
        updateDropTarget(event);
        int insertAt = dropIndex;
        IndexedDisplayable dropOnto = dropOntoCell == null ? null : dropOntoCell.getItem();
        clearDropTarget(false);

        Dragboard db = event.getDragboard();
        boolean completed = false;
        if (db.hasFiles()) {
            for (File file : db.getFiles()) {
                if (!isSupportedFile(file)) {
                    continue;
                }
                if (Utils.fileIsImage(file)) {
                    add(new ImageDisplayable(file));
                } else if (Utils.fileIsVideo(file)) {
                    add(new VideoDisplayable(file.getPath()));
                } else if (isPresentation(file)) {
                    new AddPowerpointActionHandler().addPresentation(List.of(file));
                } else {
                    new AddPdfActionHandler().addPDF(List.of(file));
                }
                completed = true;
            }
        } else {
            String dbLocation = db.getString();
            Object content = db.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT);
            if (content instanceof SongDisplayableList songs) {
                List<IndexedDisplayable> displayables = songs.getSongDisplayables();
                for (IndexedDisplayable indexedDisplayable : displayables) {
                    SongDisplayable song = (SongDisplayable) indexedDisplayable.displayable();
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
                completed = insertDisplayables(displayables, insertAt);
            } else if ("tempdisp".equals(dbLocation)) {
                if (tempDisp != null) {
                    completed = insertDisplayables(List.of(tempDisp), insertAt);
                    tempDisp = null;
                }
            } else if (dbLocation != null && isSupportedMediaFile(new File(dbLocation))) {
                File file = new File(dbLocation);
                boolean isVideo = Utils.fileIsVideo(file);
                if (dropOnto != null && dropOnto.displayable() instanceof TextDisplayable textDisplayable) {
                    try {
                        applyBackground(dropOnto, textDisplayable, file, isVideo);
                        completed = true;
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't copy background file", ex);
                    }
                } else {
                    Displayable displayable = isVideo ? new VideoDisplayable(dbLocation) : new ImageDisplayable(file);
                    completed = insertDisplayables(List.of(new IndexedDisplayable(displayable, -1)), insertAt);
                }
            }
        }
        recalculateDisplayableIndexes();
        event.setDropCompleted(completed);
        event.consume();
    }

    private static boolean isSupportedMediaFile(File file) {
        return file.isFile() && (Utils.fileIsImage(file) || Utils.fileIsVideo(file));
    }

    private static boolean isPresentation(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".ppt") || name.endsWith(".pptx");
    }

    private static boolean isSupportedFile(File file) {
        return file.isFile() && (isSupportedMediaFile(file) || isPresentation(file)
                || file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"));
    }

    private boolean insertDisplayables(List<IndexedDisplayable> displayables, int insertAt) {
        if (displayables.isEmpty()) {
            return false;
        }
        ObservableList<IndexedDisplayable> items = listView.getItems();
        // Keep the original occurrences: insertions and removals change positions,
        // and equal songs may appear more than once in a schedule.
        Map<Integer, IndexedDisplayable> originals = new HashMap<>();
        for (IndexedDisplayable item : items) {
            originals.put(item.index(), item);
        }
        int position = insertAt < 0 ? items.size() : Math.min(insertAt, items.size());
        IndexedDisplayable lastAdded = null;
        for (IndexedDisplayable dragged : displayables) {
            IndexedDisplayable original = originals.remove(dragged.index());
            if (original != null) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i) == original) {
                        items.remove(i);
                        if (i < position) {
                            position--;
                        }
                        break;
                    }
                }
            }
            lastAdded = new IndexedDisplayable(dragged.displayable(), position);
            items.add(position++, lastAdded);
        }
        recalculateDisplayableIndexes();
        listView.getSelectionModel().clearSelection();
        listView.getSelectionModel().select(position - 1);
        listView.requestFocus();
        IndexedDisplayable selected = lastAdded;
        Platform.runLater(() -> {
            QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(selected.displayable(), 0);
            QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
        });
        return true;
    }

    private void applyBackground(IndexedDisplayable item, TextDisplayable textDisplayable, File source, boolean video) throws IOException {
        File directory = video ? QueleaProperties.get().getVidDir() : QueleaProperties.get().getImageDir();
        File stored = copyBackgroundToLibrary(source, directory);
        ThemeDTO theme = textDisplayable.getTheme();
        SerializableDropShadow dropShadow = theme.getShadow();
        if (dropShadow == null || (dropShadow.getColor().equals(Color.WHITE) && dropShadow.getOffsetX() == 0 && dropShadow.getOffsetY() == 0)) {
            dropShadow = new SerializableDropShadow(Color.BLACK, 3, 3, 2, 0, true);
        }
        Background background = video ? new VideoBackground(stored.getName(), 0, true) : new ImageBackground(stored.getName());
        ThemeDTO newTheme = new ThemeDTO(theme.getSerializableFont(), theme.getFontPaint(), theme.getTranslateSerializableFont(), theme.getTranslateFontPaint(), background, dropShadow, theme.getSerializableFont().isBold(), theme.getSerializableFont().isItalic(), theme.getTranslateSerializableFont().isBold(), theme.getTranslateSerializableFont().isItalic(), theme.getTextPosition(), theme.getTextAlignment());
        for (TextSection section : textDisplayable.getSections()) {
            section.setTheme(newTheme);
        }
        textDisplayable.setTheme(newTheme);
        if (item.displayable() instanceof SongDisplayable song) {
            if (QueleaProperties.get().getUseDefaultTranslation()) {
                String defaultTranslation = QueleaProperties.get().getDefaultTranslationName();
                if (defaultTranslation != null && !defaultTranslation.trim().isEmpty()) {
                    song.setCurrentTranslationLyrics(defaultTranslation);
                }
            }
            Utils.updateSongInBackground(song, true, false);
        }
        if (QueleaProperties.get().getPreviewOnImageUpdate()) {
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getListView().getSelectionModel().clearSelection();
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getListView().getSelectionModel().select(item);
        }
        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().refresh();
    }

    private static File copyBackgroundToLibrary(File source, File directory) throws IOException {
        Path sourcePath = source.toPath();
        Path library = Files.createDirectories(directory.toPath());
        String name = source.getName();
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        String extension = dot > 0 ? name.substring(dot) : "";
        // Reuse identical media, but never overwrite a different library file.
        for (int suffix = 1; ; suffix++) {
            Path target = library.resolve(suffix == 1 ? name : stem + " (" + suffix + ")" + extension);
            if (Files.exists(target)) {
                if (Files.isRegularFile(target) && (Files.isSameFile(sourcePath, target) || Files.mismatch(sourcePath, target) == -1)) {
                    return target.toFile();
                }
                continue;
            }
            try {
                return Files.copy(sourcePath, target).toFile();
            } catch (FileAlreadyExistsException ex) {
                // Another import claimed this name; try the next one.
            }
        }
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
                Displayable displayable = listView.itemsProperty().get().get(i).displayable();
                // Song equality omits metadata that still needs to be saved.
                if (displayable != schedule.getDisplayable(i)) {
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
            schedule.add(listView.itemsProperty().get().get(i).displayable());
        }
        return schedule;
    }

    /**
     * Get the list view on this schedule list.
     *
     * @return the list view on this schedule list.
     */
    public ListView<IndexedDisplayable> getListView() {
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
        for (int i = 0; i < schedule.getSize(); i++) {
            Displayable displayable = schedule.getDisplayable(i);
            if (displayable instanceof SongDisplayable) {
                ((SongDisplayable) displayable).matchID();
            }
            listView.itemsProperty().get().add(new IndexedDisplayable(displayable, i));
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
        ObservableList<IndexedDisplayable> itemp = listView.itemsProperty().get();
        int selectedIndex = listView.selectionModelProperty().get().getSelectedIndex();

        IndexedDisplayable idxDisplayable = null;
        for (IndexedDisplayable displayable : itemp) {
            if (displayable.displayable().equals(song)) {
                idxDisplayable = displayable;
                break;
            }
        }

        if (idxDisplayable != null) {
            itemp.set(idxDisplayable.index(), new IndexedDisplayable(new SongDisplayable("", ""), idxDisplayable.index()));
            itemp.set(idxDisplayable.index(), idxDisplayable);
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
    public MultipleSelectionModel<IndexedDisplayable> getSelectionModel() {
        return listView.getSelectionModel();
    }

    /**
     * Get the items property of the underlying list.
     * <p>
     *
     * @return the items property of the underlying list.
     */
    public ObjectProperty<ObservableList<IndexedDisplayable>> itemsProperty() {
        return listView.itemsProperty();
    }

    /**
     * Return the items of the underlying list.
     * <p>
     *
     * @return the items of the underlying list.
     */
    public ObservableList<IndexedDisplayable> getItems() {
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
            Displayable d = listView.selectionModelProperty().get().getSelectedItem().displayable();
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
            recalculateDisplayableIndexes();
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
        recalculateDisplayableIndexes();
    }

    public void recalculateDisplayableIndexes() {
        for (int i = 0; i < getItems().size(); i++) {
            IndexedDisplayable item = getItems().get(i);
            item.setIndex(i);
        }
    }
}
