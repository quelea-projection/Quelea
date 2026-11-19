package org.quelea.services.importexport;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("SongBeamer import")
public class SongBeamerParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-songbeamer-test").toString());
        }
    }

    @Test
    public void importsMetadataAndAlternatingTranslations() throws Exception {
        SongDisplayable song = parse("#Title=Grace\n#Author=Jane\n#key=G\n#TitleLang2=French\n"
                + "---\nVerse 1\nAmazing grace\nGr\u00e2ce infinie\n---\nChorus\nSing\nChantez\n");
        Assertions.assertEquals("Grace", song.getTitle());
        Assertions.assertEquals("Jane", song.getAuthor());
        Assertions.assertEquals("G", song.getKey());
        Assertions.assertTrue(song.getLyrics(false, false, false).contains("Amazing grace"));
        Assertions.assertTrue(song.getLyrics(false, false, false).contains("Sing"));
        Assertions.assertTrue(song.getTranslations().get("French").contains("Gr\u00e2ce infinie"));
        Assertions.assertTrue(song.getTranslations().get("French").contains("Chantez"));
    }

    @Test
    public void importsCopyrightTag() throws Exception {
        SongDisplayable song = parse("#Title=Song\n#(c)=Copyright holder\n---\nWords\n");
        Assertions.assertEquals("Copyright holder", song.getCopyright());
    }

    @Test
    public void suppliesDefaultsForMissingTitleAndAuthor() throws Exception {
        SongDisplayable song = parse("---\nA line of lyrics\n");
        Assertions.assertEquals("Untitled", song.getTitle());
        Assertions.assertEquals("", song.getAuthor());
    }

    private SongDisplayable parse(String text) throws Exception {
        Path path = directory.resolve("song.sng");
        Files.writeString(path, text, Charset.forName("windows-1250"));
        return new SongBeamerParser().getSongs(path.toFile(), null).get(0);
    }
}
