package org.quelea.data.displayable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.QueleaPropertyKeys;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bible passages shown in several versions")
public class BiblePassageVersionsTest {

    private static final String ALPHA = "Alpha";
    private static final String BETA = "Beta, the + second";
    private static final String NEW_TESTAMENT = "New Testament";
    private static final int GENESIS = 1;
    private static final int JOHN = 43;

    @TempDir
    static Path home;

    @BeforeAll
    static void createBibles() throws Exception {
        QueleaProperties.init(home.toString());
        Files.createDirectories(QueleaProperties.get().getBibleDir().toPath());
        writeBible("alpha.xml", ALPHA,
                book(GENESIS, "Genesis", 1, verse(1, "Alpha one."), verse(2, "Alpha two."), verse(3, "Alpha three.")),
                book(JOHN, "John", 3, verse(16, "Alpha sixteen."), verse(17, "Alpha seventeen."), verse(18, "Alpha eighteen."), verse(19, "Alpha nineteen.")));
        writeBible("beta.xml", BETA,
                book(GENESIS, "Första Mosebok", 1, verse(1, "Beta one."), verse(2, "Beta two.")),
                book(JOHN, "Johannes", 3, verse(16, "Beta sixteen."), verse(17, "Beta seventeen."), verse(18, "Beta eighteen."), verse(19, "Beta nineteen.")));
        writeBible("nt.xml", NEW_TESTAMENT,
                book(JOHN, "John", 3, verse(16, "Testament sixteen."), verse(17, "Testament seventeen.")));
        BibleManager.get().loadBibles(false);
    }

    @AfterEach
    void resetOptions() {
        QueleaProperties props = QueleaProperties.get();
        props.remove(QueleaPropertyKeys.maxBibleVersesKey);
        props.remove(QueleaPropertyKeys.useMaxBibleCharsKey);
        props.remove(QueleaPropertyKeys.splitBibleVersesKey);
        props.remove(QueleaPropertyKeys.maxCharsKey);
        props.remove(QueleaPropertyKeys.showVerseNumbersKey);
        props.remove(QueleaPropertyKeys.alwaysUseMultiBibleKey);
        props.remove(QueleaPropertyKeys.multiBibleVersionsKey);
    }

    @Test
    @DisplayName("Each version gets its own line on the slide")
    void versionsAreSeparateLines() {
        BiblePassage passage = john(16, 16).withVersions(bibles(ALPHA, BETA));
        assertArrayEquals(new String[]{"<sup>16</sup>Alpha sixteen.", "<sup>16</sup>Beta sixteen."}, slide(passage, 0));
    }

    @Test
    @DisplayName("Saving and opening a schedule keeps the versions apart")
    void roundTripKeepsVersions() throws Exception {
        BiblePassage passage = john(16, 17).withVersions(bibles(ALPHA, BETA));
        BiblePassage reloaded = reload(passage);
        assertTrue(reloaded.isParallel());
        assertEquals(passage.getSections().length, reloaded.getSections().length);
        for (int i = 0; i < passage.getSections().length; i++) {
            assertArrayEquals(slide(passage, i), slide(reloaded, i));
        }
        assertEquals(passage.getPreviewText(), reloaded.getPreviewText());
    }

    @Test
    @DisplayName("The chosen order survives a reload, whatever order bibles load in")
    void roundTripKeepsOrder() throws Exception {
        for (List<Bible> order : List.of(bibles(ALPHA, BETA), bibles(BETA, ALPHA))) {
            BiblePassage reloaded = reload(john(16, 16).withVersions(order));
            assertEquals(order, reloaded.getBibleVersions());
        }
    }

    @Test
    @DisplayName("Older Quelea versions still find a single bible in the schedule")
    void xmlNamesPrimaryBibleOnly() {
        String xml = john(16, 16).withVersions(bibles(ALPHA, BETA)).getXML();
        assertTrue(xml.contains("bible=\"" + ALPHA + "\""), xml);
    }

    @Test
    @DisplayName("A version that isn't installed keeps its text")
    void missingBibleKeepsText() throws Exception {
        String xml = john(16, 16).withVersions(bibles(ALPHA, BETA)).getXML();
        BiblePassage reloaded = parse(xml.replace("bible=\"" + ALPHA + "\"", "bible=\"Gone\"").replace("name=\"" + ALPHA + "\"", "name=\"Gone\""));
        assertEquals(1, reloaded.getVerses().length);
        assertArrayEquals(new String[]{"<sup>16</sup>Alpha sixteen.", "<sup>16</sup>Beta sixteen."}, slide(reloaded, 0));
        assertEquals(bibles(BETA), reloaded.getBibleVersions());
    }

    @Test
    @DisplayName("A passage in one version knows which bible it's in")
    void singlePassageReportsItsBible() {
        assertFalse(john(16, 16).isParallel());
        assertEquals(bibles(ALPHA), john(16, 16).getBibleVersions());
    }

