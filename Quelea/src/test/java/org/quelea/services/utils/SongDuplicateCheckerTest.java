package org.quelea.services.utils;

import java.util.Random;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SongDuplicateCheckerTest")
public class SongDuplicateCheckerTest {

    @Test
    @DisplayName("Lyrics within a tenth of the shorter length are similar")
    public void similarLyrics() {
        String lyrics = "a".repeat(100);
        Assertions.assertTrue(SongDuplicateChecker.isSimilar(lyrics, lyrics));
        Assertions.assertTrue(SongDuplicateChecker.isSimilar(lyrics, "b".repeat(10) + "a".repeat(90)));
        Assertions.assertFalse(SongDuplicateChecker.isSimilar(lyrics, "b".repeat(11) + "a".repeat(89)));
        Assertions.assertTrue(SongDuplicateChecker.isSimilar(lyrics, lyrics + "b".repeat(10)));
        Assertions.assertFalse(SongDuplicateChecker.isSimilar(lyrics, lyrics + "b".repeat(11)));
        Assertions.assertTrue(SongDuplicateChecker.isSimilar("", ""));
        Assertions.assertFalse(SongDuplicateChecker.isSimilar("", "a"));
    }

    @Test
    @DisplayName("The result is the same as comparing the full edit distance")
    public void sameAsFullEditDistance() {
        Random random = new Random(1);
        for (int i = 0; i < 5_000; i++) {
            StringBuilder first = new StringBuilder();
            int length = random.nextInt(120);
            for (int j = 0; j < length; j++) {
                first.append((char) ('a' + random.nextInt(4)));
            }
            StringBuilder second = new StringBuilder(first);
            int edits = random.nextInt(25);
            for (int j = 0; j < edits && second.length() > 0; j++) {
                int position = random.nextInt(second.length());
                switch (random.nextInt(3)) {
                    case 0 -> second.deleteCharAt(position);
                    case 1 -> second.insert(position, 'z');
                    default -> second.setCharAt(position, 'y');
                }
            }
            String a = first.toString();
            String b = second.toString();
            boolean expected = new LevenshteinDistance().apply(a, b) <= Math.min(a.length(), b.length()) / 10;
            Assertions.assertEquals(expected, SongDuplicateChecker.isSimilar(a, b), a + " / " + b);
        }
    }
}
