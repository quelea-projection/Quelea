package org.quelea.windows.lyrics;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.windows.main.DisplayCanvas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.colourTheme;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.videoTheme;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/**
 * Video backgrounds in theme previews. Editing a video theme used to create a
 * new drawer - and with it a new, never stopped video pipeline - for every
 * change, so dragging the shadow radius or spread slider started hundreds of
 * players decoding at once until the machine ran out of memory.
 * <p>
 * Needs JavaFX and GStreamer; skipped where they aren't available.
 */
@DisplayName("LyricDrawer video backgrounds")
public class LyricDrawerVideoTest {

    private DisplayCanvas canvas;
    private LyricDrawer drawer;
    private int baseline;

    @BeforeEach
    public void setUp() throws Exception {
        assumeVideoAvailable();
        baseline = activePipelines();
        onFx(() -> {
            canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            new Scene(new StackPane(canvas), 320, 180);
            drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (drawer != null) {
            // Leave no player running for the next test.
            onFx(() -> drawer.setPlayVideo(false));
        }
    }

    @Test
    @DisplayName("Repeated theme updates reuse a single video player")
    public void repeatedThemeUpdatesReuseOnePlayer() throws Exception {
        // Like dragging the shadow radius slider: one theme update per tick.
        for (int tick = 0; tick < 150; tick++) {
            double radius = 1 + tick % 40;
            onFx(() -> drawer.setTheme(videoTheme(radius)));
            assertEquals(baseline + 1, activePipelines(),
                    "Only one video player should exist, after " + (tick + 1) + " updates");
        }
    }

    @Test
    @DisplayName("Switching to a non-video theme releases the player")
    public void nonVideoThemeReleasesPlayer() throws Exception {
        onFx(() -> drawer.setTheme(videoTheme(2)));
        assertEquals(baseline + 1, activePipelines());
        onFx(() -> drawer.setTheme(colourTheme()));
        assertEquals(baseline, activePipelines(), "The video player should be disposed");
    }

    @Test
    @DisplayName("Turning video off releases the player")
    public void stopPlayingReleasesPlayer() throws Exception {
        onFx(() -> drawer.setTheme(videoTheme(2)));
        assertEquals(baseline + 1, activePipelines());
        onFx(() -> drawer.setPlayVideo(false));
        assertEquals(baseline, activePipelines(), "The video player should be disposed");
        onFx(() -> {
            drawer.setPlayVideo(true);
            drawer.setTheme(videoTheme(2));
        });
        assertEquals(baseline + 1, activePipelines(), "Video should play again once turned back on");
    }

    @Test
    @DisplayName("A hidden preview shows a still frame and keeps no player running")
    public void hiddenPreviewShowsStillFrame() throws Exception {
        onFx(() -> drawer.setPlayVideo(false));
        for (int tick = 0; tick < 20; tick++) {
            double radius = 1 + tick;
            onFx(() -> drawer.setTheme(videoTheme(radius)));
        }
        assertTrue(waitFor(() -> backgroundImage() != null && backgroundImage().getWidth() > 1, 15_000),
                "The background should show a still frame of the video");
        assertTrue(waitFor(() -> activePipelines() == baseline, 5_000),
                "No video player should be left running for a hidden preview, found "
                        + (activePipelines() - baseline));
        Image still = onFx(this::backgroundImage);
        assertTrue(hasColour(still), "The still frame shouldn't be blank");
    }

    private Image backgroundImage() {
        Node background = canvas.getCanvasBackground();
        return background instanceof ImageView ? ((ImageView) background).getImage() : null;
    }

    private static boolean hasColour(Image image) {
        var reader = image.getPixelReader();
        for (int x = 0; x < image.getWidth(); x += 8) {
            for (int y = 0; y < image.getHeight(); y += 8) {
                if ((reader.getArgb(x, y) & 0xFFFFFF) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
