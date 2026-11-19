package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("OpenSong import")
public class OpensongParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-opensong-test").toString());
        }
    }

    @Test
    public void importsMetadataAndSectionLabelsFromZip() throws Exception {
        Path zip = directory.resolve("songs.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            entry(out, "song.xml", "<song><title>Grace &amp; peace</title><author>Jane</author>"
                    + "<copyright>2020</copyright><key>G</key><ccli>123</ccli><capo>2</capo>"
                    + "<lyrics>[V1]\nAmazing grace\n[C]\nSing with joy</lyrics></song>");
            entry(out, "untitled.xml", "<song><lyrics>no title</lyrics></song>");
        }
        List<SongDisplayable> songs = new OpensongParser().getSongs(zip.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        SongDisplayable song = songs.get(0);
        Assertions.assertEquals("Grace & peace", song.getTitle());
        Assertions.assertEquals("Jane", song.getAuthor());
        Assertions.assertEquals("2020", song.getCopyright());
        Assertions.assertEquals("G", song.getKey());
        Assertions.assertEquals("123", song.getCcli());
        Assertions.assertEquals("2", song.getCapo());
        Assertions.assertTrue(song.getLyrics(false, false, false).contains("Amazing grace"));
        Assertions.assertTrue(song.getLyrics(false, false, false).contains("Sing with joy"));
    }

    @Test
    @Disabled("OpenSong section markers do not begin new Quelea sections")
    public void keepsAdjacentVerseAndChorusAsSeparateSections() throws Exception {
        // Section boundaries control slide navigation and song sequences. Emit
        // a blank separator before each new OpenSong section marker so adjacent
        // verse and chorus blocks remain separate after SongDisplayable parses them.
        Path zip = directory.resolve("sections.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            entry(out, "song.xml", "<song><title>Test</title><lyrics>[V1]\nAmazing grace\n[C]\nSing with joy</lyrics></song>");
        }
        SongDisplayable song = new OpensongParser().getSongs(zip.toFile(), null).get(0);
        Assertions.assertEquals("Verse 1\nAmazing grace\n\nChorus\nSing with joy", song.getLyrics(false, false, false));
    }

    @Test
    public void badEntryDoesNotStopOtherSongsBeingImported() throws Exception {
        Path zip = directory.resolve("mixed.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            entry(out, "broken.xml", "<song>");
            entry(out, "good.xml", "<song><title>Good</title><lyrics>Words</lyrics></song>");
        }
        List<SongDisplayable> songs = new OpensongParser().getSongs(zip.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Good", songs.get(0).getTitle());
    }

    @Test
    @Disabled("OpenSong XML currently resolves external entities")
    public void rejectsExternalXmlEntitiesInImportedSongs() throws Exception {
        // An untrusted song can read local files into imported song text or
        // reference external resources. Disable DTDs and external entities on
        // the XML parser, and verify normal OpenSong files still import.
        Path secret = Files.writeString(directory.resolve("local-data.txt"), "SENTINEL-PRIVATE-DATA");
        String xml = "<!DOCTYPE song [<!ENTITY data SYSTEM \"" + secret.toUri().toASCIIString() + "\">]>"
                + "<song><title>&data;</title><lyrics>Words</lyrics></song>";
        Path zip = directory.resolve("untrusted.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            entry(out, "song.xml", xml);
        }
        Assertions.assertTrue(new OpensongParser().getSongs(zip.toFile(), null).isEmpty());
    }

    private void entry(ZipOutputStream out, String name, String text) throws Exception {
        out.putNextEntry(new ZipEntry(name));
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }
}
