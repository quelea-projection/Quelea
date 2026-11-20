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

@DisplayName("FreeWorship import")
public class FreeWorshipParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-freeworship-test").toString());
        }
    }

    @Test
    public void importsMetadataAndLinesFromSeparateVerses() throws Exception {
        SongDisplayable song = parse("<song><properties><titles><title>Grace</title></titles>"
                + "<authors><author>Jane</author></authors><copyright>2020</copyright>"
                + "<publisher>Publisher</publisher><ccliNo>123</ccliNo></properties>"
                + "<lyrics><verse><lines>First<br/>Second</lines></verse>"
                + "<verse><lines>Chorus</lines></verse></lyrics></song>");
        Assertions.assertEquals("Grace", song.getTitle());
        Assertions.assertEquals("Jane", song.getAuthor());
        Assertions.assertEquals("2020", song.getCopyright());
        Assertions.assertEquals("Publisher", song.getPublisher());
        Assertions.assertEquals("123", song.getCcli());
        Assertions.assertEquals("First\nSecond\n\nChorus", song.getLyrics(false, false, false));
    }

    @Test
    public void decodesXmlEntitiesInLyrics() throws Exception {
        SongDisplayable song = parse("<song><properties><titles><title>Song</title></titles></properties>"
                + "<lyrics><verse><lines>Grace &amp; peace &lt;forever&gt;</lines></verse></lyrics></song>");
        Assertions.assertEquals("Grace & peace <forever>", song.getLyrics(false, false, false));
    }

    @Test
    public void readsCdataAndLineBreaksAlongsideEscapedText() throws Exception {
        SongDisplayable song = parse("<song><properties><titles><title>Song</title></titles></properties>"
                + "<lyrics><verse><lines><![CDATA[Grace & peace]]><br/>Forever &amp; ever</lines></verse></lyrics></song>");
        Assertions.assertEquals("Grace & peace\nForever & ever", song.getLyrics(false, false, false));
    }

    private SongDisplayable parse(String xml) throws Exception {
        Path input = Files.writeString(directory.resolve("song.xml"), xml, StandardCharsets.UTF_8);
        return new FreeWorshipParser().getSongs(input.toFile(), null).get(0);
    }
}
