package org.quelea.windows.newsong;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.ensureProjectionWindow;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.videoTheme;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/**
 * The theme editor with a video theme. Each change in the editor (e.g. every
 * tick of the shadow radius / spread slider) used to create a new preview
 * drawer with its own video player that was never stopped, so memory use grew
 * by gigabytes within seconds. Needs JavaFX and GStreamer; skipped where they
 * aren't available.
 */
@DisplayName("ThemePanel video preview")
public class ThemePanelVideoTest {

    private Stage stage;
    private ThemePanel panel;
    private int baseline;

    @BeforeEach
    public void setUp() throws Exception {
        assumeVideoAvailable();
        baseline = activePipelines();
        onFx(() -> {
            ensureProjectionWindow();
            panel = new ThemePanel();
            stage = new Stage();
            stage.setScene(new Scene(panel, 800, 600));
            stage.show();
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (stage != null) {
            onFx(() -> stage.hide());
        }
    }

    @Test
    @DisplayName("Editing a video theme keeps a single preview player")
    public void editingKeepsOnePlayer() throws Exception {
        for (int tick = 0; tick < 100; tick++) {
            double radius = 1 + tick % 40;
            onFx(() -> panel.setTheme(videoTheme(radius)));
            int extra = activePipelines() - baseline;
            assertTrue(extra <= 1, "At most one preview player should exist, found " + extra
                    + " after " + (tick + 1) + " theme changes");
        }
        onFx(() -> { }); // the panel redraws in a runLater; let it finish
        assertTrue(activePipelines() - baseline <= 1, "At most one preview player should exist");
    }

    @Test
    @DisplayName("Closing the theme editor stops the preview video")
    public void closingStopsPreview() throws Exception {
        onFx(() -> panel.setTheme(videoTheme(2)));
        assertTrue(waitFor(() -> activePipelines() == baseline + 1, 5_000),
                "The open editor should play the video preview");
        onFx(() -> stage.hide());
        assertTrue(waitFor(() -> activePipelines() == baseline, 5_000),
                "No preview player should keep running once the editor is closed");
    }
}
