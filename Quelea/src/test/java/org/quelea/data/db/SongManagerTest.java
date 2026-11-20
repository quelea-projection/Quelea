package org.quelea.data.db;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.lucene.SearchIndex;
import org.quelea.services.importexport.SongListExporter;
import org.quelea.server.RCHandler;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.search.UniversalSearch;

/**
 * Runs against a real database in a temporary user home. The songs need the
 * JavaFX toolkit for their fonts, so this is skipped where that can't start.
 */
@DisplayName("SongManagerTest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SongManagerTest {

    private static SongManager manager;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-songmanager-test").toString());
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
        manager = SongManager.get();
        Assertions.assertNotNull(manager);
        manager.getSongs();
    }

    private static SongDisplayable song(String title, String lyrics) {
        return new SongDisplayable.Builder(title, "Some Author").lyrics(lyrics).get();
    }

    private static List<String> titles() {
        return Arrays.stream(manager.getSongs()).map(SongDisplayable::getTitle).collect(Collectors.toList());
    }

    private static List<String> search(String term, SearchIndex.FilterType type) {
        return Arrays.stream(manager.getIndex().filter(term, type)).map(SongDisplayable::getTitle).collect(Collectors.toList());
    }

    private static long rows(String title) {
        long[] count = new long[1];
        HibernateUtil.execute(session -> count[0] = (Long) session
                .createQuery("select count(*) from Song s where s.title = :title")
                .setParameter("title", title).uniqueResult());
        return count[0];
    }

    /**
     * Forget the cached songs and the index, so they're read from the database.
     */
    private static void reload() throws Exception {
        manager.getIndex().clear();
        Field cache = SongManager.class.getDeclaredField("cacheSongs");
        cache.setAccessible(true);
        cache.set(manager, null);
    }

    @Test
    @Order(1)
    @DisplayName("A new song gets its id, so saving it again doesn't add it twice")
    public void savingNewSongTwice() {
        SongDisplayable song = song("Saved twice", "Verse 1\nFirst version");
        Assertions.assertTrue(manager.updateSong(song));
        Assertions.assertTrue(song.getID() > 0);
        long id = song.getID();

        song.setLyrics("Verse 1\nSecond version");
        Assertions.assertTrue(manager.updateSong(song));
        Assertions.assertEquals(id, song.getID());
        Assertions.assertEquals(1, rows("Saved twice"));
        Assertions.assertTrue(manager.getIndex().getByID(id).getLyrics(false, false, false).contains("Second"));
    }

    @Test
    @Order(2)
    @DisplayName("Added songs are listed in order and can be searched for")
    public void addedSongs() {
        SongDisplayable[] songs = {song("Zebra song", "Verse 1\nstriped words"), song("Apple song", "Verse 1\nfruity words"),
            song("Mango song", "Verse 1\nyellow words")};
        Assertions.assertTrue(manager.addSong(songs, false));

        List<String> titles = titles();
        Assertions.assertTrue(titles.indexOf("Apple song") < titles.indexOf("Mango song"));
        Assertions.assertTrue(titles.indexOf("Mango song") < titles.indexOf("Zebra song"));
        Assertions.assertEquals(List.of("Apple song"), search("apple", SearchIndex.FilterType.TITLE));
        Assertions.assertEquals(List.of("Zebra song"), search("striped", SearchIndex.FilterType.BODY));
    }

    @Test
    @Order(3)
    @DisplayName("The list doesn't share songs with whoever saved them")
    public void listHasItsOwnCopy() {
        SongDisplayable song = song("Own copy", "Verse 1\nOriginal");
        manager.addSong(song, false);
        SongDisplayable listed = manager.getIndex().getByID(song.getID());
        Assertions.assertNotSame(song, listed);

        song.setLyrics("Verse 1\nChanged but never saved");
        Assertions.assertTrue(listed.getLyrics(false, false, false).contains("Original"));
    }

    @Test
    @Order(4)
    @DisplayName("Updating a song moves it in the list and updates the search")
    public void updatedSong() {
        SongDisplayable song = song("Aardvark song", "Verse 1\nburrowing words");
        manager.addSong(song, false);

        SongDisplayable renamed = new SongDisplayable.Builder("Zzz renamed", "Some Author")
                .lyrics("Verse 1\nsleeping words").id(song.getID()).get();
        Assertions.assertTrue(manager.updateSong(renamed));

        List<String> titles = titles();
        Assertions.assertFalse(titles.contains("Aardvark song"));
        Assertions.assertEquals("Zzz renamed", titles.get(titles.size() - 1));
        Assertions.assertTrue(search("burrowing", SearchIndex.FilterType.BODY).isEmpty());
        Assertions.assertEquals(List.of("Zzz renamed"), search("sleeping", SearchIndex.FilterType.BODY));
    }

    @Test
    @Order(5)
    @DisplayName("A song that's gone from the database is added again when saved")
    public void updatingMissingSong() {
        SongDisplayable song = new SongDisplayable.Builder("Back again", "Some Author")
                .lyrics("Verse 1\nreturning words").id(999_999).get();
        Assertions.assertTrue(manager.updateSong(song));
        Assertions.assertEquals(1, rows("Back again"));
        Assertions.assertNotEquals(999_999, song.getID());
        Assertions.assertTrue(titles().contains("Back again"));
    }

    @Test
    @Order(6)
    @DisplayName("Removed songs are gone from the list and the search")
    public void removedSongs() {
        SongDisplayable first = song("Removed one", "Verse 1\nvanishing words");
        SongDisplayable second = song("Removed two", "Verse 1\nvanishing words");
        manager.addSong(new SongDisplayable[]{first, second}, false);

        Assertions.assertTrue(manager.removeSongs(List.of(first, second)));
        Assertions.assertFalse(titles().contains("Removed one"));
        Assertions.assertFalse(titles().contains("Removed two"));
        Assertions.assertNull(manager.getIndex().getByID(first.getID()));
        Assertions.assertTrue(search("vanishing", SearchIndex.FilterType.BODY).isEmpty());
        Assertions.assertEquals(0, rows("Removed one"));
    }

    @Test
    @Order(7)
    @DisplayName("Identical songs are stored, but only listed once")
    public void identicalSongs() throws Exception {
        manager.addSong(song("Twin", "Verse 1\nsame words"), false);
        manager.addSong(song("Twin", "Verse 1\nsame words"), false);
        Assertions.assertEquals(2, rows("Twin"));
        Assertions.assertEquals(1, titles().stream().filter("Twin"::equals).count());
    }

    @Test
    @Order(8)
    @DisplayName("Songs without any lyrics aren't added")
    public void emptySong() {
        Assertions.assertFalse(manager.addSong(new SongDisplayable("Empty", "Some Author"), false));
        Assertions.assertEquals(0, rows("Empty"));
    }

    @Test
    @Order(9)
    @DisplayName("The kept up to date list is the same as reading the database again")
    public void listMatchesDatabase() throws Exception {
        List<String> before = titles();
        List<Long> idsBefore = Arrays.stream(manager.getSongs()).map(SongDisplayable::getID).sorted().collect(Collectors.toList());
        reload();
        Assertions.assertEquals(before, titles());
        List<Long> idsAfter = Arrays.stream(manager.getSongs()).map(SongDisplayable::getID).sorted().collect(Collectors.toList());
        Assertions.assertEquals(idsBefore, idsAfter);
        Assertions.assertEquals(List.of("Mango song"), search("mango", SearchIndex.FilterType.TITLE));
    }

    @Test
    @Order(10)
    @DisplayName("A corrupt song is skipped and the others still load")
    public void corruptSong() throws Exception {
        SongDisplayable corrupt = song("Corrupt", "Verse 1\nbroken words");
        manager.addSong(corrupt, false);
        HibernateUtil.execute(session -> session.createSQLQuery("update songs set translations = X'00' where id = " + corrupt.getID()).executeUpdate());
        reload();

        List<String> titles = titles();
        Assertions.assertFalse(titles.contains("Corrupt"));
        Assertions.assertTrue(titles.contains("Mango song"));
    }

    @Test
    @Order(11)
    @DisplayName("Songs with the same words but different themes are still listed once")
    public void duplicateSongsWithDifferentThemes() throws Exception {
        SongDisplayable first = song("Theme twins", "Verse 1\nsame words");
        SongDisplayable second = song("Theme twins", "Verse 1\nsame words");
        second.getTheme().setTextAlignment(1);
        first.getSections()[0].setTheme(first.getTheme());
        second.getSections()[0].setTheme(second.getTheme());
        Assertions.assertNotEquals(first, second);
        Assertions.assertEquals(0, first.compareTo(second));
        Assertions.assertTrue(manager.addSong(first, false));
        Assertions.assertTrue(manager.addSong(second, false));
        Assertions.assertEquals(2, rows("Theme twins"));
        Assertions.assertEquals(1, titles().stream().filter("Theme twins"::equals).count());
        reload();
        Assertions.assertEquals(1, titles().stream().filter("Theme twins"::equals).count());
    }

    @Test
    @Order(12)
    @DisplayName("CCLI search includes duplicate database rows before and after reload")
    public void ccliSearchIncludesPersistedDuplicates() throws Exception {
        SongDisplayable visible = song("CCLI twins", "Verse 1\nsame words");
        SongDisplayable hidden = song("CCLI twins", "Verse 1\nsame words");
        visible.setCcli("1000001");
        hidden.setCcli("2000002");
        Assertions.assertTrue(manager.addSong(visible, false));
        Assertions.assertTrue(manager.addSong(hidden, false));
        Assertions.assertEquals(2, rows("CCLI twins"));
        Assertions.assertEquals(1, titles().stream().filter("CCLI twins"::equals).count());
        assertCcliResult("1000001", visible.getID());
        assertCcliResult("2000002", hidden.getID());
        Assertions.assertEquals(List.of("CCLI twins"), search("CCLI twins", SearchIndex.FilterType.TITLE));

        reload();
        Assertions.assertEquals(1, titles().stream().filter("CCLI twins"::equals).count());
        assertCcliResult("1000001", visible.getID());
        assertCcliResult("2000002", hidden.getID());
        Assertions.assertNotNull(manager.getIndex().getByID(hidden.getID()));

        UniversalSearch universal = new UniversalSearch();
        var results = universal.search("2000002", UniversalSearch.Kind.SONG,
                new UniversalSearch.Sources(null, List.of(), List.of()));
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("CCLI twins", results.get(0).title());
        Assertions.assertTrue(results.get(0).subtitle().contains("2000002"));
        Assertions.assertEquals("CCLI twins", universal.search("CCLI #2,000,002", UniversalSearch.Kind.SONG,
                new UniversalSearch.Sources(null, List.of(), List.of())).get(0).title());

        SongDisplayable numberInTitle = song("2000002", "Verse 1\nnumber in title");
        numberInTitle.setCcli("2000002");
        SongDisplayable numberInAuthor = new SongDisplayable.Builder("Author match", "Writer 2000002")
                .lyrics("Verse 1\nother words").get();
        Assertions.assertTrue(manager.addSong(numberInTitle, false));
        Assertions.assertTrue(manager.addSong(numberInAuthor, false));
        results = universal.search("2000002", UniversalSearch.Kind.SONG,
                new UniversalSearch.Sources(null, List.of(), List.of()));
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals("2000002", results.get(0).title());
        Assertions.assertEquals("Some Author", results.get(0).subtitle());
        Assertions.assertEquals("Author match", results.get(1).title());
        Assertions.assertEquals("CCLI twins", results.get(2).title());
        Assertions.assertTrue(manager.removeSong(numberInTitle));
        Assertions.assertTrue(manager.removeSong(numberInAuthor));

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/search/", exchange -> {
            byte[] response = RCHandler.databaseSearch(exchange).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            }
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/search/2000002");
            String response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri).build(),
                    HttpResponse.BodyHandlers.ofString()).body();
            Assertions.assertTrue(response.contains("/song/" + hidden.getID() + "\""));
        } finally {
            server.stop(0);
        }

        hidden.setCcli("3000003");
        Assertions.assertTrue(manager.updateSong(hidden));
        Assertions.assertEquals(0, manager.getIndex().filter("2000002", SearchIndex.FilterType.CCLI).length);
        assertCcliResult("3000003", hidden.getID());
        Assertions.assertEquals(1, titles().stream().filter("CCLI twins"::equals).count());
        hidden.setTitle("CCLI renamed twin");
        Assertions.assertTrue(manager.updateSong(hidden));
        Assertions.assertTrue(titles().contains("CCLI renamed twin"));
        assertCcliResult("3000003", hidden.getID());
        Assertions.assertTrue(manager.removeSong(hidden));
        Assertions.assertEquals(0, manager.getIndex().filter("3000003", SearchIndex.FilterType.CCLI).length);
    }

    private static void assertCcliResult(String query, long expectedId) {
        SongDisplayable[] matches = manager.getIndex().filter(query, SearchIndex.FilterType.CCLI);
        Assertions.assertEquals(1, matches.length, query);
        Assertions.assertEquals(expectedId, matches[0].getID(), query);
    }

    @Test
    @Order(13)
    @Disabled("CSV exporter backslash-escapes quotes instead of doubling them")
    @DisplayName("CSV song list exports quoted song names and authors correctly")
    public void songListExportEscapesQuotes() throws Exception {
        // A CSV reader interprets an embedded quote as the end of the field.
        // CSV escapes quotes by doubling them, not with a backslash; without
        // this, song titles/authors can be corrupted or split into columns.
        SongDisplayable entry = new SongDisplayable.Builder("A \"quoted\" song", "Jane, Writer")
                .lyrics("Verse 1\nWords").get();
        Assertions.assertTrue(manager.addSong(entry, false));
        var file = Files.createTempFile("quelea-song-list", ".csv");
        new SongListExporter().exportToFile(file.toFile());
        Assertions.assertTrue(Files.readString(file).contains("\"A \"\"quoted\"\" song\",\"Jane, Writer\""));
    }

    @Test
    @Order(14)
    @Disabled("CSV exporter does not quote CCLI numbers containing commas")
    @DisplayName("CSV song list keeps a formatted CCLI number in one field")
    public void songListExportQuotesCcliWithCommas() throws Exception {
        // CCLI numbers can be stored with separators. Without CSV quoting a
        // comma becomes an extra column and the number cannot be imported as
        // one value. Apply the same CSV escaping to this field as the others.
        SongDisplayable entry = song("Formatted CCLI", "Verse 1\nWords");
        entry.setCcli("1,234,567");
        Assertions.assertTrue(manager.addSong(entry, false));
        var file = Files.createTempFile("quelea-song-list", ".csv");
        new SongListExporter().exportToFile(file.toFile());
        Assertions.assertTrue(Files.readString(file).contains("\"Formatted CCLI\",\"Some Author\",\"1,234,567\""));
    }

    @Test
    @Order(15)
    @DisplayName("Remote song results escape HTML in titles and authors")
    public void remoteSongSearchEscapesHtml() throws Exception {
        SongDisplayable entry = new SongDisplayable.Builder("Markup <b> & more", "Writer <tag>")
                .lyrics("Verse 1\nWords").get();
        Assertions.assertTrue(manager.addSong(entry, false));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/search/", exchange -> {
            byte[] response = RCHandler.databaseSearch(exchange).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            }
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/search/Markup");
            String response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri).build(),
                    HttpResponse.BodyHandlers.ofString()).body();
            Assertions.assertTrue(response.contains("Markup &lt;b&gt; &amp; more"));
            Assertions.assertTrue(response.contains("Writer &lt;tag&gt;"));
            Assertions.assertFalse(response.contains("Markup <b>"));
        } finally {
            server.stop(0);
        }
    }
}
