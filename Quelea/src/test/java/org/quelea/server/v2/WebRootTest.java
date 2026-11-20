package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The web remote's files are served next to the API.
 */
class WebRootTest {

    @TempDir
    Path webRoot;

    private final HttpClient http = HttpClient.newHttpClient();
    private ExecutorService dispatcher;
    private RemoteApiServer server;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(webRoot.resolve("index.html"), "<!DOCTYPE html><title>Quelea Remote</title>");
        Files.writeString(webRoot.resolve("app.js"), "'use strict';");
        Files.createDirectory(webRoot.resolve("sub"));
        dispatcher = Executors.newSingleThreadExecutor();
        server = new RemoteApiServer(new RemoteApiServer.Config(0, false, "test", webRoot), new FakeQueleaControl(),
                new RemoteEventBus(dispatcher), DeviceAuth.NONE, null);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
        dispatcher.shutdownNow();
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort() + path)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void indexIsServedWithSecurityHeaders() throws Exception {
        HttpResponse<String> page = get("/");
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("Quelea Remote"));
        String csp = page.headers().firstValue("Content-Security-Policy").orElse("");
        assertTrue(csp.contains("default-src 'self'"));
        assertTrue(csp.contains("blob:"), "theme thumbnails are fetched with the token and shown as blob: URLs");
        assertEquals("nosniff", page.headers().firstValue("X-Content-Type-Options").orElse(""));
        assertEquals("no-cache", page.headers().firstValue("Cache-Control").orElse(""));
    }

    @Test
    void scriptsAreServed() throws Exception {
        HttpResponse<String> script = get("/app.js");
        assertEquals(200, script.statusCode());
        assertTrue(script.headers().firstValue("Content-Type").orElse("").contains("javascript"));
    }

    @Test
    void apiStillAnswers() throws Exception {
        HttpResponse<String> info = get("/api/v2/info");
        assertEquals(200, info.statusCode());
        assertTrue(info.body().contains("\"api\":2"));
        assertEquals(404, get("/api/v2/nothing").statusCode());
    }

    @Test
    void noDirectoryListingsOrEscapes() throws Exception {
        assertTrue(get("/sub/").statusCode() >= 400, "directory listings are off");
        assertTrue(get("/../build.gradle").statusCode() >= 400);
        assertEquals(404, get("/missing.html").statusCode());
    }
}
