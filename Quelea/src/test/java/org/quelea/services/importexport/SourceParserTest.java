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

@DisplayName("Source songbook HTML import")
public class SourceParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-source-test").toString());
        }
    }

    @Test
    public void readsLyricsTitleAndAuthor() throws Exception {
        write("song.html", "<!-- InstanceBeginEditable name=\"content\" -->"
                + "<p>Amazing grace,<br>How sweet the sound</p><!-- InstanceEndEditable -->"
                + "<!-- InstanceBeginEditable name=\"Author\" -->John Newton<!-- InstanceEndEditable -->");
        List<SongDisplayable> songs = new SourceParser().getSongs(directory.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Amazing grace", songs.get(0).getTitle());
        Assertions.assertEquals("John Newton", songs.get(0).getAuthor());
        Assertions.assertEquals("Amazing grace,\nHow sweet the sound", songs.get(0).getLyrics(false, false, false));
    }

    @Test
    public void ignoresHtmlFilesWithoutTheSongMarkers() throws Exception {
        write("readme.html", "<html><body>Not a song</body></html>");
        Assertions.assertTrue(new SourceParser().getSongs(directory.toFile(), null).isEmpty());
    }

    @Test
    public void missingAuthorMarkerLeavesAuthorEmpty() throws Exception {
        write("song.html", "<!-- InstanceBeginEditable name=\"content\" -->"
                + "<p>Amazing grace<br>How sweet the sound</p><!-- InstanceEndEditable -->");
        List<SongDisplayable> songs = new SourceParser().getSongs(directory.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("", songs.get(0).getAuthor());
    }

    @Test
    public void ignoresUnterminatedLyricsSection() throws Exception {
        write("broken.html", "<!-- InstanceBeginEditable name=\"content\" --><p>Missing end marker</p>");
        Assertions.assertTrue(new SourceParser().getSongs(directory.toFile(), null).isEmpty());
    }

    private void write(String name, String text) throws Exception {
        Files.writeString(directory.resolve(name), text, StandardCharsets.UTF_8);
    }
}
