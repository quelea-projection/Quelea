/*
 * This file is part of Quelea, free projection software for churches.
 *
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.data.bible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.services.lucene.SearchIndexUtils;

/**
 * Recognises bible references in free text, such as "Joh 3:16", "1 Kor 13:4-7"
 * or "Psalm 23". The book is looked up in a bible, so abbreviations work in the
 * language of that bible, and something only counts as a reference if the
 * passage really exists there.
 * <p/>
 * A passage is written the same way as in the library bible panel, with the
 * addition that "3 16", "3.16" and "3,16" all mean chapter 3, verse 16, and
 * that a lone number after a verse is another verse: "3:16,18".
 *
 * @author Arvid
 */
public final class BibleReferenceParser {

    /**
     * A passage of a bible that was recognised.
     *
     * @param book the book of the passage.
     * @param passage the chapters and verses, e.g. "3:16-18".
     * @param verses the verses of the passage, never empty.
     * @param multi true if the verses should be shown with their chapter.
     */
    public record Reference(BibleBook book, String passage, List<BibleVerse> verses, boolean multi) {

        /**
         * Get the location in the form used for bible passages in the schedule.
         *
         * @return the book and the passage, e.g. "John 3:16".
         */
        public String getLocation() {
            return book + " " + passage;
        }
    }

    /** A book number, a book name and something that starts with a number. */
    private static final Pattern QUERY = Pattern.compile(
            "^\\s*(?<book>(?:[1-3][.\\s]*)?\\p{L}[\\p{L}.\\s]*?)[.\\s]*(?<passage>\\d[\\d\\s:.,;\\-–—]*)$");
    private static final Pattern SECTION = Pattern.compile(
            "(?<from>\\d{1,3})(?::(?<fromVerse>\\d{1,3}))?(?:-(?:(?<toChapter>\\d{1,3}):)?(?<to>\\d{1,3}))?");
    private static final Pattern BOOK_NUMBER_WORD = Pattern.compile(
            "^(iii|ii|i|forsta|andra|tredje|first|second|third)(?:[.\\s]+)(?=\\p{L})");
    private static final Map<String, String> BOOK_NUMBERS = Map.of(
            "i", "1", "ii", "2", "iii", "3",
            "forsta", "1", "andra", "2", "tredje", "3",
            "first", "1", "second", "2", "third", "3");
    private static final int MIN_BOOK_LENGTH = 2;
    private static final int MAX_ABBREVIATION_LENGTH = 3;
    private static final int NO_MATCH = Integer.MAX_VALUE;

    private BibleReferenceParser() {
        throw new AssertionError();
    }

    /**
     * Find the passages a query could refer to. A query can fit more than one
     * book: "Jo 3" could be Joshua, Job, Joel, Jonah or John. Only the books
     * that fit best are returned, in the order of the bible.
     *
     * @param query the text to look at.
     * @param bible the bible to look the passage up in.
     * @return the passages, empty if the query isn't a bible reference.
     */
    public static List<Reference> parse(String query, Bible bible) {
        Matcher matcher = QUERY.matcher(query);
        if (bible == null || !matcher.matches()) {
            return List.of();
        }
        String bookQuery = normaliseBookName(matcher.group("book"));
        String passage = normalisePassage(matcher.group("passage"));
        if (bookQuery.length() < MIN_BOOK_LENGTH) {
            return List.of();
        }
        int bestRank = NO_MATCH;
        List<Reference> best = new ArrayList<>();
        for (BibleBook book : bible.getBooks()) {
            int rank = rankBook(bookQuery, book);
            if (rank > bestRank || rank == NO_MATCH) {
                continue;
            }
            Reference reference = resolve(book, passage);
            if (reference == null) {
                continue;
            }
            if (rank < bestRank) {
                bestRank = rank;
                best.clear();
            }
            best.add(reference);
        }
        return best;
    }

    /**
     * Work out how well a query fits a book, where lower is better.
     */
    private static int rankBook(String query, BibleBook book) {
        if (book.getBookName() == null) {
            return NO_MATCH;
        }
        String name = normaliseBookName(book.getBookName());
        String shortName = book.getBSName() == null ? name : normaliseBookName(book.getBSName());
        if (query.equals(name) || query.equals(shortName)) {
            return 0;
        }
        if (name.startsWith(query) || shortName.startsWith(query)) {
            return 1;
        }
        // "Kor 13" for 1 and 2 Corinthians
        if (name.substring(leadingDigits(name)).startsWith(query)) {
            return 2;
        }
        // "Apg" for Apostlagärningarna and "Mt" for Matthew
        if (query.length() <= MAX_ABBREVIATION_LENGTH && isAbbreviationOf(query, name)) {
            return 3;
        }
        return NO_MATCH;
    }

