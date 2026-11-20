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

@DisplayName("Presentation Manager song import")
class PresentationManagerParserTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-presentation-manager").toString());
        }
    }

    @Test
    void readsUtf16MetadataVersesAndMissingClosingSongTag() throws Exception {
        List<SongDisplayable> songs = parse("Presentation Manager\n<?xml version=\"1.0\"?>\n"
                + "<song><attributes><Title>Gr\u00e2ce &amp; Peace</Title><Author>Jean</Author></attributes>"
                + "<verses><verse>First line\nSecond line</verse><verse>Another verse</verse></verses>\n");
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Gr\u00e2ce & Peace", songs.get(0).getTitle());
        Assertions.assertEquals("Jean", songs.get(0).getAuthor());
        Assertions.assertEquals("First line\nSecond line\n\nAnother verse", songs.get(0).getLyrics(false, false, false));
    }

    @Test
    void invalidXmlIsSkipped() throws Exception {
        Assertions.assertTrue(parse("header\n<song><attributes>").isEmpty());
    }

    @Test
    @Disabled("Presentation Manager XML currently expands external file entities")
    void importedXmlCannotReadLocalFilesViaExternalEntities() throws Exception {
        // Imported XML must not be able to load arbitrary local files. Disable
        // external entity and DTD loading in the parser's DocumentBuilderFactory.
        Path secret = Files.writeString(directory.resolve("private.txt"), "PRIVATE_TEST_CONTENT");
        List<SongDisplayable> songs = parse("header\n<!DOCTYPE song [<!ENTITY data SYSTEM \""
                + secret.toUri() + "\">]>\n<song><attributes><Title>&data;</Title></attributes>"
                + "<verses><verse>Words</verse></verses></song>");
        Assertions.assertTrue(songs.stream().noneMatch(song -> song.getTitle().contains("PRIVATE_TEST_CONTENT")));
    }

    private List<SongDisplayable> parse(String text) throws Exception {
        Path file = directory.resolve("input.xml");
        Files.writeString(file, text, StandardCharsets.UTF_16LE);
        return new PresentationManagerParser().getSongs(file.toFile(), null);
    }
}
