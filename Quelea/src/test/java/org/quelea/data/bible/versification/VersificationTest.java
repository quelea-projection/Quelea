package org.quelea.data.bible.versification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleVerse;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Versification")
public class VersificationTest {

    private static final int PSALMS = 19;
    private static final int JOEL = 29;

    private static Bible english;
    private static Bible hebrew;
    private static Bible mixed;
    private static Bible unknown;

    @BeforeAll
    static void createBibles() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-versification-test").toString());
        }
        english = bible("English",
                book(PSALMS, "Psalms", 6, 12, 8),
                book(JOEL, "Joel", 20, 32, 21));
        hebrew = bible("Hebrew",
                book(PSALMS, "Psaltaren", 6, 12, 9),
                book(JOEL, "Joel", 20, 27, 5, 21));
        mixed = bible("Mixed",
                book(PSALMS, "Psaltaren", 6, 12, 9),
                book(JOEL, "Joel", 20, 32, 21));
        unknown = bible("Unknown",
                book(PSALMS, "Psalms", 6, 12, 8),
                book(JOEL, "Joel", 20, 32, 10));
    }

    @Test
    @DisplayName("Each passage's numbering is found on its own")
    void sniffsEachPassage() {
        SniffingRules.Rule joel = rule("Jol.2:28-3:21");
        SniffingRules.Rule psalm = rule("Psa.3:1-3:8");
        assertEquals(0, english.getVersification().getColumn(joel));
        assertEquals(1, hebrew.getVersification().getColumn(joel));
        assertEquals(0, mixed.getVersification().getColumn(joel));
        assertEquals(0, english.getVersification().getColumn(psalm));
        assertEquals(1, hebrew.getVersification().getColumn(psalm));
        assertEquals(1, mixed.getVersification().getColumn(psalm));
        assertEquals(Versification.UNKNOWN, unknown.getVersification().getColumn(joel));
        assertEquals(Versification.NOT_APPLICABLE, english.getVersification().getColumn(rule("Jon.1:17-2:10")));
    }

    @Test
    @DisplayName("Verses are paired between numbering traditions")
    void pairsAcrossTraditions() {
        assertMatch(english, JOEL, 2, 28, hebrew, "3:1");
        assertMatch(english, JOEL, 3, 21, hebrew, "4:21");
        assertMatch(hebrew, JOEL, 3, 5, english, "2:32");
        assertMatch(english, JOEL, 2, 28, mixed, "2:28");
        assertMatch(mixed, JOEL, 3, 1, hebrew, "4:1");
    }

    @Test
    @DisplayName("A psalm title that is a verse of its own pairs with the first verse")
    void psalmTitles() {
        assertMatch(english, PSALMS, 3, 1, hebrew, "3:1 3:2");
        assertMatch(hebrew, PSALMS, 3, 1, english, "3:1");
        assertMatch(hebrew, PSALMS, 3, 2, english, "3:1");
        assertMatch(hebrew, PSALMS, 3, 9, mixed, "3:9");
        assertMatch(english, PSALMS, 2, 12, hebrew, "2:12");
    }

    @Test
    @DisplayName("Verses are paired by number, with a warning, where a numbering isn't known")
    void unknownNumbering() {
        Versification.Match match = Versification.match(verse(english, JOEL, 2, 28), unknown);
        assertFalse(match.certain());
        assertEquals("2:28", describe(match));
        assertFalse(Versification.match(verse(unknown, JOEL, 3, 1), hebrew).certain());
        assertTrue(Versification.match(verse(unknown, JOEL, 1, 1), hebrew).certain());
    }

    @Test
    @DisplayName("Books are recognised by name, then by number")
    void booksByNameThenNumber() {
        BookCodes codes = BookCodes.get();
        assertTrue(codes.getCodesForName("Psalm").contains("PSA"));
        assertTrue(codes.getCodesForName("I Samuel").contains("1SA"));
        assertTrue(codes.getCodesForName("1. Könige").contains("1KI"));
        assertEquals("WIS", codes.identify("Wisdom of Solomon", null, 45, counts("WIS")));
        assertEquals("GEN", codes.identify("Första Mosebok", null, 1, counts("GEN")));
        assertEquals("SIR", codes.identify(null, null, 70, counts("SIR")));
        assertEquals("SUS", codes.identify("Susanna", null, 87, new int[]{64}));
        assertEquals("BEL", codes.identify("Bel and the Dragon", null, 87, new int[]{42}));
        assertEquals("OBA", codes.identify("Obadja", null, 31, new int[]{21}));
        assertNull(codes.identify("Unknown book", null, 200, new int[]{5}));
    }

    @Test
    @DisplayName("A book whose name and number don't fit its content is recognised by the content")
    void mislabelledBooks() {
        // The Douay-Rheims file calls Sirach "Tobit" and gives it Tobit's number
        assertEquals("SIR", BookCodes.get().identify("Tobit", null, 69, counts("SIR")));
        // A name and a number that disagree are settled by the content
        assertEquals("WIS", BookCodes.get().identify("Wisdom", null, 70, counts("WIS")));
        assertEquals("1MA", BookCodes.get().identify("Baruch", null, 73, counts("1MA")));
    }

    @Test
    @DisplayName("Books are paired by which book they are, not by their number")
    void booksPairedByIdentity() throws Exception {
        Bible luther = bible("Luther", book(69, "Tobias", counts("TOB")), book(70, "Sirach", counts("SIR")));
        Bible douay = bible("Douay", book(69, "Tobit", counts("SIR")), book(79, "Tobias", counts("TOB")));
        assertSame(douay.getBookByNumber(69), douay.getMatchingBook(luther.getBookByNumber(70)));
        assertSame(douay.getBookByNumber(79), douay.getMatchingBook(luther.getBookByNumber(69)));
        assertSame(luther.getBookByNumber(70), luther.getMatchingBook(douay.getBookByNumber(69)));
        Bible protestant = bible("Protestant", book(JOEL, "Joel", 20, 32, 21));
        assertNull(protestant.getMatchingBook(luther.getBookByNumber(70)));
    }

    private static void assertMatch(Bible from, int book, int chapter, int verse, Bible to, String expected) {
        Versification.Match match = Versification.match(verse(from, book, chapter, verse), to);
        assertTrue(match.certain(), from.getName() + " " + chapter + ":" + verse);
        assertEquals(expected, describe(match), from.getName() + " " + chapter + ":" + verse + " in " + to.getName());
    }

    private static String describe(Versification.Match match) {
        List<String> ret = new ArrayList<>();
        for (BibleVerse verse : match.verses()) {
            ret.add(verse.getChapterNum() + ":" + verse.getNum());
        }
        return String.join(" ", ret);
    }

    private static SniffingRules.Rule rule(String name) {
        for (SniffingRules.Rule rule : SniffingRules.get().getRules()) {
            if (rule.getName().equals(name)) {
                return rule;
            }
        }
        throw new AssertionError("No rule " + name);
    }

    private static BibleVerse verse(Bible bible, int book, int chapter, int verse) {
        BibleBook found = bible.getBookByNumber(book);
        return found.getChapter(chapter - 1).getVerse(verse);
    }

    /**
     * Get the verse counts of a book in the English versification.
     */
    static int[] counts(String code) {
        try (Reader reader = BookCodes.open("eng.json")) {
            JsonArray chapters = JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("maxVerses").getAsJsonArray(code);
            int[] ret = new int[chapters.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = Integer.parseInt(chapters.get(i).getAsString());
            }
            return ret;
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    private static String book(int number, String name, int... verseCounts) {
        StringBuilder ret = new StringBuilder("<BIBLEBOOK bnumber=\"" + number + "\" bname=\"" + name + "\">");
        for (int c = 0; c < verseCounts.length; c++) {
            ret.append("<CHAPTER cnumber=\"").append(c + 1).append("\">");
            for (int v = 1; v <= verseCounts[c]; v++) {
                ret.append("<VERS vnumber=\"").append(v).append("\">").append(name).append(' ')
                        .append(c + 1).append(':').append(v).append("</VERS>");
            }
            ret.append("</CHAPTER>");
        }
        return ret.append("</BIBLEBOOK>").toString();
    }

    private static Bible bible(String name, String... books) throws Exception {
        String xml = "<XMLBIBLE biblename=\"" + name + "\">" + String.join("", books) + "</XMLBIBLE>";
        return Bible.parseXML(DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement(), name);
    }
}
