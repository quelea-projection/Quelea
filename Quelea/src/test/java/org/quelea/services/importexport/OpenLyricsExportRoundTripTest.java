package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("OpenLyrics export and import")
public class OpenLyricsExportRoundTripTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-openlyrics-roundtrip-test").toString());
        }
    }

    @Test
    public void exportedXmlCanBeReimportedWithMetadataAndLyrics() throws Exception {
        SongDisplayable original = new SongDisplayable("Grace & peace", "Jane <Writer>");
        original.setCcli("12345");
        original.setLyrics("Verse 1\nAmazing grace\nHow sweet\n\nChorus\nSing with joy");
        var getXml = OpenLyricsExporter.class.getDeclaredMethod("getXML", SongDisplayable.class);
        getXml.setAccessible(true);
        String xml = (String) getXml.invoke(null, original);
        Path file = Files.writeString(directory.resolve("song.xml"), xml, StandardCharsets.UTF_8);
        var songs = new OpenLyricsParser().getSongs(file.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        SongDisplayable restored = songs.get(0);
        Assertions.assertEquals(original.getTitle(), restored.getTitle());
        Assertions.assertEquals(original.getAuthor(), restored.getAuthor());
        Assertions.assertEquals(original.getCcli(), restored.getCcli());
        String lyrics = restored.getLyrics(false, false, false);
        Assertions.assertTrue(lyrics.contains("Amazing grace\nHow sweet"));
        Assertions.assertTrue(lyrics.contains("Sing with joy"));
    }
}
