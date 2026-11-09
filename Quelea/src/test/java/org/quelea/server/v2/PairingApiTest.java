package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pairing end to end: the real server with a real registry and pairing
 * service, against a fake Quelea.
 */
class PairingApiTest {

    @TempDir
    Path dir;

    private final HttpClient http = HttpClient.newHttpClient();
    private ExecutorService dispatcher;
    private DeviceRegistry registry;
    private PairingService pairing;
    private RemoteApiServer server;

    @BeforeEach
    void setUp() throws Exception {
        dispatcher = Executors.newSingleThreadExecutor();
        registry = new DeviceRegistry(dir.resolve("devices.json"), new MutableClock(), new SecureRandom());
        pairing = new PairingService(registry, () -> "quelea", () -> true);
        server = new RemoteApiServer(new RemoteApiServer.Config(0, false, "test"), new FakeQueleaControl(),
                new RemoteEventBus(dispatcher), registry, pairing);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
        dispatcher.shutdownNow();
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

    private String pairWithCode(String name) throws Exception {
        String code = pairing.open(Scope.CONTROL).code();
        HttpResponse<String> response = send("POST", "/api/v2/pair", null,
                "{\"code\":\"" + code + "\",\"name\":\"" + name + "\",\"platform\":\"ios\"}");
        assertEquals(200, response.statusCode(), response.body());
        return json(response).get("token").getAsString();
    }

    @Test
    void infoShowsWhetherPairingIsOpen() throws Exception {
        assertFalse(json(send("GET", "/api/v2/info", null, null)).get("pairing").getAsBoolean());
        pairing.open(Scope.CONTROL);
        assertTrue(json(send("GET", "/api/v2/info", null, null)).get("pairing").getAsBoolean());
    }

    @Test
    void pairedTokenOpensTheApi() throws Exception {
        String code = pairing.open(Scope.CONTROL).code();
        HttpResponse<String> paired = send("POST", "/api/v2/pair", null,
                "{\"code\":\"" + code + "\",\"name\":\"Arvids iPhone\",\"platform\":\"ios\"}");
        assertEquals(200, paired.statusCode());
        JsonObject body = json(paired);
        assertTrue(body.get("deviceId").getAsString().startsWith("d_"));
        assertEquals("control", body.get("scope").getAsString());
        String token = body.get("token").getAsString();

        assertEquals(200, send("GET", "/api/v2/state", token, null).statusCode());
        assertEquals(200, send("POST", "/api/v2/live/next", token, null).statusCode());
    }

    @Test
    void pairingErrors() throws Exception {
        assertEquals("pairing_closed", errorCode(send("POST", "/api/v2/pair", null,
                "{\"code\":\"ABCDEF\",\"name\":\"x\"}")));
        pairing.open(Scope.CONTROL);
        HttpResponse<String> wrong = send("POST", "/api/v2/pair", null, "{\"code\":\"ZZZZZZ\",\"name\":\"x\"}");
        assertEquals(401, wrong.statusCode());
        assertEquals("wrong_secret", errorCode(wrong));
        assertEquals(400, send("POST", "/api/v2/pair", null, "{\"code\":\"ZZZZZZ\"}").statusCode());
        assertEquals(400, send("POST", "/api/v2/pair", null, "{\"name\":\"x\"}").statusCode());
    }

    @Test
    void tooManyWrongCodesGive429() throws Exception {
        pairing.open(Scope.CONTROL);
        HttpResponse<String> last = null;
        for (int i = 0; i < PairingService.MAX_FAILURES; i++) {
            last = send("POST", "/api/v2/pair", null, "{\"code\":\"ZZZZZZ\",\"name\":\"x\"}");
        }
        assertEquals(429, last.statusCode());
        assertEquals("locked", errorCode(last));
    }

    @Test
    void passwordPairing() throws Exception {
        HttpResponse<String> paired = send("POST", "/api/v2/pair", null,
                "{\"password\":\"quelea\",\"name\":\"Laptop\",\"platform\":\"web\"}");
        assertEquals(200, paired.statusCode());
        assertEquals(401, send("POST", "/api/v2/pair", null, "{\"password\":\"nope\",\"name\":\"x\"}").statusCode());
    }

    @Test
    void deviceCanUnpairItself() throws Exception {
        String token = pairWithCode("Phone");
        HttpResponse<String> response = send("DELETE", "/api/v2/devices/me", token, null);
        assertEquals(200, response.statusCode());
        assertEquals(401, send("GET", "/api/v2/state", token, null).statusCode());
        assertTrue(registry.list().isEmpty());
    }

    @Test
    void revokingClosesTheDevicesWebSocket() throws Exception {
        String token = pairWithCode("Phone");
        BlockingQueue<JsonObject> messages = new LinkedBlockingQueue<>();
        CompletableFuture<Integer> closed = new CompletableFuture<>();
        WebSocket socket = http.newWebSocketBuilder().buildAsync(
                URI.create("ws://localhost:" + server.getPort() + "/api/v2/ws"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        messages.add(JsonParser.parseString(data.toString()).getAsJsonObject());
                        ws.request(1);
                        return null;
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                        closed.complete(statusCode);
                        return null;
                    }
                }).get(5, TimeUnit.SECONDS);
        socket.sendText("{\"type\":\"auth\",\"token\":\"" + token + "\"}", true);
        JsonObject snapshot = messages.poll(5, TimeUnit.SECONDS);
        assertNotNull(snapshot);
        assertEquals("snapshot", snapshot.get("type").getAsString());

        registry.revoke(registry.list().getFirst().id());

        JsonObject revoked = messages.poll(5, TimeUnit.SECONDS);
        assertNotNull(revoked);
        assertEquals("device.revoked", revoked.get("type").getAsString());
        assertEquals(EventHub.CLOSE_UNAUTHORIZED, closed.get(5, TimeUnit.SECONDS));
    }
}
