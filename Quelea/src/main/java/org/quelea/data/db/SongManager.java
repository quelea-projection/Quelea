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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
 *
 * @author Michael
 */
public final class SongManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static volatile SongManager INSTANCE;
    private final SongSearchIndex index;
    /**
     * All the songs in sorted order, or null until they've first been loaded.
     * After that it's kept up to date on every change, so the database never
     * needs reading again. The search index keeps hold of the same songs.
     */
    private List<SongDisplayable> cacheSongs;
    private volatile CountDownLatch indexBuilt = new CountDownLatch(0);
    private final Set<DatabaseListener> listeners;

    /**
     * Initialise the song database.
     */
    private SongManager() {
        listeners = new HashSet<>();
        index = new SongSearchIndex();
    }

    /**
     * Get the singleton instance of this class. Return null if there was an
     * error with the database.
     * <p/>
     *
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
     * Get the underlying search index used by this database. The index is built
     * in the background after the songs are loaded, so this waits for that to
     * finish; don't call it on the JavaFX thread.
     * <p/>
     *
     * @return the search index.
     */
    public SongSearchIndex getIndex() {
        awaitIndex();
        return index;
    }

    /**
     * Wait until the search index has been built, if that's in progress.
     */
    private void awaitIndex() {
        try {
            indexBuilt.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Register a database listener with this database.
     * <p/>
     *
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
     *
     * @return an array of all the songs in the database.
     */
    public synchronized SongDisplayable[] getSongs(LoadingPane loadingPane) {
        if (cacheSongs == null) {
            cacheSongs = loadSongs(loadingPane);
            buildIndex(new ArrayList<>(cacheSongs));
        }
        return cacheSongs.toArray(new SongDisplayable[0]);
    }

    /**
     * Read all the songs from the database. One bad row makes the fast query
     * fail as a whole, so if that happens fall back to reading them one by one,
     * which can skip over the bad rows.
     */
    private List<SongDisplayable> loadSongs(LoadingPane loadingPane) {
        try {
            return readSongs(loadingPane, SongDao::getSongs);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Couldn't load all the songs at once, loading them one by one", ex);
            return readSongs(loadingPane, SongDao::getSongsOneByOne);
        }
    }

    private List<SongDisplayable> readSongs(LoadingPane loadingPane, Function<SongDao, List<Song>> query) {
        final Set<SongDisplayable> songs = new TreeSet<>();
        HibernateUtil.execute((Session session) -> {
            List<Song> songsList = query.apply(new SongDao(session));
            final int progressStep = Math.max(1, songsList.size() / 100);
            for (int si = 0; si < songsList.size(); si++) {
                final int finalSi = si;
                if (loadingPane != null && si % progressStep == 0) {
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
                songs.add(toDisplayable(song));
            }
            if (loadingPane != null) {
                Platform.runLater(() -> {
                    loadingPane.setProgress(-1);
                });
            }
        });
        return new ArrayList<>(songs);
    }

    /**
     * Build the search index in the background, so the song list doesn't have
     * to wait for it. Anything that needs the index waits for this to finish.
     */
    private void buildIndex(List<SongDisplayable> songs) {
        final CountDownLatch built = new CountDownLatch(1);
        indexBuilt = built;
        Thread thread = new Thread(() -> {
            try {
                LOGGER.log(Level.INFO, "Adding {0} songs to index", songs.size());
                index.addAll(songs);
            } finally {
                built.countDown();
            }
        }, "song-index-builder");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Create the displayable for a song in the database. This must be called
     * with an open session.
     */
    private static SongDisplayable toDisplayable(Song song) {
        final SongDisplayable songDisplayable = new SongDisplayable.Builder(song.getTitle(),
                song.getAuthor())
                .ccli(song.getCcli())
                .year(song.getYear())
                .publisher(song.getPublisher())
                .copyright(song.getCopyright())
                .key(song.getKey())
                .info(song.getInfo())
                .capo(song.getCapo())
                .translations(song.getTranslations())
                .sequence(song.getSequence())
                .lyrics(song.getLyrics())
                .id(song.getId()).get();
        final Theme theme = song.getTheme();
        final ThemeDTO themedto = ThemeDTO.getDTO(theme);
        for (TextSection section : songDisplayable.getSections()) {
            section.setTheme(themedto);
        }
        songDisplayable.setTheme(themedto);
        return songDisplayable;
    }

    /**
     * Copy the translations of a song, so the song in the cache doesn't share
     * them with the song that was passed in. The translations can be null.
     */
    private static HashMap<String, String> copyOf(HashMap<String, String> translations) {
        return translations == null ? null : new HashMap<>(translations);
    }

    /**
     * Add a song to the sorted cache.
     *
     * @return true if it was added, false if an identical song is already there
     * (the list only ever shows one of those.)
     */
    private boolean insertIntoCache(SongDisplayable song) {
        int position = Collections.binarySearch(cacheSongs, song);
        if (position >= 0) {
            return false;
        }
        cacheSongs.add(-position - 1, song);
        return true;
    }

    public boolean addSong(final SongDisplayable song, final boolean fireUpdate) {
        return addSong(new SongDisplayable[]{song}, fireUpdate);
    }

    public boolean addSong(final Collection<SongDisplayable> song, final boolean fireUpdate) {
        return addSong(song.toArray(new SongDisplayable[song.size()]), fireUpdate);
    }

    /**
     * Add a song to the database. The given songs get the id they've been
     * stored with.
     * <p/>
     *
     * @param songs      the songs to add.
     * @param fireUpdate true if the update should be fired to listeners when
     *                   adding this song, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean addSong(final SongDisplayable[] songs, final boolean fireUpdate) {
        awaitIndex();
        final List<SongDisplayable> adjustedSongs = new ArrayList<>();
        for (SongDisplayable song : songs) {
            if (song.getSectionsWithoutSequence().length > 0) {
                adjustedSongs.add(song);
            }
        }
        if (adjustedSongs.isEmpty()) {
            return false;
        }
        final List<SongDisplayable> storedSongs = new ArrayList<>();
        try {
            HibernateUtil.execute((Session session) -> {
                for (SongDisplayable song : adjustedSongs) {
                    final boolean nullTheme = song.getSections()[0].getTheme() == null;
                    Song newSong = new Song(song.getTitle(),
                            song.getAuthor(),
                            song.getLyrics(true, true, false),
                            song.getCcli(),
                            song.getCopyright(),
                            song.getYear(),
                            song.getPublisher(),
                            song.getKey(),
                            song.getCapo(),
                            song.getInfo(),
                            nullTheme ? ThemeDTO.DEFAULT_THEME.getTheme() : new Theme(song.getSections()[0].getTheme().getTheme()),
                            copyOf(song.getTranslations()),
                            song.getSequence());
                    session.save(newSong);
                    storedSongs.add(toDisplayable(newSong));
                }
            });
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Couldn't add song", ex);
            return false;
        }
        for (int i = 0; i < adjustedSongs.size(); i++) {
            adjustedSongs.get(i).setID(storedSongs.get(i).getID());
        }
        if (cacheSongs != null) {
            index.addAll(storedSongs.stream().filter(this::insertIntoCache).collect(Collectors.toList()));
        }
        if (fireUpdate) {
            fireUpdate();
        }
        return true;
    }

    /**
     * Update a song in the database.
     * <p/>
     *
     * @param song the song to update.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean updateSong(final SongDisplayable song) {
        return updateSong(song, true);
    }

    /**
     * Update a song in the database.
     * <p/>
     *
     * @param song          the song to update.
     * @param addIfNotFound true if the song should be added if it's not found,
     *                      false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean updateSong(final SongDisplayable song, boolean addIfNotFound) {
        awaitIndex();
        final SongDisplayable[] storedSong = new SongDisplayable[1];
        if (song.getID() > 0) {
            try {
                HibernateUtil.execute((Session session) -> {
                    Song updatedSong;
                    final boolean nullTheme = song.getSections()[0].getTheme() == null;
                    try {
                        updatedSong = new SongDao(session).getSongById(song.getID());
                        updatedSong.setAuthor(song.getAuthor());
                        updatedSong.setYear(song.getYear());
                        updatedSong.setCapo(song.getCapo());
                        updatedSong.setCcli(song.getCcli());
                        updatedSong.setCopyright(song.getCopyright());
                        updatedSong.setInfo(song.getInfo());
                        updatedSong.setSequence(song.getSequence());
                        updatedSong.setLyrics(song.getLyrics(true, true, false));
                        updatedSong.setKey(song.getKey());
                        updatedSong.setPublisher(song.getPublisher());
                        updatedSong.setTitle(song.getTitle());
                        updatedSong.setTranslations(copyOf(song.getTranslations()));
                        updatedSong.setTheme(nullTheme ? ThemeDTO.DEFAULT_THEME.getTheme() : new Theme(song.getSections()[0].getTheme().getTheme()));
                        session.update(updatedSong);
                        storedSong[0] = toDisplayable(updatedSong);
                    } catch (ObjectNotFoundException e) {
                        LOGGER.log(Level.INFO, "Updating song that doesn't exist");
                    }
                });
            } catch (IllegalStateException ex) {
                LOGGER.log(Level.WARNING, "Error with database update, try to remove and add...", ex);
                try {
                    removeSong(song);
                    LOGGER.log(Level.WARNING, "Try to add...", ex);
                    addSong(song, true);
                    return true;
                } catch (IllegalStateException ex2) {
                    LOGGER.log(Level.WARNING, "Remove / add trick not working.", ex2);
                    return false;
                }
            }
        }
        if (storedSong[0] == null) {
            if (addIfNotFound) {
                addSong(song, true);
            }
            return addIfNotFound;
        }
        if (cacheSongs != null) {
            cacheSongs.removeIf(cached -> cached.getID() == song.getID());
            if (insertIntoCache(storedSong[0])) {
                index.update(storedSong[0]);
            } else {
                index.remove(storedSong[0]);
            }
        }
        return true;
    }

    /**
     * Remove a song from the database.
     * <p/>
     *
     * @param song the song to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean removeSong(final SongDisplayable song) {
        return removeSongs(List.of(song));
    }

    /**
     * Remove songs from the database.
     * <p/>
     *
     * @param songs the songs to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean removeSongs(final List<SongDisplayable> songs) {
        awaitIndex();
        List<Long> ids = songs.stream().map(SongDisplayable::getID).collect(Collectors.toList());
        LOGGER.log(Level.INFO, "Removing songs {0}", ids);
        try {
            HibernateUtil.execute((Session session) -> {
                for (SongDisplayable song : songs) {
                    Song deletedSong = new SongDao(session).getSongById(song.getID());
                    session.delete(deletedSong);
                }
            });
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Couldn't remove songs " + ids, ex);
            return false;
        }
        if (cacheSongs != null) {
            final Set<Long> removedIds = new HashSet<>(ids);
            cacheSongs.removeIf(cached -> removedIds.contains(cached.getID()));
            index.removeAll(songs);
        }
        fireUpdate();
        LOGGER.log(Level.INFO, "Removed song {0}", ids);
        return true;
    }
}