    private static int leadingDigits(String name) {
        int count = 0;
        while (count < name.length() && Character.isDigit(name.charAt(count))) {
            count++;
        }
        return count;
    }

    private static boolean isAbbreviationOf(String abbreviation, String name) {
        if (name.isEmpty() || abbreviation.charAt(0) != name.charAt(0)) {
            return false;
        }
        int found = 0;
        for (int i = 0; i < name.length() && found < abbreviation.length(); i++) {
            if (name.charAt(i) == abbreviation.charAt(found)) {
                found++;
            }
        }
        return found == abbreviation.length();
    }

    /**
     * Make book names comparable: "II Kor.", "2 kor" and "Andra Kor" all give
     * "2kor".
     */
    private static String normaliseBookName(String name) {
        String folded = SearchIndexUtils.fold(name).trim();
        Matcher number = BOOK_NUMBER_WORD.matcher(folded);
        if (number.find()) {
            folded = BOOK_NUMBERS.get(number.group(1)) + folded.substring(number.end());
        }
        return folded.replaceAll("[^\\p{L}\\d]", "");
    }

    /**
     * Tidy a passage up. Trailing separators are dropped so that the results
     * don't disappear for a moment while "3:16-18" is being typed.
     */
    private static String normalisePassage(String passage) {
        String ret = passage.replaceAll("[–—]", "-")
                .replaceAll("\\s*([:,;-])\\s*", "$1")
                .replaceAll("[\\s.:,;-]+$", "");
        if (!ret.contains(":")) {
            ret = ret.replaceFirst("^(\\d+)[\\s.,](?=\\d)", "$1:");
        }
        return ret;
    }

    /**
     * Look a passage up in a book.
     *
     * @return the passage, or null if any part of it doesn't exist.
     */
    private static Reference resolve(BibleBook book, String passage) {
        String[] sections = passage.split("[,;]");
        List<BibleVerse> verses = new ArrayList<>();
        boolean multi = sections.length > 1;
        int chapter = -1;
        for (String section : sections) {
            Matcher matcher = SECTION.matcher(section);
            if (!matcher.matches()) {
                return null;
            }
            int from = Integer.parseInt(matcher.group("from"));
            int to = matcher.group("to") == null ? from : Integer.parseInt(matcher.group("to"));
            List<BibleVerse> found;
            if (matcher.group("fromVerse") != null) {
                int fromVerse = Integer.parseInt(matcher.group("fromVerse"));
                int toChapter = matcher.group("toChapter") == null ? from : Integer.parseInt(matcher.group("toChapter"));
                int toVerse = matcher.group("to") == null ? fromVerse : to;
                found = getVerses(book, from, fromVerse, toChapter, toVerse);
                chapter = toChapter;
                multi |= from != toChapter;
            } else if (chapter != -1) {
                found = getVerses(book, chapter, from, chapter, to);
            } else {
                found = getVerses(book, from, 1, to, Integer.MAX_VALUE);
                multi |= from != to;
            }
            if (found.isEmpty()) {
                return null;
            }
            verses.addAll(found);
        }
        return new Reference(book, passage, verses, multi);
    }

    /**
     * Get the verses between two places in a book. Chapter and verse numbers
     * start at 1.
     */
    private static List<BibleVerse> getVerses(BibleBook book, int fromChapter, int fromVerse, int toChapter, int toVerse) {
        List<BibleVerse> ret = new ArrayList<>();
        for (int c = fromChapter; c <= toChapter; c++) {
            BibleChapter chapter = book.getChapter(c - 1);
            if (chapter == null) {
                return List.of();
            }
            int first = c == fromChapter ? fromVerse : 1;
            int last = c == toChapter ? toVerse : Integer.MAX_VALUE;
            Arrays.stream(chapter.getVerses())
                    .filter(verse -> verse.getNum() >= first && verse.getNum() <= last)
                    .sorted(Comparator.comparingInt(BibleVerse::getNum))
                    .forEach(ret::add);
        }
        return ret;
    }
}
