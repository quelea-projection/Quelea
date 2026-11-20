package org.quelea.server;

import com.sun.net.httpserver.HttpServer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Remote server lifecycle")
public class ServerLifecycleTest {
    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-server-test").toString());
        }
    }

    @Test
    public void mobileLyricsServerServesARealStaticResourceAndStops() throws Exception {
        MobileLyricsServer server = new MobileLyricsServer(0);
        try {
            Assertions.assertFalse(server.isRunning());
            server.start();
            Assertions.assertTrue(server.isRunning());
            int port = port(server);
            HttpURLConnection request = (HttpURLConnection) URI.create("http://127.0.0.1:" + port + "/cross.gif").toURL().openConnection();
            request.setConnectTimeout(5000);
            request.setReadTimeout(5000);
            try {
                Assertions.assertEquals(200, request.getResponseCode());
                try (var body = request.getInputStream()) {
                    Assertions.assertArrayEquals(Files.readAllBytes(Path.of("icons", "cross.gif")), body.readAllBytes());
                }
            } finally {
                request.disconnect();
            }
        } finally {
            server.stop();
        }
        Assertions.assertFalse(server.isRunning());
    }

    @Test
    public void remoteAndDiscoveryServersReportTheirLifecycle() throws Exception {
        RemoteControlServer remote = new RemoteControlServer(0);
        AutoDetectServer discovery = new AutoDetectServer(0);
        try {
            Assertions.assertFalse(remote.isRunning());
            Assertions.assertFalse(discovery.isRunning());
            remote.start();
            discovery.start();
            Assertions.assertTrue(remote.isRunning());
            Assertions.assertTrue(discovery.isRunning());
        } finally {
            remote.stop();
            discovery.stop();
        }
        Assertions.assertFalse(remote.isRunning());
        Assertions.assertFalse(discovery.isRunning());
    }

    private int port(Object wrapper) throws Exception {
        var field = wrapper.getClass().getDeclaredField("server");
        field.setAccessible(true);
        return ((HttpServer) field.get(wrapper)).getAddress().getPort();
    }
}
