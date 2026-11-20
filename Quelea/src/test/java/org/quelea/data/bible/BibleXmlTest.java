package org.quelea.data.bible;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Bible XML")
public class BibleXmlTest {
    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-bible-xml-test").toString());
        }
    }

    @Test
    public void readsCommonVerseNumberAttributes() throws Exception {
        for (String attribute : new String[]{"vnumber", "number", "n", "id"}) {
            BibleVerse verse = BibleVerse.parseXML(element("<verse " + attribute + "=\"3\">  Grace &amp; peace\nforever </verse>"));
            Assertions.assertEquals(3, verse.getNum(), attribute);
            Assertions.assertEquals("Grace & peace forever", verse.getVerseText(), attribute);
        }
        Assertions.assertEquals(16, BibleVerse.parseXML(element("<verse osisID=\"John.3.16\">Text</verse>")).getNum());
        Assertions.assertNull(BibleVerse.parseXML(element("<verse vnumber=\"bad\">Text</verse>")));
    }

    @Test
    public void chapterXmlRoundTripKeepsVersesAndEscapedText() throws Exception {
        BibleChapter chapter = BibleChapter.parseXML(element("<chapter cnumber=\"2\"><verse vnumber=\"1\">"
                + "Grace &amp; peace</verse><verse vnumber=\"2\">Second</verse></chapter>"), -1);
        BibleChapter restored = BibleChapter.parseXML(element(chapter.toXML()), -1);
        Assertions.assertEquals(2, restored.getNum());
        Assertions.assertNotNull(restored.getVerse(1));
        Assertions.assertEquals("Grace & peace", restored.getVerse(1).getVerseText());
        Assertions.assertEquals("Second", restored.getVerse(2).getVerseText());
    }

    @Test
    public void wholeBibleXmlRoundTripKeepsBookChapterAndVerse() throws Exception {
        String xml = "<XMLBIBLE biblename=\"Test &amp; praise\"><BIBLEBOOK bnumber=\"1\" bname=\"Genesis\">"
                + "<CHAPTER cnumber=\"1\"><VERS vnumber=\"1\">Light &amp; life</VERS></CHAPTER>"
                + "</BIBLEBOOK></XMLBIBLE>";
        Bible bible = Bible.parseXML(element(xml), "Fallback");
        Bible restored = Bible.parseXML(element(bible.toXML()), "Fallback");
        Assertions.assertEquals("Test & praise", restored.getBibleName());
        Assertions.assertEquals("Genesis", restored.getBooks()[0].getBookName());
        Assertions.assertEquals("Light & life", restored.getBooks()[0].getChapter(0).getVerse(1).getVerseText());
    }

    @Test
    public void unnamedBookUsesProvidedFallbackName() throws Exception {
        BibleBook book = BibleBook.parseXML(element("<book><chapter cnumber=\"1\"><verse vnumber=\"1\">"
                + "In the beginning</verse></chapter></book>"), 1, "Genesis");
        Assertions.assertEquals("Genesis", book.getBookName());
        Assertions.assertEquals("Genesis", book.getBSName());
        Assertions.assertTrue(book.toXML().contains("bname=\"Genesis\""));
    }

    @Test
    public void missingBookNamesReceiveDistinctFallbacksWhenImportingABible() throws Exception {
        Bible bible = Bible.parseXML(element("<XMLBIBLE>\n"
                + "<book><chapter cnumber=\"1\"><verse vnumber=\"1\">First</verse></chapter></book>\n"
                + "<book><chapter cnumber=\"1\"><verse vnumber=\"1\">Second</verse></chapter></book>\n"
                + "</XMLBIBLE>"), "Fallback");
        Assertions.assertEquals("Book 1", bible.getBooks()[0].getBookName());
        Assertions.assertEquals("Book 2", bible.getBooks()[1].getBookName());
    }

    @Test
    public void chapterCaretIndexesPointToChapterHeadings() throws Exception {
        BibleBook book = BibleBook.parseXML(element("<book bname=\"Example\">"
                + "<chapter cnumber=\"1\"><verse vnumber=\"1\">First</verse></chapter>"
                + "<chapter cnumber=\"2\"><verse vnumber=\"1\">Second</verse></chapter></book>"), 1, "Example");
        String text = book.getFullText();
        Assertions.assertEquals(text.indexOf("Chapter 1"), book.getCaretIndexOfChapter(1));
        Assertions.assertEquals(text.indexOf("Chapter 2"), book.getCaretIndexOfChapter(2));
    }

    @Test
    public void chapterCaptionTakesPrecedenceOverFallbackBookName() throws Exception {
        BibleBook book = BibleBook.parseXML(element("<book><chapter cnumber=\"1\">"
                + "<caption>Chapter book name</caption><verse vnumber=\"1\">Words</verse>"
                + "</chapter></book>"), 1, "Fallback");
        Assertions.assertEquals("Chapter book name", book.getBookName());
    }

    @Test
    @Disabled("Sparse verses can be iterated and serialized in HashMap order")
    public void sparseVersesRemainInNumericalReadingOrder() throws Exception {
        // A chapter's verse map currently has unspecified HashMap iteration
        // order. The library and saved XML use that order directly, so sparse
        // or out-of-order verses can appear out of sequence. Return verses in
        // numerical order, and use that same order for text and serialization.
        BibleChapter chapter = BibleChapter.parseXML(element("<chapter cnumber=\"1\">"
                + "<verse vnumber=\"16\">Sixteenth</verse><verse vnumber=\"1\">First</verse>"
                + "</chapter>"), 1);
        Assertions.assertArrayEquals(new int[]{1, 16},
                java.util.Arrays.stream(chapter.getVerses()).mapToInt(BibleVerse::getNum).toArray());
        Assertions.assertTrue(chapter.getFullText().indexOf("First") < chapter.getFullText().indexOf("Sixteenth"));
        Assertions.assertTrue(chapter.toXML().indexOf("vnumber=\"1\"") < chapter.toXML().indexOf("vnumber=\"16\""));
    }

    private Element element(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    }
}
