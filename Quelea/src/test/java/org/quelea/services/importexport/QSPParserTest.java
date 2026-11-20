package org.quelea.services.importexport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("QSP song import")
class QSPParserTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-qsp-import").toString());
        }
    }

    @Test
    void importsMultipleSongsAndSkipsMalformedArchiveMembers() throws Exception {
        SongDisplayable first = new SongDisplayable("A & B", "Author");
        first.setLyrics("First verse");
        first.setCcli("12345");
        SongDisplayable second = new SongDisplayable("Caf\u00e9", "Writer");
        second.setLyrics("Second verse");
        Path file = directory.resolve("songs.qsp");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            write(out, "first.xml", first.getXML());
            write(out, "broken.xml", "<song>");
            write(out, "second.xml", second.getXML());
        }
        List<SongDisplayable> songs = new QSPParser().getSongs(file.toFile(), null);
        Assertions.assertEquals(2, songs.size());
        Assertions.assertEquals("A & B", songs.get(0).getTitle());
        Assertions.assertEquals("12345", songs.get(0).getCcli());
        Assertions.assertEquals("First verse", songs.get(0).getLyrics(false, false, false));
        Assertions.assertEquals("Caf\u00e9", songs.get(1).getTitle());
        Assertions.assertEquals("Second verse", songs.get(1).getLyrics(false, false, false));
    }

    @Test
    void rejectsAFileThatIsNotAZipArchive() throws Exception {
        Path file = directory.resolve("broken.qsp");
        Files.writeString(file, "not an archive");
        Assertions.assertThrows(IOException.class, () -> new QSPParser().getSongs(file.toFile(), null));
    }

    private void write(ZipOutputStream out, String name, String text) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }
}
