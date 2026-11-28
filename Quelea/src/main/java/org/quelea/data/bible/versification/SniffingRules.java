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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.services.utils.LoggerUtils;

/**
 * The Copenhagen Alliance sniffing rules. Each rule covers a passage where
 * numbering traditions differ. It has a column per tradition (or group of
 * traditions that agree), tests that tell which column a bible follows, and
 * rows that say which verses correspond in each column.
 *
 * @author Arvid
 */
public final class SniffingRules {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Pattern REF = Pattern.compile(
            "^(\\w+)\\.(\\d+):(\\d+)(?:\\.(\\d+))?(?:-(?:(\\d+):)?(\\d+)(?:\\.(\\d+))?)?$");
    private static final Pattern TEST_REF = Pattern.compile(
            "^(\\w+)\\.(\\d+):(\\d+)(?:-(\\d+))?(?:\\.(\\d+))?(?:\\*(\\d+))?$");
    private static volatile SniffingRules instance;

    private final List<Rule> rules = new ArrayList<>();
    private final Map<String, List<Rule>> rulesByBook = new HashMap<>();

    private SniffingRules() {
        try (Reader reader = BookCodes.open("merged_rules.json")) {
            for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                Rule rule = parseRule(element.getAsJsonObject());
                if (rule != null) {
                    rules.add(rule);
                    for (String book : rule.books) {
                        rulesByBook.computeIfAbsent(book, k -> new ArrayList<>()).add(rule);
                    }
                }
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Couldn't read the versification rules", ex);
        }
    }

    /**
     * Get the rules, loading them the first time.
     *
     * @return the rules.
     */
    public static SniffingRules get() {
        if (instance == null) {
            synchronized (SniffingRules.class) {
                if (instance == null) {
                    instance = new SniffingRules();
                }
            }
        }
        return instance;
    }

    /**
     * Get all the rules.
     *
     * @return the rules, in the order of the file.
     */
    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Get the rules whose rows mention a book.
     *
     * @param book the code of the book.
     * @return the rules, in the order of the file.
     */
    public List<Rule> getRules(String book) {
        return rulesByBook.getOrDefault(book, Collections.emptyList());
    }

    private static Rule parseRule(JsonObject json) {
        JsonArray testColumns = json.getAsJsonArray("tests");
        List<List<Test>> tests = new ArrayList<>();
        for (JsonElement column : testColumns) {
            List<Test> list = new ArrayList<>();
            for (JsonElement test : column.getAsJsonArray()) {
                list.add(Test.parse(test.getAsString()));
            }
            tests.add(list);
        }
        int columns = tests.size();
        List<List<List<VerseRef>>> rows = new ArrayList<>();
        for (JsonElement range : json.getAsJsonArray("ranges")) {
            for (Map.Entry<String, JsonElement> entry : range.getAsJsonObject().entrySet()) {
                JsonArray cells = entry.getValue().getAsJsonArray();
                if (cells.size() < columns) {
                    continue;
                }
                List<List<VerseRef>> expanded = new ArrayList<>();
                for (int c = 0; c < columns; c++) {
                    expanded.add(expand(cells.get(c).getAsString()));
                }
                rows.addAll(split(entry.getKey(), expanded));
            }
        }
        return new Rule(json.get("name").getAsString(), tests, rows);
    }

