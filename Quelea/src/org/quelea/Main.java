/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
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
package org.quelea;

import com.inet.jortho.SpellChecker;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import name.antonsmirnov.javafx.dialog.Dialog;
import org.quelea.bible.BibleManager;
import org.quelea.languages.LabelGrabber;
import org.quelea.phonehome.PhoneHome;
import org.quelea.powerpoint.OOUtils;
import org.quelea.splash.SplashWindow;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.UpdateChecker;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;

/**
 * The main class, sets everything in motion...
 * <p/>
 * @author Michael
 */
public final class Main {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static MainWindow mainWindow;
    private static LyricWindow fullScreenWindow;
    private static LyricWindow stageWindow;

    /**
     * Don't instantiate me. I bite.
     */
    private Main() {
        throw new AssertionError();
    }

    /**
     * Starts the program off, this is the first thing that is executed by
     * Quelea when the program starts.
     * <p/>
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        final SplashWindow splashWindow = new SplashWindow();
        splashWindow.setVisible(true);

        //Hack to initialise JavaFX
        new JFXPanel();

        new UserFileChecker(QueleaProperties.getQueleaUserHome()).checkUserFiles();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        LOGGER.log(Level.INFO, "Number of displays: {0}", gds.length);

        int controlScreenProp = QueleaProperties.get().getControlScreen();
        final int controlScreen;
        final int projectorScreen = QueleaProperties.get().getProjectorScreen();
        final int stageScreen = QueleaProperties.get().getStageScreen();

        if(gds.length <= controlScreenProp) {
            controlScreen = 0;
        }
        else {
            controlScreen = controlScreenProp;
        }
        final boolean lyricsHidden;
        if(!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= gds.length || projectorScreen < 0)) {
            lyricsHidden = true;
        }
        else {
            lyricsHidden = false;
        }
        final boolean stageHidden;
        if(!QueleaProperties.get().isStageModeCoords() && (stageScreen >= gds.length || stageScreen < 0)) {
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
                    fullScreenWindow = new LyricWindow(Utils.getBoundsFromRect(gds[0].getDefaultConfiguration().getBounds()), false);
                    fullScreenWindow.hide();
                }
                else if(QueleaProperties.get().isProjectorModeCoords()) {
                    LOGGER.log(Level.INFO, "Starting projector display: ", QueleaProperties.get().getProjectorCoords());
                    fullScreenWindow = new LyricWindow(QueleaProperties.get().getProjectorCoords(), false);
                }
                else {
                    LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
                    fullScreenWindow = new LyricWindow(Utils.getBoundsFromRect(gds[projectorScreen].getDefaultConfiguration().getBounds()), false);
                }
                Application.get().setLyricWindow(fullScreenWindow);
                fullScreenWindow.toFront();

                if(stageHidden) {
                    LOGGER.log(Level.INFO, "Hiding stage display on monitor 0 (base 0!)");
                    stageWindow = new LyricWindow(Utils.getBoundsFromRect(gds[0].getDefaultConfiguration().getBounds()), true);
                    stageWindow.hide();
                }
                else if(QueleaProperties.get().isStageModeCoords()) {
                    LOGGER.log(Level.INFO, "Starting stage display: ", QueleaProperties.get().getStageCoords());
                    stageWindow = new LyricWindow(QueleaProperties.get().getStageCoords(), true);
                }
                else {
                    LOGGER.log(Level.INFO, "Starting stage display on monitor {0} (base 0!)", stageScreen);
                    stageWindow = new LyricWindow(Utils.getBoundsFromRect(gds[stageScreen].getDefaultConfiguration().getBounds()), true);
                }
                Application.get().setStageWindow(stageWindow);
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

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(SongDatabase.get().errorOccurred()) {
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("already.running.title"), LabelGrabber.INSTANCE.getLabel("already.running.error"));
                    System.exit(1);
                }
                OOUtils.attemptInit();
                mainWindow = new MainWindow(true);

                new UpdateChecker().checkUpdate(false, false, false); //Check updates
                PhoneHome.INSTANCE.phone(); //Phone home

                LOGGER.log(Level.INFO, "Registering canvases");
                mainWindow.getMainPanel().getLivePanel().registerLyricCanvas(fullScreenWindow.getCanvas());
                mainWindow.getMainPanel().getLivePanel().registerLyricWindow(fullScreenWindow);
                if(lyricsHidden) {
                    fullScreenWindow.hide();
                }
                else {
                    fullScreenWindow.show();
                }
                mainWindow.getMainPanel().getLivePanel().registerLyricCanvas(stageWindow.getCanvas());
                mainWindow.getMainPanel().getLivePanel().registerLyricWindow(stageWindow);
                if(stageHidden) {
                    stageWindow.hide();
                }
                else {
                    stageWindow.show();
                }
                LOGGER.log(Level.INFO, "Registered canvases.");

                LOGGER.log(Level.INFO, "Final loading bits");
                try {
                    bibleLoader.join(); //Make sure bibleloader has finished loading
                }
                catch(InterruptedException ex) {
                }
//                Utils.centreOnMonitor(mainWindow, controlScreen);
                showWarning(gds.length);
                mainWindow.toFront();
                splashWindow.setVisible(false);
                mainWindow.show();
                new ShortcutManager().addShortcuts();
                BibleManager.get().buildIndex();
                LOGGER.log(Level.INFO, "Loaded everything.");

                if(args.length > 0) {
                    LOGGER.log(Level.INFO, "Opening schedule through argument: {0}", args[0]);
                    Application.get().openSchedule(new File(args[0]));
                }
            }
        });
    }

    /**
     * If it's appropriate, show the warning about only having 1 monitor.
     * <p/>
     * @param numMonitors the number of monitors.
     */
    private static void showWarning(int numMonitors) {
        if(numMonitors <= 1 && QueleaProperties.get().showSingleMonitorWarning()) {
            Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("one.monitor.title"), LabelGrabber.INSTANCE.getLabel("one.monitor.warning"));
        }
    }
}
