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

                public void run() {
                    while (true) {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException ex) {
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
        } catch (IOException ex) {
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

    public void registerBibleChangeListener(BibleChangeListener listener) {
        listeners.add(listener);
    }

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
