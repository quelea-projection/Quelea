/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main;

import com.inet.jortho.SpellChecker;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.db.SongManager;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.servivces.languages.LabelGrabber;
import org.quelea.services.phonehome.PhoneHome;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.ShortcutManager;
import org.quelea.services.utils.UpdateChecker;
import org.quelea.services.utils.UserFileChecker;
import org.quelea.services.utils.Utils;
import org.quelea.splash.SplashStage;

/**
 * The main class, sets everything in motion...
 * <p/>
 * @author Michael
 */
public final class Main extends Application {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private MainWindow mainWindow;
    private DisplayStage fullScreenWindow;
    private DisplayStage stageWindow;

    /**
     * Starts the program off, this is the first thing that is executed by
     * Quelea when the program starts.
     */
    @Override
    public void start(Stage stage) {
        final SplashStage splashWindow = new SplashStage();
        splashWindow.show();

        new UserFileChecker(QueleaProperties.getQueleaUserHome()).checkUserFiles();

        final ObservableList<Screen> monitors = Screen.getScreens();
        Screen screen;
        LOGGER.log(Level.INFO, "Number of displays: {0}", monitors.size());

        int controlScreenProp = QueleaProperties.get().getControlScreen();
        final int controlScreen;
        final int projectorScreen = QueleaProperties.get().getProjectorScreen();
        final int stageScreen = QueleaProperties.get().getStageScreen();
        final int monitorNumber = monitors.size();

        if(controlScreenProp < monitorNumber) {
            controlScreen = controlScreenProp;
        }
        else {
            controlScreen = 0;
        }

        final boolean lyricsHidden;
        if(!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitorNumber || projectorScreen < 0)) {
            lyricsHidden = true;
        }
        else {
            lyricsHidden = false;
        }
        final boolean stageHidden;
        if(!QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitorNumber || stageScreen < 0)) {
            stageHidden = true;
        }
        else {
            stageHidden = false;
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(lyricsHidden) {
                    LOGGER.log(Level.INFO, "Hiding projector display on monitor 0 (base 0!)");
                    fullScreenWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), false);
                    fullScreenWindow.hide();
                }
                else if(QueleaProperties.get().isProjectorModeCoords()) {
                    LOGGER.log(Level.INFO, "Starting projector display: ", QueleaProperties.get().getProjectorCoords());
                    fullScreenWindow = new DisplayStage(QueleaProperties.get().getProjectorCoords(), false);
                }
                else {
                    LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
                    fullScreenWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(projectorScreen).getVisualBounds()), false);
                }
                QueleaApp.get().setAppWindow(fullScreenWindow);
                fullScreenWindow.toFront();

                if(stageHidden) {
                    LOGGER.log(Level.INFO, "Hiding stage display on monitor 0 (base 0!)");
                    stageWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), true);
                    stageWindow.hide();
                }
                else if(QueleaProperties.get().isStageModeCoords()) {
                    LOGGER.log(Level.INFO, "Starting stage display: ", QueleaProperties.get().getStageCoords());
                    stageWindow = new DisplayStage(QueleaProperties.get().getStageCoords(), true);
                }
                else {
                    LOGGER.log(Level.INFO, "Starting stage display on monitor {0} (base 0!)", stageScreen);
                    stageWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(stageScreen).getVisualBounds()), true);
                }
                QueleaApp.get().setStageWindow(stageWindow);
                stageWindow.toFront();
            }
        });

        LOGGER.log(Level.INFO, "Loading bibles");
        final Thread bibleLoader = new Thread() {
            @Override
            public void run() {
                BibleManager.get();
                LOGGER.log(Level.INFO, "Loading bibles done");
            }
        };
        bibleLoader.start();

        LOGGER.log(Level.INFO, "Registering dictionary");
        try {
            SpellChecker.registerDictionaries(new File("dictionaries").toURI().toURL(), "en", "en");
            SpellChecker.getOptions().setLanguageDisableVisible(false);
            SpellChecker.getOptions().setCaseSensitive(false);
        }
        catch(MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't load dictionaries", ex);
        }
        LOGGER.log(Level.INFO, "Registered dictionary");

        BibleManager.get().buildIndex();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(SongManager.get().errorOccurred()) {
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("already.running.title"), LabelGrabber.INSTANCE.getLabel("already.running.error"));
                    System.exit(1);
                }
                SongManager.get().getSongs(); //Add all the songs to the index
                OOUtils.attemptInit();
                mainWindow = new MainWindow(true);

                new UpdateChecker().checkUpdate(false, false, false); //Check updates
                PhoneHome.INSTANCE.phone(); //Phone home

                LOGGER.log(Level.INFO, "Registering canvases");
                mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(fullScreenWindow.getCanvas());
                mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(fullScreenWindow);
                if(lyricsHidden) {
                    fullScreenWindow.hide();
                }
                else {
                    fullScreenWindow.show();
                }
                mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(stageWindow.getCanvas());
                mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(stageWindow);
                if(stageHidden) {
                    stageWindow.hide();
                }
                else {
                    stageWindow.show();
                }
                LOGGER.log(Level.INFO, "Registered canvases.");

                if(QueleaProperties.get().getDragAndDrop()) {
                    Utils.enableDragAndDrop();
                    LOGGER.log(Level.INFO, "Enabled drag and drop functionality.");
                }

                LOGGER.log(Level.INFO, "Final loading bits");
                try {
                    bibleLoader.join(); //Make sure bibleloader has finished loading
                }
                catch(InterruptedException ex) {
                }
//                Utils.centreOnMonitor(mainWindow, controlScreen);
                mainWindow.toFront();
                new ShortcutManager().addShortcuts(mainWindow);
                LOGGER.log(Level.INFO, "Loaded everything.");

                if(!getParameters().getRaw().isEmpty()) {
                    String schedulePath = getParameters().getRaw().get(0);
                    LOGGER.log(Level.INFO, "Opening schedule through argument: {0}", schedulePath);
                    QueleaApp.get().openSchedule(new File(schedulePath));
                }

                mainWindow.show();
                QueleaApp.get().initialiseWatchers();
                splashWindow.hide();
                showWarning(monitorNumber);
            }
        });

    }

    /**
     * If it's appropriate, show the warning about only having 1 monitor.
     * <p/>
     * @param numMonitors the number of monitors.
     */
    private void showWarning(int numMonitors) {
        if(numMonitors <= 1 && QueleaProperties.get().showSingleMonitorWarning()) {
            Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("one.monitor.title"), LabelGrabber.INSTANCE.getLabel("one.monitor.warning"));
        }
    }
}
