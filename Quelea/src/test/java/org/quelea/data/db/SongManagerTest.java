package org.quelea.data.db;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javafx.application.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.lucene.SearchIndex;
import org.quelea.services.utils.QueleaProperties;

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
}
