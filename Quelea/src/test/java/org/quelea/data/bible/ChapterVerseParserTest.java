package org.quelea.data.bible;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@DisplayName("Bible library chapter and verse input")
class ChapterVerseParserTest {
    @Test
    void parsesChapterAndSingleVerse() {
        assertRange("1", 0, 0, 0, -1);
        assertRange("3:", 2, 0, 2, -1);
        assertRange(" 3:16 ", 2, 16, 2, 16);
        assertRange(" 3 : 16 ", 2, 16, 2, 16);
    }

    @Test
    void parsesVerseRangesAndCrossChapterRanges() {
        assertRange("3:16-18", 2, 16, 2, 18);
        assertRange("3:16 - 4:2", 2, 16, 3, 2);
        assertRange("3:16-", 2, 16, 2, 1000);
    }

    @Test
    void emptyAndNonNumericInputAreNotAChapter() {
        Assertions.assertTrue(new ChapterVerseParser("").getFromChapter() < 0);
        Assertions.assertTrue(new ChapterVerseParser("abc").getFromChapter() < 0);
        Assertions.assertTrue(new ChapterVerseParser(null).getFromChapter() < 0);
    }

    @Test
    @Disabled("A malformed verse leaves a partially parsed chapter selected")
    void invalidVerseDoesNotSilentlySelectWholeChapter() {
        // If part of an input cannot be parsed, the library must not display a
        // whole chapter as if it were the user's intended verse. Parse into
        // temporary values and publish them only after validating the full input.
        Assertions.assertTrue(new ChapterVerseParser("3:abc").getFromChapter() < 0);
    }

    private void assertRange(String input, int chapter, int verse, int endChapter, int endVerse) {
        ChapterVerseParser parser = new ChapterVerseParser(input);
        Assertions.assertEquals(chapter, parser.getFromChapter(), input);
        Assertions.assertEquals(verse, parser.getFromVerse(), input);
        Assertions.assertEquals(endChapter, parser.getToChapter(), input);
        Assertions.assertEquals(endVerse, parser.getToVerse(), input);
    }
}
