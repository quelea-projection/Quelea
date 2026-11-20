package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("EpicWorship song import")
class EpicWorshipParserTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-epic-import").toString());
        }
    }

    @Test
    void importsMultipleSongsAndSections() throws Exception {
        List<SongDisplayable> songs = parse("{\"songs\":[{\"name\":\"Grace\",\"author\":\"Jane\","
                + "\"content\":\"First line\\nSecond line\\n\\nChorus line\"},"
                + "{\"name\":\"Peace\",\"author\":\"John\",\"content\":\"Another song\"}]}");
        Assertions.assertEquals(2, songs.size());
        Assertions.assertEquals("Grace", songs.get(0).getTitle());
        Assertions.assertEquals("Jane", songs.get(0).getAuthor());
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("First line\nSecond line"));
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("Chorus line"));
        Assertions.assertEquals("Peace", songs.get(1).getTitle());
        Assertions.assertEquals("John", songs.get(1).getAuthor());
    }

    @Test
    void anEmptySongPackContainsNoSongs() throws Exception {
        Assertions.assertTrue(parse("{\"songs\":[]}").isEmpty());
    }

    @Test
    void readsValidIndentedJsonWithEscapedQuotes() throws Exception {
        List<SongDisplayable> songs = parse("{\n  \"songs\": [\n"
                + "    {\"name\": \"Say \\\"hello\\\"\", \"author\": \"Jane\", "
                + "\"content\": \"Sing\\nAgain\"}\n  ]\n}");
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Say \"hello\"", songs.get(0).getTitle());
        Assertions.assertEquals("Jane", songs.get(0).getAuthor());
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("Sing\nAgain"));
    }

    @Test
    void readsJsonEscapesCrLfAndDelimitersInsideText() throws Exception {
        List<SongDisplayable> songs = parse("{\"songs\":[{\"content\":\"Verse one\\r\\n{words}, {more}\\r\\n\\r\\nChorus\\nLine\","
                + "\"name\":\"Caf\\u00e9 : \\\"yes\\\"\",\"author\":\"A \\\\ B\"}]}");
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Caf\u00e9 : \"yes\"", songs.get(0).getTitle());
        Assertions.assertEquals("A \\ B", songs.get(0).getAuthor());
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("Verse one\n{words}, {more}"));
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("Chorus\nLine"));
    }

    @Test
    void malformedOrUnexpectedSongPacksReportIoErrors() {
        for (String json : List.of("", "{\"songs\":[", "{}", "{\"songs\":null}", "{\"songs\":[1]}")) {
            Assertions.assertThrows(IOException.class, () -> parse(json), json);
        }
    }

    private List<SongDisplayable> parse(String json) throws Exception {
        Path file = Files.writeString(directory.resolve("songs.json"), json, StandardCharsets.UTF_8);
        return new EpicWorshipParser().getSongs(file.toFile(), null);
    }
}
