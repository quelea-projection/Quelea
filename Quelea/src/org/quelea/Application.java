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

import java.io.File;
import javax.swing.JOptionPane;
import org.quelea.languages.LabelGrabber;
import org.quelea.sound.AudioPlayer;
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
    private LyricWindow stageWindow;
    private AudioPlayer audioPlayer = new AudioPlayer();

    /**
     * Get the singleton instance.
     * @return the instance.
     */
    public static Application get() {
        return INSTANCE;
    }
    
    /**
     * Open a given schedule file in Quelea.
     * @param file the file to open.
     */
    public void openSchedule(File file) {
        Schedule schedule = Schedule.fromFile(file);
        if (schedule == null) {
            JOptionPane.showMessageDialog(Application.get().getMainWindow(),
                    LabelGrabber.INSTANCE.getLabel("error.schedule.message"),
                    LabelGrabber.INSTANCE.getLabel("error.schedule.title"), JOptionPane.ERROR_MESSAGE, null);
        }
        else {
            Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().setSchedule(schedule);
        }
    }
    
    /**
     * Get Quelea's audio player.
     */
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    /**
     * Get the lyric window.
     * @return the lyric window.
     */
    public LyricWindow getLyricWindow() {
        return lyricWindow;
    }

    /**
     * Get the stage window.
     * @return the stage window.
     */
    public LyricWindow getStageWindow() {
        return stageWindow;
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
     * Set the stage window.
     * @param lyricWindow the stage window.
     */
    public void setStageWindow(LyricWindow lyricWindow) {
        this.stageWindow = lyricWindow;
    }

    /**
     * Set the main window.
     * @param mainWindow the main window.
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }
}