    @Test
    @DisplayName("A passage in one version is drawn as before")
    void singlePassageIsUnchanged() {
        BiblePassage passage = john(16, 17);
        assertEquals(1, passage.getSections().length);
        assertEquals(1, slide(passage, 0).length);
    }

    @Test
    @DisplayName("Asking for no versions changes nothing rather than blanking the text")
    void noVersionsGivesNoPassage() {
        assertNull(john(16, 16).withVersions(new ArrayList<>()));
        assertNull(john(16, 16).withVersions(null));
    }

    @Test
    @DisplayName("Switching to one version leaves a plain passage in that bible")
    void switchToOneVersion() {
        BiblePassage passage = john(16, 16).withVersions(bibles(BETA));
        assertFalse(passage.isParallel());
        assertEquals(bibles(BETA), passage.getBibleVersions());
        assertEquals("Johannes 3:16-16\n" + BETA, passage.getPreviewText());
        assertArrayEquals(new String[]{"<sup>16</sup>Beta sixteen."}, slide(passage, 0));
    }

    @Test
    @DisplayName("A verse missing from one version is reported, and still shown in the other")
    void missingVerseIsReported() {
        BiblePassage passage = genesis(3, 3).withVersions(bibles(ALPHA, BETA));
        assertArrayEquals(new String[]{"<sup>3</sup>Alpha three."}, slide(passage, 0));
        assertEquals(List.of(BETA + ": 1:3"), passage.getMissingVerses());
    }

    @Test
    @DisplayName("A verse missing from the first version is shown from the second")
    void missingFromFirstVersion() throws Exception {
        BiblePassage passage = genesis(3, 3).withVersions(bibles(BETA, ALPHA));
        assertEquals(List.of(BETA + ": 1:3"), passage.getMissingVerses());
        assertArrayEquals(new String[]{"<sup>3</sup>Alpha three."}, slide(reload(passage), 0));
    }

    @Test
    @DisplayName("Switching to a bible without the verse drops and reports it")
    void singleVersionDropsMissingVerse() {
        BiblePassage passage = genesis(2, 3).withVersions(bibles(BETA));
        assertEquals(1, passage.getVerses().length);
        assertEquals(List.of(BETA + ": 1:3"), passage.getMissingVerses());
    }

    @Test
    @DisplayName("Books are matched by number, not by position in the bible")
    void booksMatchedByNumber() {
        assertNull(genesis(1, 1).withVersions(bibles(NEW_TESTAMENT)));
        BiblePassage passage = genesis(1, 1).withVersions(bibles(ALPHA, NEW_TESTAMENT));
        assertArrayEquals(new String[]{"<sup>1</sup>Alpha one."}, slide(passage, 0));
        assertArrayEquals(new String[]{"<sup>16</sup>Alpha sixteen.", "<sup>16</sup>Testament sixteen."},
                slide(john(16, 16).withVersions(bibles(ALPHA, NEW_TESTAMENT)), 0));
    }

    @Test
    @DisplayName("A bible name with a plus sign doesn't lose verses on reload")
    void plusInBibleName() throws Exception {
        BiblePassage passage = john(16, 16).withVersions(bibles(BETA));
        assertEquals(1, reload(passage).getVerses().length);
    }

    @Test
    @DisplayName("The whole location is kept, including anything after a space")
    void locationWithSpaces() {
        BibleVerse[] verses = {verse(ALPHA, JOHN, 3, 16), verse(ALPHA, JOHN, 3, 18)};
        BiblePassage passage = new BiblePassage(ALPHA, "John 3:16, 18", verses, false).withVersions(bibles(BETA, ALPHA));
        assertEquals("Johannes 3:16, 18\n" + BETA + " + " + ALPHA, passage.getPreviewText());
    }

    @Test
    @DisplayName("The verses per slide are shared between the versions")
    void maxVersesIsShared() {
        QueleaProperties.get().setProperty(QueleaPropertyKeys.maxBibleVersesKey, "4");
        BiblePassage passage = john(16, 19).withVersions(bibles(ALPHA, BETA));
        assertEquals(2, passage.getSections().length);
        assertArrayEquals(new String[]{"<sup>16</sup>Alpha sixteen. <sup>17</sup>Alpha seventeen.",
                "<sup>16</sup>Beta sixteen. <sup>17</sup>Beta seventeen."}, slide(passage, 0));
    }

    @Test
    @DisplayName("At least one verse is shown per slide")
    void atLeastOneVersePerSlide() {
        QueleaProperties.get().setProperty(QueleaPropertyKeys.maxBibleVersesKey, "1");
        assertEquals(4, john(16, 19).withVersions(bibles(ALPHA, BETA)).getSections().length);
    }

