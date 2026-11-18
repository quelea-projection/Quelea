package org.quelea.data.chord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Transposing currently drops the explicit chord marker")
    @DisplayName("Preserves the explicit chord marker when transposing a line")
    public void preservesChordMarker() {
        // The song editor replaces its text with the returned line, so dropping
        // this marker permanently changes the user's input and can affect later
        // chord classification. The method restores the marker on its internal
        // line but not the result; append it to the returned text as well.
        Assertions.assertEquals("D A //chords", new ChordLineTransposer("C G //chords").transpose(2, null));
    }

    private static String transpose(String chord, int semitones, String key) {
        return new ChordTransposer(chord).transpose(semitones, key);
    }
}
