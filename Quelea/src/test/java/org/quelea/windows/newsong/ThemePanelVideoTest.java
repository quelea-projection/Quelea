package org.quelea.windows.newsong;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.ImageBackground;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    @DisplayName("Editing a video theme preserves its stretch setting")
    public void videoStretchSurvivesEditing() throws Exception {
        ThemeDTO defaults = ThemeDTO.DEFAULT_THEME;
        for (boolean stretch : new boolean[]{false, true}) {
            ThemeDTO theme = new ThemeDTO(defaults.getSerializableFont(), defaults.getFontPaint(),
                    defaults.getTranslateSerializableFont(), defaults.getTranslateFontPaint(),
                    new VideoBackground(videoTheme(2).getBackground().getString(), stretch), defaults.getShadow(),
                    false, false, false, false, -1, 0);
            onFx(() -> {
                panel.setTheme(theme);
                VideoBackground background = assertInstanceOf(VideoBackground.class, panel.getTheme().getBackground());
                assertEquals(stretch, background.getStretch());
            });
        }
    }

    @Test
    @DisplayName("Alignment and stretch changes are detected as unsaved")
    public void detectsThemeChanges() throws Exception {
        onFx(() -> {
            ThemeDTO initial = videoTheme(2);
            panel.setTheme(initial);
            panel.resetSaveHash();
            assertFalse(panel.hashChanged());
            ThemeDTO aligned = videoTheme(2);
            aligned.setTextAlignment(1);
            panel.setTheme(aligned);
            assertTrue(panel.hashChanged(), "Changing alignment should prompt to save");
            panel.resetSaveHash();
            assertFalse(panel.hashChanged());
            ThemeDTO stretched = new ThemeDTO(aligned.getSerializableFont(), aligned.getFontPaint(),
                    aligned.getTranslateSerializableFont(), aligned.getTranslateFontPaint(),
                    new VideoBackground(((VideoBackground) aligned.getBackground()).getVideoFile().getAbsolutePath(), true),
                    aligned.getShadow(), aligned.isBold(), aligned.isItalic(), aligned.isTranslateBold(),
                    aligned.isTranslateItalic(), aligned.getTextPosition(), aligned.getTextAlignment());
            panel.setTheme(stretched);
            assertTrue(panel.hashChanged(), "Changing stretch should prompt to save");
        });
    }

    @Test
    @DisplayName("An unchanged image theme is not treated as an edit")
    public void unchangedImageThemeIsNotMarkedDirty() throws Exception {
        onFx(() -> {
            ThemeDTO d = ThemeDTO.DEFAULT_THEME;
            ThemeDTO image = new ThemeDTO(d.getSerializableFont(), d.getFontPaint(),
                    d.getTranslateSerializableFont(), d.getTranslateFontPaint(),
                    new ImageBackground("missing-background.png"), d.getShadow(),
                    false, false, false, false, -1, 0);
            panel.setTheme(image);
            panel.resetSaveHash();
            assertFalse(panel.hashChanged());
        });
    }

    @Test
    @DisplayName("The theme dialog selects and returns an alignment-only change")
    public void themeDialogKeepsAlignmentChanges() throws Exception {
        onFx(() -> {
            EditThemeDialog dialog = new EditThemeDialog();
            try {
                ThemeDTO first = org.quelea.testutil.VideoTestSupport.colourTheme();
                first.setThemeName("Same name");
                ThemeDTO aligned = org.quelea.testutil.VideoTestSupport.colourTheme();
                aligned.setThemeName("Same name");
                aligned.setTextAlignment(1);
                dialog.setTheme(first);
                dialog.setTheme(aligned);
                var confirm = EditThemeDialog.class.getDeclaredField("confirmButton");
                confirm.setAccessible(true);
                ((Button) confirm.get(dialog)).fire();
                assertEquals(1, dialog.getTheme().getTextAlignment());
            } finally {
                dialog.hide();
            }
            return null;
        });
    }
}
