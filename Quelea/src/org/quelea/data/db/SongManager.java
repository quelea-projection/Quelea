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
package org.quelea.data.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.quelea.data.ThemeDTO;
import org.quelea.data.db.model.Song;
import org.quelea.data.db.model.Theme;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.lucene.SearchIndex;
import org.quelea.services.lucene.SongSearchIndex;
import org.quelea.services.utils.DatabaseListener;
import org.quelea.services.utils.LoggerUtils;

/**
 * Manage songs persistent operations.
 * <p/>
 * @author Michael
 */
public final class SongManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final SongManager INSTANCE = new SongManager();
    private SearchIndex<SongDisplayable> index;
    private boolean addedToIndex;
    private boolean error;
    private final Set<DatabaseListener> listeners;

    /**
     * Initialise the song database.
     */
    private SongManager() {
        listeners = new HashSet<>();
        addedToIndex = false;
        index = new SongSearchIndex();
        error = false;
    }

    /**
     * Get the singleton instance of this class.
     * <p/>
     * @return the singleton instance of this class.
     */
    public static SongManager get() {
        return INSTANCE;
    }

    /**
     * Get the underlying search index used by this database.
     * <p/>
     * @return the search index.
     */
    public SearchIndex<SongDisplayable> getIndex() {
        return index;
    }

    /**
     * Determine if an error occurred initialising the database.
     * <p/>
     * @return true if an error occurred, false if all is ok.
     */
    public boolean errorOccurred() {
        return error;
    }

    /**
     * Register a database listener with this database.
     * <p/>
     * @param listener the listener.
     */
    public void registerDatabaseListener(DatabaseListener listener) {
        listeners.add(listener);
    }

    /**
     * Fire off the database listeners.
     */
    public synchronized void fireUpdate() {
        for(DatabaseListener listener : listeners) {
            listener.databaseChanged();
        }
    }

    /**
     * Get all the songs in the database.
     * <p/>
     * @return an array of all the songs in the database.
     */
    public synchronized SongDisplayable[] getSongs() {
        if(Platform.isFxApplicationThread()) {
            LOGGER.log(Level.WARNING, "getSongs() should not be called on platform thread!", new RuntimeException("Debug exception"));
        }
        final Set<SongDisplayable> songs = new TreeSet<>();
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                for(Song song : new SongDao(session).getSongs()) {
                    final String[] tags = new String[song.getTags().size()];

                    for(int i = 0; i < song.getTags().size(); i++) {
                        tags[i] = song.getTags().get(i);
                    }
                    final SongDisplayable songDisplayable = new SongDisplayable.Builder(song.getTitle(),
                            song.getAuthor())
                            .lyrics(song.getLyrics())
                            .ccli(song.getCcli())
                            .year(song.getYear())
                            .tags(tags)
                            .publisher(song.getPublisher())
                            .copyright(song.getCopyright())
                            .key(song.getKey())
                            .info(song.getInfo())
                            .capo(song.getCapo())
                            .id(song.getId()).get();
                    final Theme theme = song.getTheme();
                    for(TextSection section : songDisplayable.getSections()) {
                        section.setTheme(ThemeDTO.getDTO(theme));
                    }
                    song.setTheme(theme);
                    songs.add(songDisplayable);
                }
            }
        });

        if(!addedToIndex) {
            addedToIndex = true;
            LOGGER.log(Level.INFO, "Adding {0} songs to index", songs.size());
            index.addAll(songs);
        }
        return songs.toArray(new SongDisplayable[songs.size()]);
    }

    /**
     * Add a song to the database.
     * <p/>
     * @param song the song to add.
     * @param fireUpdate true if the update should be fired to listeners when
     * adding this song, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean addSong(final SongDisplayable song, final boolean fireUpdate) {
        if(song.getSections().length == 0) {
            return false;
        }
        final boolean nullTheme = song.getSections()[0].getTheme() == null;
        final boolean nullTags = song.getTags() == null;
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                Song newSong = new Song(song.getTitle(),
                        song.getAuthor(),
                        song.getLyrics(true, true),
                        song.getCcli(),
                        song.getCopyright(),
                        song.getYear(),
                        song.getPublisher(),
                        song.getKey(),
                        song.getCapo(),
                        song.getInfo(),
                        nullTheme ? ThemeDTO.DEFAULT_THEME.getTheme() : new Theme(song.getSections()[0].getTheme().getTheme()),
                        nullTags ? new ArrayList<String>() : Arrays.asList(song.getTags()));
                session.save(newSong);
            }
        });
        if(addedToIndex) {
            index.add(song);
        }
        if(fireUpdate) {
            fireUpdate();
        }
        return true;
    }

    /**
     * Update a song in the database.
     * <p/>
     * @param song the song to update.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean updateSong(final SongDisplayable song) {
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                Song updatedSong = null;
                final boolean nullTheme = song.getSections()[0].getTheme() == null;
                final boolean nullTags = song.getTags() == null;
                try {
                    updatedSong = new SongDao(session).getSongById(song.getID());
                    updatedSong.setAuthor(song.getAuthor());
                    updatedSong.setYear(song.getYear());
                    updatedSong.setCapo(song.getCapo());
                    updatedSong.setCcli(song.getCcli());
                    updatedSong.setCopyright(song.getCopyright());
                    updatedSong.setInfo(song.getInfo());
                    updatedSong.setLyrics(song.getLyrics(true, true));
                    updatedSong.setKey(song.getKey());
                    updatedSong.setPublisher(song.getPublisher());
                    updatedSong.setTags(nullTags ? new ArrayList<String>() : Arrays.asList(song.getTags()));
                    updatedSong.setTitle(song.getTitle());
                    updatedSong.setTheme(nullTheme ? ThemeDTO.DEFAULT_THEME.getTheme() : new Theme(song.getSections()[0].getTheme().getTheme()));
                    session.update(updatedSong);
                    new Thread() {
                        public void run() {
                            if(addedToIndex) {
                                index.remove(song);
                                index.add(song);
                            }
                        }
                    }.start();
                }
                catch(ObjectNotFoundException e) {
                    LOGGER.log(Level.INFO, "Updating song that doesn't exist, adding instead");
                    addSong(song, true);
                }
            }
        });

        return true;
    }

    /**
     * Remove a song from the database.
     * <p/>
     * @param song the song to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean removeSong(final SongDisplayable song) {
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                Song deletedSong = new SongDao(session).getSongById(song.getID());
                session.delete(deletedSong);
            }
        });
        fireUpdate();
        return true;
    }
}
