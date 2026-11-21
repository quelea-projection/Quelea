package org.quelea.data.bible;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.BibleReferenceParser.Lookup;
import org.quelea.data.bible.BibleReferenceParser.Reference;
import org.quelea.services.utils.QueleaProperties;
import org.w3c.dom.Document;

/**
 * The bibles tab looks references up while they're typed, and has to find the
 * same passage as the universal search does for the same text.
 */
@DisplayName("BibleReferenceLookupTest")
public class BibleReferenceLookupTest {

    private static Bible bible;

    @BeforeAll
    public static void createBible() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-test").toString());
        StringBuilder xml = new StringBuilder("<XMLBIBLE biblename=\"Test\">");
        appendBook(xml, 1, "1 Mosebok", 3, 5);
        appendBook(xml, 5, "5 Mosebok", 34, 5);
        appendBook(xml, 6, "Josua", 24, 5);
        appendBook(xml, 18, "Job", 42, 5);
        appendBook(xml, 19, "Psaltaren", 150, 6);
        appendBook(xml, 29, "Joel", 3, 5);
        appendBook(xml, 32, "Jona", 4, 5);
        appendBook(xml, 43, "Johannes", 21, 30);
        appendBook(xml, 62, "1 Johannesbrevet", 5, 10);
        xml.append("</XMLBIBLE>");
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.toString().getBytes(StandardCharsets.UTF_8)));
        bible = Bible.parseXML(document.getDocumentElement(), "Test");
    }

    private static void appendBook(StringBuilder xml, int number, String name, int chapters, int verses) {
        xml.append("<BIBLEBOOK bnumber=\"").append(number).append("\" bname=\"").append(name).append("\">");
        for (int c = 1; c <= chapters; c++) {
            xml.append("<CHAPTER cnumber=\"").append(c).append("\">");
            for (int v = 1; v <= verses; v++) {
                xml.append("<VERS vnumber=\"").append(v).append("\">").append(name).append(' ')
                        .append(c).append(':').append(v).append("</VERS>");
            }
            xml.append("</CHAPTER>");
        }
        xml.append("</BIBLEBOOK>");
    }

    private static List<String> names(List<BibleBook> books) {
        return books.stream().map(BibleBook::getBookName).toList();
    }

    @Test
    @DisplayName("Finds the books that fit a book name being typed")
    public void findsBooksWhileTyping() {
        Lookup lookup = BibleReferenceParser.lookup("jo", bible);
        Assertions.assertEquals(List.of("Josua", "Job", "Joel", "Jona", "Johannes"), names(lookup.books()));
        Assertions.assertEquals("Josua", lookup.book().getBookName());
        Assertions.assertEquals("", lookup.passage());
        Assertions.assertNull(lookup.reference());

        Assertions.assertEquals(List.of("Johannes"), names(BibleReferenceParser.lookup("joh", bible).books()));
        Assertions.assertEquals(List.of("1 Johannesbrevet"), names(BibleReferenceParser.lookup("1 joh", bible).books()));
        Assertions.assertEquals(List.of("1 Johannesbrevet"), names(BibleReferenceParser.lookup("I Joh", bible).books()));
        Assertions.assertEquals(List.of("5 Mosebok"), names(BibleReferenceParser.lookup("5 mos", bible).books()));
        Assertions.assertEquals(List.of("Psaltaren"), names(BibleReferenceParser.lookup("Psaltaren ", bible).books()));
    }

    @Test
    @DisplayName("Finds nothing for text that isn't a book")
    public void findsNothing() {
        for (String query : new String[]{"", " ", "j", "1", "1 ", "xyz", "xyz 3:16", "3:16"}) {
            Assertions.assertSame(Lookup.NONE, BibleReferenceParser.lookup(query, bible), query);
        }
        Assertions.assertSame(Lookup.NONE, BibleReferenceParser.lookup("joh 3", null));
    }

    @Test
    @DisplayName("Finds the chapter while the verses are typed")
    public void findsChapter() {
        Lookup lookup = BibleReferenceParser.lookup("joh 3:", bible);
        Assertions.assertEquals("Johannes", lookup.book().getBookName());
        Assertions.assertEquals(3, lookup.chapter());
        Assertions.assertEquals("Johannes 3", lookup.reference().getLocation());

        lookup = BibleReferenceParser.lookup("joh 3:99", bible);
        Assertions.assertEquals(3, lookup.chapter());
        Assertions.assertNull(lookup.reference());

        lookup = BibleReferenceParser.lookup("joh 30", bible);
        Assertions.assertEquals("Johannes", lookup.book().getBookName());
        Assertions.assertEquals(0, lookup.chapter());
        Assertions.assertNull(lookup.reference());
    }

    @Test
    @DisplayName("Picks the book where the passage exists, as the universal search does")
    public void picksBookWithThePassage() {
        // Only Job and Psaltaren of the "j" books have a chapter 40
        Lookup lookup = BibleReferenceParser.lookup("jo 40:2", bible);
        Assertions.assertEquals("Job", lookup.book().getBookName());
        Assertions.assertEquals(40, lookup.chapter());
        Assertions.assertEquals("Job 40:2", lookup.reference().getLocation());
    }

    @Test
    @DisplayName("Finds the same passage as the universal search")
    public void agreesWithParse() {
        String[] queries = {"jo 3", "jo 3:16", "jo 40", "joh 3:16-18", "Joh 3 16", "Joh. 3.16", "joh 3:16,18",
            "joh 3:16-4:2", "joh 3-4", "ps 23", "psalt 150:6", "1 mos 1:1", "I Mos 2", "1Mos 3:1-", "1 joh 5:10",
            "joh 3:40", "jona 5", "joh 3:16 ", "  joh 3:16"};
        for (String query : queries) {
            List<Reference> parsed = BibleReferenceParser.parse(query, bible);
            Reference looked = BibleReferenceParser.lookup(query, bible).reference();
            if (parsed.isEmpty()) {
                Assertions.assertNull(looked, query);
            } else {
                Assertions.assertNotNull(looked, query);
                Assertions.assertEquals(parsed.getFirst().getLocation(), looked.getLocation(), query);
                Assertions.assertEquals(parsed.getFirst().verses(), looked.verses(), query);
            }
        }
    }

    @Test
    @DisplayName("Looks a passage up in a given book")
    public void resolvesPassage() {
        BibleBook john = bible.getBookByNumber(43);
        Assertions.assertEquals("Johannes 3:16-18", BibleReferenceParser.resolvePassage(john, "3:16-18").getLocation());
        Assertions.assertEquals("Johannes 3:16", BibleReferenceParser.resolvePassage(john, " 3 16 ").getLocation());
        Assertions.assertNull(BibleReferenceParser.resolvePassage(john, "30"));
        Assertions.assertNull(BibleReferenceParser.resolvePassage(john, ""));
        Assertions.assertNull(BibleReferenceParser.resolvePassage(null, "3"));
    }

    @Test
    @DisplayName("Finds every book of the bundled bibles from the name the bibles tab writes")
    public void findsBundledBooks() {
        File[] files = new File("bibles").listFiles((dir, name) -> name.endsWith(".xml") && !name.startsWith("."));
        Assertions.assertNotNull(files);
        Assertions.assertTrue(files.length > 0);
        Stream.of(files).map(Bible::parseBible).forEach(bundled -> {
            for (BibleBook book : bundled.getBooks()) {
                Lookup lookup = BibleReferenceParser.lookup(book.getBookName() + " ", bundled);
                Assertions.assertSame(book, lookup.book(), bundled.getBibleName() + ": " + book.getBookName());
                Lookup verse = BibleReferenceParser.lookup(book.getBookName() + " 1:1", bundled);
                Assertions.assertSame(book, verse.reference().book(), bundled.getBibleName() + ": " + book.getBookName());
            }
        });
    }
}
