package org.quelea.services.importexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Plain text song import")
public class PlainTextParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-plain-text-test").toString());
        }
    }

    @Test
    public void importsMetadataAndMultipleSections() throws Exception {
        Path input = write("song.txt", "title: Amazing Grace\nAuthor: John Newton\nCCLI: 123\n"
                + "Copyright: Public domain\nYear: 1779\nPublisher: Example\nKey: G\nCapo: 2\nInfo: Slow\n"
                + "Verse 1\nAmazing grace\nHow sweet the sound\n\nChorus\nSing with joy\n");
        SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
        Assertions.assertEquals("Amazing Grace", song.getTitle());
        Assertions.assertEquals("John Newton", song.getAuthor());
        Assertions.assertEquals("123", song.getCcli());
        Assertions.assertEquals("Public domain", song.getCopyright());
        Assertions.assertEquals("1779", song.getYear());
        Assertions.assertEquals("Example", song.getPublisher());
        Assertions.assertEquals("G", song.getKey());
        Assertions.assertEquals("2", song.getCapo());
        Assertions.assertEquals("Slow", song.getInfo());
        Assertions.assertEquals("Verse 1\nAmazing grace\nHow sweet the sound\n\nChorus\nSing with joy", song.getLyrics(false, false, false));
    }

    @Test
    public void usesFilenameAsTitleAndReadsFinalSectionWithoutBlankLine() throws Exception {
        Path input = write("My Song.txt", "One line\nSecond line");
        SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
        Assertions.assertEquals("My Song", song.getTitle());
        Assertions.assertEquals("One line\nSecond line", song.getLyrics(false, false, false));
    }

    @Test
    public void importsChordProChordsWithoutLosingLyrics() throws Exception {
        Path input = write("chords.txt", "[C]Amazing [G]grace\n");
        SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
        Assertions.assertEquals("Amazing grace", song.getLyrics(false, false, false));
        Assertions.assertTrue(song.getLyrics(true, false, false).contains("C"));
        Assertions.assertTrue(song.getLyrics(true, false, false).contains("G"));
    }

    @Test
    public void importsOnlyTextFilesFromDirectory() throws Exception {
        write("first.txt", "First verse");
        write("second.txt", "Second verse");
        write("ignored.pdf", "Not a song");
        Files.createDirectory(directory.resolve("nested.txt"));
        List<SongDisplayable> songs = new PlainTextParser().getSongs(directory.toFile(), null);
        Assertions.assertEquals(2, songs.size());
        Assertions.assertEquals(java.util.Set.of("first", "second"), songs.stream().map(SongDisplayable::getTitle).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    public void acceptsUpperCaseTextFileExtension() throws Exception {
        Path input = write("Song.TXT", "A verse");
        Assertions.assertEquals(1, new PlainTextParser().getSongs(input.toFile(), null).size());
    }

    @Test
    public void metadataMatchingDoesNotDependOnDefaultLocale() throws Exception {
        Path input = write("song.txt", "TITLE: Song title\nWords\n");
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
            Assertions.assertEquals("Song title", song.getTitle());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void recognizesMetadataAfterUtf8ByteOrderMark() throws Exception {
        Path input = write("bom.txt", "\ufeffTitle: Café\nWords\n");
        SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
        Assertions.assertEquals("Café", song.getTitle());
        Assertions.assertEquals("Words", song.getLyrics(false, false, false));
    }

    @Test
    public void importsUtf16LittleEndianWithByteOrderMark() throws Exception {
        Path input = directory.resolve("utf16.txt");
        byte[] content = "Title: Café\nWords\n".getBytes(StandardCharsets.UTF_16LE);
        byte[] bytes = new byte[content.length + 2];
        bytes[0] = (byte) 0xff;
        bytes[1] = (byte) 0xfe;
        System.arraycopy(content, 0, bytes, 2, content.length);
        Files.write(input, bytes);
        SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
        Assertions.assertEquals("Café", song.getTitle());
        Assertions.assertEquals("Words", song.getLyrics(false, false, false));
    }

    @Test
    public void importsLegacySingleByteTextWithoutByteOrderMark() throws Exception {
        Path input = directory.resolve("legacy.txt");
        Files.writeString(input, "Title: Café\nVoilà l'été à Paris\n", java.nio.charset.Charset.forName("windows-1252"));
        SongDisplayable song = new PlainTextParser().getSongs(input.toFile(), null).get(0);
        Assertions.assertEquals("Café", song.getTitle());
        Assertions.assertEquals("Voilà l'été à Paris", song.getLyrics(false, false, false));
    }

    private Path write(String name, String text) throws Exception {
        return Files.writeString(directory.resolve(name), text, StandardCharsets.UTF_8);
    }
}
