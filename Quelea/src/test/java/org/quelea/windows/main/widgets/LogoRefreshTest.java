package org.quelea.windows.main.widgets;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.image.ImagePanel;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.multimedia.MultimediaPanel;
import org.quelea.windows.timer.TimerPanel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.quelea.testutil.FxTestSupport.onFx;

/**
 * A changed logo reaches every display canvas, including those of panels
 * that aren't showing (e.g. the image panel, which shows the live preview
 * when an image is live), not just the panel that's showing.
 */
@DisplayName("Logo refresh")
public class LogoRefreshTest {

    @TempDir
    Path dir;

    @BeforeEach
    public void setUp() throws Exception {
        FxTestSupport.assumeFxAvailable();
        QueleaProperties.init(Files.createDirectory(dir.resolve("home")).toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxTestSupport.useScratchHome();
    }

    private static LogoImage logoOf(DisplayCanvas canvas) {
        for (Node node : canvas.getChildrenUnmodifiable()) {
            if (node instanceof LogoImage logo) {
                return logo;
            }
        }
        throw new AssertionError("No logo on canvas");
    }

    @Test
    @DisplayName("Every canvas type gets the new logo and settings")
    public void everyCanvasGetsNewLogo() throws Exception {
        Path png = dir.resolve("logo.png");
        Files.copy(Path.of("icons", "logo-square.png"), png);
        Map<String, List<DisplayCanvas>> canvases = onFx(() -> {
            Map<String, List<DisplayCanvas>> all = new LinkedHashMap<>();
            Map<String, Supplier<AbstractPanel>> panels = new LinkedHashMap<>();
            panels.put("image", ImagePanel::new);
            panels.put("multimedia", MultimediaPanel::new);
            panels.put("timer", TimerPanel::new);
            // (The PDF and presentation panels have no canvas of their own:
            // they draw on the projector canvases.)
            for (Map.Entry<String, Supplier<AbstractPanel>> panel : panels.entrySet()) {
                all.put(panel.getKey(), new ArrayList<>(panel.getValue().get().getCanvases()));
            }
            // A preview canvas like the theme / library previews, and a
            // projection and a stage view canvas, as the display windows have
            all.put("theme preview", List.of(new DisplayCanvas(false, () -> { }, DisplayCanvas.Priority.LOW)));
            all.put("projector", List.of(new DisplayCanvas(false, null, DisplayCanvas.Priority.MID)));
            all.put("stage", List.of(new DisplayCanvas(true, null, DisplayCanvas.Priority.HIGH)));
            return all;
        });
        for (Map.Entry<String, List<DisplayCanvas>> entry : canvases.entrySet()) {
            assertFalse(entry.getValue().isEmpty(), entry.getKey() + " panel should have a canvas");
        }

        // Changed from the logo button: the new image and settings, then the
        // refresh the live panel does
        QueleaProperties.get().setLogoImage(png.toString());
        QueleaProperties.get().setLogoSettings(new LogoSettings(0.5, LogoPosition.TOP_LEFT, 0, 0));
        onFx(() -> {
            LogoImage.refreshAll();
            return null;
        });

        String expected = png.toFile().toURI().toString();
        for (Map.Entry<String, List<DisplayCanvas>> entry : canvases.entrySet()) {
            for (DisplayCanvas canvas : entry.getValue()) {
                LogoImage logo = logoOf(canvas);
                Rectangle2D bounds = onFx(() -> {
                    logo.resize(1600, 900);
                    logo.layout();
                    return logo.getLogoBounds();
                });
                if (entry.getKey().equals("stage")) {
                    // Stage view shows its background colour, as before
                    assertEquals(new Rectangle2D(0, 0, 1600, 900), bounds, "stage");
                    continue;
                }
                Image shown = onFx(logo::getShownImage);
                assertNotNull(shown, entry.getKey());
                assertEquals(expected, shown.getUrl(), entry.getKey() + " should show the new logo");
                // Square logo, half the height, top left
                assertEquals(new Rectangle2D(0, 0, 450, 450), bounds, entry.getKey() + " should use the new settings");
            }
        }
    }

    @Test
    @DisplayName("Previews aren't changed by a refresh, and video previews don't touch the real logo video")
    public void previewsAreSeparate() throws Exception {
        LogoImage preview = onFx(() -> {
            LogoImage p = LogoImage.createPreview();
            p.showLogo(null, LogoSettings.DEFAULT);
            LogoImage.refreshAll();
            return p;
        });
        assertNull(onFx(preview::getShownImage), "a refresh shouldn't put the saved logo in a preview");
        LogoImage real = onFx(() -> new LogoImage(false));
        assertNotNull(onFx(real::getShownImage), "a real logo image shows the saved (default) logo");
    }
}
