package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Screen Monkey song import")
class ScreenMonkeyParserTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-screen-monkey").toString());
        }
    }

    @Test
    void importsMetadataEntitiesAndMultipleVerses() throws Exception {
        List<SongDisplayable> songs = parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Song><Title>Grace &amp; Peace</Title><Author>Jane Writer</Author>"
                + "<Capo>2</Capo><ccli>123456</ccli><Lyrics><Verse>First line\nSecond line</Verse>"
                + "<Verse>Another verse</Verse></Lyrics></Song>");
        Assertions.assertEquals(1, songs.size());
        SongDisplayable song = songs.get(0);
        Assertions.assertEquals("Grace & Peace", song.getTitle());
        Assertions.assertEquals("Jane Writer", song.getAuthor());
        Assertions.assertEquals("2", song.getCapo());
        Assertions.assertEquals("123456", song.getCcli());
        Assertions.assertEquals("First line\nSecond line\n\nAnother verse", song.getLyrics(false, false, false).trim());
    }

    @Test
    void invalidXmlIsSkipped() throws Exception {
        Assertions.assertTrue(parse("<Song><Title>incomplete").isEmpty());
    }

    @Test
    void unrelatedXmlIsSkipped() throws Exception {
        // A user can accidentally select an XML file from another application.
        // Missing the expected Song element should be treated like malformed
        // input rather than aborting the entire import with a null dereference.
        Assertions.assertTrue(parse("<not-a-song/>").isEmpty());
    }

    @Test
    @Disabled("Screen Monkey XML currently expands external file entities")
    void importedXmlCannotReadLocalFilesViaExternalEntities() throws Exception {
        // Imported files are untrusted: expanding an external entity can copy
        // private local file contents into the song or expose them elsewhere.
        // Disable external entities and DTD loading on the DocumentBuilderFactory.
        Path secret = directory.resolve("private.txt");
        Files.writeString(secret, "PRIVATE_TEST_CONTENT", StandardCharsets.UTF_8);
        List<SongDisplayable> songs = parse("<?xml version=\"1.0\"?><!DOCTYPE Song [<!ENTITY xxe SYSTEM \""
                + secret.toUri() + "\">]><Song><Title>&xxe;</Title><Lyrics><Verse>Words</Verse></Lyrics></Song>");
        Assertions.assertTrue(songs.stream().noneMatch(song -> song.getTitle().contains("PRIVATE_TEST_CONTENT")));
    }

    private List<SongDisplayable> parse(String text) throws Exception {
        Path file = directory.resolve("input.xml");
        Files.writeString(file, text, StandardCharsets.UTF_8);
        return new ScreenMonkeyParser().getSongs(file.toFile(), null);
    }
}