    @Test
    @DisplayName("A long verse is split across slides when the options allow it")
    void longVerseIsSplit() {
        QueleaProperties props = QueleaProperties.get();
        props.setProperty(QueleaPropertyKeys.useMaxBibleCharsKey, "false");
        props.setProperty(QueleaPropertyKeys.maxCharsKey, "8");
        props.setProperty(QueleaPropertyKeys.showVerseNumbersKey, "false");
        BiblePassage passage = john(16, 16).withVersions(bibles(ALPHA, BETA));
        assertTrue(passage.getSections().length > 1);
        StringBuilder alpha = new StringBuilder();
        StringBuilder beta = new StringBuilder();
        for (int i = 0; i < passage.getSections().length; i++) {
            String[] lines = slide(passage, i);
            alpha.append(lines[0]).append(' ');
            if (lines.length > 1) {
                beta.append(lines[1]).append(' ');
            }
        }
        assertEquals("Alpha sixteen.", alpha.toString().trim());
        assertEquals("Beta sixteen.", beta.toString().trim());
    }

    @Test
    @DisplayName("A long verse stays whole when the options say so")
    void longVerseKeptWhole() {
        QueleaProperties props = QueleaProperties.get();
        props.setProperty(QueleaPropertyKeys.useMaxBibleCharsKey, "false");
        props.setProperty(QueleaPropertyKeys.splitBibleVersesKey, "true");
        props.setProperty(QueleaPropertyKeys.maxCharsKey, "8");
        assertEquals(1, john(16, 16).withVersions(bibles(ALPHA, BETA)).getSections().length);
    }

    @Test
    @DisplayName("New passages use the versions set in the options")
    void defaultVersions() {
        BibleManager manager = BibleManager.get();
        Bible alpha = manager.getBibleFromName(ALPHA);
        assertEquals(bibles(ALPHA), manager.getDefaultVersions(alpha));
        QueleaProperties.get().setProperty(QueleaPropertyKeys.alwaysUseMultiBibleKey, "true");
        QueleaProperties.get().setProperty(QueleaPropertyKeys.multiBibleVersionsKey, BETA + "\nGone\n" + ALPHA);
        assertEquals(bibles(ALPHA, BETA), manager.getDefaultVersions(alpha));
        assertEquals(bibles(BETA, ALPHA), manager.getDefaultVersions(manager.getBibleFromName(BETA)));
    }

    private static BiblePassage john(int from, int to) {
        return passage(JOHN, 3, "John 3", from, to);
    }

    private static BiblePassage genesis(int from, int to) {
        return passage(GENESIS, 1, "Genesis 1", from, to);
    }

    private static BiblePassage passage(int book, int chapter, String chapterName, int from, int to) {
        List<BibleVerse> verses = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            verses.add(verse(ALPHA, book, chapter, i));
        }
        return new BiblePassage(ALPHA, chapterName + ":" + from + "-" + to, verses.toArray(new BibleVerse[0]), false);
    }

    private static BibleVerse verse(String bible, int book, int chapter, int verse) {
        BibleVerse ret = BibleManager.get().getBibleFromName(bible).getBookByNumber(book).getChapter(chapter - 1).getVerse(verse);
        assertNotNull(ret);
        return ret;
    }

    private static List<Bible> bibles(String... names) {
        List<Bible> ret = new ArrayList<>();
        for (String name : names) {
            ret.add(BibleManager.get().getBibleFromName(name));
        }
        return ret;
    }

    private static String[] slide(BiblePassage passage, int index) {
        return passage.getSections()[index].getText(false, false);
    }

    private static BiblePassage reload(BiblePassage passage) throws Exception {
        return parse(passage.getXML());
    }

    private static BiblePassage parse(String xml) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return BiblePassage.parseXML(document.getDocumentElement());
    }

    private static void writeBible(String file, String name, String... books) throws Exception {
        String xml = "<XMLBIBLE biblename=\"" + name + "\">" + String.join("", books) + "</XMLBIBLE>";
        Files.writeString(QueleaProperties.get().getBibleDir().toPath().resolve(file), xml);
    }

    private static String book(int number, String name, int chapter, String... verses) {
        StringBuilder chapters = new StringBuilder();
        for (int i = 1; i < chapter; i++) {
            chapters.append("<CHAPTER cnumber=\"").append(i).append("\"><VERS vnumber=\"1\">Filler.</VERS></CHAPTER>");
        }
        return "<BIBLEBOOK bnumber=\"" + number + "\" bname=\"" + name + "\">" + chapters
                + "<CHAPTER cnumber=\"" + chapter + "\">" + String.join("", verses) + "</CHAPTER></BIBLEBOOK>";
    }

    private static String verse(int number, String text) {
        return "<VERS vnumber=\"" + number + "\">" + text + "</VERS>";
    }
}
