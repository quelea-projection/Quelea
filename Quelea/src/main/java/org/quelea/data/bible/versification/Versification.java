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
package org.quelea.data.bible.versification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleChapter;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.bible.versification.SniffingRules.Rule;
import org.quelea.data.bible.versification.SniffingRules.Test;
import org.quelea.data.bible.versification.SniffingRules.TestRef;
import org.quelea.data.bible.versification.SniffingRules.VerseRef;

/**
 * The numbering a bible follows, passage by passage. Bible files don't say
 * which numbering tradition they follow, and many follow one tradition in
 * some books and another in others, so each sniffing rule is tested on its
 * own.
 *
 * @author Arvid
 */
public final class Versification {

    /**
     * The column of a rule whose tests no column, or several columns that
     * pair verses differently, passed.
     */
    public static final int UNKNOWN = -1;

    /**
     * The column of a rule about books the bible doesn't contain.
     */
    public static final int NOT_APPLICABLE = -2;

    private final Map<String, BibleBook> books = new HashMap<>();
    private final Map<Rule, Integer> columns = new HashMap<>();

    /**
     * Work out the numbering of a bible.
     *
     * @param bible the bible.
     */
    public Versification(Bible bible) {
        for (BibleBook book : bible.getBooks()) {
            String code = book.getBookCode();
            if (code != null && !books.containsKey(code)) {
                books.put(code, book);
            }
        }
        for (Rule rule : SniffingRules.get().getRules()) {
            columns.put(rule, findColumn(rule));
        }
    }

    /**
     * Get the column of a rule this bible follows.
     *
     * @param rule the rule.
     * @return the column, {@link #UNKNOWN} or {@link #NOT_APPLICABLE}.
     */
    public int getColumn(Rule rule) {
        return columns.getOrDefault(rule, NOT_APPLICABLE);
    }

    /**
     * Find the verses in another bible that correspond to a verse.
     *
     * @param verse the verse, in a bible that is loaded.
     * @param target the bible to look in.
     * @return the verses, and whether the numbering of both bibles is known
     * where the verse is.
     */
    public static Match match(BibleVerse verse, Bible target) {
        BibleBook sourceBook = verse.getChapter().getBook();
        Bible source = sourceBook.getBible();
        BibleBook targetBook = target.getMatchingBook(sourceBook);
        String code = sourceBook.getBookCode();
        if (source == null || source == target || code == null) {
            return byNumber(verse, targetBook, source == target);
        }
        VerseRef ref = new VerseRef(code, verse.getChapterNum(), verse.getNum());
        Versification from = source.getVersification();
        Versification to = target.getVersification();
        boolean certain = true;
        for (Rule rule : SniffingRules.get().getRules(code)) {
            int fromColumn = from.getColumn(rule);
            if (fromColumn < 0) {
                if (fromColumn == UNKNOWN && rule.coversAny(ref)) {
                    certain = false;
                }
                continue;
            }
            if (!rule.covers(fromColumn, ref)) {
                continue;
            }
            int toColumn = to.getColumn(rule);
            if (toColumn < 0) {
                return byNumber(verse, targetBook, false);
            }
            if (toColumn == fromColumn || rule.sameRows(fromColumn, toColumn)) {
                return byNumber(verse, targetBook, true);
            }
            List<BibleVerse> ret = new ArrayList<>();
            for (VerseRef mapped : rule.map(fromColumn, toColumn, ref)) {
                BibleVerse found = find(mapped.book().equals(code) ? targetBook : target.getBookByCode(mapped.book()),
                        mapped.chapter(), mapped.verse());
                if (found != null) {
                    ret.add(found);
                }
            }
            return new Match(ret, true);
        }
        return byNumber(verse, targetBook, certain);
    }

    private static Match byNumber(BibleVerse verse, BibleBook book, boolean certain) {
        BibleVerse found = find(book, verse.getChapterNum(), verse.getNum());
        return new Match(found == null ? List.of() : List.of(found), certain);
    }

