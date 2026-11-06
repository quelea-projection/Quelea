package org.quelea.services.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LineTypeCheckerTest")
public class LineTypeCheckerTest {

    private static LineTypeChecker.Type typeOf(String line) {
        return new LineTypeChecker(line).getLineType();
    }

    @Test
    @DisplayName("Lines of chords are recognised")
    public void chordLines() {
        for (String line : new String[]{"C", "Am7", "G/B", "Dsus4", "F#m", "Bb", "Cmaj7", "Ddim", "E+", "Gaug", "Cadd9",
                "C G Am F", "  C   G  ", "(G) D", "C x2", "2x G", "C-G", "A♯ B♭", "Cº", "Cø", "Hello //chords"}) {
            Assertions.assertEquals(LineTypeChecker.Type.CHORDS, typeOf(line), line);
        }
    }

    @Test
    @DisplayName("Lines of lyrics are not taken for chords")
    public void lyricLines() {
        for (String line : new String[]{"Amazing grace", "Lord", "Be still my soul", "God is good", "Du är god",
                "Halleluja", "A mighty fortress", "C G Am love", "C G //lyrics"}) {
            Assertions.assertEquals(LineTypeChecker.Type.NORMAL, typeOf(line), line);
        }
    }

    @Test
    @DisplayName("The quick character check agrees with the regular expression")
    public void characterCheckAgreesWithRegex() {
        String alphabet = "abcdefghijmnosuABCDEFGHMxyzL#♯♭/0123456789+ºøå";
        java.util.Random random = new java.util.Random(1);
        for (int i = 0; i < 20_000; i++) {
            StringBuilder word = new StringBuilder();
            int length = 1 + random.nextInt(5);
            for (int j = 0; j < length; j++) {
                word.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            String line = word.toString();
            if (line.matches(".*[xX0-9].*") || typeOf(line) == LineTypeChecker.Type.TITLE) {
                continue; // Repeat markers such as "x2" are stripped before the chord check
            }
            Assertions.assertEquals(line.matches("^" + LineTypeChecker.CHORD_REGEX + "$"),
                    typeOf(line) == LineTypeChecker.Type.CHORDS, line);
        }
    }
}
