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
package org.quelea.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.application.Platform;
import org.quelea.QueleaApp;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.library.ImageListPanel;

/**
 * This file watcher calls the update method on the ImageListPanel class when a
 * change has been made on the native file structure.
 *
 * @author Ben Goodwin
 */
public class ImageFileWatcher {

    private static final ImageFileWatcher INSTANCE = new ImageFileWatcher();
    private ImageListPanel imageLP;
    private Path imgPath = QueleaProperties.get().getImageDir().getAbsoluteFile().toPath();
    private WatchService watcher;
    private boolean running = true;
    private Thread t;

    private ImageFileWatcher() {
        imageLP = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getImagePanel().getImagePanel();

        startWatching();
    }

    private void startWatching() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            imgPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            System.out.println("0");
            t = new Thread() {
                @Override
                public void run() {
                    System.out.println("1");
                    final WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        return;
                    }
                    System.out.println("2");
                    while (running) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            System.out.println("3");
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }
                            System.out.println("4");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("5");
                                    imageLP.refresh();
                                    System.out.println("6");
                                }
                            });
                        }
                    }
                }
            };
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Is called when the directory of the ImagePanel is changed </p>
     *
     * @param newDir The new image directory location
     */
    public void changeDir(File newDir) {
        imgPath = newDir.getAbsoluteFile().toPath();
        running = false;
        try {
            watcher.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        running = true;
        startWatching();
    }

    /**
     * Returns the instance of this class </p>
     *
     * @return INSTANCE
     */
    public static ImageFileWatcher get() {
        return INSTANCE;
    }
}