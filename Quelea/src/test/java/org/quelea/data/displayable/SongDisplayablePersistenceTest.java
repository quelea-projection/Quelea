package org.quelea.data.displayable;

import java.nio.file.Files;
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
    @Disabled("Copying a song increments the original song's hash component")
    public void copyingASongDoesNotChangeTheOriginalHash() {
        // Copying an object used as a hash-map or hash-set key must not make
        // that key unreachable. Revisit count in the copy constructor and
        // hashCode together, preserving any required duplicate-song behavior.
        SongDisplayable original = new SongDisplayable("Song", "Author");
        original.setLyrics("A verse");
        int hash = original.hashCode();
        new SongDisplayable(original);
        Assertions.assertEquals(hash, original.hashCode());
    }

    @Test
    @Disabled("Changing the sequence does not refresh the cached section order")
    public void changingSequenceReordersExistingSections() {
        // Existing songs should follow an edited sequence immediately. Invalidate
        // and rebuild the sequence whenever it changes, including before direct
        // getLyrics calls, and account for missing or malformed sequence labels.
        SongDisplayable song = new SongDisplayable("Song", "Author");
        song.setLyrics("Verse 1\nFirst verse\n\nChorus\nSing along");
        song.setSequence("C V1 C");
        Assertions.assertEquals("Chorus\nSing along\n\nVerse 1\nFirst verse\n\nChorus\nSing along",
                song.getLyrics(false, false, true));
    }
}
