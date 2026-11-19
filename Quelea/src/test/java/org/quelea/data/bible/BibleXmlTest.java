package org.quelea.data.bible;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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

    private Element element(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    }
}
