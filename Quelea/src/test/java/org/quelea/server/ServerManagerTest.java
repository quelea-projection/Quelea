package org.quelea.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.QueleaPropertyKeys;
import org.quelea.windows.main.QueleaApp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ServerManagerTest {

    private static boolean fxAvailable;

    @BeforeAll
    static void startToolkit() throws IOException {
        QueleaProperties.init(Files.createTempDirectory("quelea-servermanager-test").toString());
        try {
            CompletableFuture<Void> started = new CompletableFuture<>();
            Platform.startup(() -> started.complete(null));
            started.get(30, TimeUnit.SECONDS);
            fxAvailable = true;
        } catch (IllegalStateException alreadyStarted) {
            fxAvailable = true;
        } catch (Throwable ex) {
            fxAvailable = false;
        }
    }

    @AfterEach
    void stopServers() {
        ServerManager.INSTANCE.stopAll();
    }

    @Test
    void startsMovesAndStopsTheServersWithoutARestart() throws Exception {
        assumeTrue(fxAvailable, "JavaFX toolkit not available");
        int lyricsA = freePort();
        int lyricsB = freePort();
        int remote = freePort();
        set(QueleaPropertyKeys.autoDetectPortKey, freePort());
        set(QueleaPropertyKeys.remoteControlPortKey, remote);
        set(QueleaPropertyKeys.useRemoteControlKey, false);

        set(QueleaPropertyKeys.useMobLyricsKey, true);
        set(QueleaPropertyKeys.mobLyricsPortKey, lyricsA);
        assertTrue(ServerManager.INSTANCE.applySettings());
        assertTrue(listening(lyricsA));
        assertFalse(listening(remote));
        assertNotNull(QueleaApp.get().getMobileLyricsServer());
        assertTrue(lyricsRunningOnFxThread());

        assertFalse(ServerManager.INSTANCE.applySettings(), "nothing changed, nothing restarted");

        set(QueleaPropertyKeys.mobLyricsPortKey, lyricsB);
        assertTrue(ServerManager.INSTANCE.applySettings());
        assertFalse(listening(lyricsA));
        assertTrue(listening(lyricsB));

        set(QueleaPropertyKeys.useRemoteControlKey, true);
        assertTrue(ServerManager.INSTANCE.applySettings());
        assertTrue(listening(remote));
        assertNotNull(QueleaApp.get().getRemoteControlServer());

        set(QueleaPropertyKeys.useMobLyricsKey, false);
        set(QueleaPropertyKeys.useRemoteControlKey, false);
        assertTrue(ServerManager.INSTANCE.applySettings());
        assertFalse(listening(lyricsB));
        assertFalse(listening(remote));
        assertNull(QueleaApp.get().getMobileLyricsServer());
        assertNull(QueleaApp.get().getRemoteControlServer());
        assertFalse(lyricsRunningOnFxThread());
    }

    private static boolean lyricsRunningOnFxThread() throws Exception {
        CompletableFuture<Boolean> running = new CompletableFuture<>();
        Platform.runLater(() -> running.complete(ServerManager.INSTANCE.lyricsRunningProperty().get()));
        return running.get(10, TimeUnit.SECONDS);
    }

    private static void set(String key, Object value) {
        QueleaProperties.get().setProperty(key, String.valueOf(value));
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static boolean listening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
