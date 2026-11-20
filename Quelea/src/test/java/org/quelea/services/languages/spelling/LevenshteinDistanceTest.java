package org.quelea.services.languages.spelling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Spelling distance")
public class LevenshteinDistanceTest {
    @Test
    public void countsInsertionsDeletionsAndSubstitutions() {
        Assertions.assertEquals(0, distance("song", "song"));
        Assertions.assertEquals(1, distance("song", "songs"));
        Assertions.assertEquals(1, distance("songs", "song"));
        Assertions.assertEquals(1, distance("song", "sang"));
        Assertions.assertEquals(3, distance("kitten", "sitting"));
        Assertions.assertEquals(2, distance("ab", "ba"));
    }

    @Test
    public void handlesEmptyStringsAndOtherCharSequences() {
        Assertions.assertEquals(0, distance("", ""));
        Assertions.assertEquals(4, distance("", "word"));
        Assertions.assertEquals(4, distance("word", ""));
        Assertions.assertEquals(1, LevenshteinDistance.computeLevenshteinDistance(new StringBuilder("cat"), "bat"));
    }

    private static int distance(String first, String second) {
        int result = LevenshteinDistance.computeLevenshteinDistance(first, second);
        Assertions.assertEquals(result, LevenshteinDistance.computeLevenshteinDistance(second, first));
        return result;
    }
}
