package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("OpenLyrics import")
public class OpenLyricsParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-openlyrics-test").toString());
        }
    }

    @Test
    public void importsSongMetadataLyricsAndVerseOrder() throws Exception {
        String xml = "<song xmlns=\"http://openlyrics.info/namespace/2009/song\" version=\"0.9\">"
                + "<properties><titles><title>Grace &amp; peace</title></titles>"
                + "<authors><author>Jane</author><author>John</author></authors>"
                + "<ccliNo>123</ccliNo><copyright>Public domain</copyright>"
                + "<verseOrder>v1 c1 v1</verseOrder></properties>"
                + "<lyrics><verse name=\"v1\"><lines>Amazing grace<br/>How sweet</lines></verse>"
                + "<verse name=\"c1\"><lines>Sing with joy</lines></verse></lyrics></song>";
        Path path = Files.writeString(directory.resolve("song.xml"), xml, StandardCharsets.UTF_8);
        List<SongDisplayable> songs = new OpenLyricsParser().getSongs(path.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        SongDisplayable song = songs.get(0);
        Assertions.assertEquals("Grace & peace", song.getTitle());
        Assertions.assertEquals("Jane, John", song.getAuthor());
        Assertions.assertEquals("123", song.getCcli());
        Assertions.assertEquals("Public domain", song.getCopyright());
        Assertions.assertEquals("V1 C1 V1", song.getSequence());
        Assertions.assertTrue(song.getLyrics(false, false, false).contains("Amazing grace"));
        Assertions.assertTrue(song.getLyrics(false, false, false).contains("Sing with joy"));
        Assertions.assertTrue(Files.exists(Files.move(path, directory.resolve("renamed.xml"))));
    }

    @Test
    public void malformedXmlDoesNotProduceASong() throws Exception {
        Path path = Files.writeString(directory.resolve("broken.xml"), "<song>", StandardCharsets.UTF_8);
        Assertions.assertTrue(new OpenLyricsParser().getSongs(path.toFile(), null).isEmpty());
    }
}
