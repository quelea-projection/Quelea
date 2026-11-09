package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MediaLibraryTest {

    @TempDir
    Path dir;

    private static ByteArrayInputStream bytes(int count) {
        return new ByteArrayInputStream(new byte[count]);
    }

    @Test
    void listsOnlyFilesQueleaShows() throws IOException {
        Files.write(dir.resolve("b.JPG"), new byte[3]);
        Files.write(dir.resolve("a.png"), new byte[2]);
        Files.write(dir.resolve("notes.txt"), new byte[1]);
        Files.write(dir.resolve(".hidden.png"), new byte[1]);
        Files.createDirectory(dir.resolve("folder.png"));
        List<Catalog.MediaFile> files = MediaLibrary.list(dir, MediaLibrary.Kind.IMAGE);
        assertEquals(List.of(new Catalog.MediaFile("a.png", 2), new Catalog.MediaFile("b.JPG", 3)), files);
        assertTrue(MediaLibrary.list(dir.resolve("missing"), MediaLibrary.Kind.IMAGE).isEmpty());
    }

    @Test
    void namesCannotLeaveTheFolder() throws IOException {
        Files.write(dir.resolve("a.png"), new byte[2]);
        assertEquals(dir.resolve("a.png"), MediaLibrary.resolve(dir, MediaLibrary.Kind.IMAGE, "a.png"));
        for (String bad : List.of("../a.png", "sub/a.png", "..\\a.png", ".a.png", "a.txt", "", "c:a.png")) {
            assertThrows(NoSuchElementException.class, () -> MediaLibrary.resolve(dir, MediaLibrary.Kind.IMAGE, bad),
                    bad);
            assertThrows(IllegalArgumentException.class, () -> MediaLibrary.checkName(MediaLibrary.Kind.IMAGE, bad),
                    bad);
        }
        assertThrows(NoSuchElementException.class, () -> MediaLibrary.resolve(dir, MediaLibrary.Kind.IMAGE, "b.png"));
        assertThrows(IllegalArgumentException.class, () -> MediaLibrary.checkName(MediaLibrary.Kind.VIDEO, "a.png"));
    }

    @Test
    void uploadsNeverReplaceAFile() throws IOException {
        Files.write(dir.resolve("photo.jpg"), new byte[1]);
        Catalog.MediaFile saved = MediaLibrary.save(dir, MediaLibrary.Kind.IMAGE, "photo.jpg", 10, bytes(10));
        assertEquals(new Catalog.MediaFile("photo (2).jpg", 10), saved);
        assertEquals(1, Files.size(dir.resolve("photo.jpg")));
        assertEquals(10, Files.size(dir.resolve("photo (2).jpg")));
        assertEquals("clip (3).mp4", MediaLibrary.numbered("clip.mp4", 3));
    }

    @Test
    void brokenUploadsLeaveNothingBehind() throws IOException {
        assertThrows(IOException.class,
                () -> MediaLibrary.save(dir, MediaLibrary.Kind.IMAGE, "short.jpg", 100, bytes(10)));
        assertThrows(IllegalArgumentException.class,
                () -> MediaLibrary.save(dir, MediaLibrary.Kind.IMAGE, "empty.jpg", -1, bytes(0)));
        ApiException tooLarge = assertThrows(ApiException.class,
                () -> MediaLibrary.save(dir, MediaLibrary.Kind.IMAGE, "big.jpg", 60L * 1024 * 1024, bytes(1)));
        assertEquals(413, tooLarge.getStatus());
        try (var listing = Files.list(dir)) {
            assertFalse(listing.findAny().isPresent(), "no partial or temporary files");
        }
    }
}
