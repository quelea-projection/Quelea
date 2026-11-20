package org.quelea.services.utils;

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

@DisplayName("Song packs")
public class SongPackTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-song-pack-test").toString());
        }
    }

    @Test
    public void readsSongsAndSkipsMalformedEntries() throws Exception {
        SongDisplayable first = new SongDisplayable("Grace & peace", "Jane");
        first.setLyrics("First verse");
        SongDisplayable second = new SongDisplayable("Caf\u00e9", "John");
        second.setLyrics("Second verse");
        Path archive = directory.resolve("songs.qsp");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive), StandardCharsets.UTF_8)) {
            entry(out, "first.xml", first.getXML());
            entry(out, "invalid.xml", "<song>");
            entry(out, "second.xml", second.getXML());
        }
        List<SongDisplayable> songs = SongPack.fromFile(archive.toFile()).getSongs();
        Assertions.assertEquals(2, songs.size());
        Assertions.assertEquals("Grace & peace", songs.get(0).getTitle());
        Assertions.assertEquals("First verse", songs.get(0).getLyrics(false, false, false));
        Assertions.assertEquals("Caf\u00e9", songs.get(1).getTitle());
    }

    @Test
    public void returnedListDoesNotModifyThePack() {
        SongPack pack = new SongPack();
        pack.addSong(new SongDisplayable("Song", "Author"));
        pack.getSongs().clear();
        Assertions.assertEquals(1, pack.getSongs().size());
    }

    private void entry(ZipOutputStream out, String name, String xml) throws Exception {
        out.putNextEntry(new ZipEntry(name));
        out.write(xml.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }
}
