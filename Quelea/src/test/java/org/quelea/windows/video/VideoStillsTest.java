package org.quelea.windows.video;

import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.VideoTestSupport;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/**
 * Still frames are used for video themes that aren't on screen (hidden
 * previews, remote control thumbnails), so no video has to keep decoding for
 * them. Needs JavaFX and GStreamer; skipped where they aren't available.
 */
@DisplayName("VideoStills")
public class VideoStillsTest {

    private int baseline;

    @BeforeEach
    public void setUp() {
        VideoTestSupport.assumeVideoAvailable();
        baseline = activePipelines();
    }

    private static Optional<Image> load(File file) throws Exception {
        return onFx(() -> VideoStills.load(file)).get(20, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Grabs a frame and releases the player afterwards")
    public void grabsFrameAndReleasesPlayer() throws Exception {
        Optional<Image> still = load(VideoTestSupport.TEST_VIDEO);
        assertTrue(still.isPresent(), "A still frame should have been grabbed");
        assertTrue(still.get().getWidth() > 1 && still.get().getHeight() > 1);
        assertTrue(waitFor(() -> activePipelines() == baseline, 5_000),
                "The player used to grab the frame should be disposed");
    }

    @Test
    @DisplayName("Each video is only grabbed once")
    public void stillsAreCached() throws Exception {
        Image first = load(VideoTestSupport.TEST_VIDEO).orElseThrow();
        Image cached = onFx(() -> VideoStills.getCached(VideoTestSupport.TEST_VIDEO));
        assertSame(first, cached);
        int before = activePipelines();
        Image again = load(VideoTestSupport.TEST_VIDEO).orElseThrow();
        assertSame(first, again);
        assertTrue(activePipelines() <= before, "A cached still shouldn't start a new player");
    }

    @Test
    @DisplayName("A missing video gives no still and leaves no player behind")
    public void missingVideo() throws Exception {
        Optional<Image> still = load(new File("vid/does-not-exist.mp4"));
        assertFalse(still.isPresent());
        assertTrue(waitFor(() -> activePipelines() == baseline, 5_000));
    }
}
