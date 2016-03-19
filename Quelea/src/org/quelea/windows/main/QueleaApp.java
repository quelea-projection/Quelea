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
package org.quelea.windows.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.javafx.dialog.Dialog;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.server.AutoDetectServer;
import org.quelea.server.MobileLyricsServer;
import org.quelea.server.RemoteControlServer;
import org.quelea.services.languages.LabelGrabber;

/**
 * A singleton class for grabbing application wide objects with ease such as the main window.
 * @author Michael
 */
public class QueleaApp {

    private static final QueleaApp INSTANCE = new QueleaApp();
    private MainWindow mainWindow;
    private DisplayStage projectionWindow;
    private DisplayStage stageWindow;
    private MobileLyricsServer mls;
    private RemoteControlServer rcs;
    private AutoDetectServer ads;
    private List<Runnable> runnables = new ArrayList<>();
    private volatile boolean loaded;

    /**
     * Get the singleton instance.
     * @return the instance.
     */
    public static QueleaApp get() {
        return INSTANCE;
    }
    
    public synchronized void doOnLoad(Runnable r) {
        if(loaded) {
            r.run();
        }
        else {
            runnables.add(r);
        }
    }
    
    public synchronized void doneLoading() {
        loaded = true;
        for(Runnable r : runnables) {
            r.run();
        }
    }
    
    /**
     * Open a given schedule file in Quelea.
     * @param file the file to open.
     */
    public void openSchedule(File file) {
        Schedule schedule = Schedule.fromFile(file);
        if (schedule == null) {
            Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.schedule.title"), LabelGrabber.INSTANCE.getLabel("error.schedule.message"));
        }
        else {
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().setSchedule(schedule);
        }
    }

    /**
     * Get the projection window.
     * @return the projection window.
     */
    public DisplayStage getProjectionWindow() {
        return projectionWindow;
    }

    /**
     * Get the stage window.
     * @return the stage window.
     */
    public DisplayStage getStageWindow() {
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
     * Get the current mobile lyrics server in use. Null if none is in use.
     * <p>
     * @return the current mobile lyrics server in use.
     */
    public MobileLyricsServer getMobileLyricsServer() {
        return mls;
    }
    /**
     * Get the current remote control server in use. Null if none is in use.
     * <p>
     * @return the current remote control server in use.
     */
    public RemoteControlServer getRemoteControlServer() {
        return rcs;
    }
    /**
     * Set the mobile lyrics server currently in use.
     * @param mls the mobile lyrics server.
     */
    public void setMobileLyricsServer(MobileLyricsServer mls) {
        this.mls = mls;
    }
    
    /**
     * Set the remote control server currently in use.
     * @param rcs the mobile lyrics server.
     */
    public void setRemoteControlServer(RemoteControlServer rcs) {
        this.rcs = rcs;
    }

    /**
     * Set the projection window.
     * @param projectionWindow the projection window.
     */
    public void setProjectionWindow(DisplayStage projectionWindow) {
        this.projectionWindow = projectionWindow;
    }

    /**
     * Set the stage window.
     * @param lyricWindow the stage window.
     */
    public void setStageWindow(DisplayStage lyricWindow) {
        this.stageWindow = lyricWindow;
    }

    /**
     * Set the main window.
     * @param mainWindow the main window.
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setAutoDetectServer(AutoDetectServer ads) {
        this.ads = ads;
    }
}
