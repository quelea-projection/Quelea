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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.quelea.services.lucene.SongSearchIndex;
import org.quelea.services.utils.DatabaseListener;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.widgets.LoadingPane;

/**
 * Manage songs persistent operations.
 * <p/>
 * @author Michael
 */
public final class SongManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static volatile SongManager INSTANCE;
    private final SongSearchIndex index;
    private boolean indexIsClear;
    private SoftReference<SongDisplayable[]> cacheSongs = new SoftReference<>(null);
    private final Set<DatabaseListener> listeners;

    /**
     * Initialise the song database.
     */
    private SongManager() {
        listeners = new HashSet<>();
        indexIsClear = true;
        index = new SongSearchIndex();
    }

    /**
     * Get the singleton instance of this class. Return null if there was an
     * error with the database.
     * <p/>
     * @return the singleton instance of this class.
     */
    public static synchronized SongManager get() {
        if (INSTANCE == null) {
            if (HibernateUtil.init()) {
                INSTANCE = new SongManager();
            } else {
                return null;
            }
        }
        return INSTANCE;
    }

    /**
     * Get the underlying search index used by this database.
     * <p/>
     * @return the search index.
     */
    public SongSearchIndex getIndex() {
        return index;
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
        for (DatabaseListener listener : listeners) {
            listener.databaseChanged();
        }
    }

    public synchronized SongDisplayable[] getSongs() {
        return getSongs(null);
    }

    /**
     * Get all the songs in the database.
     * <p/>
     * @return an array of all the songs in the database.
     */
    public synchronized SongDisplayable[] getSongs(LoadingPane loadingPane) {
        if (cacheSongs.get() != null) {
            return cacheSongs.get();
        }
        final Set<SongDisplayable> songs = new TreeSet<>();
        HibernateUtil.execute((Session session) -> {
            List<Song> songsList = new SongDao(session).getSongs();
            for (int si = 0; si < songsList.size(); si++) {
                final int finalSi = si;
                if (loadingPane != null) {
                    Platform.runLater(() -> {
                        loadingPane.setProgress((double) finalSi / songsList.size());
                    });
                }
                Song song = songsList.get(si);
                try {
                    song.getTitle();
                } catch (Exception ex) {
                    /*
                     * Sometimes (rarely) a song can become corrupt - not entirely 
                     * sure why, but this allows us to load the database ok whilst
                     * still skipping over the corrupt entries.
                     */
                    LOGGER.log(Level.WARNING, "Song with id " + song.getId() + " is corrupt, skipping...", ex);
                    continue;
                }
                final String[] tags = new String[song.getTags().size()];
                for (int i = 0; i < song.getTags().size(); i++) {
                    tags[i] = song.getTags().get(i);
                }
                final SongDisplayable songDisplayable = new SongDisplayable.Builder(song.getTitle(),
                        song.getAuthor())
                        .ccli(song.getCcli())
                        .year(song.getYear())
                        .tags(tags)
                        .publisher(song.getPublisher())
                        .copyright(song.getCopyright())
                        .key(song.getKey())
                        .info(song.getInfo())
                        .capo(song.getCapo())
                        .translations(song.getTranslations())
                        .lyrics(song.getLyrics())
                        .id(song.getId()).get();
                final Theme theme = song.getTheme();
                final ThemeDTO themedto = ThemeDTO.getDTO(theme);
                for (TextSection section : songDisplayable.getSections()) {
                    section.setTheme(themedto);
                }
                songDisplayable.setTheme(themedto);
                songs.add(songDisplayable);
            }
            if (loadingPane != null) {
                Platform.runLater(() -> {
                    loadingPane.setProgress(-1);
                });
            }
        });

        if (indexIsClear) {
            indexIsClear = false;
            LOGGER.log(Level.INFO, "Adding {0} songs to index", songs.size());
            index.addAll(songs);
        }
        SongDisplayable[] songArr = songs.toArray(new SongDisplayable[songs.size()]);
        cacheSongs = new SoftReference<>(songArr);
        return songArr;
    }

    public boolean addSong(final SongDisplayable song, final boolean fireUpdate) {
        return addSong(new SongDisplayable[]{song}, fireUpdate);
    }

    public boolean addSong(final Collection<SongDisplayable> song, final boolean fireUpdate) {
        return addSong(song.toArray(new SongDisplayable[song.size()]), fireUpdate);
    }

    /**
     * Add a song to the database.
     * <p/>
     * @param songs the songs to add.
     * @param fireUpdate true if the update should be fired to listeners when
     * adding this song, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean addSong(final SongDisplayable[] songs, final boolean fireUpdate) {
        cacheSongs.clear();
        clearIndex();
        final List<SongDisplayable> adjustedSongs = new ArrayList<>();
        for (SongDisplayable song : songs) {
            if (song.getSections().length > 0) {
                adjustedSongs.add(song);
            }
        }
        if (adjustedSongs.isEmpty()) {
            return false;
        }
        try {
            HibernateUtil.execute((Session session) -> {
                for (SongDisplayable song : adjustedSongs) {
                    final boolean nullTheme = song.getSections()[0].getTheme() == null;
                    final boolean nullTags = song.getTags() == null;
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
                            nullTags ? new ArrayList<>() : Arrays.asList(song.getTags()),
                            song.getTranslations());
                    session.save(newSong);
                }
            });
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Couldn't add song", ex);
        }
        getSongs();
        if (fireUpdate) {
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
        return updateSong(song, true);
    }

    /**
     * Update a song in the database.
     * <p/>
     * @param song the song to update.
     * @param addIfNotFound true if the song should be added if it's not found,
     * false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean updateSong(final SongDisplayable song, boolean addIfNotFound) {
        index.remove(song);
        try {
            HibernateUtil.execute((Session session) -> {
                Song updatedSong;
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
                    updatedSong.setTags(nullTags ? new ArrayList<>() : Arrays.asList(song.getTags()));
                    updatedSong.setTitle(song.getTitle());
                    updatedSong.setTranslations(song.getTranslations());
                    updatedSong.setTheme(nullTheme ? ThemeDTO.DEFAULT_THEME.getTheme() : new Theme(song.getSections()[0].getTheme().getTheme()));
                    session.update(updatedSong);
                    index.add(song);
                } catch (ObjectNotFoundException e) {
                    LOGGER.log(Level.INFO, "Updating song that doesn't exist, adding instead");
                    addSong(song, true);
                }
            });
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Error with database update, try to remove and add...", ex);
            try {
                removeSong(song);
                LOGGER.log(Level.WARNING, "Try to add...", ex);
                addSong(song, false);
                return true;
            } catch (IllegalStateException ex2) {
                LOGGER.log(Level.WARNING, "Remove / add trick not working.", ex2);
                return false;
            }
        }

        return true;
    }

    /**
     * Remove a song from the database.
     * <p/>
     * @param song the song to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean removeSong(final SongDisplayable song) {
        LOGGER.log(Level.INFO, "Removing song {0}", song.getID());
        cacheSongs.clear();
        try {
            HibernateUtil.execute((Session session) -> {
                Song deletedSong = new SongDao(session).getSongById(song.getID());
                session.delete(deletedSong);
            });
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Couldn't remove song " + song.getID(), ex);
            return false;
        }
        index.remove(song);
        fireUpdate();
        LOGGER.log(Level.INFO, "Removed song {0}", song.getID());
        return true;
    }

    private void clearIndex() {
        index.clear();
        indexIsClear = true;
    }
}
