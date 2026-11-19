package org.quelea.windows.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.testutil.VideoTestSupport;

import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/** Uses the bundled video in a temporary location; no audio output is needed. */
@DisplayName("Video still cache")
public class VideoStillsCacheTest {

    private Path video;
    private int baseline;

    @BeforeEach
    public void setUp(@TempDir Path directory) throws IOException {
        VideoTestSupport.assumeVideoAvailable();
        baseline = activePipelines();
        video = Files.copy(VideoTestSupport.TEST_VIDEO.toPath(), directory.resolve("video copy.mp4"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (video != null) {
            Assertions.assertTrue(waitFor(() -> activePipelines() == baseline, 15_000),
                    "The still player must be disposed");
            // Disposal stops the native pipeline asynchronously. Windows may
            // keep the source file open briefly after its pipeline count drops.
            Assertions.assertTrue(waitFor(() -> {
                try {
                    Files.deleteIfExists(video);
                    return true;
                } catch (IOException ex) {
                    return false;
                }
            }, 10_000), "The still player must release its source file");
        }
    }

    @Test
    @DisplayName("Requests for the same file share a pending grab and its cached result")
    public void pendingRequestsAreShared() throws Exception {
        List<CompletableFuture<Optional<Image>>> requests = onFx(() -> List.of(
                VideoStills.load(video.toFile()), VideoStills.load(video.toFile())));
        Assertions.assertSame(requests.get(0), requests.get(1));
        Image still = requests.get(0).get(20, TimeUnit.SECONDS).orElseThrow();
        Assertions.assertTrue(still.getWidth() > 1 && still.getHeight() > 1);
        Assertions.assertSame(still, onFx(() -> VideoStills.getCached(video.toFile())));
        Assertions.assertSame(still, onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).orElseThrow());
    }

    @Test
    @DisplayName("A modified video is grabbed again instead of using the old still")
    public void modificationInvalidatesCache() throws Exception {
        Image original = onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).orElseThrow();
        FileTime originalTime = Files.getLastModifiedTime(video);
        Files.setLastModifiedTime(video, FileTime.fromMillis(originalTime.toMillis() + 30_000));
        Assertions.assertNotEquals(originalTime.toMillis(), Files.getLastModifiedTime(video).toMillis());
        Assertions.assertNull(onFx(() -> VideoStills.getCached(video.toFile())));
        Image updated = onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).orElseThrow();
        Assertions.assertNotSame(original, updated);
        Assertions.assertSame(updated, onFx(() -> VideoStills.getCached(video.toFile())));
    }

    @Test
    @DisplayName("A video filename containing a URI-reserved character can be opened")
    public void filenameWithHash() throws Exception {
        video = Files.move(video, video.resolveSibling("video #1.mp4"));
        Assertions.assertTrue(onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).isPresent());
    }

    @Test
    @DisplayName("A video filename containing non-ASCII characters can be opened")
    public void filenameWithNonAsciiCharacters() throws Exception {
        video = Files.move(video, video.resolveSibling("video café.mp4"));
        Assertions.assertTrue(onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).isPresent());
    }

    @Test
    @DisplayName("A video in a Unicode directory with reserved filename characters can be opened")
    public void unicodeDirectoryAndReservedCharacters() throws Exception {
        Path directory = Files.createDirectory(video.getParent().resolve("\u5a92\u4f53 \uD83D\uDCF9"));
        video = Files.move(video, directory.resolve("\u6b4c caf\u00e9 #100%.mp4"));
        Assertions.assertTrue(onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).isPresent());
    }

    @Test
    @DisplayName("A video filename with decomposed Unicode can be opened")
    public void filenameWithDecomposedUnicode() throws Exception {
        video = Files.move(video, video.resolveSibling("video cafe\u0301.mp4"));
        Assertions.assertTrue(onFx(() -> VideoStills.load(video.toFile())).get(20, TimeUnit.SECONDS).isPresent());
    }
}