    /**
     * Find a verse in a book.
     *
     * @return the verse, or null if it doesn't exist or is empty.
     */
    private static BibleVerse find(BibleBook book, int chapter, int verse) {
        BibleChapter found = getChapter(book, chapter);
        if (found == null) {
            return null;
        }
        BibleVerse ret = found.getVerse(verse);
        return ret == null || ret.getText() == null || ret.getText().isBlank() ? null : ret;
    }

    private static BibleChapter getChapter(BibleBook book, int number) {
        if (book == null) {
            return null;
        }
        BibleChapter chapter = book.getChapter(number - 1);
        if (chapter != null && chapter.getNum() == number) {
            return chapter;
        }
        for (BibleChapter other : book.getChapters()) {
            if (other.getNum() == number) {
                return other;
            }
        }
        return null;
    }

    private int findColumn(Rule rule) {
        boolean applicable = false;
        for (String book : rule.getBooks()) {
            if (books.containsKey(book)) {
                applicable = true;
                break;
            }
        }
        if (!applicable || rule.isIdentity()) {
            return NOT_APPLICABLE;
        }
        int ret = UNKNOWN;
        for (int column = 0; column < rule.getColumns(); column++) {
            if (passes(rule.getTests(column))) {
                if (ret == UNKNOWN) {
                    ret = column;
                } else if (!rule.sameRows(ret, column)) {
                    return UNKNOWN;
                }
            }
        }
        return ret;
    }

    private boolean passes(List<Test> tests) {
        for (Test test : tests) {
            if (!passes(test)) {
                return false;
            }
        }
        return !tests.isEmpty();
    }

    private boolean passes(Test test) {
        TestRef left = test.left();
        if (left == null) {
            return false;
        }
        BibleChapter chapter = getChapter(books.get(left.verse().book()), left.verse().chapter());
        if (test.op().equals("=") && "NotExist".equals(test.keyword())) {
            // Bible files don't divide verses into parts
            return left.part() >= 0 || chapter == null || getText(left) == null;
        }
        if (chapter == null || left.part() >= 0) {
            return false;
        }
        String text = getText(left);
        if (text == null) {
            return false;
        }
        switch (test.op()) {
            case "=":
                if ("Last".equals(test.keyword())) {
                    return isLast(chapter, left.lastVerse());
                }
                return "Exist".equals(test.keyword()) && getText(left) != null;
            case "<":
            case ">":
                TestRef right = test.right();
                String other = right == null || right.part() >= 0 ? null : getText(right);
                if (other == null) {
                    return false;
                }
                long leftLength = (long) text.length() * left.factor();
                long rightLength = (long) other.length() * right.factor();
                return test.op().equals("<") ? leftLength <= rightLength : leftLength >= rightLength;
            default:
                return false;
        }
    }

    private static boolean isLast(BibleChapter chapter, int verse) {
        int last = 0;
        for (BibleVerse other : chapter.getVerses()) {
            if (other.getText() != null && !other.getText().isBlank()) {
                last = Math.max(last, other.getNum());
            }
        }
        return last == verse;
    }

    /**
     * Get the text of the verses in a test.
     *
     * @return the text, or null if any of the verses doesn't exist.
     */
    private String getText(TestRef ref) {
        StringBuilder ret = new StringBuilder();
        for (VerseRef verse : ref.getVerses()) {
            BibleVerse found = find(books.get(verse.book()), verse.chapter(), verse.verse());
            if (found == null) {
                return null;
            }
            ret.append(found.getText().replaceAll("\\s+", " ").trim()).append(' ');
        }
        return ret.toString().trim();
    }

    /**
     * The verses in one bible that correspond to a verse in another.
     *
     * @param verses the verses, in order. Empty if the bible doesn't have the
     * verse.
     * @param certain true if the numbering of both bibles is known where the
     * verse is, false if the verses were paired by number because it isn't.
     */
    public record Match(List<BibleVerse> verses, boolean certain) {
    }
}
