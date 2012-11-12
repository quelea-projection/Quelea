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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 *
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
     *
     * @return the singleton instance of this class.
     */
    public static SongManager get() {
        return INSTANCE;
    }

    /**
     * Get the underlying search index used by this database.
     *
     * @return the search index.
     */
    public SearchIndex<SongDisplayable> getIndex() {
        return index;
    }

    /**
     * Determine if an error occurred initialising the database.
     *
     * @return true if an error occurred, false if all is ok.
     */
    public boolean errorOccurred() {
        return error;
    }

    /**
     * Register a database listener with this database.
     *
     * @param listener the listener.
     */
    public void registerDatabaseListener(DatabaseListener listener) {
        listeners.add(listener);
    }

    /**
     * Fire off the database listeners.
     */
    public void fireUpdate() {
        for (DatabaseListener listener : listeners) {
            listener.databaseChanged();
        }
    }

    /**
     * Get all the songs in the database.
     *
     * @return an array of all the songs in the database.
     */
    public SongDisplayable[] getSongs() {

        final List<SongDisplayable> songs = new ArrayList<>();
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                for (Song song : new SongDao(session).getSongs()) {
                    final String[] tags = new String[song.getTags().size()];
                    
                    for(int i = 0; i < song.getTags().size(); i++){
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
                    for (TextSection section : songDisplayable.getSections()) {
                        final Theme theme = song.getTheme();
                        section.setTheme(ThemeDTO.getDTO(theme));
                    }
                    songs.add(songDisplayable);
                }
            }
        });

        if (!addedToIndex) {
            addedToIndex = true;
            LOGGER.log(Level.INFO, "Adding songs to index");
            index.addAll(songs);
        }
        return songs.toArray(new SongDisplayable[songs.size()]);
    }

    /**
     * Add a song to the database.
     *
     * @param song the song to add.
     * @param fireUpdate true if the update should be fired to listeners when
     * adding this song, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean addSong(final SongDisplayable song, boolean fireUpdate) {
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                Song newSong = new Song(song.getTitle(),
                        song.getAuthor(),
                        song.getLyrics(false, false),
                        song.getCcli(),
                        song.getCopyright(),
                        song.getYear(),
                        song.getPublisher(),
                        song.getKey(), 
                        song.getCapo(), 
                        song.getInfo(),
                        new Theme(song.getSections()[0].getTheme().getTheme()),//@todo check if theme mapping is correct
                        Arrays.asList(song.getTags()));
                session.save(newSong);
            }
        });

        if (fireUpdate) {
            fireUpdate();
        }
        return true;
    }

    /**
     * Update a song in the database.
     *
     * @param song the song to update.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean updateSong(final SongDisplayable song) {
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                Song updatedSong = null;
                try {
                    updatedSong = new SongDao(session).getSongById(song.getID());
                    updatedSong.setAuthor(song.getAuthor());
                    updatedSong.setYear(song.getYear());
                    updatedSong.setCapo(song.getCapo());
                    updatedSong.setCcli(song.getCcli());
                    updatedSong.setCopyright(song.getCopyright());
                    updatedSong.setInfo(song.getInfo());
                    updatedSong.setLyrics(song.getLyrics(false, false));
                    updatedSong.setKey(song.getKey());
                    updatedSong.setPublisher(song.getPublisher());
                    updatedSong.setTags(Arrays.asList(song.getTags()));
                    updatedSong.setTitle(song.getTitle());
                    session.update(updatedSong);
                } catch (ObjectNotFoundException e) {
                    LOGGER.log(Level.INFO, "Updating song that doesn't exist, adding instead");
                    addSong(song, true);
                }

            }
        });
        fireUpdate();
        return true;
    }

    /**
     * Remove a song from the database.
     *
     * @param song the song to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean removeSong(final SongDisplayable song) {
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
