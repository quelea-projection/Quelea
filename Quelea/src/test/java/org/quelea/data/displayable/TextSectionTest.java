package org.quelea.data.displayable;

import java.io.StringReader;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
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
    @DisplayName("Removes comment markers regardless of their letter case")
    public void removesUpperCaseMarkers() {
        String[] lines = {"C G//CHORDS", "Ama_zing grace//LyRiCs", "A heading//TITLE"};
        TextSection section = new TextSection("Verse", lines, null, false);
        Assertions.assertArrayEquals(new String[]{"C G", "Ama_zing grace", "A heading"}, section.getText(true, false));
        Assertions.assertArrayEquals(new String[]{"Amazing grace", "A heading"}, section.getText(false, false));
        Assertions.assertArrayEquals(lines, section.getText(true, true));
    }

    @Test
    @DisplayName("Removes only a trailing marker, preserving earlier marker text")
    public void removesOnlyTheTrailingMarker() {
        TextSection section = new TextSection("Verse", new String[]{
            "Keep //lyrics here//LYRICS  ", "Keep //chords here//ChOrDs", "Keep //title here//title"
        }, null, false);
        Assertions.assertArrayEquals(new String[]{"Keep //lyrics here", "Keep //chords here", "Keep //title here"},
                section.getText(true, false));
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
