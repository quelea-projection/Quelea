package org.quelea.data.bible;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.BibleReferenceParser.Reference;
import org.quelea.services.utils.QueleaProperties;
import org.w3c.dom.Document;

@DisplayName("BibleReferenceParserTest")
public class BibleReferenceParserTest {

    private static Bible bible;

    @BeforeAll
    public static void createBible() throws Exception {
        // The logging needs the properties, which shouldn't be the real ones of whoever runs this
        QueleaProperties.init(Files.createTempDirectory("quelea-test").toString());
        StringBuilder xml = new StringBuilder("<XMLBIBLE biblename=\"Test\">");
        appendBook(xml, 1, "1 Mosebok", 3, 5);
        appendBook(xml, 2, "2 Mosebok", 3, 5);
        appendBook(xml, 3, "Josua", 4, 5);
        appendBook(xml, 4, "Job", 4, 5);
        appendBook(xml, 5, "Psaltaren", 25, 6);
        appendBook(xml, 6, "Höga Visan", 2, 5);
        appendBook(xml, 7, "Johannes", 4, 30);
        appendBook(xml, 8, "Apostlagärningarna", 2, 5);
        appendBook(xml, 9, "1 Korinthierbrevet", 16, 20);
        appendBook(xml, 10, "2 Korinthierbrevet", 13, 20);
        appendBook(xml, 11, "1 Johannesbrevet", 5, 10);
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

    @Test
    @DisplayName("Recognises the ways a reference is written")
    public void recognisesReferences() {
        assertLocation("Johannes 3:16", "Johannes 3:16");
        assertLocation("joh 3:16", "Johannes 3:16");
        assertLocation("JOH 3:16", "Johannes 3:16");
        assertLocation("Joh 3 16", "Johannes 3:16");
        assertLocation("Joh 3.16", "Johannes 3:16");
        assertLocation("Joh 3,16", "Johannes 3:16");
        assertLocation("Joh. 3:16", "Johannes 3:16");
        assertLocation("Joh 3 : 16 - 18", "Johannes 3:16-18");
        assertLocation("Joh 3:16–18", "Johannes 3:16-18");
        assertLocation("Joh 3:16-", "Johannes 3:16");
        assertLocation("Joh 3:", "Johannes 3");
        assertLocation("Ps 23", "Psaltaren 23");
        assertLocation("Psalt 23:1", "Psaltaren 23:1");
        assertLocation("1 Mos 3:1", "1 Mosebok 3:1");
        assertLocation("1Mos 3:1", "1 Mosebok 3:1");
        assertLocation("1. Mos 3:1", "1 Mosebok 3:1");
        assertLocation("I Mos 3:1", "1 Mosebok 3:1");
        assertLocation("II Mos 3:1", "2 Mosebok 3:1");
        assertLocation("Andra Mos 3:1", "2 Mosebok 3:1");
        assertLocation("Första Kor 13:4-7", "1 Korinthierbrevet 13:4-7");
        assertLocation("1 Kor 13", "1 Korinthierbrevet 13");
        assertLocation("Höga Visan 2:1", "Höga Visan 2:1");
        assertLocation("Hoga 2:1", "Höga Visan 2:1");
        assertLocation("Apg 2:1", "Apostlagärningarna 2:1");
    }

    private static void assertLocation(String query, String location) {
        Assertions.assertEquals(List.of(location), locations(query), query);
    }

    @Test
    @DisplayName("Leaves everything else alone")
    public void ignoresOtherText() {
        for (String query : List.of(
                "", "Johannes", "Amazing Grace", "Amazing Grace 2", "10 000 reasons", "23", "3:16",
                "Joh 99", "Joh 3:99", "Joh 3:18-16", "Joh 3:16 hej", "Ps 23,24", "J 3", "Oh 3")) {
            Assertions.assertEquals(List.of(), BibleReferenceParser.parse(query, bible), query);
        }
    }

    @Test
    @DisplayName("Finds no references without a bible")
    public void handlesNoBible() {
        Assertions.assertEquals(List.of(), BibleReferenceParser.parse("Joh 3:16", null));
    }

    @Test
    @DisplayName("Offers every book a query fits, in bible order")
    public void offersAllBooksThatFit() {
        Assertions.assertEquals(List.of("Josua 3", "Job 3", "Johannes 3"), locations("Jo 3"));
    }

    @Test
    @DisplayName("Skips a book where the passage doesn't exist")
    public void skipsBooksWithoutThePassage() {
        Assertions.assertEquals(List.of("Johannes 3:30"), locations("Jo 3:30"));
    }

    @Test
    @DisplayName("Prefers a book that starts with the query over a numbered one")
    public void prefersPlainBookOverNumberedBook() {
        Assertions.assertEquals(List.of("Johannes 3"), locations("Joh 3"));
    }

    @Test
    @DisplayName("Finds numbered books without the number")
    public void findsNumberedBooksWithoutTheNumber() {
        Assertions.assertEquals(List.of("1 Korinthierbrevet 13", "2 Korinthierbrevet 13"), locations("Kor 13"));
        Assertions.assertEquals(List.of("1 Korinthierbrevet 16"), locations("Kor 16"));
    }

    @Test
    @DisplayName("Finds the right verses")
    public void findsVerses() {
        Assertions.assertEquals(List.of("3:16"), verses("Joh 3:16"));
        Assertions.assertEquals(List.of("3:16", "3:17", "3:18"), verses("Joh 3:16-18"));
        Assertions.assertEquals(List.of("3:29", "3:30"), verses("Joh 3:29-99"));
        Assertions.assertEquals(List.of("3:16", "3:18", "3:20", "3:21"), verses("Joh 3:16,18,20-21"));
        Assertions.assertEquals(List.of("3:1", "4:2"), verses("Joh 3:1;4:2"));
        Assertions.assertEquals(List.of("1:4", "1:5", "2:1", "2:2"), verses("Apg 1:4-2:2"));
        Assertions.assertEquals(30, verses("Joh 3").size());
        Assertions.assertEquals(60, verses("Joh 3-4").size());
    }

    @Test
    @DisplayName("Shows chapters in the verse numbers when a passage has several parts")
    public void marksPassagesWithSeveralParts() {
        Assertions.assertFalse(reference("Joh 3:16-18").multi());
        Assertions.assertTrue(reference("Joh 3-4").multi());
        Assertions.assertTrue(reference("Joh 3:30-4:2").multi());
        Assertions.assertTrue(reference("Joh 3:16,18").multi());
    }

    private static Reference reference(String query) {
        return BibleReferenceParser.parse(query, bible).getFirst();
    }

    private static List<String> locations(String query) {
        return BibleReferenceParser.parse(query, bible).stream().map(Reference::getLocation).toList();
    }

    private static List<String> verses(String query) {
        return reference(query).verses().stream()
                .map(verse -> verse.getChapter().getNum() + ":" + verse.getNum())
                .toList();
    }
}
