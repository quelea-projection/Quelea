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
package org.quelea.services.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.library.ImageListPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * This file watcher calls the update method on the ImageListPanel class when a
 * change has been made on the native file structure.
 * <p/>
 * @author Ben Goodwin
 */
public class ImageFileWatcher {

    private static final ImageFileWatcher INSTANCE = new ImageFileWatcher();
    private ImageListPanel imageLP;
    private Path imgPath = QueleaProperties.get().getImageDir().getAbsoluteFile().toPath();
    private WatchService watcher;
    private boolean running = true;
    private int count = 0;

    private ImageFileWatcher() {
        imageLP = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getImagePanel().getImagePanel();

        startWatching();
    }

    private void startWatching() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            imgPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            final Thread t = new Thread() {
                @Override
                public void run() {
                    final WatchKey key;
                    try {
                        key = watcher.take();
                    }
                    catch(InterruptedException ex) {
                        return;
                    }
                    while(!isInterrupted()) {
                        for(WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if(kind != StandardWatchEventKinds.OVERFLOW) {
                                Utils.fxRunAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageLP.refresh();
                                    }
                                });
                            }
                        }
                    }
                    try {
                        watcher.close();
                    }
                    catch(IOException ex) {
                        LoggerUtils.getLogger().log(Level.WARNING, "Could not close watcher, will cause memory leak.", ex);
                    }
                }
            };
            t.setName("file-watcher" + count);
            count++;
            t.start();
        }
        catch(IOException e) {
            LoggerUtils.getLogger().log(Level.WARNING, "Could not start watching underlying file structure for Image panel.", e);
        }
    }

    /**
     * Is called when the directory of the ImagePanel is changed </p>
     * <p/>
     * @param newDir The new image directory location
     */
    public void changeDir(File newDir) {
        imgPath = newDir.getAbsoluteFile().toPath();
        for(Thread th : Thread.getAllStackTraces().keySet()) {
            if(th.getName().startsWith("file-watcher")) {
                try {
                    th.interrupt();
                }
                catch(Exception e) {
                    LoggerUtils.getLogger().log(Level.WARNING, "Could not interupt current watcher thread - thread overload");
                    return;
                }
            }
        }
        startWatching();
    }

    /**
     * Returns the instance of this class </p>
     * <p/>
     * @return INSTANCE
     */
    public static ImageFileWatcher get() {
        return INSTANCE;
    }
}