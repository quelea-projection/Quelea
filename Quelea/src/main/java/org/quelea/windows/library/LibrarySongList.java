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

import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.lucene.SongSearchIndex;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.AddSongActionHandler;
import org.quelea.windows.main.widgets.AddSongPromptOverlay;
import org.quelea.windows.main.widgets.LoadingPane;

/**
 * The list that displays the songs in the library.
 * <p/>
 * @author Michael
 */
public class LibrarySongList extends StackPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final LibraryPopupMenu popupMenu;
    private ListView<SongDisplayable> songList;
    private LoadingPane loadingOverlay;
    private LibrarySongPreviewCanvas previewCanvas;
    private AddSongPromptOverlay addSongOverlay;

    /**
     * Create a new library song list.
     * <p/>
     * @param popup true if we want a popup menu to appear on items in this list
     * when right clicked, false if not.
     * @popup true if this list should popup a context menu when right clicked,
     * false otherwise.
     */
    public LibrarySongList(boolean popup) {
        songList = new ListView<>();
        loadingOverlay = new LoadingPane();
        addSongOverlay = new AddSongPromptOverlay();
        setAlignment(Pos.CENTER);
        getChildren().add(songList);
        getChildren().add(addSongOverlay);
        addSongOverlay.show();
        songList.itemsProperty().addListener((val, oldList, newList) -> {
            if (songList.getItems().isEmpty()) {
                addSongOverlay.show();
            } else {
                addSongOverlay.hide();
            }
        });
        getChildren().add(loadingOverlay);
            previewCanvas = new LibrarySongPreviewCanvas();
            StackPane.setAlignment(previewCanvas, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(previewCanvas, new Insets(10));
            getChildren().add(previewCanvas);
            songList.focusedProperty().addListener((observable, oldFocused, focused) -> {
                if (QueleaProperties.get().getShowDBSongPreview() && focused && songList.getSelectionModel().getSelectedItem() != null) {
                    previewCanvas.show();
                } else {
                    previewCanvas.hide();
                }
            });
        Callback<ListView<SongDisplayable>, ListCell<SongDisplayable>> callback = (lv) -> {
            final ListCell<SongDisplayable> cell = new ListCell<SongDisplayable>() {
                @Override
                protected void updateItem(SongDisplayable item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox textBox = new HBox();
                        if (item.getLastSearch() == null) {
                            Text text = new Text(item.getTitle());
                            text.getStyleClass().add("text");
                            textBox.getChildren().add(text);
                        } else {
                            int startIndex = item.getTitle().toLowerCase().indexOf(item.getLastSearch().toLowerCase());
                            if (startIndex == -1) {
                                Text text = new Text(item.getTitle());
                                text.getStyleClass().add("text");
                                textBox.getChildren().add(text);
                            } else {
                                Text initText = new Text(item.getTitle().substring(0, startIndex));
                                initText.getStyleClass().add("text");
                                textBox.getChildren().add(initText);
                                String boldTextStr = item.getTitle().substring(startIndex, startIndex + item.getLastSearch().length());
                                Text boldText = new Text(boldTextStr);
                                boldText.setStyle("-fx-font-weight:bold;");
                                boldText.getStyleClass().add("text");
                                textBox.getChildren().add(boldText);
                                Text plainText = new Text(item.getTitle().substring(startIndex + item.getLastSearch().length()));
                                plainText.getStyleClass().add("text");
                                textBox.getChildren().add(plainText);
                            }
                        }
                        setGraphic(textBox);
                    }
                }
            };
            cell.setOnDragDetected((event) -> {
                SongDisplayable displayable = cell.getItem();
                if (displayable != null) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, displayable);
                    db.setContent(content);
                }
                event.consume();
            });
            return cell;
        };
        popupMenu = new LibraryPopupMenu();
        songList.setOnMouseClicked((MouseEvent t) -> {
            if (t.getClickCount() == 2 && songList.getSelectionModel().getSelectedItem() != null) {
                new AddSongActionHandler(QueleaProperties.get().getDefaultSongDBUpdate()).handle(null);
            } else if (t.getClickCount() == 1 && (t.isControlDown())) {
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(songList.getSelectionModel().getSelectedItem(), 0);
            }
        });
        songList.selectionModelProperty().get().selectedItemProperty().addListener((observable, oldSong, song) -> {
            if (previewCanvas != null) {
                previewCanvas.setSong(song);
                if (song != null && QueleaProperties.get().getShowDBSongPreview()) {
                    previewCanvas.show();
                } else {
                    previewCanvas.hide();
                }
            }
            if(QueleaProperties.get().getImmediateSongDBPreview()) {
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(songList.getSelectionModel().getSelectedItem(), 0);
            }
        });
        songList.setOnKeyPressed((KeyEvent t) -> {
            if (t.getCode() == KeyCode.ENTER) {
                new AddSongActionHandler(QueleaProperties.get().getDefaultSongDBUpdate()).handle(null);
            }
        });
        if (popup) {
            songList.setCellFactory(DisplayableListCell.forListView(popupMenu, callback, null));
        }
        new Thread() {
            public void run() {
                refresh();
            }
        }.start();
        SongManager.get().registerDatabaseListener(() -> {
            refresh();
        });
    }
    private ExecutorService filterService = Executors.newSingleThreadExecutor();
    private Future<?> filterFuture;

    /**
     * Filter the results in this list by a specific search term.
     * <p/>
     * @param search the search term to use.
     */
    public void filter(final String search) {
        if (filterFuture != null) {
            filterFuture.cancel(true);
        }
        Platform.runLater(() -> {
            setLoading(true);
        });
        LOGGER.log(Level.INFO, "Performing search for {0}", search);
        filterFuture = filterService.submit(() -> {
            final ObservableList<SongDisplayable> songs = FXCollections.observableArrayList();

            // empty or null search strings do not need to be filtered - lest they get added twice
            if (search == null || search.trim().isEmpty() || Pattern.compile("[^\\w ]", Pattern.UNICODE_CHARACTER_CLASS).matcher(search).replaceAll("").isEmpty()) {
                TreeSet<SongDisplayable> m = new TreeSet<>();
                LOGGER.log(Level.INFO, "Empty song search performed");
                for (SongDisplayable song : SongManager.get().getSongs()) {
                    song.setLastSearch(null);
                    m.add(song);
                }
                songs.addAll(m);
                LOGGER.log(Level.INFO, "{0} songs in list", songs.size());
            } else {
                TreeSet<SongDisplayable> m = new TreeSet<>();
                LOGGER.log(Level.INFO, "Filtering songs by title");
                SongDisplayable[] titleSongs = SongManager.get().getIndex().filter(search, SongSearchIndex.FilterType.TITLE);
                LOGGER.log(Level.INFO, "Filtered songs by title");
                for (SongDisplayable song : titleSongs) {
                    song.setLastSearch(search);
                    m.add(song);
                }
                songs.addAll(m);
                m.clear();
                LOGGER.log(Level.INFO, "{0} songs in list", songs.size());

                LOGGER.log(Level.INFO, "Filtering songs by lyrics");
                SongDisplayable[] lyricSongs = SongManager.get().getIndex().filter(search, SongSearchIndex.FilterType.BODY);
                LOGGER.log(Level.INFO, "Filtered songs by lyrics");
                for (SongDisplayable song : lyricSongs) {
                    song.setLastSearch(null);
                    m.add(song);
                }
                songs.addAll(m);
                m.clear();
                LOGGER.log(Level.INFO, "{0} songs in list", songs.size());

                LOGGER.log(Level.INFO, "Filtering songs by author");
                SongDisplayable[] authorSongs = SongManager.get().getIndex().filter(search, SongSearchIndex.FilterType.AUTHOR);
                LOGGER.log(Level.INFO, "Filtered songs by author");
                for (SongDisplayable song : authorSongs) {
                    m.add(song);
                }
                songs.addAll(m);
                m.clear();
                LOGGER.log(Level.INFO, "{0} songs in list", songs.size());
            }

            Platform.runLater(() -> {
                LOGGER.log(Level.INFO, "Setting song list");
                songList.setItems(FXCollections.observableArrayList()); //Force search display update
                songList.setItems(songs);
                if (!songs.isEmpty()) {
                    LOGGER.log(Level.INFO, "Selecting first song");
                    songList.getSelectionModel().select(0);
                }
                LOGGER.log(Level.INFO, "Setting no longer loading");
                setLoading(false);
                LOGGER.log(Level.INFO, "Song search done");
            });
        });

    }

    /**
     * Get the currently selected song.
     * <p/>
     * @return the currently selected song, or null if none is selected.
     */
    public SongDisplayable getSelectedValue() {
        return songList.selectionModelProperty().get().getSelectedItem();
    }

    /**
     * Get the popup menu associated with this list.
     * <p/>
     * @return the popup menu.
     */
    public LibraryPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Get the actual list view in this song list.
     * <p>
     * @return the list view object.
     */
    public ListView<SongDisplayable> getListView() {
        return songList;
    }

    private void refresh() {
        Platform.runLater(() -> {
            setLoading(true);
        });
        final ObservableList<SongDisplayable> songs = FXCollections.observableArrayList(SongManager.get().getSongs(loadingOverlay));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                songList.itemsProperty().set(songs);
                setLoading(false);
            }
        });
    }

    public void setLoading(boolean loading) {
        if (loading) {
            loadingOverlay.show();
        } else {
            loadingOverlay.hide();
        }
    }
}
