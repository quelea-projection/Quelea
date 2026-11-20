package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        RemoteApiServer s = new RemoteApiServer(new RemoteApiServer.Config(0, publicEnabled, "2027.0-test", null),
                control, bus, AUTH, null);
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
        // The chords come with where they fall, for apps to draw them.
        JsonObject line = live.getAsJsonArray("chorded").get(0).getAsJsonObject();
        assertEquals("'Twas grace", line.get("text").getAsString());
        assertEquals("C", line.getAsJsonArray("chords").get(1).getAsJsonObject().get("chord").getAsString());
        assertEquals(3, line.getAsJsonArray("chords").get(1).getAsJsonObject().get("at").getAsInt());
        assertEquals("Det var nåd", live.getAsJsonObject("translations").getAsJsonArray("Svenska").get(0).getAsString());
        assertEquals("", live.get("liveText").getAsString());
    }

    @Test
    void publicBibleVersesByBookNumber() throws Exception {
        JsonObject passage = json(send("GET", "/api/v2/public/bible/verses?translation=KJV&book=43&verses=3:16,3:17",
                null, null));
        assertEquals("Verse 3:17", passage.getAsJsonArray("verses").get(1).getAsJsonObject().get("text").getAsString());
        assertEquals(400, send("GET", "/api/v2/public/bible/verses?translation=KJV&book=43&verses=x:y", null, null)
                .statusCode());
        assertEquals(404, send("GET", "/api/v2/public/bible/verses?translation=Nope&book=43&verses=3:16", null, null)
                .statusCode());
        assertEquals(200, send("GET", "/api/v2/public/bible/translations", null, null).statusCode());
        HttpResponse<String> languages = send("GET", "/api/v2/public/song-languages", null, null);
        assertEquals("Svenska", JsonParser.parseString(languages.body()).getAsJsonArray().get(0)
                .getAsJsonObject().get("name").getAsString());
    }

    @Test
    void publicLiveCarriesTheLiveText() throws Exception {
        LiveText.set("Welcome!");
        try {
            assertEquals("Welcome!", json(send("GET", "/api/v2/public/live", null, null)).get("liveText").getAsString());
        } finally {
            LiveText.set("");
        }
    }

    @Test
    void publicEndpointsAreGoneWhenDisabled() throws Exception {
        server.stop();
        server = start(false);
        assertEquals(404, send("GET", "/api/v2/public/live", null, null).statusCode());
        assertThrows(ExecutionException.class, () -> connect("/api/v2/public/ws", Map.of()));
    }

    // ---- Schedule changes ----

    @Test
    void addSongAndBibleReturnNewItemAndSchedule() throws Exception {
        HttpResponse<String> song = send("POST", "/api/v2/schedule/items", CONTROL_TOKEN, "{\"songId\":42}");
        assertEquals(200, song.statusCode());
        assertEquals("it_3", json(song).get("itemId").getAsString());
        assertEquals(5, json(song).getAsJsonObject("schedule").get("revision").getAsLong());

        HttpResponse<String> bible = send("POST", "/api/v2/schedule/items", CONTROL_TOKEN,
                "{\"bible\":{\"translation\":\"KJV\",\"reference\":\"John 3:16\"}}");
        assertEquals("it_4", json(bible).get("itemId").getAsString());
        assertEquals(List.of("it_1", "it_2", "it_3", "it_4"), control.schedule);
    }

    @Test
    void addErrors() throws Exception {
        assertEquals(404, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN, "{\"songId\":7}").statusCode());
        assertEquals(404, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN,
                "{\"bible\":{\"translation\":\"Nope\",\"reference\":\"John 3:16\"}}").statusCode());
        assertEquals(400, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN,
                "{\"bible\":{\"translation\":\"KJV\",\"reference\":\"Hezekiah 9:9\"}}").statusCode());
        assertEquals(400, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN, "{}").statusCode());
        assertEquals(403, send("POST", "/api/v2/schedule/items", VIEW_TOKEN, "{\"songId\":42}").statusCode());
    }

    @Test
    void staleRevisionGives409AndChangesNothing() throws Exception {
        HttpResponse<String> response = send("POST", "/api/v2/schedule/items?ifRevision=3", CONTROL_TOKEN,
                "{\"songId\":42}");
        assertEquals(409, response.statusCode());
        assertEquals("conflict", errorCode(response));
        assertEquals(List.of("it_1", "it_2"), control.schedule);

        assertEquals(200, send("POST", "/api/v2/schedule/items?ifRevision=4", CONTROL_TOKEN,
                "{\"songId\":42}").statusCode());
        assertEquals(400, send("DELETE", "/api/v2/schedule/items/it_1?ifRevision=x", CONTROL_TOKEN, null).statusCode());
    }

    @Test
    void removeAndMoveItems() throws Exception {
        HttpResponse<String> moved = send("POST", "/api/v2/schedule/items/it_2/move", CONTROL_TOKEN, "{\"to\":0}");
        assertEquals(200, moved.statusCode());
        assertEquals(List.of("it_2", "it_1"), control.schedule);
        assertEquals(400, send("POST", "/api/v2/schedule/items/it_2/move", CONTROL_TOKEN, "{\"to\":9}").statusCode());

        HttpResponse<String> removed = send("DELETE", "/api/v2/schedule/items/it_1", CONTROL_TOKEN, null);
        assertEquals(200, removed.statusCode());
        assertEquals("it_1", json(removed).get("itemId").getAsString());
        assertEquals(List.of("it_2"), control.schedule);
        assertEquals(404, send("DELETE", "/api/v2/schedule/items/it_1", CONTROL_TOKEN, null).statusCode());
    }

    @Test
    void controlTheLiveVideo() throws Exception {
        assertEquals(400, send("POST", "/api/v2/live/media/play", CONTROL_TOKEN, null).statusCode(),
                "a song cannot play");
        assertTrue(json(send("GET", "/api/v2/state", VIEW_TOKEN, null)).get("media").isJsonNull());

        control.media = new LiveState.Media("video", false, false, 0.0);
        JsonObject media = json(send("POST", "/api/v2/live/media/play", CONTROL_TOKEN, null)).getAsJsonObject("media");
        assertEquals("video", media.get("kind").getAsString());
        assertTrue(media.get("playing").getAsBoolean());

        media = json(send("PUT", "/api/v2/live/media", CONTROL_TOKEN, "{\"position\":0.25,\"loop\":true}"))
                .getAsJsonObject("media");
        assertEquals(0.25, media.get("position").getAsDouble());
        assertTrue(media.get("loop").getAsBoolean());
        assertEquals(400, send("PUT", "/api/v2/live/media", CONTROL_TOKEN, "{\"position\":1.5}").statusCode());
        assertEquals(400, send("PUT", "/api/v2/live/media", CONTROL_TOKEN, "{\"position\":\"half\"}").statusCode());

        assertFalse(json(send("POST", "/api/v2/live/media/pause", CONTROL_TOKEN, null))
                .getAsJsonObject("media").get("playing").getAsBoolean());
        assertEquals(0.0, json(send("POST", "/api/v2/live/media/stop", CONTROL_TOKEN, null))
                .getAsJsonObject("media").get("position").getAsDouble());
        assertEquals(403, send("POST", "/api/v2/live/media/play", VIEW_TOKEN, null).statusCode());
    }

    @Test
    void timersPlayButCannotJump() throws Exception {
        control.media = new LiveState.Media("timer", true, null, null);
        assertFalse(json(send("POST", "/api/v2/live/media/pause", CONTROL_TOKEN, null))
                .getAsJsonObject("media").get("playing").getAsBoolean());
        assertEquals(400, send("PUT", "/api/v2/live/media", CONTROL_TOKEN, "{\"loop\":true}").statusCode());
    }

    @Test
    void listAndAddLibraryFiles() throws Exception {
        HttpResponse<String> images = send("GET", "/api/v2/library/images", VIEW_TOKEN, null);
        assertEquals(200, images.statusCode());
        assertEquals("Sunrise.jpg", JsonParser.parseString(images.body()).getAsJsonArray().get(0)
                .getAsJsonObject().get("name").getAsString());
        assertEquals(200, send("GET", "/api/v2/library/videos", VIEW_TOKEN, null).statusCode());
        assertEquals(404, send("GET", "/api/v2/library/audio", VIEW_TOKEN, null).statusCode());

        HttpResponse<String> thumb = send("GET", "/api/v2/library/images/thumb.png?name=Sunrise.jpg", VIEW_TOKEN, null);
        assertEquals(200, thumb.statusCode());
        assertEquals("image/png", thumb.headers().firstValue("content-type").orElse(""));
        assertEquals(404, send("GET", "/api/v2/library/images/thumb.png?name=Missing.jpg", VIEW_TOKEN, null)
                .statusCode());

        HttpResponse<String> added = send("POST", "/api/v2/schedule/items?ifRevision=4", CONTROL_TOKEN,
                "{\"image\":\"Sunrise.jpg\"}");
        assertEquals(200, added.statusCode());
        assertEquals("it_3", json(added).get("itemId").getAsString());
        assertTrue(control.calls.contains("addLibraryFile IMAGE Sunrise.jpg"));
        send("POST", "/api/v2/schedule/items", CONTROL_TOKEN, "{\"video\":\"Intro.mp4\"}");
        assertTrue(control.calls.contains("addLibraryFile VIDEO Intro.mp4"));
        assertEquals(404, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN, "{\"image\":\"Missing.jpg\"}")
                .statusCode());
        assertEquals(403, send("POST", "/api/v2/schedule/items", VIEW_TOKEN, "{\"image\":\"Sunrise.jpg\"}")
                .statusCode());
    }

    @Test
    void addDocumentsAndWebPages() throws Exception {
        assertEquals(200, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN,
                "{\"document\":\"Predikan.pptx\"}").statusCode());
        assertTrue(control.calls.contains("addLibraryFile DOCUMENT Predikan.pptx"));
        assertEquals(200, send("GET", "/api/v2/library/documents", VIEW_TOKEN, null).statusCode());

        assertEquals(200, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN,
                "{\"url\":\"quelea.org/docs\"}").statusCode());
        assertTrue(control.calls.contains("addWebPage http://quelea.org/docs"));
        assertEquals(400, send("POST", "/api/v2/schedule/items", CONTROL_TOKEN,
                "{\"url\":\"file:///etc/passwd\"}").statusCode());
        assertEquals(403, send("POST", "/api/v2/schedule/items", VIEW_TOKEN,
                "{\"url\":\"https://quelea.org\"}").statusCode());
    }

    @Test
    void uploadToALibrary() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort()
                + "/api/v2/library/images?name=" + java.net.URLEncoder.encode("Min bild.jpg", "UTF-8")))
                .header("Authorization", "Bearer " + CONTROL_TOKEN)
                .header("Content-Type", "image/jpeg")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(new byte[5000]))
                .build();
        HttpResponse<String> uploaded = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, uploaded.statusCode());
        assertEquals("Min bild.jpg", json(uploaded).get("name").getAsString());
        assertEquals(List.of("Min bild.jpg 5000 5000"), control.uploads);

        assertEquals(400, send("PUT", "/api/v2/library/images?name=..%2Fevil.jpg", CONTROL_TOKEN, "x").statusCode());
        assertEquals(400, send("PUT", "/api/v2/library/images?name=notes.txt", CONTROL_TOKEN, "x").statusCode());
        assertEquals(400, send("PUT", "/api/v2/library/images", CONTROL_TOKEN, "x").statusCode());
        assertEquals(403, send("PUT", "/api/v2/library/images?name=a.jpg", VIEW_TOKEN, "x").statusCode());
        assertEquals(1, control.uploads.size());
    }

    @Test
    void createANewSong() throws Exception {
        HttpResponse<String> created = send("POST", "/api/v2/songs", CONTROL_TOKEN,
                "{\"title\":\"  Ny  sång \",\"author\":\"Okänd\",\"ccli\":\"\","
                + "\"lyrics\":\"Vers 1\\r\\nRad ett\\r\\n\\r\\nRefräng\\r\\nRad två\\n\"}");
        assertEquals(200, created.statusCode());
        JsonObject song = json(created);
        assertEquals(101, song.get("id").getAsLong());
        assertEquals("Ny sång", song.get("title").getAsString(), "whitespace is tidied");
        assertEquals(2, song.getAsJsonArray("sections").size());
        Catalog.NewSong saved = control.createdSongs.get(0);
        assertEquals("Vers 1\nRad ett\n\nRefräng\nRad två", saved.lyrics(), "line endings are normalised");
        assertNull(saved.ccli(), "blank fields become null");
    }

    @Test
    void newSongErrors() throws Exception {
        assertEquals(400, send("POST", "/api/v2/songs", CONTROL_TOKEN, "{\"lyrics\":\"Hej\"}").statusCode());
        assertEquals(400, send("POST", "/api/v2/songs", CONTROL_TOKEN, "{\"title\":\"Hej\"}").statusCode());
        assertEquals(400, send("POST", "/api/v2/songs", CONTROL_TOKEN,
                "{\"title\":\"Hej\",\"lyrics\":\"" + "x".repeat(20_001) + "\"}").statusCode());
        assertEquals(400, send("POST", "/api/v2/songs", CONTROL_TOKEN,
                "{\"title\":\"" + "x".repeat(201) + "\",\"lyrics\":\"Hej\"}").statusCode());
        assertEquals(403, send("POST", "/api/v2/songs", VIEW_TOKEN,
                "{\"title\":\"Hej\",\"lyrics\":\"Hej\"}").statusCode());
        assertTrue(control.createdSongs.isEmpty());
    }

    @Test
    void showAndRemoveNotices() throws Exception {
        HttpResponse<String> empty = send("GET", "/api/v2/notices", VIEW_TOKEN, null);
        assertEquals(200, empty.statusCode());
        assertEquals(0, json(empty).getAsJsonArray("notices").size());
        assertEquals("Coffee after the service",
                json(empty).getAsJsonArray("saved").get(0).getAsJsonObject().get("text").getAsString());

        HttpResponse<String> added = send("POST", "/api/v2/notices", CONTROL_TOKEN,
                "{\"text\":\"  Car  ABC 123,\\nlights on \",\"times\":3,\"color\":\"#ffcc00\"}");
        assertEquals(200, added.statusCode());
        JsonObject notice = json(added).getAsJsonArray("notices").get(0).getAsJsonObject();
        assertEquals("nt_1", notice.get("id").getAsString());
        assertEquals("Car ABC 123, lights on", notice.get("text").getAsString(), "whitespace is tidied");
        assertEquals(3, notice.get("timesLeft").getAsInt());

        send("POST", "/api/v2/notices", CONTROL_TOKEN, "{\"text\":\"Welcome\",\"times\":0}");
        assertTrue(control.calls.contains("addNotice Welcome 0 null"), "0 means until removed");
        assertTrue(control.calls.contains("addNotice Car ABC 123, lights on 3 #ffcc00"));
        send("POST", "/api/v2/notices", CONTROL_TOKEN, "{\"text\":\"Once\"}");
        assertTrue(control.calls.contains("addNotice Once 1 null"), "once by default");

        HttpResponse<String> removed = send("DELETE", "/api/v2/notices/nt_1", CONTROL_TOKEN, null);
        assertEquals(200, removed.statusCode());
        assertEquals(2, json(removed).getAsJsonArray("notices").size());
        assertEquals(404, send("DELETE", "/api/v2/notices/nt_1", CONTROL_TOKEN, null).statusCode());
    }

    @Test
    void noticeErrors() throws Exception {
        assertEquals(400, send("POST", "/api/v2/notices", CONTROL_TOKEN, "{}").statusCode());
        assertEquals(400, send("POST", "/api/v2/notices", CONTROL_TOKEN, "{\"text\":\"   \"}").statusCode());
        assertEquals(400, send("POST", "/api/v2/notices", CONTROL_TOKEN,
                "{\"text\":\"Hi\",\"times\":101}").statusCode());
        assertEquals(400, send("POST", "/api/v2/notices", CONTROL_TOKEN,
                "{\"text\":\"Hi\",\"times\":1.5}").statusCode());
        assertEquals(400, send("POST", "/api/v2/notices", CONTROL_TOKEN,
                "{\"text\":\"Hi\",\"color\":\"red\"}").statusCode());
        assertEquals(400, send("POST", "/api/v2/notices", CONTROL_TOKEN,
                "{\"text\":\"" + "x".repeat(501) + "\"}").statusCode());
        assertEquals(403, send("POST", "/api/v2/notices", VIEW_TOKEN, "{\"text\":\"Hi\"}").statusCode());
        assertEquals(403, send("DELETE", "/api/v2/notices/nt_1", VIEW_TOKEN, null).statusCode());
        assertEquals(401, send("GET", "/api/v2/notices", null, null).statusCode());
        assertTrue(control.notices.isEmpty());
    }

    @Test
    void transposeASong() throws Exception {
        HttpResponse<String> response = send("POST", "/api/v2/schedule/items/it_1/transpose", CONTROL_TOKEN,
                "{\"semitones\":-2}");
        assertEquals(200, response.statusCode());
        assertEquals("it_1", json(response).get("itemId").getAsString());
        assertTrue(control.calls.contains("transpose it_1 -2"));
        assertEquals(400, send("POST", "/api/v2/schedule/items/it_2/transpose", CONTROL_TOKEN,
                "{\"semitones\":1}").statusCode(), "not a song");
        assertEquals(400, send("POST", "/api/v2/schedule/items/it_1/transpose", CONTROL_TOKEN, "{}").statusCode());
        assertEquals(403, send("POST", "/api/v2/schedule/items/it_1/transpose", VIEW_TOKEN,
                "{\"semitones\":1}").statusCode());
    }

    @Test
    void duplicateEntryIdsAreAccepted() throws Exception {
        control.schedule.add("it_2-2");
        assertEquals(200, send("DELETE", "/api/v2/schedule/items/it_2-2", CONTROL_TOKEN, null).statusCode());
        assertEquals(404, send("DELETE", "/api/v2/schedule/items/bogus", CONTROL_TOKEN, null).statusCode());
    }

    // ---- Songs, bibles, themes and images ----

    @Test
    void songSearchAndDetails() throws Exception {
        HttpResponse<String> found = send("GET", "/api/v2/songs?q=amazing%20grace&limit=5", VIEW_TOKEN, null);
        assertEquals(200, found.statusCode());
        assertEquals(42, JsonParser.parseString(found.body()).getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong());
        assertTrue(control.calls.contains("searchSongs amazing grace 5"));
        assertEquals(400, send("GET", "/api/v2/songs", VIEW_TOKEN, null).statusCode());

        JsonObject song = json(send("GET", "/api/v2/songs/42", VIEW_TOKEN, null));
        assertEquals("John Newton", song.get("author").getAsString());
        assertEquals("Svenska", song.getAsJsonArray("translations").get(0).getAsString());
        assertEquals(404, send("GET", "/api/v2/songs/7", VIEW_TOKEN, null).statusCode());
    }

    @Test
    void bibleLookups() throws Exception {
        assertEquals(2, JsonParser.parseString(send("GET", "/api/v2/bible/translations", VIEW_TOKEN, null).body())
                .getAsJsonArray().size());
        HttpResponse<String> books = send("GET", "/api/v2/bible/books?translation=Svenska%201917", VIEW_TOKEN, null);
        assertEquals(50, JsonParser.parseString(books.body()).getAsJsonArray().get(0).getAsJsonObject()
                .get("chapters").getAsInt());
        assertEquals(404, send("GET", "/api/v2/bible/books?translation=Nope", VIEW_TOKEN, null).statusCode());

        JsonObject passage = json(send("GET", "/api/v2/bible/passage?translation=KJV&ref=John%203%3A16", VIEW_TOKEN, null));
        assertEquals(16, passage.getAsJsonArray("verses").get(0).getAsJsonObject().get("verse").getAsInt());
        assertTrue(control.calls.contains("biblePassage KJV / John 3:16"));
    }

    @Test
    void themesAndImages() throws Exception {
        assertEquals(2, JsonParser.parseString(send("GET", "/api/v2/themes", VIEW_TOKEN, null).body())
                .getAsJsonArray().size());
        assertEquals(200, send("PUT", "/api/v2/live/theme", CONTROL_TOKEN, "{\"name\":\"Blue\"}").statusCode());
        assertEquals(404, send("PUT", "/api/v2/live/theme", CONTROL_TOKEN, "{\"name\":\"Pink\"}").statusCode());

        HttpRequest thumb = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort()
                + "/api/v2/themes/thumb.png?name=Blue")).header("Authorization", "Bearer " + VIEW_TOKEN).build();
        HttpResponse<byte[]> image = http.send(thumb, HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, image.statusCode());
        assertEquals("image/png", image.headers().firstValue("Content-Type").orElse(""));
        assertTrue(java.util.Arrays.equals(FakeQueleaControl.PNG, image.body()));

        assertEquals(200, send("GET", "/api/v2/schedule/items/it_2/slides/0.png", VIEW_TOKEN, null).statusCode());
        assertTrue(control.calls.contains("slideImage it_2 0"));
        assertEquals(404, send("GET", "/api/v2/schedule/items/it_2/slides/9.png", VIEW_TOKEN, null).statusCode());
        assertEquals(401, send("GET", "/api/v2/schedule/items/it_2/slides/0.png", null, null).statusCode());
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
    void mediaChangesAreSentAsEvents() throws Exception {
        Connection c = connectAndAuth(VIEW_TOKEN);
        assertEquals("snapshot", c.recorder().next().get("type").getAsString());
        bus.publish(new RemoteEvent.MediaChanged("it_2", new LiveState.Media("audio", true, false, null)));
        JsonObject event = c.recorder().next();
        assertEquals("live.media", event.get("type").getAsString());
        assertEquals("it_2", event.getAsJsonObject("data").get("itemId").getAsString());
        assertTrue(event.getAsJsonObject("data").getAsJsonObject("media").get("playing").getAsBoolean());
    }

    @Test
    void noticeChangesAreSentAsEvents() throws Exception {
        Connection c = connectAndAuth(VIEW_TOKEN);
        assertEquals("snapshot", c.recorder().next().get("type").getAsString());
        bus.publish(new RemoteEvent.NoticesChanged(List.of(new Notices.Active("nt_4", "Welcome", null, "#ffffff"))));
        JsonObject event = c.recorder().next();
        assertEquals("notices.changed", event.get("type").getAsString());
        JsonObject notice = event.getAsJsonObject("data").getAsJsonArray("notices").get(0).getAsJsonObject();
        assertEquals("nt_4", notice.get("id").getAsString());
        assertTrue(notice.get("timesLeft") == null || notice.get("timesLeft").isJsonNull(), "until removed");
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
        bus.publish(new RemoteEvent.NoticesChanged(List.of()));
        bus.publish(new RemoteEvent.RecordingChanged(true));
        bus.publish(new RemoteEvent.ScreenChanged(false, true, false));
        JsonObject event = c.recorder().next();
        assertEquals("live.screen", event.get("type").getAsString());
        bus.publish(new RemoteEvent.LiveTextChanged("Welcome!"));
        JsonObject text = c.recorder().next();
        assertEquals("live.text", text.get("type").getAsString());
        assertEquals("Welcome!", text.getAsJsonObject("data").get("text").getAsString());
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
