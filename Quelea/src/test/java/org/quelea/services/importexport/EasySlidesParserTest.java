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

@DisplayName("EasySlides import")
public class EasySlidesParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-easyslides-test").toString());
        }
    }

    @Test
    public void importsMultipleSongsAndTheirMetadata() throws Exception {
        String xml = "<songs><song><title1>Grace &amp; peace</title1><writer>Jane</writer><capo>2</capo>"
                + "<copyright>Public domain</copyright><contents>[Verse 1] First line\nSecond line[Chorus] Sing</contents></song>"
                + "<song><title1>Another</title1><capo>-1</capo><contents>Words</contents></song>"
                + "<song><contents>Missing title</contents></song></songs>";
        Path input = Files.writeString(directory.resolve("songs.xml"), xml, StandardCharsets.UTF_8);
        List<SongDisplayable> songs = new EasySlidesParser().getSongs(input.toFile(), null);
        Assertions.assertEquals(2, songs.size());
        SongDisplayable first = songs.get(0);
        Assertions.assertEquals("Grace & peace", first.getTitle());
        Assertions.assertEquals("Jane", first.getAuthor());
        Assertions.assertEquals("2", first.getCapo());
        Assertions.assertEquals("Public domain", first.getCopyright());
        Assertions.assertEquals("First line\nSecond line\n\nSing", first.getLyrics(false, false, false));
        Assertions.assertEquals("", songs.get(1).getCapo());
    }

    @Test
    public void importsUtf8FileWithBom() throws Exception {
        String xml = "\ufeff<songs><song><title1>Caf\u00e9</title1><contents>Words</contents></song></songs>";
        Path input = Files.writeString(directory.resolve("unicode.xml"), xml, StandardCharsets.UTF_8);
        List<SongDisplayable> songs = new EasySlidesParser().getSongs(input.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Caf\u00e9", songs.get(0).getTitle());
    }
}
