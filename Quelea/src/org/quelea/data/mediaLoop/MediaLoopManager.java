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
package org.quelea.data.mediaLoop;

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
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.quelea.data.ThemeDTO;
import org.quelea.data.db.HibernateUtil;
import org.quelea.data.db.model.MediaLoop;
import org.quelea.data.db.model.Theme;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.lucene.MediaLoopSearchIndex;
import org.quelea.services.lucene.SearchIndex;
import org.quelea.services.lucene.MediaLoopSearchIndex;
import org.quelea.services.utils.DatabaseListener;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

/**
 * Manage media loops persistent operations.
 * <p/>
 * @author Greg
 */
public final class MediaLoopManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static volatile MediaLoopManager INSTANCE;
    private final SearchIndex<MediaLoopDisplayable> index;
    private boolean indexIsClear;
    private SoftReference<MediaLoopDisplayable[]> cacheMediaLoops = new SoftReference<>(null);
    private final Set<DatabaseListener> listeners;

    /**
     * Initialise the Media loop database.
     */
    private MediaLoopManager() {
        listeners = new HashSet<>();
        indexIsClear = true;
        index = new MediaLoopSearchIndex();
    }

    /**
     * Get the singleton instance of this class. Return null if there was an
     * error with the database.
     * <p/>
     * @return the singleton instance of this class.
     */
    public static synchronized MediaLoopManager get() {
        if (INSTANCE == null) {
            if (HibernateUtil.init()) {
                INSTANCE = new MediaLoopManager();
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
    public SearchIndex<MediaLoopDisplayable> getIndex() {
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

    /**
     * Get all the mediaLoops in the database.
     * <p/>
     * @return an array of all the mediaLoops in the database.
     */
    public synchronized MediaLoopDisplayable[] getMediaLoops() {
//        if(Platform.isFxApplicationThread()) {
//            LOGGER.log(Level.WARNING, "getMediaLoops() should not be called on platform thread!", new RuntimeException("Debug exception"));
//        }
        if (cacheMediaLoops.get() != null) {
            return cacheMediaLoops.get();
        }
        final Set<MediaLoopDisplayable> mediaLoops = new TreeSet<>();
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                for (final MediaLoop mediaLoop : new MediaLoopDao(session).getMediaLoops()) {
                    Utils.fxRunAndWait(new Runnable() {

                        @Override
                        public void run() {

                            final MediaLoopDisplayable mediaLoopDisplayable = new MediaLoopDisplayable.Builder(mediaLoop.getTitle(),
                                    mediaLoop.getMedia())
                                    .id(mediaLoop.getId()).get();
                            mediaLoops.add(mediaLoopDisplayable);
                        }
                    });
                }
            }
        });

        if (indexIsClear) {
            indexIsClear = false;
            LOGGER.log(Level.INFO, "Adding {0} mediaLoops to index", mediaLoops.size());
            index.addAll(mediaLoops);
        }
        MediaLoopDisplayable[] mediaLoopArr = mediaLoops.toArray(new MediaLoopDisplayable[mediaLoops.size()]);
        cacheMediaLoops = new SoftReference<>(mediaLoopArr);
        return mediaLoopArr;
    }

    /**
     * Add Media loop to database
     *
     * @param mediaLoop the media loop to add
     * @param fireUpdate true if the update should be fired to listeners when
     * adding this mediaLoop, false otherwise.
     * @return true if the operation succeeded, false otherwise
     */
    public boolean addMediaLoop(final MediaLoopDisplayable mediaLoop, final boolean fireUpdate) {
        return addMediaLoop(new MediaLoopDisplayable[]{mediaLoop}, fireUpdate);
    }

    /**
     * Add media loo to the database.
     *
     * @param mediaLoop the media loops to add
     * @param fireUpdate true if the update should be fired to listeners when
     * adding this mediaLoop, false otherwise.
     * @return true if the operation succeeded, false otherwise
     */
    public boolean addMediaLoop(final Collection<MediaLoopDisplayable> mediaLoop, final boolean fireUpdate) {
        return addMediaLoop(mediaLoop.toArray(new MediaLoopDisplayable[mediaLoop.size()]), fireUpdate);
    }

    /**
     * Add a mediaLoop to the database.
     * <p/>
     * @param mediaLoops the mediaLoops to add.
     * @param fireUpdate true if the update should be fired to listeners when
     * adding this mediaLoop, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean addMediaLoop(final MediaLoopDisplayable[] mediaLoops, final boolean fireUpdate) {
        cacheMediaLoops.clear();
        clearIndex();
        final List<MediaLoopDisplayable> adjustedMediaLoops = new ArrayList<>();
        for (MediaLoopDisplayable mediaLoop : mediaLoops) {
            if (mediaLoop.getMediaFiles().size() > 0) {
                adjustedMediaLoops.add(mediaLoop);
            }
        }
        if (adjustedMediaLoops.isEmpty()) {
            return false;
        }
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                for (MediaLoopDisplayable mediaLoop : adjustedMediaLoops) {
                     MediaLoop newMediaLoop = new MediaLoop(mediaLoop.getPreviewText(),
                            mediaLoop.getMediaFiles());
                    session.save(newMediaLoop);
                }
            }
        });
        getMediaLoops();
        if (fireUpdate) {
            fireUpdate();
        }
        return true;
    }

    /**
     * Update a mediaLoop in the database.
     * <p/>
     * @param mediaLoop the mediaLoop to update.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean updateMediaLoop(final MediaLoopDisplayable mediaLoop) {
        return updateMediaLoop(mediaLoop, true);
    }

    /**
     * Update a mediaLoop in the database.
     * <p/>
     * @param mediaLoop the mediaLoop to update.
     * @param addIfNotFound true if the mediaLoop should be added if it's not
     * found, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean updateMediaLoop(final MediaLoopDisplayable mediaLoop, boolean addIfNotFound) {
        index.remove(mediaLoop);
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                MediaLoop updatedMediaLoop;
                try {
                    updatedMediaLoop = new MediaLoopDao(session).getMediaLoopById(mediaLoop.getID());
                    updatedMediaLoop.setTitle(mediaLoop.getPreviewText());
                    updatedMediaLoop.setMedia(mediaLoop.getMediaFiles());
                    session.update(updatedMediaLoop);
                    index.add(mediaLoop);
                } catch (ObjectNotFoundException e) {
                    LOGGER.log(Level.INFO, "Updating media loop that doesn't exist, adding instead");
                    addMediaLoop(mediaLoop, true);
                }
            }
        });

        return true;
    }

    /**
     * Remove a mediaLoop from the database.
     * <p/>
     * @param mediaLoop the mediaLoop to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public synchronized boolean removeMediaLoop(final MediaLoopDisplayable mediaLoop) {
        cacheMediaLoops.clear();
        HibernateUtil.execute(new HibernateUtil.SessionCallback() {
            @Override
            public void execute(Session session) {
                MediaLoop deletedMediaLoop = new MediaLoopDao(session).getMediaLoopById(mediaLoop.getID());
                session.delete(deletedMediaLoop);
            }
        });
        index.remove(mediaLoop);
        fireUpdate();
        return true;
    }

    /**
     * Clears the index
     */
    private void clearIndex() {
        index.clear();
        indexIsClear = true;
    }
}
