package org.quelea.windows.video;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.VideoTestSupport;

import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/** Player lifecycle tests need GStreamer but do not depend on video codecs. */
@DisplayName("VidDisplay lifecycle")
public class VidDisplayLifecycleTest {

    private VidDisplay display;
    private int baseline;

    @BeforeEach
    public void setUp() {
        VideoTestSupport.assumeVideoAvailable();
        baseline = activePipelines();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (display != null) {
            onFx(display::dispose);
            Assertions.assertTrue(waitFor(() -> activePipelines() == baseline, 5_000),
                    "The test player must not leave a pipeline active");
        }
    }

    @Test
    @DisplayName("Disposing a video-only player is idempotent")
    public void disposeIsIdempotent() throws Exception {
        onFx(() -> {
            display = new VidDisplay(false);
            Assertions.assertEquals(baseline + 1, activePipelines());
            display.setURI(VideoTestSupport.TEST_VIDEO.toURI());
            display.setLoop(true);
            Assertions.assertEquals(VideoTestSupport.TEST_VIDEO.toURI(), display.getUri());
            Assertions.assertTrue(display.isLoop());

            display.dispose();
            display.dispose();
            Assertions.assertEquals(baseline, activePipelines());
            Assertions.assertNull(display.getUri());
            Assertions.assertEquals(-1, display.getDuration());
            Assertions.assertDoesNotThrow(() -> {
                display.play();
                display.pause();
                display.stop();
                display.seek(0.5);
            });
        });
    }

    @Test
    @DisplayName("Repeatedly replacing video-only players releases every pipeline")
    public void repeatedPlayerReplacement() throws Exception {
        onFx(() -> {
            for (int i = 0; i < 8; i++) {
                display = new VidDisplay(false);
                Assertions.assertEquals(baseline + 1, activePipelines(), "player " + i);
                display.dispose();
                Assertions.assertEquals(baseline, activePipelines(), "player " + i);
            }
        });
    }
}
