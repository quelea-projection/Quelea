package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class BibleSlidesTest {

    @Test
    void wholeVersesPerSlide() {
        assertEquals(List.of(List.of(0, 1), List.of(2)), BibleSlides.versesPerSlide(
                List.of("<sup>16</sup>For God so loved <sup>17</sup>For God sent not",
                        "<sup>18</sup>He that believeth"),
                List.of("For God so loved", "For God sent not", "He that believeth")));
    }

    @Test
    void aVerseSplitOverTwoSlidesIsOnBoth() {
        assertEquals(List.of(List.of(0, 1), List.of(1, 2)), BibleSlides.versesPerSlide(
                List.of("One two three four", "five six seven"),
                List.of("One two", "three four five", "six seven")));
    }

    @Test
    void withoutVerseNumbersToo() {
        assertEquals(List.of(List.of(0), List.of(0)), BibleSlides.versesPerSlide(
                List.of("a b c", "d e"), List.of("a  b c d e")));
    }
}
