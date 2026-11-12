/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.server.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Works out which verses of a Bible passage are on which slide. Quelea
 * fills its slides word by word from the verses (see BiblePassage), so
 * counting words, without the verse numbers, finds every verse's slides;
 * a verse split over two slides is on both.
 */
final class BibleSlides {

    private static final Pattern VERSE_NUMBER = Pattern.compile("<sup>[^<]*</sup>");
    private static final Pattern SPACE = Pattern.compile("\\s+");

    private BibleSlides() {
    }

    /**
     * @param slides the slides' display text, verse numbers in {@code <sup>}.
     * @param verses the passage's verse texts, in order.
     * @return for each slide, the indices of the verses on it.
     */
    static List<List<Integer>> versesPerSlide(List<String> slides, List<String> verses) {
        List<List<Integer>> result = new ArrayList<>();
        int verse = 0;
        int wordsLeftInVerse = verse < verses.size() ? words(verses.get(verse)) : 0;
        for (String slide : slides) {
            List<Integer> onSlide = new ArrayList<>();
            int words = words(VERSE_NUMBER.matcher(slide).replaceAll(" "));
            while (words > 0 && verse < verses.size()) {
                if (wordsLeftInVerse == 0) {
                    // An empty verse: skip it.
                    verse++;
                    wordsLeftInVerse = verse < verses.size() ? words(verses.get(verse)) : 0;
                    continue;
                }
                if (onSlide.isEmpty() || onSlide.get(onSlide.size() - 1) != verse) {
                    onSlide.add(verse);
                }
                int taken = Math.min(words, wordsLeftInVerse);
                words -= taken;
                wordsLeftInVerse -= taken;
                if (wordsLeftInVerse == 0) {
                    verse++;
                    wordsLeftInVerse = verse < verses.size() ? words(verses.get(verse)) : 0;
                }
            }
            result.add(onSlide);
        }
        return result;
    }

    private static int words(String text) {
        String trimmed = text == null ? "" : text.strip();
        return trimmed.isEmpty() ? 0 : SPACE.split(trimmed).length;
    }
}