    /**
     * Split a row of verse-by-verse correspondences into one row per verse.
     */
    private static List<List<List<VerseRef>>> split(String kind, List<List<VerseRef>> row) {
        int size = -1;
        boolean even = kind.equals("OneToOne");
        for (List<VerseRef> cell : row) {
            if (cell.isEmpty()) {
                continue;
            }
            if (size == -1) {
                size = cell.size();
            } else if (size != cell.size()) {
                even = false;
            }
        }
        if (!even || size <= 1) {
            return List.of(row);
        }
        List<List<List<VerseRef>>> ret = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<List<VerseRef>> single = new ArrayList<>();
            for (List<VerseRef> cell : row) {
                single.add(cell.isEmpty() ? List.of() : List.of(cell.get(i)));
            }
            ret.add(single);
        }
        return ret;
    }

    /**
     * Expand a reference such as "Psa.31:2-24" into its verses. Parts of
     * verses count as the whole verse, since bible files don't divide verses.
     *
     * @return the verses, or an empty list for a verse that doesn't exist in
     * that tradition.
     */
    static List<VerseRef> expand(String text) {
        String cleaned = text.replaceAll("\\[.*?\\]", "").replaceAll("[,;\\s]+$", "").trim();
        Matcher matcher = REF.matcher(cleaned);
        if (!matcher.matches()) {
            return List.of();
        }
        String book = matcher.group(1).toUpperCase(Locale.ROOT);
        int chapter = Integer.parseInt(matcher.group(2));
        int verse = Integer.parseInt(matcher.group(3));
        if (matcher.group(6) == null) {
            return List.of(new VerseRef(book, chapter, verse));
        }
        if (matcher.group(4) != null && matcher.group(5) == null && matcher.group(7) == null) {
            // A range of parts of one verse, such as 31:1.0-1
            return List.of(new VerseRef(book, chapter, verse));
        }
        int endVerse = Integer.parseInt(matcher.group(6));
        if (matcher.group(5) != null && Integer.parseInt(matcher.group(5)) != chapter) {
            return List.of(new VerseRef(book, chapter, verse),
                    new VerseRef(book, Integer.parseInt(matcher.group(5)), endVerse));
        }
        List<VerseRef> ret = new ArrayList<>();
        for (int v = verse; v <= endVerse; v++) {
            ret.add(new VerseRef(book, chapter, v));
        }
        return ret;
    }

    /**
     * A verse in one numbering.
     *
     * @param book the code of the book.
     * @param chapter the chapter number.
     * @param verse the verse number.
     */
    public record VerseRef(String book, int chapter, int verse) {
    }

    /**
     * One rule.
     */
    public static final class Rule {

        private final String name;
        private final List<List<Test>> tests;
        private final List<List<List<VerseRef>>> rows;
        private final List<String> books = new ArrayList<>();
        private final List<Map<VerseRef, List<Integer>>> rowsByColumn = new ArrayList<>();

        private Rule(String name, List<List<Test>> tests, List<List<List<VerseRef>>> rows) {
            this.name = name;
            this.tests = tests;
            this.rows = rows;
            for (int c = 0; c < tests.size(); c++) {
                rowsByColumn.add(new HashMap<>());
            }
            for (int r = 0; r < rows.size(); r++) {
                for (int c = 0; c < tests.size(); c++) {
                    for (VerseRef ref : rows.get(r).get(c)) {
                        rowsByColumn.get(c).computeIfAbsent(ref, k -> new ArrayList<>()).add(r);
                        if (!books.contains(ref.book())) {
                            books.add(ref.book());
                        }
                    }
                }
            }
        }

        /**
         * Get the name of the rule, such as "Psa.31:1-31:24".
         *
         * @return the name.
         */
        public String getName() {
            return name;
        }

        /**
         * Get the number of columns.
         *
         * @return the number of columns.
         */
        public int getColumns() {
            return tests.size();
        }

        /**
         * Get the tests a bible must pass to follow a column.
         *
         * @param column the column.
         * @return the tests.
         */
        public List<Test> getTests(int column) {
            return tests.get(column);
        }

        /**
         * Get the codes of the books the rule's rows mention.
         *
         * @return the codes.
         */
        public List<String> getBooks() {
            return books;
        }

        /**
         * Determine whether a verse is in the rows of a column.
         *
         * @param column the column.
         * @param verse the verse in that column's numbering.
         * @return true if the rule says where the verse goes.
         */
        public boolean covers(int column, VerseRef verse) {
            return rowsByColumn.get(column).containsKey(verse);
        }

        /**
         * Determine whether a verse is in the rows of any column.
         *
         * @param verse the verse.
         * @return true if any column mentions it.
         */
        public boolean coversAny(VerseRef verse) {
            for (Map<VerseRef, List<Integer>> column : rowsByColumn) {
                if (column.containsKey(verse)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Find the verses in one column that correspond to a verse in another.
         *
         * @param from the column of the verse.
         * @param to the column to map to.
         * @param verse the verse.
         * @return the corresponding verses, in order. Empty if the verse has
         * no counterpart in that column.
         */
        public List<VerseRef> map(int from, int to, VerseRef verse) {
            List<VerseRef> ret = new ArrayList<>();
            for (int row : rowsByColumn.get(from).getOrDefault(verse, List.of())) {
                for (VerseRef ref : rows.get(row).get(to)) {
                    if (!ret.contains(ref)) {
                        ret.add(ref);
                    }
                }
            }
            return ret;
        }

        /**
         * Determine whether two columns pair every verse the same way, so that
         * it doesn't matter which of them a bible follows.
         *
         * @param a one column.
         * @param b the other column.
         * @return true if the columns have the same verses in every row.
         */
        public boolean sameRows(int a, int b) {
            if (a == b) {
                return true;
            }
            for (List<List<VerseRef>> row : rows) {
                if (!row.get(a).equals(row.get(b))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Determine whether every column pairs the verses the same way, so
         * the rule doesn't change anything.
         *
         * @return true if all columns have the same verses in every row.
         */
        public boolean isIdentity() {
            for (int c = 1; c < getColumns(); c++) {
                if (!sameRows(0, c)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * One test, such as "Psa.31:24=Last" or "Mal.4:5&lt;Mal.4:6".
     *
     * @param left the verse tested.
     * @param op the operator: "=", "&lt;" or "&gt;".
     * @param keyword for "=", what is tested: Last, Exist or NotExist.
     * @param right for "&lt;" and "&gt;", the verse compared with.
     */
    public record Test(TestRef left, String op, String keyword, TestRef right) {

        static Test parse(String text) {
            Matcher matcher = Pattern.compile("^(.*?)([=<>])(.*)$").matcher(text.trim());
            if (!matcher.matches()) {
                return new Test(null, "?", null, null);
            }
            TestRef left = TestRef.parse(matcher.group(1));
            String op = matcher.group(2);
            if (op.equals("=")) {
                return new Test(left, op, matcher.group(3).trim(), null);
            }
            return new Test(left, op, null, TestRef.parse(matcher.group(3)));
        }
    }

    /**
     * The verses in a test: one verse, a part of one, or a range of verses in
     * one chapter.
     *
     * @param verse the first verse.
     * @param lastVerse the number of the last verse, the same as the first
     * for a single verse.
     * @param part the part of the verse, or -1 for whole verses.
     * @param factor what to multiply the length of the verses by.
     */
    public record TestRef(VerseRef verse, int lastVerse, int part, int factor) {

        static TestRef parse(String text) {
            Matcher matcher = TEST_REF.matcher(text.trim());
            if (!matcher.matches()) {
                return null;
            }
            VerseRef verse = new VerseRef(matcher.group(1).toUpperCase(Locale.ROOT),
                    Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
            int last = matcher.group(4) == null ? verse.verse() : Integer.parseInt(matcher.group(4));
            int part = matcher.group(5) == null ? -1 : Integer.parseInt(matcher.group(5));
            int factor = matcher.group(6) == null ? 1 : Integer.parseInt(matcher.group(6));
            return new TestRef(verse, last, part, factor);
        }

        /**
         * Get the verses.
         *
         * @return the verses, in order.
         */
        public List<VerseRef> getVerses() {
            List<VerseRef> ret = new ArrayList<>();
            for (int v = verse.verse(); v <= lastVerse; v++) {
                ret.add(new VerseRef(verse.book(), verse.chapter(), v));
            }
            return ret;
        }
    }
}
