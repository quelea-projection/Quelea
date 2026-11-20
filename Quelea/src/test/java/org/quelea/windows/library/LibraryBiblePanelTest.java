package org.quelea.windows.library;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.Bible;
import org.quelea.services.utils.QueleaProperties;
import org.w3c.dom.Document;

@DisplayName("LibraryBiblePanelTest")
public class LibraryBiblePanelTest {

    private static Bible bible;

    @BeforeAll
    public static void createBible() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-test").toString());
        String xml = "<XMLBIBLE biblename=\"Test\">"
                + book(1, "Genesis", "Gen")
                + book(9, "1 Samuel", null)
                + book(19, "Psaltaren", null)
                + book(22, "Höga Visan", null)
                + book(43, "Joh", null)
                + book(46, "1. Korinthierbrevet", null)
                + book(67, "Tobit", null)
                + "</XMLBIBLE>";
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        bible = Bible.parseXML(document.getDocumentElement(), "Test");
    }

    private static String book(int number, String name, String shortName) {
        return "<BIBLEBOOK bnumber=\"" + number + "\" bname=\"" + name + "\""
                + (shortName == null ? "" : " bsname=\"" + shortName + "\"")
                + "><CHAPTER cnumber=\"1\"><VERS vnumber=\"1\">Text</VERS></CHAPTER></BIBLEBOOK>";
    }

    @Test
    @DisplayName("Shortens book names so they fit in the grid")
    public void shortensBookNames() {
        Assertions.assertEquals("Gen", LibraryBiblePanel.getShortName(bible.getBookByNumber(1)));
        Assertions.assertEquals("1 Sam", LibraryBiblePanel.getShortName(bible.getBookByNumber(9)));
        Assertions.assertEquals("Psal", LibraryBiblePanel.getShortName(bible.getBookByNumber(19)));
        Assertions.assertEquals("Höga", LibraryBiblePanel.getShortName(bible.getBookByNumber(22)));
        Assertions.assertEquals("Joh", LibraryBiblePanel.getShortName(bible.getBookByNumber(43)));
        Assertions.assertEquals("1. Kor", LibraryBiblePanel.getShortName(bible.getBookByNumber(46)));
    }

    @Test
    @DisplayName("Puts books in the right testament")
    public void groupsBooksByTestament() {
        Assertions.assertEquals(0, LibraryBiblePanel.getSection(bible.getBookByNumber(22)));
        Assertions.assertEquals(1, LibraryBiblePanel.getSection(bible.getBookByNumber(43)));
        Assertions.assertEquals(2, LibraryBiblePanel.getSection(bible.getBookByNumber(67)));
    }
}
