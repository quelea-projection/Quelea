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
package org.quelea.data.bible;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.lucene.BibleSearchIndex;
import org.quelea.services.lucene.SearchIndex;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * Loads and manages the available getBibles.
 * <p/>
 * @author Michael
 */
public final class BibleManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final BibleManager INSTANCE = new BibleManager();
    private final List<Bible> bibles;
    private final List<BibleChangeListener> listeners;
    private final SearchIndex<BibleChapter> index;
    private boolean indexInit;
    private final List<Runnable> onIndexInit;

    /**
     * Create a new bible manager.
     */
    private BibleManager() {
        bibles = new ArrayList<>();
        listeners = new ArrayList<>();
        index = new BibleSearchIndex();
        indexInit = false;
        onIndexInit = new ArrayList<>();
        loadBibles(false);
    }

    /**
     * Get the instance of this singleton class.
     * <p/>
     * @return the instance of this singleton class.
     */
    public static BibleManager get() {
        return INSTANCE;
    }

    /**
     * Run the given runnable as soon as the index is initialised, or
     * immediately if the index is currently initialised.
     * <p/>
     * @param r the runnable to run.
     */
    public void runOnIndexInit(Runnable r) {
        if(indexInit) {
            r.run();
        }
        else {
            onIndexInit.add(r);
        }
    }

    /**
     * Register a bible change listener on this bible manager. The listener will
     * be activated whenever a change occurs.
     * <p/>
     * @param listener the listener to register.
     */
    public void registerBibleChangeListener(BibleChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Notify all the listeners that a change has occurred.
     */
    private void updateListeners() {
        for(BibleChangeListener listener : listeners) {
            listener.updateBibles();
        }
    }

    /**
     * Get all the bibles held in this manager.
     * <p/>
     * @return all the getBibles.
     */
    public Bible[] getBibles() {
        return bibles.toArray(new Bible[bibles.size()]);
    }

    /**
     * Get the underlying search index used by this bible manager.
     * <p/>
     * @return the search index.
     */
    public SearchIndex<BibleChapter> getIndex() {
        return index;
    }
    
    /**
     * Reload bibles and trigger listeners.
     * <p/>
     */
    public void refresh() {
        loadBibles(false);
        updateListeners();
    }
    
    /**
     * Reload bibles, update search index and trigger listeners
     * <p/>
     */
    public void refreshAndLoad() {
        loadBibles(true);
        updateListeners();
    }

    /**
     * Reload all the bibles from the bibles directory into this bible manager.
     * <p/>
     * @param updateIndex update the search index with new bible structure
     */
    public void loadBibles(boolean updateIndex) {
        if(updateIndex) {
            indexInit = false;
        }
        bibles.clear();
        File biblesFile = QueleaProperties.get().getBibleDir();
        if(!biblesFile.exists()) {
            biblesFile.mkdir();
        }
        for(File file : biblesFile.listFiles()) {
            if(file.getName().toLowerCase().endsWith(".xml") || file.getName().toLowerCase().endsWith(".xmm")) {
                final Bible bible = Bible.parseBible(file);
                if(bible != null) {
                    bibles.add(bible);
                }
            }
        }
        if(updateIndex) {
            buildIndex();
        }
    }

    /**
     * Determine if the search index is initialised.
     * <p/>
     * @return true if the index is initialised, false otherwise.
     */
    public boolean isIndexInit() {
        return indexInit;
    }

    /**
     * Builds the search index from the current bibles.
     */
    public void buildIndex() {
        indexInit = false;
        final StatusPanel[] panel = new StatusPanel[1];
        if(QueleaApp.get().getMainWindow() != null) {
            Utils.fxRunAndWait(new Runnable() {
                @Override
                public void run() {
                    panel[0] = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("building.bible.index"));
                    panel[0].removeCancelButton();
                    panel[0].getProgressBar().setProgress(-1);
                }
            });
        }
        new Thread() {
            @Override
            public void run() {
                LOGGER.log(Level.INFO, "Adding bibles to index");
                List<BibleChapter> chapters = new ArrayList<>(bibles.size() * 66);
                for(Bible bible : bibles) {
                    LOGGER.log(Level.FINE, "Adding {0} bible to index", bible.getName());
                    index.clear();
                    for(BibleBook book : bible.getBooks()) {
                        chapters.addAll(Arrays.asList(book.getChapters()));
                    }
                    LOGGER.log(Level.FINE, "Added {0}.", bible.getName());
                }
                index.addAll(chapters);
                LOGGER.log(Level.INFO, "Finished Adding bibles to index");
                indexInit = true;
                for(Runnable r : onIndexInit) {
                    r.run();
                }
                onIndexInit.clear();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(panel[0] != null) {
                            panel[0].done();
                        }
                    }
                });
            }
        }.start();
    }
}
