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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.db.LegacyDB;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.server.MobileLyricsServer;
import org.quelea.server.RemoteControlServer;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.phonehome.PhoneHome;
import org.quelea.services.utils.FontInstaller;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.ShortcutManager;
import org.quelea.services.utils.UpdateChecker;
import org.quelea.services.utils.UserFileChecker;
import org.quelea.services.utils.Utils;
import org.quelea.windows.multimedia.VLCWindow;
import org.quelea.windows.splash.SplashStage;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

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
    private DisplayStage textOnlyWindow;
    private Dialog vlcWarningDialog;

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Starts the program off, this is the first thing that is executed by
     * Quelea when the program starts.
     * <p>
     * @param stage the stage JavaFX provides that we don't use (ignored)
     */
    @Override
    public void start(Stage stage) {
        setupExceptionHandling();
        setupTranslator();
        final SplashStage splashWindow = new SplashStage();
        splashWindow.show();

        new Thread() {
            @Override
            public void run() {
                try {
                    final boolean VLC_OK = new NativeDiscovery().discover();
                    final boolean VLC_INIT;
                    if (VLC_OK) {
                        VLC_INIT = VLCWindow.INSTANCE.isInit();
                    } else {
                        VLC_INIT = false;
                    }
                    new FontInstaller().setupBundledFonts();
                    new UserFileChecker(QueleaProperties.getQueleaUserHome()).checkUserFiles();

                    if (!new File(QueleaProperties.getQueleaUserHome(), "database_new").exists()
                            && new File(QueleaProperties.getQueleaUserHome(), "database").exists()) {
                        SongDisplayable[] songs = LegacyDB.get().getSongs();
                        LOGGER.log(Level.INFO, "Importing {0} songs from legacy DB", songs.length);
                        for (SongDisplayable song : songs) {
                            SongManager.get().addSong(song, false);
                        }
                    }

                    final ObservableList<Screen> monitors = Screen.getScreens();
                    LOGGER.log(Level.INFO, "Number of displays: {0}", monitors.size());

                    final int projectorScreen = QueleaProperties.get().getProjectorScreen();
                    final int stageScreen = QueleaProperties.get().getStageScreen();
                    final int textOnlyScreen = QueleaProperties.get().getTextOnlyScreen();
                    final int monitorNumber = monitors.size();

                    final boolean lyricsHidden;
                    if (!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitorNumber || projectorScreen < 0)) {
                        lyricsHidden = true;
                    } else {
                        lyricsHidden = false;
                    }
                    final boolean stageHidden;
                    if (!QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitorNumber || stageScreen < 0)) {
                        stageHidden = true;
                    } else {
                        stageHidden = false;
                    }
                    final boolean textOnlyHidden;
                    if (!QueleaProperties.get().isTextOnlyModeCoords() && (textOnlyScreen >= monitorNumber || textOnlyScreen < 0)) {
                        textOnlyHidden = true;
                    } else {
                        textOnlyHidden = false;
                    }

                    if (QueleaProperties.get().getUseMobLyrics()) {
                        LOGGER.log(Level.INFO, "Starting lyric server on {0}", QueleaProperties.get().getMobLyricsPort());
                        try {
                            MobileLyricsServer mls = new MobileLyricsServer(QueleaProperties.get().getMobLyricsPort());
                            mls.start();
                            QueleaApp.get().setMobileLyricsServer(mls);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "Couldn't create lyric server", ex);
                        }
                    } else {
                        LOGGER.log(Level.INFO, "Mobile lyrics disabled");
                    }

                    if (QueleaProperties.get().getUseRemoteControl()) {
                        LOGGER.log(Level.INFO, "Starting remote control server on {0}", QueleaProperties.get().getRemoteControlPort());
                        try {
                            RemoteControlServer rcs = new RemoteControlServer(QueleaProperties.get().getRemoteControlPort());
                            rcs.start();
                            QueleaApp.get().setRemoteControlServer(rcs);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "Couldn't create remote control server", ex);
                        }
                    } else {
                        LOGGER.log(Level.INFO, "Remote control disabled");
                    }

                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            if (lyricsHidden) {
                                LOGGER.log(Level.INFO, "Hiding projector display on monitor 0 (base 0!)");
                                fullScreenWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), DisplayType.PROJECTION);
                                fullScreenWindow.hide();
                            } else if (QueleaProperties.get().isProjectorModeCoords()) {
                                LOGGER.log(Level.INFO, "Starting projector display: ", QueleaProperties.get().getProjectorCoords());
                                fullScreenWindow = new DisplayStage(QueleaProperties.get().getProjectorCoords(), DisplayType.PROJECTION);
                            } else {
                                LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
                                fullScreenWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(projectorScreen).getVisualBounds()), DisplayType.PROJECTION);
                            }
                            QueleaApp.get().setProjectionWindow(fullScreenWindow);
                            //fullScreenWindow.toFront();

                            if (stageHidden) {
                                LOGGER.log(Level.INFO, "Hiding stage display on monitor 0 (base 0!)");
                                stageWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), DisplayType.STAGE);
                                stageWindow.hide();
                            } else if (QueleaProperties.get().isStageModeCoords()) {
                                LOGGER.log(Level.INFO, "Starting stage display: ", QueleaProperties.get().getStageCoords());
                                stageWindow = new DisplayStage(QueleaProperties.get().getStageCoords(), DisplayType.STAGE);
                            } else {
                                LOGGER.log(Level.INFO, "Starting stage display on monitor {0} (base 0!)", stageScreen);
                                stageWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(stageScreen).getVisualBounds()), DisplayType.STAGE);
                            }
                            QueleaApp.get().setStageWindow(stageWindow);
                            //stageWindow.toFront();

                            if (textOnlyHidden) {
                                LOGGER.log(Level.INFO, "Hiding text only display on monitor 0 (base 0!)");
                                textOnlyWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), DisplayType.TEXT_ONLY);
                                textOnlyWindow.hide();
                            } else if (QueleaProperties.get().isTextOnlyModeCoords()) {
                                LOGGER.log(Level.INFO, "Starting text only display: ", QueleaProperties.get().getTextOnlyCoords());
                                textOnlyWindow = new DisplayStage(QueleaProperties.get().getTextOnlyCoords(), DisplayType.TEXT_ONLY);
                            } else {
                                LOGGER.log(Level.INFO, "Starting text only display on monitor {0} (base 0!)", textOnlyScreen);
                                textOnlyWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(textOnlyScreen).getVisualBounds()), DisplayType.TEXT_ONLY);
                            }
                            QueleaApp.get().setTextOnlyWindow(textOnlyWindow);

                            LOGGER.log(Level.INFO, "Loading bibles");
                            final Thread bibleLoader = new Thread() {
                                @Override
                                public void run() {
                                    BibleManager.get();
                                    LOGGER.log(Level.INFO, "Loading bibles done");
                                }
                            };
                            bibleLoader.start();
                            try {
                                bibleLoader.join();
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            BibleManager.get().buildIndex();

                            if (SongManager.get() == null) {
                                Dialog.showAndWaitError(LabelGrabber.INSTANCE.getLabel("already.running.title"), LabelGrabber.INSTANCE.getLabel("already.running.error"));
                                System.exit(1);
                            }
                            SongManager.get().getSongs();
                            OOUtils.attemptInit();
                            try {
                                bibleLoader.join(); //Make sure bibleloader has finished loading

                            } catch (InterruptedException ex) {
                            }
                            mainWindow = new MainWindow(true);

                            new UpdateChecker().checkUpdate(false, false, false); //Check updates
                            PhoneHome.INSTANCE.phone(); //Phone home

                            LOGGER.log(Level.INFO, "Registering canvases");
                            mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(fullScreenWindow.getCanvas());
                            mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(fullScreenWindow);
                            mainWindow.getNoticeDialog().registerCanvas(fullScreenWindow.getCanvas());
                            if (lyricsHidden) {
                                fullScreenWindow.hide();
                            } else {
                                fullScreenWindow.show();
                            }

                            mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(textOnlyWindow.getCanvas());
                              mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(textOnlyWindow);
                            mainWindow.getNoticeDialog().registerCanvas(textOnlyWindow.getCanvas());
                            if (textOnlyHidden) {
                                textOnlyWindow.hide();
                            } else {
                                textOnlyWindow.show();
                            }
                            mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(stageWindow.getCanvas());
                            mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(stageWindow);
                            if (stageHidden) {
                                stageWindow.hide();
                            } else {
                                stageWindow.show();
                            }

                            LOGGER.log(Level.INFO, "Registered canvases.");

                            if (QueleaProperties.get().getDragAndDrop()) {
                                Utils.enableDragAndDrop();
                                LOGGER.log(Level.INFO, "Enabled drag and drop functionality.");
                            }

                            LOGGER.log(Level.INFO, "Final loading bits");
                            //                Utils.centreOnMonitor(mainWindow, controlScreen);
                            //                mainWindow.toFront();
                            new ShortcutManager().addShortcuts(mainWindow);
                            LOGGER.log(Level.INFO, "Loaded everything.");

                            if (!getParameters().getRaw().isEmpty()) {
                                String schedulePath = getParameters().getRaw().get(0);
                                LOGGER.log(Level.INFO, "Opening schedule through argument: {0}", schedulePath);
                                QueleaApp.get().openSchedule(new File(schedulePath));
                            }
                            mainWindow.show();
                            splashWindow.hide();
                            showMonitorWarning(monitorNumber);
                            if (VLC_OK && VLC_INIT) {
                                VLCWindow.INSTANCE.refreshPosition();
                            } else { //Couldn't find the VLC libraries.
                                String message;
                                if (VLC_OK) {
                                    message = LabelGrabber.INSTANCE.getLabel("vlc.version.message");
                                } else {
                                    message = LabelGrabber.INSTANCE.getLabel("vlc.warning.message");
                                }
                                Dialog.Builder vlcWarningDialogBuilder = new Dialog.Builder()
                                        .create()
                                        .setTitle(LabelGrabber.INSTANCE.getLabel("vlc.warning.title"))
                                        .setMessage(message)
                                        .addLabelledButton(LabelGrabber.INSTANCE.getLabel("continue.without.video"), new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent t) {
                                                vlcWarningDialog.hide();
                                            }
                                        });
                                if (java.awt.Desktop.isDesktopSupported()) {
                                    vlcWarningDialogBuilder.addLabelledButton(LabelGrabber.INSTANCE.getLabel("download.vlc"), new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent t) {
                                            try {
                                                java.awt.Desktop.getDesktop().browse(new URI("http://www.videolan.org/vlc/index.html"));
                                            } catch (URISyntaxException | IOException ex) {
                                                LOGGER.log(Level.WARNING, "Couldn't open browser", ex);
                                            }
                                            vlcWarningDialog.hide();
                                        }
                                    });
                                }
                                vlcWarningDialog = vlcWarningDialogBuilder.setWarningIcon().build();
                                vlcWarningDialog.showAndWait();
                            }
                            QueleaApp.get().doneLoading();
                        }
                    });
                } catch (Throwable ex) {
                    LOGGER.log(Level.SEVERE, "Uncaught exception during application start-up", ex);
                    Dialog.showAndWaitError(LabelGrabber.INSTANCE.getLabel("startup.error.title"), LabelGrabber.INSTANCE.getLabel("startup.error.text").replace("$1", Utils.getDebugLog().getAbsolutePath()));
                    System.exit(1);
                }
            }
        }.start();
    }

    /**
     * +
     * If it's appropriate, show the warning about only having 1 monitor.
     * <p/>
     * @param numMonitors the number of monitors.
     */
    private void showMonitorWarning(int numMonitors) {
        if (numMonitors <= 1 && QueleaProperties.get().showSingleMonitorWarning()) {
            Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("one.monitor.title"), LabelGrabber.INSTANCE.getLabel("one.monitor.warning"));
        }
    }

    private void setupExceptionHandling() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    private void setupTranslator() {
        com.memetix.mst.translate.Translate.setClientId("quelea-projection");
        com.memetix.mst.translate.Translate.setClientSecret("wk4+wd9YJkjIHmz2qwD1oR7pP9/kuHOL6OsaOKEi80U=");
    }

    private class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.log(Level.SEVERE, "Uncaught exception on thread: " + t.getName(), e);
        }
    }
}
