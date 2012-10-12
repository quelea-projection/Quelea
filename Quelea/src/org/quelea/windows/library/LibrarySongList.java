/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.quelea.QueleaApp;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;
import org.quelea.lucene.SongSearchIndex;
import org.quelea.utils.DatabaseListener;
import org.quelea.utils.LoggerUtils;

/**
 * The list that displays the songs in the library.
 * <p/>
 * @author Michael
 */
public class LibrarySongList extends ListView<Song> implements DatabaseListener {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final LibraryPopupMenu popupMenu;
    private final boolean popup;

    /**
     * Create a new library song list.
     * <p/>
     * @popup true if this list should popup a context menu when right clicked,
     * false otherwise.
     */
    public LibrarySongList(boolean popup) {
        this.popup = popup;
        Callback<ListView<Song>, ListCell<Song>> callback = new Callback<ListView<Song>, ListCell<Song>>() {
            @Override
            public ListCell<Song> call(ListView<Song> p) {
                return new TextFieldListCell<>(new StringConverter<Song>() {
                    @Override
                    public String toString(Song song) {
                        return song.getListHTML();
                    }

                    @Override
                    public Song fromString(String string) {
                        //Implementation not needed.
                        return null;
                    }
                });
            }
        };
        popupMenu = new LibraryPopupMenu();
        setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if(t.getClickCount() == 2) {
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get().add(getSelectedValue());
                }
            }
        });
        if(popup) {
            setCellFactory(ContextMenuListCell.<Song>forListView(popupMenu, callback));
        }
        databaseChanged();
        SongDatabase.get().registerDatabaseListener(this);
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
        filterFuture = filterService.submit(new Runnable() {
            @Override
            public void run() {
                final ObservableList<Song> songs = FXCollections.observableArrayList();

                // empty or null search strings do not need to be filtered - lest they get added twice
                if(search == null || search.trim().isEmpty()) {
                    TreeSet<Song> m = new TreeSet<>();
                    for(Song song : SongDatabase.get().getSongs()) {
                        song.setLastSearch(null);
                        m.add(song);
                    }
                    songs.addAll(m);
                }
                else {
                    TreeSet<Song> m = new TreeSet<>();
                    Song[] titleSongs = SongDatabase.get().getIndex().filter(search, SongSearchIndex.FilterType.TITLE);
                    for(Song song : titleSongs) {
                        song.setLastSearch(search);
                        m.add(song);
                    }
                    songs.addAll(m);
                    m.clear();
                    
                    Song[] lyricSongs = SongDatabase.get().getIndex().filter(search, SongSearchIndex.FilterType.BODY);
                    for(Song song : lyricSongs) {
                        song.setLastSearch(null);
                        m.add(song);
                    }
                    songs.addAll(m);
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        setItems(songs);
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
        filterFuture = filterService.submit(new Runnable() {
            @Override
            public void run() {
                final ObservableList<Song> allSongs = FXCollections.observableArrayList(SongDatabase.get().getSongs());
                final ObservableList<Song> songs = new SimpleListProperty<>();
                for(int i = 0; i < allSongs.size(); i++) {
                    boolean add = true;
                    Song s = allSongs.get(i);
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
                        setItems(songs);
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
    public Song getSelectedValue() {
        return selectionModelProperty().get().getSelectedItem();
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
     * Update the contents of the list.
     */
    @Override
    public final void databaseChanged() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                itemsProperty().set(FXCollections.observableArrayList(SongDatabase.get().getSongs()));
            }
        });

    }
}
