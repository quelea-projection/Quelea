package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Starts the real v2 server on a free port against a fake Quelea and talks to
 * it over HTTP and WebSocket.
 */
class RemoteApiServerTest {

    private static final String VIEW_TOKEN = "view-token";
    private static final String CONTROL_TOKEN = "control-token";
    private static final DeviceAuth AUTH = token -> switch (token == null ? "" : token) {
        case VIEW_TOKEN -> Optional.of(new DeviceAuth.Device("d_view", "Stage tablet", Scope.VIEW));
        case CONTROL_TOKEN -> Optional.of(new DeviceAuth.Device("d_ctrl", "Phone", Scope.CONTROL));
        default -> Optional.empty();
    };

    private final HttpClient http = HttpClient.newHttpClient();
    private ExecutorService dispatcher;
    private RemoteEventBus bus;
    private FakeQueleaControl control;
    private RemoteApiServer server;

    @BeforeEach
    void setUp() throws Exception {
        dispatcher = Executors.newSingleThreadExecutor();
        bus = new RemoteEventBus(dispatcher);
        control = new FakeQueleaControl();
        server = start(true);
    }

    @AfterEach
    void tearDown() {
        server.stop();
        dispatcher.shutdownNow();
    }

    private RemoteApiServer start(boolean publicEnabled) throws Exception {
        RemoteApiServer s = new RemoteApiServer(new RemoteApiServer.Config(0, publicEnabled, "2027.0-test"),
                control, bus, AUTH);
        s.start();
        return s;
    }

