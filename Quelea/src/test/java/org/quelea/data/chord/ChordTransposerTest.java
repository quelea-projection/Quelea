package org.quelea.data.chord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ChordTransposerTest")
public class ChordTransposerTest {

    @Test
    @DisplayName("Transposes up and down across octave boundaries")
    public void transposesSemitones() {
        Assertions.assertEquals("C#", transpose("C", 1, null));
        Assertions.assertEquals("B", transpose("C", -1, null));
        Assertions.assertEquals("C", transpose("C", 12, null));
        Assertions.assertEquals("B", transpose("C", -13, null));
        Assertions.assertEquals("C", transpose("C#", 11, null));
    }

    @Test
    @DisplayName("Understands enharmonic input and preserves chord suffixes")
    public void understandsAccidentalsAndSuffixes() {
        Assertions.assertEquals("Bb", transpose("A#", 0, null));
        Assertions.assertEquals("B", transpose("Cb", 0, null));
        Assertions.assertEquals("Em7", transpose("Dm7", 2, null));
        Assertions.assertEquals("Esus4", transpose("F#sus4", -2, null));
    }

    @Test
    @DisplayName("Uses the destination key to spell accidentals")
    public void spellsAccidentalsForDestinationKey() {
        Assertions.assertEquals("C#", transpose("C", 1, "D"));
        Assertions.assertEquals("Db", transpose("C", 1, "F"));
        Assertions.assertEquals("A#", transpose("A", 1, "D"));
        Assertions.assertEquals("Bb", transpose("A", 1, "F"));
    }

    @Test
    @DisplayName("Transposes both the chord and the bass note")
    public void transposesSlashChords() {
        Assertions.assertEquals("E/G#", transpose("D/F#", 2, null));
        Assertions.assertEquals("E/G#", transpose("E/Ab", 0, null));
        Assertions.assertEquals("Am/C", transpose("Gm/Bb", 2, null));
    }

    @Test
    @DisplayName("Transposes a chord line while keeping its spacing")
    public void preservesLineSpacing() {
        Assertions.assertEquals("D   A/C#  Bm", new ChordLineTransposer("C   G/B  Am").transpose(2, null));
    }

    @Test
    @DisplayName("Preserves the explicit chord marker when transposing a line")
    public void preservesChordMarker() {
        Assertions.assertEquals("D A //chords", new ChordLineTransposer("C G //chords").transpose(2, null));
    }

    @Test
    @DisplayName("Preserves marker case and surrounding spacing")
    public void preservesMarkerCaseAndSpacing() {
        Assertions.assertEquals("  D\t A/C#  //ChOrDs", new ChordLineTransposer("  C\t G/B  //ChOrDs").transpose(2, null));
        Assertions.assertEquals("D//chords", new ChordLineTransposer("C//chords").transpose(2, null));
        Assertions.assertEquals("  //CHORDS", new ChordLineTransposer("  //CHORDS").transpose(2, null));
    }

    @Test
    @DisplayName("Repeated transpositions use the original chord line")
    public void repeatedTranspositionsKeepTheOriginalLine() {
        ChordLineTransposer transposer = new ChordLineTransposer("C G //chords");
        Assertions.assertEquals("D A //chords", transposer.transpose(2, null));
        Assertions.assertEquals("B F# //chords", transposer.transpose(-1, null));
    }

    private static String transpose(String chord, int semitones, String key) {
        return new ChordTransposer(chord).transpose(semitones, key);
    }
}
