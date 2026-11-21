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
package org.quelea.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * Starts, stops and restarts the mobile lyrics, remote control and
 * auto-detection servers so they always match the current settings, without
 * Quelea having to be restarted.
 *
 * @author Arvid
 */
public final class ServerManager {

    public static final ServerManager INSTANCE = new ServerManager();
    private static final Logger LOGGER = LoggerUtils.getLogger();

    private MobileLyricsServer lyricsServer;
    private int lyricsPort = -1;
    private RemoteControlServer remoteServer;
    private int remotePort = -1;
    private AutoDetectServer autoDetectServer;
    private final ReadOnlyBooleanWrapper lyricsRunning = new ReadOnlyBooleanWrapper(false);

    private ServerManager() {
    }

    /**
     * Start, stop or restart each server to match the current settings. A
     * server that is already running with the right port is left alone.
     *
     * @return true if anything was started or stopped.
     */
    public synchronized boolean applySettings() {
        QueleaProperties props = QueleaProperties.get();
        boolean changed = applyLyrics(props.getUseMobLyrics(), props.getMobLyricsPort());
        changed |= applyRemote(props.getUseRemoteControl(), props.getRemoteControlPort());
        changed |= applyAutoDetect(lyricsServer != null || remoteServer != null, props.getAutoDetectPort());
        return changed;
    }

    /**
     * Stop every server, for example when Quelea exits.
     */
    public synchronized void stopAll() {
        applyLyrics(false, -1);
        applyRemote(false, -1);
        applyAutoDetect(false, -1);
    }

    /**
     * Whether the mobile lyrics server is running. Only changes on the JavaFX
     * thread, so it can be bound to controls.
     *
     * @return the property.
     */
    public ReadOnlyBooleanProperty lyricsRunningProperty() {
        return lyricsRunning.getReadOnlyProperty();
    }

    private boolean applyLyrics(boolean enabled, int port) {
        if (enabled && lyricsServer != null && port == lyricsPort) {
            return false;
        }
        boolean changed = false;
        if (lyricsServer != null) {
            LOGGER.log(Level.INFO, "Stopping mobile lyrics server");
            lyricsServer.stop();
            lyricsServer = null;
            lyricsPort = -1;
            changed = true;
        }
        if (enabled) {
            LOGGER.log(Level.INFO, "Starting lyric server on {0}", port);
            try {
                MobileLyricsServer server = new MobileLyricsServer(port);
                server.start();
                lyricsServer = server;
                lyricsPort = port;
                changed = true;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't create lyric server", ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Mobile lyrics disabled");
        }
        QueleaApp.get().setMobileLyricsServer(lyricsServer);
        boolean running = lyricsServer != null;
        if (Platform.isFxApplicationThread()) {
            lyricsRunning.set(running);
        } else {
            Platform.runLater(() -> lyricsRunning.set(running));
        }
        return changed;
    }

    private boolean applyRemote(boolean enabled, int port) {
        if (enabled && remoteServer != null && port == remotePort) {
            return false;
        }
        boolean changed = false;
        if (remoteServer != null) {
            LOGGER.log(Level.INFO, "Stopping remote control server");
            remoteServer.stop();
            remoteServer = null;
            remotePort = -1;
            changed = true;
        }
        if (enabled) {
            LOGGER.log(Level.INFO, "Starting remote control server on {0}", port);
            try {
                RemoteControlServer server = new RemoteControlServer(port);
                server.start();
                remoteServer = server;
                remotePort = port;
                changed = true;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't create remote control server", ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Remote control disabled");
        }
        QueleaApp.get().setRemoteControlServer(remoteServer);
        return changed;
    }

    private boolean applyAutoDetect(boolean enabled, int port) {
        if (enabled == (autoDetectServer != null)) {
            return false;
        }
        if (autoDetectServer != null) {
            LOGGER.log(Level.INFO, "Stopping auto-detection server");
            autoDetectServer.stop();
            autoDetectServer = null;
        }
        if (enabled) {
            LOGGER.log(Level.INFO, "Starting auto-detection server on {0}", port);
            try {
                AutoDetectServer server = new AutoDetectServer(port);
                server.start();
                autoDetectServer = server;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't create auto-detect server", ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Auto-detect servers disabled");
        }
        QueleaApp.get().setAutoDetectServer(autoDetectServer);
        return true;
    }
}
