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
package org.quelea;

import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.StatusPanelGroup;

/**
 * A singleton class for grabbing application wide objects with ease such as the main window.
 * @author Michael
 */
public class Application {

    private static final Application INSTANCE = new Application();
    private MainWindow mainWindow;
    private LyricWindow lyricWindow;

    /**
     * Get the singleton instance.
     * @return the instance.
     */
    public static Application get() {
        return INSTANCE;
    }

    /**
     * Get the lyric window.
     * @return the lyric window.
     */
    public LyricWindow getLyricWindow() {
        return lyricWindow;
    }

    /**
     * Get the main window.
     * @return the main window.
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    /**
     * Get the status panel group. Shortcut method but provided here for
     * convenience.
     * @return the status panel group.
     */
    public StatusPanelGroup getStatusGroup() {
        return mainWindow.getMainPanel().getStatusPanelGroup();
    }

    /**
     * Set the lyric window.
     * @param lyricWindow the lyric window.
     */
    public void setLyricWindow(LyricWindow lyricWindow) {
        this.lyricWindow = lyricWindow;
    }

    /**
     * Set the main window.
     * @param mainWindow the main window.
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }
}
