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

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.lucene.SongSearchIndex;
import org.quelea.services.utils.DatabaseListener;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.AddSongActionHandler;
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
    private LoadingPane overlay;
    private final boolean popup;

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
        overlay = new LoadingPane();
        setAlignment(Pos.CENTER);
        getChildren().add(songList);
        getChildren().add(overlay);
        this.popup = popup;
        Callback<ListView<SongDisplayable>, ListCell<SongDisplayable>> callback = new Callback<ListView<SongDisplayable>, ListCell<SongDisplayable>>() {
            @Override
            public ListCell<SongDisplayable> call(ListView<SongDisplayable> p) {
                final TextFieldListCell<SongDisplayable> cell = new TextFieldListCell<>(new StringConverter<SongDisplayable>() {
                    @Override
                    public String toString(SongDisplayable song) {
                        return song.getListHTML();
                    }

                    @Override
                    public SongDisplayable fromString(String string) {
                        //Implementation not needed.
                        return null;
                    }
                });
                cell.setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, cell.getItem());
                        db.setContent(content);
                        event.consume();
                    }
                });
                return cell;
            }
        };
        popupMenu = new LibraryPopupMenu();
        songList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getClickCount() == 2) {
                    new AddSongActionHandler().handle(null);
                }
                else if(t.getClickCount()==1 && t.isControlDown()) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(songList.getSelectionModel().getSelectedItem(), 0);
                }
            }
        });
        songList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode() == KeyCode.ENTER) {
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(getSelectedValue());
                }
            }
        });
        if(popup) {
            songList.setCellFactory(DisplayableListCell.<SongDisplayable>forListView(popupMenu, callback, null));
        }
        new Thread() {
            public void run() {
                refresh();
            }
        }.start();
        SongManager.get().registerDatabaseListener(new DatabaseListener() {

            @Override
            public void databaseChanged() {
                refresh();
            }
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
        if(filterFuture != null) {
            filterFuture.cancel(true);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setLoading(true);
            }
        });
        filterFuture = filterService.submit(new Runnable() {
            @Override
            public void run() {
                final ObservableList<SongDisplayable> songs = FXCollections.observableArrayList();

                // empty or null search strings do not need to be filtered - lest they get added twice
                if(search == null || search.trim().isEmpty()) {
                    TreeSet<SongDisplayable> m = new TreeSet<>();
                    for(SongDisplayable song : SongManager.get().getSongs()) {
                        song.setLastSearch(null);
                        m.add(song);
                    }
                    songs.addAll(m);
                }
                else {
                    TreeSet<SongDisplayable> m = new TreeSet<>();
                    SongDisplayable[] titleSongs = SongManager.get().getIndex().filter(search, SongSearchIndex.FilterType.TITLE);
                    for(SongDisplayable song : titleSongs) {
                        song.setLastSearch(search);
                        m.add(song);
                    }

                    SongDisplayable[] lyricSongs = SongManager.get().getIndex().filter(search, SongSearchIndex.FilterType.BODY);
                    for(SongDisplayable song : lyricSongs) {
                        song.setLastSearch(null);
                        m.add(song);
                    }

                    SongDisplayable[] authorSongs = SongManager.get().getIndex().filter(search, SongSearchIndex.FilterType.AUTHOR);
                    for(SongDisplayable song : authorSongs) {
                        m.add(song);
                    }

                    songs.addAll(m);
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        songList.setItems(songs);
                        setLoading(false);
                    }
                });
            }
        });

    }

    /**
     * Filter songs in this song list by a certain number of tags. Only songs
     * that contain all the tags will be displayed and pass the filter.
     * <p/>
     * @param filterTags the tags to filter on.
     */
    public void filterByTag(final List<String> filterTags) {
        if(filterFuture != null) {
            filterFuture.cancel(true);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setLoading(true);
            }
        });
        filterFuture = filterService.submit(new Runnable() {
            @Override
            public void run() {
                final ObservableList<SongDisplayable> allSongs = FXCollections.observableArrayList(SongManager.get().getSongs());
                final ObservableList<SongDisplayable> songs = new SimpleListProperty<>();
                for(int i = 0; i < allSongs.size(); i++) {
                    boolean add = true;
                    SongDisplayable s = allSongs.get(i);
                    String[] songTags = s.getTags();
                    for(String filterTag : filterTags) {
                        if(filterTag.trim().isEmpty()) {
                            continue;
                        }
                        boolean inPlace = false;
                        for(String songtag : songTags) {
                            if(filterTag.trim().equalsIgnoreCase(songtag.trim())) {
                                inPlace = true;
                            }
                        }
                        if(!inPlace) {
                            add = false;
                        }
                    }
                    if(add) {
                        songs.add(s);
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        songList.setItems(songs);
                        setLoading(false);
                    }
                });
            }
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setLoading(true);
            }
        });
        final ObservableList<SongDisplayable> songs = FXCollections.observableArrayList(SongManager.get().getSongs());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                songList.itemsProperty().set(songs);
                setLoading(false);
            }
        });
    }

    public void setLoading(boolean loading) {
        if(loading) {
            overlay.show();
        }
        else {
            overlay.hide();
        }
    }
}
