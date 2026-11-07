package org.quelea.windows.main;

import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.colourTheme;
import static org.quelea.testutil.VideoTestSupport.videoTheme;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/**
 * Theme thumbnails for the remote control are rendered off-screen, without
 * opening the theme window, and without leaving a video playing. Needs JavaFX
 * and GStreamer; skipped where they aren't available.
 */
@DisplayName("ThemeThumbnails")
public class ThemeThumbnailsTest {

    private int baseline;

    @BeforeEach
    public void setUp() {
        assumeVideoAvailable();
        baseline = activePipelines();
    }

    @Test
    @DisplayName("A video theme thumbnail shows the video, with no player left running")
    public void videoThemeThumbnail() throws Exception {
        for (int i = 0; i < 5; i++) {
            // Like the remote control fetching all thumbnails again.
            Image thumb = ThemeThumbnails.render(videoTheme(3));
            assertEquals(ThemeThumbnails.WIDTH, (int) thumb.getWidth());
            assertEquals(ThemeThumbnails.HEIGHT, (int) thumb.getHeight());
            assertTrue(colouredFraction(thumb) > 0.5,
                    "Most of the thumbnail should show the video, not a black background");
        }
        assertTrue(waitFor(() -> activePipelines() == baseline, 5_000),
                "No video player should be left running after rendering thumbnails");
    }

    @Test
    @DisplayName("A colour theme thumbnail needs no video player")
    public void colourThemeThumbnail() throws Exception {
        Image thumb = ThemeThumbnails.render(colourTheme());
        assertEquals(ThemeThumbnails.WIDTH, (int) thumb.getWidth());
        assertEquals(baseline, activePipelines());
    }

    @Test
    @DisplayName("Thumbnails can't be rendered on the FX thread")
    public void notOnFxThread() throws Exception {
        Throwable thrown = org.quelea.testutil.VideoTestSupport.onFx(() -> {
            try {
                ThemeThumbnails.render(colourTheme());
                return null;
            } catch (IllegalStateException ex) {
                return ex;
            }
        });
        assertTrue(thrown instanceof IllegalStateException,
                "Rendering on the FX thread would deadlock waiting for the still frame");
    }

    /** Fraction of sampled pixels that aren't black. */
    private static double colouredFraction(Image image) {
        var reader = image.getPixelReader();
        int total = 0;
        int coloured = 0;
        for (int x = 0; x < image.getWidth(); x += 4) {
            for (int y = 0; y < image.getHeight(); y += 4) {
                total++;
                int rgb = reader.getArgb(x, y) & 0xFFFFFF;
                if (rgb != 0) {
                    coloured++;
                }
            }
        }
        return (double) coloured / total;
    }
}
