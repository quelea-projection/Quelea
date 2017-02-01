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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.db.LegacyDB;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.server.AutoDetectServer;
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
        System.setProperty("glass.accessible.force", "false");
        if (Utils.isMac()) {
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("icons/logo64.png"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            com.apple.eawt.Application.getApplication().setDockIconImage(img);
        }
        setupExceptionHandling();
        setupTranslator();
        final SplashStage splashWindow = new SplashStage();
        splashWindow.show();
        LOGGER.log(Level.INFO, "Started, version {0}", QueleaProperties.VERSION.getVersionString());
        
        ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

        new Thread() {
            @Override
            public void run() {
                try {
                    boolean vlcOk = false;
                    try {
                        vlcOk = new NativeDiscovery().discover();
                    } catch (Throwable ex) {
                        LOGGER.log(Level.WARNING, "Exception during VLC initialisation", ex);
                    }
                    final boolean VLC_OK = vlcOk;
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

                    if (QueleaProperties.get().getUseMobLyrics() || QueleaProperties.get().getUseRemoteControl()) {
                        LOGGER.log(Level.INFO, "Starting auto-detection server on {0}", QueleaProperties.get().getAutoDetectPort());
                        try {
                            AutoDetectServer ads = new AutoDetectServer(QueleaProperties.get().getAutoDetectPort());
                            ads.start();
                            QueleaApp.get().setAutoDetectServer(ads);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "Couldn't create auto-detect server", ex);
                        }
                    } else {
                        LOGGER.log(Level.INFO, "Auto-detect servers disabled");
                    }

                    if (QueleaProperties.get().getWebProxyHost() != null && QueleaProperties.get().getWebProxyPort() != null && QueleaProperties.get().getWebProxyUser() != null && QueleaProperties.get().getWebProxyPassword() != null) {
                        System.setProperty("http.proxyHost", QueleaProperties.get().getWebProxyHost());
                        System.setProperty("http.proxyPort", QueleaProperties.get().getWebProxyPort());
                        System.setProperty("http.proxyUser", QueleaProperties.get().getWebProxyUser());
                        System.setProperty("http.proxyPassword", QueleaProperties.get().getWebProxyPassword());
                    }
                    if (lyricsHidden) {
                        LOGGER.log(Level.INFO, "Hiding projector display on monitor 0 (base 0!)");
                        Platform.runLater(() -> {
                            fullScreenWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), false);
                            fullScreenWindow.hide();
                        });
                    } else if (QueleaProperties.get().isProjectorModeCoords()) {
                        LOGGER.log(Level.INFO, "Starting projector display: ", QueleaProperties.get().getProjectorCoords());
                        Platform.runLater(() -> {
                            fullScreenWindow = new DisplayStage(QueleaProperties.get().getProjectorCoords(), false);
                        });
                    } else {
                        LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
                        Platform.runLater(() -> {
                            fullScreenWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(projectorScreen).getBounds()), false);
                            fullScreenWindow.setFullScreenAlwaysOnTop(true);
                        });
                    }

                    Platform.runLater(() -> {
                        QueleaApp.get().setProjectionWindow(fullScreenWindow);
                    });

                    if (stageHidden) {
                        LOGGER.log(Level.INFO, "Hiding stage display on monitor 0 (base 0!)");
                        Platform.runLater(() -> {
                            stageWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), true);
                            stageWindow.hide();
                        });
                    } else if (QueleaProperties.get().isStageModeCoords()) {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.INFO, "Starting stage display: ", QueleaProperties.get().getStageCoords());
                            stageWindow = new DisplayStage(QueleaProperties.get().getStageCoords(), true);
                        });
                    } else {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.INFO, "Starting stage display on monitor {0} (base 0!)", stageScreen);
                            stageWindow = new DisplayStage(Utils.getBoundsFromRect2D(monitors.get(stageScreen).getVisualBounds()), true);
                        });
                    }

                    Platform.runLater(() -> {
                        QueleaApp.get().setStageWindow(stageWindow);
                    });

                    backgroundExecutor.submit(() -> {
                        LOGGER.log(Level.INFO, "Loading bibles");
                        BibleManager.get();
                        LOGGER.log(Level.INFO, "Loading bibles done");
                    });

                    if (SongManager.get() == null) {
                        Platform.runLater(() -> {
                            Dialog.showAndWaitError(LabelGrabber.INSTANCE.getLabel("already.running.title"), LabelGrabber.INSTANCE.getLabel("already.running.error"));
                            System.exit(1);                            
                        });
                    }
                    OOUtils.attemptInit();
                    Platform.runLater(() -> {
                        mainWindow = new MainWindow(true);
                    });
                    
                    backgroundExecutor.submit(() -> {
                        new UpdateChecker().checkUpdate(false, false, false); //Check updates
                        PhoneHome.INSTANCE.phone(); //Phone home
                    });
                    
                    LOGGER.log(Level.INFO, "Registering canvases");
                    Platform.runLater(() -> {
                        mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(fullScreenWindow.getCanvas());
                        mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(fullScreenWindow);
                        mainWindow.getNoticeDialog().registerCanvas(fullScreenWindow.getCanvas());
                        if (lyricsHidden) {
                            fullScreenWindow.hide();
                        } else {
                            fullScreenWindow.show();
                        }
                        mainWindow.getMainPanel().getLivePanel().registerDisplayCanvas(stageWindow.getCanvas());
                        mainWindow.getMainPanel().getLivePanel().registerDisplayWindow(stageWindow);
                        if (stageHidden) {
                            stageWindow.hide();
                        } else {
                            stageWindow.show();
                        }
                    });
                    
                    LOGGER.log(Level.INFO, "Adding shortcuts.");
                    Platform.runLater(() -> {
                        new ShortcutManager().addShortcuts(mainWindow);
                    });
                    LOGGER.log(Level.INFO, "Loaded everything.");

                    if (!getParameters().getRaw().isEmpty()) {
                        String schedulePath = getParameters().getRaw().get(0);
                        LOGGER.log(Level.INFO, "Opening schedule through argument: {0}", schedulePath);
                        Platform.runLater(() -> {
                            QueleaApp.get().openSchedule(new File(schedulePath));
                        });
                    }
                    if (Utils.isMac()) {
                        com.apple.eawt.Application.getApplication().setOpenFileHandler((com.apple.eawt.AppEvent.OpenFilesEvent ofe) -> {
                            List<File> files = ofe.getFiles();
                            if (files != null && files.size() > 0) {
                                Platform.runLater(() -> {
                                    QueleaApp.get().openSchedule(files.get(0));
                                });
                            }
                        });
                    }
                    
                    Platform.runLater(() -> {
                        splashWindow.hide();
                        mainWindow.getMainPanel().setSliderPos();
                        mainWindow.show();
                        showMonitorWarning(monitorNumber);
                        CheckBox convertCheckBox = QueleaApp.get().getMainWindow().getOptionsDialog().getRecordingSettingsPanel().getConvertRecordingsCheckBox();
                        if (VLC_OK && VLC_INIT) {
                            VLCWindow.INSTANCE.refreshPosition();
                            convertCheckBox.setDisable(false);
                        } else { //Couldn't find the VLC libraries.
                            QueleaProperties.get().setConvertRecordings(false);
                            convertCheckBox.setSelected(false);
                            convertCheckBox.setDisable(true);
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
                            vlcWarningDialog.show();
                        }
                        QueleaApp.get().doneLoading();
                    });
                } catch (Throwable ex) {
                    LOGGER.log(Level.SEVERE, "Uncaught exception during application start-up", ex);
                    Platform.runLater(new Runnable() {
                        public void run() {
                            Dialog.showAndWaitError(LabelGrabber.INSTANCE.getLabel("startup.error.title"), LabelGrabber.INSTANCE.getLabel("startup.error.text").replace("$1", Utils.getDebugLog().getAbsolutePath()));
                            System.exit(1);
                        }
                    });
                }
            }
        }.start();
    }

    /**
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
        com.memetix.mst.translate.Translate.setClientId(QueleaProperties.get().getTranslateClientID());
        com.memetix.mst.translate.Translate.setClientSecret(QueleaProperties.get().getTranslateClientSecret());
    }

    private class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.log(Level.SEVERE, "Uncaught exception on thread: " + t.getName(), e);
        }
    }
}
