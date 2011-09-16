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
package org.quelea.bible;

import java.nio.file.WatchEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import org.quelea.utils.QueleaProperties;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Loads and manages the available getBibles.
 * @author Michael
 */
public final class BibleManager {

    private static final BibleManager INSTANCE = new BibleManager();
    private final List<Bible> bibles;
    private final List<BibleChangeListener> listeners;

    /**
     * Create a new bible manager.
     */
    private BibleManager() {
        bibles = new ArrayList<>();
        listeners = new ArrayList<>();
        loadBibles();
        startWatching();
    }

    /**
     * Start the watcher thread.
     */
    private void startWatching() {
        try {
            final WatchService watcher = FileSystems.getDefault().newWatchService();
            final Path biblePath = FileSystems.getDefault().getPath(QueleaProperties.get().getBibleDir().getAbsolutePath());
            biblePath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            new Thread() {

                @SuppressWarnings("unchecked")
                public void run() {
                    while (true) {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        }
                        catch (InterruptedException ex) {
                            return;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == OVERFLOW) {
                                continue;
                            }

                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();
                            if (!filename.toFile().toString().toLowerCase().endsWith(".xml")) {
                                continue;
                            }

                            if (!key.reset()) {
                                break;
                            }
                            loadBibles();
                            updateListeners();

                        }
                    }
                }
            }.start();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get the instance of this singleton class.
     * @return the instance of this singleton class.
     */
    public static BibleManager get() {
        return INSTANCE;
    }

    /**
     * Register a bible change listener on this bible manager. The listener
     * will be activated whenever a change occurs.
     * @param listener the listener to register.
     */
    public void registerBibleChangeListener(BibleChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Notify all the listeners that a change has occurred.
     */
    private void updateListeners() {
        for (BibleChangeListener listener : listeners) {
            listener.updateBibles();
        }
    }

    /**
     * Get all the bibles held in this manager.
     * @return all the getBibles.
     */
    public Bible[] getBibles() {
        return bibles.toArray(new Bible[bibles.size()]);
    }

    /**
     * Reload all the bibles from the bibles directory into this bible manager.
     */
    public void loadBibles() {
        bibles.clear();
        File biblesFile = QueleaProperties.get().getBibleDir();
        if (!biblesFile.exists()) {
            biblesFile.mkdir();
        }
        for (File file : biblesFile.listFiles()) {
            if (file.getName().toLowerCase().endsWith(".xml")) {
                Bible bible = Bible.parseBible(file);
                if (bible != null) {
                    bibles.add(bible);
                }
            }
        }
    }
}
