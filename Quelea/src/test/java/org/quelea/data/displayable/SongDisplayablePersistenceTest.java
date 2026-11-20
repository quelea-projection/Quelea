package org.quelea.data.displayable;

import java.nio.file.Files;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Song persistence and sequencing")
public class SongDisplayablePersistenceTest {
    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-song-persistence-test").toString());
        }
    }

    @Test
    public void xmlRoundTripPreservesMetadataSequenceAndTranslations() {
        SongDisplayable song = new SongDisplayable("Grace & peace", "Jane <Writer>");
        song.setCcli("123");
        song.setCopyright("Rights & more");
        song.setPublisher("Publisher");
        song.setYear("2020");
        song.setKey("G");
        song.setCapo("2");
        song.setInfo("Notes & details");
        song.setSequence("V1 C V1");
        song.setLyrics("Verse 1\nFirst verse\n\nChorus\nSing along");
        song.addTranslation("Français & English", "Couplet\nLa grâce");
        song.setCurrentTranslationLyrics("Français & English");
        song.setNoDBUpdate();

        SongDisplayable restored = SongDisplayable.parseXML(song.getXML());
        Assertions.assertNotNull(restored);
        Assertions.assertEquals(song.getTitle(), restored.getTitle());
        Assertions.assertEquals(song.getAuthor(), restored.getAuthor());
        Assertions.assertEquals(song.getCcli(), restored.getCcli());
        Assertions.assertEquals(song.getCopyright(), restored.getCopyright());
        Assertions.assertEquals(song.getPublisher(), restored.getPublisher());
        Assertions.assertEquals(song.getYear(), restored.getYear());
        Assertions.assertEquals(song.getKey(), restored.getKey());
        Assertions.assertEquals(song.getCapo(), restored.getCapo());
        Assertions.assertEquals(song.getInfo(), restored.getInfo());
        Assertions.assertEquals(song.getSequence(), restored.getSequence());
        Assertions.assertEquals(song.getLyrics(true, true, false), restored.getLyrics(true, true, false));
        Assertions.assertEquals(song.getLyrics(true, true, true), restored.getLyrics(true, true, true));
        Assertions.assertEquals("Français & English", restored.getCurrentTranslationName());
        Assertions.assertEquals("Couplet\nLa grâce", restored.getCurrentTranslationLyrics());
        Assertions.assertFalse(restored.checkDBUpdate());
    }

    @Test
    public void copyingASongDoesNotChangeTheOriginalHash() {
        SongDisplayable original = new SongDisplayable("Song", "Author");
        original.setLyrics("A verse");
        var songs = new java.util.HashSet<SongDisplayable>();
        songs.add(original);
        int hash = original.hashCode();
        SongDisplayable copy = new SongDisplayable(original);
        Assertions.assertEquals(hash, original.hashCode());
        Assertions.assertTrue(songs.contains(original));
        Assertions.assertEquals(original, copy);
        Assertions.assertEquals(original.hashCode(), copy.hashCode());
        Assertions.assertTrue(songs.contains(copy));
    }

    @Test
    @Disabled("Both song XML entry points mistake a leading comment for the root element")
    public void xmlCommentBeforeRootDoesNotHideSongData() {
        // Use the document element in both parseXML overloads, after disabling
        // external XML entities. Otherwise song packs with comments can import
        // blank songs, and changing the root alone can expose existing XXE.
        SongDisplayable song = new SongDisplayable("Commented song", "Author");
        song.setLyrics("Words");
        String xml = "<?xml version=\"1.0\"?><!-- comment -->" + song.getXML();
        SongDisplayable fromString = SongDisplayable.parseXML(xml);
        SongDisplayable fromStream = SongDisplayable.parseXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNotNull(fromString);
        Assertions.assertNotNull(fromStream);
        Assertions.assertEquals("Commented song", fromString.getTitle());
        Assertions.assertEquals("Commented song", fromStream.getTitle());
    }

    @Test
    public void changingTheThemeLayoutDoesNotChangeTheContainingSongHash() {
        SongDisplayable song = new SongDisplayable("Song", "Author");
        song.setLyrics("A verse");
        var songs = new java.util.HashSet<SongDisplayable>();
        songs.add(song);
        int hash = song.hashCode();
        song.getTheme().setThemeName("Renamed");
        song.getTheme().setTextAlignment(1);
        song.getTheme().setTextPosition(2);
        Assertions.assertEquals(hash, song.hashCode());
        Assertions.assertTrue(songs.contains(song));
    }

    @Test
    public void changingSequenceReordersExistingSections() {
        SongDisplayable song = new SongDisplayable("Song", "Author");
        song.setLyrics("Verse 1\nFirst verse\n\nChorus\nSing along");
        song.setSequence("C V1 C");
        Assertions.assertEquals("Chorus\nSing along\n\nVerse 1\nFirst verse\n\nChorus\nSing along",
                song.getLyrics(false, false, true));
        SongDisplayable copy = new SongDisplayable(song);
        Assertions.assertEquals(song.getLyrics(false, false, true), copy.getLyrics(false, false, true));
        song.setSequence("  V1   C  ");
        Assertions.assertEquals("Verse 1\nFirst verse\n\nChorus\nSing along", song.getLyrics(false, false, true));
        song.setSequence(null);
        Assertions.assertEquals(song.getLyrics(false, false, false), song.getLyrics(false, false, true));
    }
}
