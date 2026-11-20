package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChordSheetTest {

    @Test
    void chordsGoWithTheLineUnderThem() {
        List<ChordSheet.ChordedLine> lines = ChordSheet.parse(List.of(
                "G           D",
                "Amazing grace how sweet the sound",
                "That saved a wretch like me"));
        assertEquals(List.of(
                new ChordSheet.ChordedLine("Amazing grace how sweet the sound",
                        List.of(new ChordSheet.ChordAt(0, "G"), new ChordSheet.ChordAt(12, "D"))),
                new ChordSheet.ChordedLine("That saved a wretch like me", List.of())), lines);
    }

    @Test
    void chordLinesWithoutLyricsStandAlone() {
        List<ChordSheet.ChordedLine> lines = ChordSheet.parse(List.of("Em  C", "G  D", "Was blind", "F#m7"));
        assertEquals(new ChordSheet.ChordedLine("", List.of(new ChordSheet.ChordAt(0, "Em"),
                new ChordSheet.ChordAt(4, "C"))), lines.get(0));
        assertEquals("Was blind", lines.get(1).text());
        assertEquals(List.of(new ChordSheet.ChordAt(0, "F#m7")), lines.get(2).chords());
    }

    @Test
    void noChordsMeansNull() {
        assertNull(ChordSheet.parse(null));
        assertNull(ChordSheet.parse(List.of("Amazing grace")));
    }

    @Test
    void sectionsWorkOutTheirChords() {
        LiveSections.Section section = new LiveSections.Section("Verse", List.of("Grace"), List.of("C", "Grace"));
        assertEquals(List.of(new ChordSheet.ChordedLine("Grace", List.of(new ChordSheet.ChordAt(0, "C")))),
                section.chorded());
    }
}