    private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort() + path))
                .timeout(Duration.ofSeconds(5))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static JsonObject json(HttpResponse<String> response) {
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static String errorCode(HttpResponse<String> response) {
        return json(response).getAsJsonObject("error").get("code").getAsString();
    }

    // ---- REST ----

    @Test
    void infoNeedsNoToken() throws Exception {
        HttpResponse<String> response = send("GET", "/api/v2/info", null, null);
        assertEquals(200, response.statusCode());
        assertEquals(2, json(response).get("api").getAsInt());
        assertEquals("2027.0-test", json(response).get("quelea").getAsString());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
    }

    @Test
    void stateNeedsValidToken() throws Exception {
        HttpResponse<String> none = send("GET", "/api/v2/state", null, null);
        assertEquals(401, none.statusCode());
        assertEquals("unauthorized", errorCode(none));
        assertEquals(401, send("GET", "/api/v2/state", "wrong", null).statusCode());

        HttpResponse<String> ok = send("GET", "/api/v2/state", VIEW_TOKEN, null);
        assertEquals(200, ok.statusCode());
        JsonObject state = json(ok);
        assertEquals("it_1", state.get("itemId").getAsString());
        assertEquals(3, state.get("sectionCount").getAsInt());
        assertEquals(4, state.get("scheduleRevision").getAsLong());
    }

    @Test
    void viewTokenCannotSendCommands() throws Exception {
        HttpResponse<String> response = send("POST", "/api/v2/live/next", VIEW_TOKEN, null);
        assertEquals(403, response.statusCode());
        assertEquals("forbidden", errorCode(response));
        assertTrue(control.calls.isEmpty());
    }

    @Test
    void commandsRunAndReturnNewState() throws Exception {
        assertEquals(200, send("POST", "/api/v2/live/next", CONTROL_TOKEN, null).statusCode());
        assertEquals(200, send("POST", "/api/v2/live/prev", CONTROL_TOKEN, null).statusCode());
        assertEquals(200, send("POST", "/api/v2/live/item/next", CONTROL_TOKEN, null).statusCode());
        assertEquals(200, send("POST", "/api/v2/live/item/prev", CONTROL_TOKEN, null).statusCode());
        HttpResponse<String> section = send("POST", "/api/v2/live/section", CONTROL_TOKEN, "{\"index\":2}");
        assertEquals(200, section.statusCode());
        assertEquals(2, json(section).get("sectionIndex").getAsInt());
        HttpResponse<String> screen = send("PUT", "/api/v2/live/screen", CONTROL_TOKEN, "{\"black\":true}");
        assertTrue(json(screen).get("black").getAsBoolean());
        HttpResponse<String> rec = send("PUT", "/api/v2/recording", CONTROL_TOKEN, "{\"recording\":true}");
        assertTrue(json(rec).get("recording").getAsBoolean());
        assertEquals(200, send("POST", "/api/v2/schedule/items/it_2/live", CONTROL_TOKEN, null).statusCode());

        assertEquals(List.of("next", "previous", "nextItem", "previousItem", "gotoSection 2",
                "setScreen null true null", "setRecording true", "goLive it_2"), control.calls);
    }

    @Test
    void badInputGives400() throws Exception {
        assertEquals("bad_request", errorCode(send("POST", "/api/v2/live/section", CONTROL_TOKEN, null)));
        assertEquals("bad_request", errorCode(send("POST", "/api/v2/live/section", CONTROL_TOKEN, "not json")));
        assertEquals("bad_request", errorCode(send("POST", "/api/v2/live/section", CONTROL_TOKEN, "{\"index\":\"x\"}")));
        HttpResponse<String> outOfRange = send("POST", "/api/v2/live/section", CONTROL_TOKEN, "{\"index\":7}");
        assertEquals(400, outOfRange.statusCode());
        assertTrue(json(outOfRange).getAsJsonObject("error").get("message").getAsString().contains("out of range"));
        assertEquals(400, send("PUT", "/api/v2/live/screen", CONTROL_TOKEN, "{\"black\":\"yes\"}").statusCode());
    }

    @Test
    void unknownItemGives404() throws Exception {
        HttpResponse<String> response = send("POST", "/api/v2/schedule/items/it_99/live", CONTROL_TOKEN, null);
        assertEquals(404, response.statusCode());
        assertEquals("not_found", errorCode(response));
    }

    @Test
    void unknownPathAndWrongMethod() throws Exception {
        assertEquals(404, send("GET", "/api/v2/nothing", CONTROL_TOKEN, null).statusCode());
        HttpResponse<String> wrongMethod = send("GET", "/api/v2/live/next", CONTROL_TOKEN, null);
        assertEquals(405, wrongMethod.statusCode());
        assertEquals("method_not_allowed", errorCode(wrongMethod));
    }

    @Test
    void stuckQueleaGives503() throws Exception {
        control.timeOut = true;
        HttpResponse<String> response = send("GET", "/api/v2/state", VIEW_TOKEN, null);
        assertEquals(503, response.statusCode());
        assertEquals("unavailable", errorCode(response));
    }

    @Test
    void scheduleAndSectionsAsJson() throws Exception {
        JsonObject schedule = json(send("GET", "/api/v2/schedule", VIEW_TOKEN, null));
        assertEquals(4, schedule.get("revision").getAsLong());
        assertEquals("it_2", schedule.getAsJsonArray("items").get(1).getAsJsonObject().get("id").getAsString());

        JsonObject sections = json(send("GET", "/api/v2/live/sections", VIEW_TOKEN, null));
        JsonObject verse1 = sections.getAsJsonArray("sections").get(0).getAsJsonObject();
        assertEquals("Verse 1", verse1.get("title").getAsString());
        assertTrue(verse1.get("chordLines").isJsonNull(), "nulls are written out");
    }

    @Test
    void publicLiveNeedsNoTokenWhenEnabled() throws Exception {
        control.gotoSection(1);
        JsonObject live = json(send("GET", "/api/v2/public/live", null, null));
        assertEquals("Amazing Grace", live.get("title").getAsString());
        assertEquals("'Twas grace", live.getAsJsonArray("lines").get(0).getAsString());
    }

    @Test
    void publicEndpointsAreGoneWhenDisabled() throws Exception {
        server.stop();
        server = start(false);
        assertEquals(404, send("GET", "/api/v2/public/live", null, null).statusCode());
        assertThrows(ExecutionException.class, () -> connect("/api/v2/public/ws", Map.of()));
    }

    // ---- WebSocket ----

    private static final class Recorder implements WebSocket.Listener {

        final BlockingQueue<JsonObject> messages = new LinkedBlockingQueue<>();
        final CompletableFuture<Integer> closeCode = new CompletableFuture<>();
        private final StringBuilder partial = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                messages.add(JsonParser.parseString(partial.toString()).getAsJsonObject());
                partial.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            closeCode.complete(statusCode);
            return null;
        }

        JsonObject next() throws InterruptedException {
            JsonObject message = messages.poll(5, TimeUnit.SECONDS);
            assertNotNull(message, "expected a WebSocket message");
            return message;
        }
    }

    private record Connection(WebSocket socket, Recorder recorder) {
    }

    private Connection connect(String path, Map<String, String> headers) throws Exception {
        Recorder recorder = new Recorder();
        WebSocket.Builder builder = http.newWebSocketBuilder();
        headers.forEach(builder::header);
        WebSocket socket = builder.buildAsync(URI.create("ws://localhost:" + server.getPort() + path), recorder)
                .get(5, TimeUnit.SECONDS);
        return new Connection(socket, recorder);
    }

    private Connection connectAndAuth(String token) throws Exception {
        Connection c = connect("/api/v2/ws", Map.of());
        c.socket().sendText("{\"type\":\"auth\",\"token\":\"" + token + "\"}", true);
        return c;
    }

    @Test
    void authenticatedClientGetsSnapshotThenEvents() throws Exception {
        bus.publish(new RemoteEvent.RecordingChanged(false)); // seq 1, before the client connects
        Connection c = connectAndAuth(VIEW_TOKEN);

        JsonObject snapshot = c.recorder().next();
        assertEquals("snapshot", snapshot.get("type").getAsString());
        assertEquals("it_1", snapshot.getAsJsonObject("data").get("itemId").getAsString());
        long snapshotSeq = snapshot.get("seq").getAsLong();

        bus.publish(new RemoteEvent.LiveSectionChanged("it_1", 2));
        JsonObject event = c.recorder().next();
        assertEquals("live.section", event.get("type").getAsString());
        assertEquals(snapshotSeq + 1, event.get("seq").getAsLong());
        assertEquals(2, event.getAsJsonObject("data").get("index").getAsInt());
        assertEquals("it_1", event.getAsJsonObject("data").get("itemId").getAsString());
    }

    @Test
    void pingGetsPong() throws Exception {
        Connection c = connectAndAuth(VIEW_TOKEN);
        assertEquals("snapshot", c.recorder().next().get("type").getAsString());
        c.socket().sendText("{\"type\":\"ping\"}", true);
        assertEquals("pong", c.recorder().next().get("type").getAsString());
    }

    @Test
    void invalidTokenClosesWith4401() throws Exception {
        Connection c = connectAndAuth("wrong");
        assertEquals(EventHub.CLOSE_UNAUTHORIZED, c.recorder().closeCode.get(5, TimeUnit.SECONDS));
        assertNull(c.recorder().messages.poll());
    }

    @Test
    void eventsAreNotSentBeforeAuth() throws Exception {
        Connection c = connect("/api/v2/ws", Map.of());
        bus.publish(new RemoteEvent.LiveSectionChanged("it_1", 1));
        assertNull(c.recorder().messages.poll(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void missingAuthClosesWith4400() throws Exception {
        Connection c = connect("/api/v2/ws", Map.of());
        int code = c.recorder().closeCode.get(EventHub.AUTH_TIMEOUT.toSeconds() + 3, TimeUnit.SECONDS);
        assertEquals(EventHub.CLOSE_BAD_MESSAGE, code);
    }

    @Test
    void otherWebSitesCannotConnect() {
        assertThrows(ExecutionException.class,
                () -> connect("/api/v2/ws", Map.of("Origin", "http://evil.example")));
    }

    @Test
    void pageFromTheSameServerMayConnect() throws Exception {
        Connection c = connect("/api/v2/ws", Map.of("Origin", "http://localhost:" + server.getPort()));
        c.socket().sendText("{\"type\":\"auth\",\"token\":\"" + VIEW_TOKEN + "\"}", true);
        assertEquals("snapshot", c.recorder().next().get("type").getAsString());
    }

    @Test
    void publicClientNeedsNoTokenAndGetsOnlyLiveEvents() throws Exception {
        Connection c = connect("/api/v2/public/ws", Map.of());
        JsonObject snapshot = c.recorder().next();
        assertEquals("snapshot", snapshot.get("type").getAsString());
        assertEquals("Amazing Grace", snapshot.getAsJsonObject("data").get("title").getAsString());

        bus.publish(new RemoteEvent.ScheduleChanged(5));
        bus.publish(new RemoteEvent.RecordingChanged(true));
        bus.publish(new RemoteEvent.ScreenChanged(false, true, false));
        JsonObject event = c.recorder().next();
        assertEquals("live.screen", event.get("type").getAsString());
        assertNull(c.recorder().messages.poll(200, TimeUnit.MILLISECONDS));
    }

    @Test
    void stoppingTheServerClosesClients() throws Exception {
        Connection c = connectAndAuth(VIEW_TOKEN);
        c.recorder().next();
        server.stop();
        assertEquals(EventHub.CLOSE_GOING_AWAY, c.recorder().closeCode.get(5, TimeUnit.SECONDS));
        server = start(true); // for tearDown
    }
}
