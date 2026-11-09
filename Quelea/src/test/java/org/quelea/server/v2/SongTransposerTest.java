package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SongTransposerTest {

    private static final String LYRICS = "Verse 1\nG       C        G\nAmazing grace, how sweet the sound\n\nChorus\nD   G\nGlory";

    @Test
    void keyMoves() {
        assertEquals("A", SongTransposer.transposeKey("G", 2));
        assertEquals("F", SongTransposer.transposeKey("G", -2));
        assertNull(SongTransposer.transposeKey(null, 2));
        assertEquals("", SongTransposer.transposeKey("", 2));
    }

    @Test
    void chordLinesMoveAndTextStays() {
        String transposed = SongTransposer.transposeLyrics(LYRICS, "A", 2);
        String[] lines = transposed.split("\n", -1);
        assertEquals("Verse 1", lines[0]);
        assertTrue(lines[1].strip().startsWith("A"), lines[1]);
        assertTrue(lines[1].contains("D"), lines[1]);
        assertEquals("Amazing grace, how sweet the sound", lines[2]);
        assertEquals("", lines[3], "section breaks stay");
        assertTrue(lines[5].strip().startsWith("E"), lines[5]);
        assertEquals("Glory", lines[6]);
    }

    @Test
    void worksWithoutAKey() {
        String transposed = SongTransposer.transposeLyrics(LYRICS, null, -2);
        assertTrue(transposed.split("\n")[1].strip().startsWith("F"), transposed);
    }

    @Test
    void roundTripGivesTheSameChords() {
        String up = SongTransposer.transposeLyrics(LYRICS, "A", 2);
        String back = SongTransposer.transposeLyrics(up, "G", -2);
        assertEquals(LYRICS.split("\n")[1].strip().replaceAll("\\s+", " "),
                back.split("\n")[1].strip().replaceAll("\\s+", " "));
    }

    @Test
    void lyricsWithoutChordsAreUnchanged() {
        String plain = "Verse 1\nAmazing grace\nhow sweet the sound";
        assertEquals(plain, SongTransposer.transposeLyrics(plain, null, 3));
    }
}
