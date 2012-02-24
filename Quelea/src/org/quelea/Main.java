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
import javax.swing.*;
import org.quelea.bible.BibleManager;
import org.quelea.languages.LabelGrabber;
import org.quelea.phonehome.PhoneHome;
import org.quelea.splash.SplashWindow;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.UpdateChecker;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

/**
 * The main class, sets everything in motion...
 *
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
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        
        org.simplericity.macify.eawt.Application macApp = new org.simplericity.macify.eawt.DefaultApplication();
        macApp.addApplicationListener(new ApplicationListener() {

            @Override
            public void handleAbout(ApplicationEvent ae) {
                System.out.println("ABOUT");
            }

            @Override
            public void handleOpenApplication(ApplicationEvent ae) {
                
            }

            @Override
            public void handleOpenFile(ApplicationEvent ae) {
                
            }

            @Override
            public void handlePreferences(ApplicationEvent ae) {
                
            }

            @Override
            public void handlePrintFile(ApplicationEvent ae) {
                
            }

            @Override
            public void handleQuit(ApplicationEvent ae) {
                
            }

            @Override
            public void handleReOpenApplication(ApplicationEvent ae) {
                
            }
        });

        final SplashWindow splashWindow = new SplashWindow();
        splashWindow.setVisible(true);

        new UserFileChecker(QueleaProperties.getQueleaUserHome()).checkUserFiles();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        LOGGER.log(Level.INFO, "Number of displays: {0}", gds.length);

        int controlScreenProp = QueleaProperties.get().getControlScreen();
        final int controlScreen;
        int projectorScreen = QueleaProperties.get().getProjectorScreen();
        int stageScreen = QueleaProperties.get().getStageScreen();

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
        if(lyricsHidden) {
            LOGGER.log(Level.INFO, "Hiding projector display on monitor 0 (base 0!)");
            fullScreenWindow = new LyricWindow(gds[0].getDefaultConfiguration().getBounds(), false);
            fullScreenWindow.setVisible(false);
        }
        else if(QueleaProperties.get().isProjectorModeCoords()) {
            LOGGER.log(Level.INFO, "Starting projector display: ", QueleaProperties.get().getProjectorCoords());
            fullScreenWindow = new LyricWindow(QueleaProperties.get().getProjectorCoords(), false);
        }
        else {
            LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
            fullScreenWindow = new LyricWindow(gds[projectorScreen].getDefaultConfiguration().getBounds(), false);
        }
        Application.get().setLyricWindow(fullScreenWindow);
        fullScreenWindow.toFront();

        final boolean stageHidden;
        if(!QueleaProperties.get().isStageModeCoords() && (stageScreen >= gds.length || stageScreen < 0)) {
            stageHidden = true;
        }
        else {
            stageHidden = false;
        }
        if(stageHidden) {
            LOGGER.log(Level.INFO, "Hiding stage display on monitor 0 (base 0!)");
            stageWindow = new LyricWindow(gds[0].getDefaultConfiguration().getBounds(), true);
            stageWindow.setVisible(false);
        }
        else if(QueleaProperties.get().isStageModeCoords()) {
            LOGGER.log(Level.INFO, "Starting stage display: ", QueleaProperties.get().getStageCoords());
            stageWindow = new LyricWindow(QueleaProperties.get().getStageCoords(), true);
        }
        else {
            LOGGER.log(Level.INFO, "Starting stage display on monitor {0} (base 0!)", stageScreen);
            stageWindow = new LyricWindow(gds[stageScreen].getDefaultConfiguration().getBounds(), true);
        }
        Application.get().setStageWindow(stageWindow);
        stageWindow.toFront();

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

        setLaf();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if(SongDatabase.get().errorOccurred()) {
                    JOptionPane.showMessageDialog(null, LabelGrabber.INSTANCE.getLabel("already.running.error"), LabelGrabber.INSTANCE.getLabel("already.running.title"), JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                mainWindow = new MainWindow(true);

                new UpdateChecker(mainWindow).checkUpdate(false, false, false); //Check updates
                PhoneHome.INSTANCE.phone(); //Phone home

                LOGGER.log(Level.INFO, "Registering canvases");
                mainWindow.getMainPanel().getLivePanel().registerLyricCanvas(fullScreenWindow.getCanvas());
                mainWindow.getMainPanel().getLivePanel().registerLyricWindow(fullScreenWindow);
                mainWindow.getMainPanel().getLivePanel().registerVideoCanvas(fullScreenWindow.getCanvas());
                fullScreenWindow.setVisible(!lyricsHidden);
                mainWindow.getMainPanel().getLivePanel().registerLyricCanvas(stageWindow.getCanvas());
                mainWindow.getMainPanel().getLivePanel().registerLyricWindow(stageWindow);
                mainWindow.getMainPanel().getLivePanel().registerVideoCanvas(stageWindow.getCanvas());
                stageWindow.setVisible(!stageHidden);
                LOGGER.log(Level.INFO, "Registered canvases.");

                LOGGER.log(Level.INFO, "Final loading bits");
                try {
                    bibleLoader.join(); //Make sure bibleloader has finished loading
                }
                catch(InterruptedException ex) {
                }
                Utils.centreOnMonitor(mainWindow, controlScreen);
                showWarning(gds.length);
                mainWindow.toFront();
                splashWindow.setVisible(false);
                mainWindow.setVisible(true);
                new ShortcutManager().addShortcuts();
                LOGGER.log(Level.INFO, "Loaded everything.");

                if(args.length > 0) {
                    LOGGER.log(Level.INFO, "Opening schedule through argument: {0}", args[0]);
                    Application.get().openSchedule(new File(args[0]));
                }
            }
        });
    }

    /**
     * Attempt to set the look and feel of the components.
     */
    private static void setLaf() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(QueleaProperties.get().getLaf());
                }
                catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    LOGGER.log(Level.INFO, "Couldn't set the look and feel.", ex);
                }

                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }
        });
    }

    /**
     * If it's appropriate, show the warning about only having 1 monitor.
     *
     * @param numMonitors the number of monitors.
     */
    private static void showWarning(int numMonitors) {
        if(numMonitors <= 1 && QueleaProperties.get().showSingleMonitorWarning()) {
            JOptionPane.showMessageDialog(mainWindow, LabelGrabber.INSTANCE.getLabel("one.monitor.warning"), LabelGrabber.INSTANCE.getLabel("one.monitor.title"),
                    JOptionPane.WARNING_MESSAGE, null);
        }
    }
}
