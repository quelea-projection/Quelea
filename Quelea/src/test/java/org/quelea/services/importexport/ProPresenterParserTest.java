package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("ProPresenter import")
public class ProPresenterParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-propresenter-test").toString());
        }
    }

    @Test
    public void versionSixImportsTitleAuthorAndMultipleSlides() throws Exception {
        String xml = "<RVPresentationDocument CCLISongTitle=\"Grace &amp; Peace\" artist=\"Jane\">"
                + slide("Amazing grace") + slide("Second slide") + "</RVPresentationDocument>";
        Path file = Files.writeString(directory.resolve("song.pro6"), xml, StandardCharsets.UTF_8);
        List<SongDisplayable> songs = new ProPresenterParser().getSongs(file.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        SongDisplayable song = songs.get(0);
        Assertions.assertEquals("Grace & Peace", song.getTitle());
        Assertions.assertEquals("Jane", song.getAuthor());
        Assertions.assertEquals("Amazing grace\n\nSecond slide", song.getLyrics(false, false, false));
    }

    @Test
    public void unsupportedVersionIsIgnored() throws Exception {
        Path file = Files.writeString(directory.resolve("song.pro7"), "<RVPresentationDocument/>");
        Assertions.assertTrue(new ProPresenterParser().getSongs(file.toFile(), null).isEmpty());
    }

    @Test
    public void versionsFourAndFiveDecodeRtfLyrics() throws Exception {
        String rtf = "{\\rtf1\\ansi Amazing grace\\par}";
        String encoded = Base64.getEncoder().encodeToString(rtf.getBytes(StandardCharsets.UTF_8));
        String xml = "<RVPresentationDocument CCLISongTitle=\"Legacy\" CCLIAuthor=\"Writer\">"
                + "<RVDisplaySlide><RVTextElement RTFData=\"" + encoded + "\"/></RVDisplaySlide>"
                + "</RVPresentationDocument>";
        for (int version : new int[]{4, 5}) {
            Path file = Files.writeString(directory.resolve("legacy.pro" + version), xml, StandardCharsets.UTF_8);
            SongDisplayable song = new ProPresenterParser().getSongs(file.toFile(), null).get(0);
            Assertions.assertEquals("Legacy", song.getTitle());
            Assertions.assertEquals("Writer", song.getAuthor());
            Assertions.assertEquals("Amazing grace", song.getLyrics(false, false, false));
        }
    }

    @Test
    public void versionSixImportsUnicodeLyricsFromBase64() throws Exception {
        String xml = "<RVPresentationDocument CCLISongTitle=\"Song\">"
                + slide("Café — 日本語") + "</RVPresentationDocument>";
        Path file = Files.writeString(directory.resolve("unicode.pro6"), xml, StandardCharsets.UTF_8);
        SongDisplayable song = new ProPresenterParser().getSongs(file.toFile(), null).get(0);
        Assertions.assertEquals("Café — 日本語", song.getLyrics(false, false, false));
    }

    @Test
    public void versionSixPayloadUsesUtf8EvenWhenXmlUsesAnotherEncoding() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                + "<RVPresentationDocument CCLISongTitle=\"Café\">"
                + slide("日本語 & café") + "</RVPresentationDocument>";
        Path file = Files.writeString(directory.resolve("latin.pro6"), xml, StandardCharsets.ISO_8859_1);
        SongDisplayable song = new ProPresenterParser().getSongs(file.toFile(), null).get(0);
        Assertions.assertEquals("Café", song.getTitle());
        Assertions.assertEquals("日本語 & café", song.getLyrics(false, false, false));
    }

    @Test
    public void malformedSlideDoesNotPreventImportingOtherSlides() throws Exception {
        String xml = "<RVPresentationDocument CCLISongTitle=\"Song\">"
                + "<RVDisplaySlide><NSString rvXMLIvarName=\"PlainText\">%%%INVALID%%%</NSString></RVDisplaySlide>"
                + slide("Valid words") + "</RVPresentationDocument>";
        Path file = Files.writeString(directory.resolve("mixed.pro6"), xml, StandardCharsets.UTF_8);
        List<SongDisplayable> songs = new ProPresenterParser().getSongs(file.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("Valid words"));
    }

    private String slide(String text) {
        String encoded = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return "<RVDisplaySlide><NSString rvXMLIvarName=\"PlainText\">" + encoded + "</NSString></RVDisplaySlide>";
    }
}
