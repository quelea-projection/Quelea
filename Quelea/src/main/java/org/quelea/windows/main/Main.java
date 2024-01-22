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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.PluginFeature;
import org.freedesktop.gstreamer.Registry;
import org.freedesktop.gstreamer.Version;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.db.SongManager;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.server.AutoDetectServer;
import org.quelea.server.MobileLyricsServer;
import org.quelea.server.RemoteControlServer;
import org.quelea.server.MidiInterfaceConnector;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FontInstaller;
import org.quelea.services.utils.GStreamerInitState;
import org.quelea.services.utils.GStreamerUtils;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.ShortcutManager;
import org.quelea.services.utils.UpdateChecker;
import org.quelea.services.utils.UserFileChecker;
import org.quelea.services.utils.Utils;
import org.quelea.utils.DesktopApi;
import org.quelea.windows.splash.SplashStage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main class, sets everything in motion...
 *
 * @author Michael
 */
public final class Main extends Application {

    private static Logger LOGGER;
    private MainWindow mainWindow;
    private DisplayStage fullScreenWindow;
    private DisplayStage stageWindow;
    private Dialog gstreamerWarningDialog;

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Starts the program off, this is the first thing that is executed by
     * Quelea when the program starts.
     * <p>
     *
     * @param stage the stage JavaFX provides that we don't use (ignored)
     */
    @Override
    public void start(Stage stage) {
        QueleaProperties.init(getParameters().getNamed().get("userhome"));
        LOGGER = LoggerUtils.getLogger();
        System.setProperty("glass.accessible.force", "false");
        setupExceptionHandling();
        setupTranslator();
        final SplashStage splashWindow = new SplashStage();
        splashWindow.show();
        LOGGER.log(Level.INFO, "Started, version {0}", QueleaProperties.VERSION.getVersionString());
        LOGGER.log(Level.INFO, "OS name: {0}", System.getProperty("os.name"));
        LOGGER.log(Level.INFO, "Using JAVA version {0}", System.getProperty("java.version"));
        LOGGER.log(Level.INFO, "64-bit: {0}", Utils.is64Bit());
        BufferedImage img;
        try {
            img = ImageIO.read(new File("icons/logo64.png"));
            Taskbar.getTaskbar().setIconImage(img);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Couldn't set icon, probably an unsupported platform and nothing to worry about: {0}", ex.getMessage());
        }
        ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

        new Thread(() -> {
            try {
                GStreamerUtils.configurePaths();
                boolean gok;
                try {
                    Gst.init(Version.BASELINE, "Quelea");
                    GStreamerUtils.setFeaturePriorities();
                    gok = true;
                }
                catch(UnsatisfiedLinkError err) {
                    LOGGER.log(Level.WARNING, "No GStreamer", err);
                    gok = false;
                }
                GStreamerInitState.INIT_SUCCESS = gok;
                final boolean gstreamerOk = gok;

                new FontInstaller().setupBundledFonts();
                new UserFileChecker(QueleaProperties.get().getQueleaUserHome()).checkUserFiles();

                final ObservableList<Screen> monitors = Screen.getScreens();
                LOGGER.log(Level.INFO, "Number of displays: {0}", monitors.size());

                final int projectorScreen = QueleaProperties.get().getProjectorScreen();
                final int stageScreen = QueleaProperties.get().getStageScreen();
                final int monitorNumber = monitors.size();

                final boolean lyricsHidden = !QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitorNumber || projectorScreen < 0);
                final boolean stageHidden = !QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitorNumber || stageScreen < 0);

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

				// -MIDI CONTROL -----------------------------------------------
				if (QueleaProperties.get().getUseMidiControl()) {
					LOGGER.log(Level.INFO, "Starting midi control interface with [{0}]", QueleaProperties.get().getMidiDeviceInterface());
					try {
						MidiInterfaceConnector midiController = new MidiInterfaceConnector(QueleaProperties.get().getMidiDeviceInterface());
						//QueleaApp.get().setMidiInterfaceConnector(midiController);// This one is for the panel
					} catch (Exception ex) {
						LOGGER.log(Level.INFO, "Couldn't create midi control interface", ex);
					}
				} else {
					LOGGER.log(Level.INFO, "Midi control disabled");
				}
				//------------------------------------------------------------

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
                    mainWindow = new MainWindow(true, gstreamerOk);
                });

                backgroundExecutor.submit(() -> {
                    new UpdateChecker().checkUpdate(false, false, false); //Check updates
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

                List<String> cmdParams = getParameters().getRaw();
                if (!cmdParams.isEmpty()) {
                    String schedulePath = cmdParams.get(cmdParams.size() - 1);
                    if (!schedulePath.contains("--userhome=") && !schedulePath.contains("-psn_")) {
                        LOGGER.log(Level.INFO, "Opening schedule through argument: {0}", schedulePath);
                        Platform.runLater(() -> {
                            QueleaApp.get().openSchedule(new File(schedulePath));
                        });
                    }
                }

                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
                        desktop.setOpenFileHandler((e) -> {
                            List<File> files = e.getFiles();
                            if (files != null && files.size() > 0) {
                                Platform.runLater(() -> {
                                    QueleaApp.get().openSchedule(files.get(0));
                                });
                            }
                        });
                    }
                }

                Platform.runLater(() -> {
                    splashWindow.hide();
                    mainWindow.getMainPanel().setSliderPos();
                    if (!Utils.isLinux()) {
                        mainWindow.show();
                    }
                    showMonitorWarning(monitorNumber);
                    if (!gstreamerOk) {
                        QueleaProperties.get().setConvertRecordings(false);
                        String message = LabelGrabber.INSTANCE.getLabel("gstreamer.warning.message");
                        Dialog.Builder gstreamerWarningDialogBuilder = new Dialog.Builder()
                                .create()
                                .setTitle(LabelGrabber.INSTANCE.getLabel("gstreamer.warning.title"))
                                .setMessage(message)
                                .addLabelledButton(LabelGrabber.INSTANCE.getLabel("continue.without.gstreamer"), (t) -> {
                                    gstreamerWarningDialog.hide();
                                });
                        gstreamerWarningDialogBuilder.addLabelledButton(LabelGrabber.INSTANCE.getLabel("download.gstreamer"), (t) -> {
                            String url = "https://gstreamer.freedesktop.org/download/";
                            DesktopApi.browse(url);
                            gstreamerWarningDialog.hide();
                        });
                        gstreamerWarningDialog = gstreamerWarningDialogBuilder.setWarningIcon().build();
                        gstreamerWarningDialog.showAndWait();
                    }
                    mainWindow.show();
                    QueleaApp.get().doneLoading();
                });
            } catch (Throwable ex) {
                LOGGER.log(Level.SEVERE, "Uncaught exception during application start-up", ex);
                Platform.runLater(() -> {
                    Dialog.showAndWaitError(LabelGrabber.INSTANCE.getLabel("startup.error.title"), LabelGrabber.INSTANCE.getLabel("startup.error.text").replace("$1", Utils.getDebugLog().getAbsolutePath()));
                    System.exit(1);
                });
            }
        }).start();
    }

    /**
     * If it's appropriate, show the warning about only having 1 monitor.
     * <p/>
     *
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