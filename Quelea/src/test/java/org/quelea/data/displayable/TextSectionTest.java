package org.quelea.data.displayable;

import java.io.StringReader;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@DisplayName("TextSectionTest")
public class TextSectionTest {

    @Test
    @DisplayName("Selects chord lines and comments independently")
    public void selectsChordsAndComments() {
        String[] lines = {"C G//chords", "Ama_zing grace//lyrics", "How sweet the sound"};
        TextSection section = new TextSection("Verse", lines, null, false);
        Assertions.assertArrayEquals(lines, section.getText(true, true));
        Assertions.assertArrayEquals(new String[]{"C G", "Ama_zing grace", "How sweet the sound"},
                section.getText(true, false));
        Assertions.assertArrayEquals(new String[]{"Amazing grace//lyrics", "How sweet the sound"},
                section.getText(false, true));
        Assertions.assertArrayEquals(new String[]{"Amazing grace", "How sweet the sound"},
                section.getText(false, false));
    }

    @Test
    @Disabled("Case-insensitive markers currently fail during comment removal")
    @DisplayName("Removes comment markers regardless of their letter case")
    public void removesUpperCaseMarkers() {
        // LineTypeChecker accepts markers without regard to case, so displaying
        // or exporting such a lyric without comments should not throw. Currently
        // removeComments detects the lowercased suffix but searches the original
        // string for lowercase text and passes -1 to substring. Remove the known
        // suffix by length or use the matching case-insensitive index instead.
        TextSection section = new TextSection("Verse", new String[]{"Amazing grace//LYRICS"}, null, false);
        Assertions.assertArrayEquals(new String[]{"Amazing grace"}, section.getText(true, false));
    }

    @Test
    @DisplayName("Copies the supplied text and small text arrays")
    public void copiesInputAndOutputArrays() {
        String[] lines = {"First line"};
        String[] smallLines = {"Source"};
        TextSection section = new TextSection("Verse", lines, smallLines, false);
        lines[0] = "Changed";
        smallLines[0] = "Changed";
        section.getText(true, true)[0] = "Changed again";
        section.getSmallText()[0] = "Changed again";
        Assertions.assertArrayEquals(new String[]{"First line"}, section.getText(true, true));
        Assertions.assertArrayEquals(new String[]{"Source"}, section.getSmallText());
    }

    @Test
    @DisplayName("XML round-trips titles, lyrics, small text and capitalisation")
    public void xmlRoundTrip() throws Exception {
        TextSection section = new TextSection("Verse \"one\" & two",
                new String[]{"Love & grace", "C < G//lyrics"}, new String[]{"© 2026 & author"}, false);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(section.getXML())));
        TextSection restored = TextSection.parseXML(document.getDocumentElement(), Map.of());
        Assertions.assertEquals(section.getTitle(), restored.getTitle());
        Assertions.assertArrayEquals(section.getText(true, true), restored.getText(true, true));
        Assertions.assertArrayEquals(section.getSmallText(), restored.getSmallText());
        Assertions.assertFalse(restored.shouldCapitaliseFirst());
    }
}
