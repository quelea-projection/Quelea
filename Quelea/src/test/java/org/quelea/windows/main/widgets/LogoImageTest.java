package org.quelea.windows.main.widgets;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.testutil.FxTestSupport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.FxTestSupport.onFx;

/**
 * The logo on the display canvas: placed by the logo settings, SVG logos
 * rendered at their real size, and black everywhere else. Needs JavaFX;
 * skipped where it can't be started.
 */
@DisplayName("LogoImage")
public class LogoImageTest {

    @TempDir
    Path dir;

    @BeforeEach
    public void setUp() throws Exception {
        FxTestSupport.assumeFxAvailable();
        Path home = Files.createDirectory(dir.resolve("home"));
        QueleaProperties.init(home.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxTestSupport.useScratchHome();
    }

    private String writeSvg() throws Exception {
        Path file = dir.resolve("logo.svg");
        // 2:1, white, so it's easy to tell apart from the black background
        Files.writeString(file, "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 20 10\">"
                + "<rect width=\"20\" height=\"10\" fill=\"#ffffff\"/></svg>", StandardCharsets.UTF_8);
        return file.toString();
    }

    private static LogoImage layOut(LogoImage logo, double width, double height) {
        logo.resize(width, height);
        logo.layout();
        return logo;
    }

    private static Image waitForImage(LogoImage logo, int width, int height) throws Exception {
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end) {
            Image image = onFx(logo::getShownImage);
            if (image != null && image.getWidth() == width && image.getHeight() == height) {
                return image;
            }
            Thread.sleep(50);
        }
        return onFx(logo::getShownImage);
    }

    @Test
    @DisplayName("Without any logo settings the logo fills the screen, centred, as before")
    public void defaultLayout() throws Exception {
        // The default logo
        Rectangle2D bounds = onFx(() -> {
            LogoImage logo = layOut(new LogoImage(false), 1920, 1080);
            Image image = logo.getShownImage();
            assertNotNull(image);
            return logo.getLogoBounds();
        });
        assertTrue(bounds.getHeight() == 1080 || bounds.getWidth() == 1920,
                "the logo should fill the height or the width, but was " + bounds);
        assertEquals(1920 - bounds.getMaxX(), bounds.getMinX(), 1, "centred horizontally");
        assertEquals(1080 - bounds.getMaxY(), bounds.getMinY(), 1, "centred vertically");
    }

    @Test
    @DisplayName("An SVG logo is placed by the settings and rendered at the size it's shown at")
    public void svgLogo() throws Exception {
        QueleaProperties.get().setLogoImage(writeSvg());
        QueleaProperties.get().setLogoSettings(new LogoSettings(0.25, LogoPosition.BOTTOM_RIGHT, 0, 0));
        LogoImage logo = onFx(() -> layOut(new LogoImage(false), 1920, 1080));
        // A box of 480x270 fits the 2:1 logo as 480x240, in the bottom right corner
        assertEquals(new Rectangle2D(1440, 840, 480, 240), onFx(logo::getLogoBounds));
        Image image = waitForImage(logo, 480, 240);
        assertEquals(480, image.getWidth(), "rendered at the size shown, not scaled");
        assertEquals(240, image.getHeight());

        // Bigger canvas: rendered again at the new size
        onFx(() -> layOut(logo, 3840, 2160));
        image = waitForImage(logo, 960, 480);
        assertEquals(960, image.getWidth());
        assertEquals(480, image.getHeight());

        // Settings changed and refreshed: applies straight away
        QueleaProperties.get().setLogoSettings(LogoSettings.DEFAULT.withPosition(LogoPosition.TOP_LEFT).withSize(0.5));
        Rectangle2D bounds = onFx(() -> {
            logo.refresh();
            logo.layout();
            return logo.getLogoBounds();
        });
        assertEquals(new Rectangle2D(0, 0, 1920, 960), bounds);
    }

    @Test
    @DisplayName("An SVG logo isn't rendered on canvases where it isn't shown")
    public void svgOnlyRenderedWhenShown() throws Exception {
        QueleaProperties.get().setLogoImage(writeSvg());
        LogoImage logo = onFx(() -> {
            LogoImage l = new LogoImage(false);
            l.setOpacity(0); // As on every display canvas until the logo button is pressed
            new Scene(new Group(l), 200, 100);
            return layOut(l, 200, 100);
        });
        Thread.sleep(300);
        assertEquals(null, onFx(logo::getShownImage), "hidden logo shouldn't be rendered");
        // Fading in: rendered on the next layout pass
        onFx(() -> {
            logo.setOpacity(0.1);
            logo.layout();
            return null;
        });
        assertEquals(200, waitForImage(logo, 200, 100).getWidth());
    }

    @Test
    @DisplayName("Everything around the logo is black, whatever is behind it")
    public void blackAroundLogo() throws Exception {
        QueleaProperties.get().setLogoImage(writeSvg());
        QueleaProperties.get().setLogoSettings(new LogoSettings(0.25, LogoPosition.TOP_LEFT, 0, 0));
        LogoImage logo = onFx(() -> {
            LogoImage l = new LogoImage(false);
            // Something colourful behind the logo, like a song's background
            StackPane canvas = new StackPane(new Rectangle(400, 200, Color.RED), l);
            new Scene(new Group(canvas), 400, 200);
            canvas.resize(400, 200);
            l.resize(400, 200);
            canvas.layout();
            l.layout();
            return l;
        });
        waitForImage(logo, 100, 50);
        WritableImage snapshot = onFx(() -> {
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            return logo.getParent().snapshot(params, null);
        });
        // The logo itself (white) in the top left...
        assertColour(Color.WHITE, snapshot.getPixelReader().getColor(50, 25));
        // ...and black, not red, everywhere else
        assertColour(Color.BLACK, snapshot.getPixelReader().getColor(150, 25));
        assertColour(Color.BLACK, snapshot.getPixelReader().getColor(50, 150));
        assertColour(Color.BLACK, snapshot.getPixelReader().getColor(399, 199));
    }

    private static void assertColour(Color expected, Color actual) {
        assertTrue(Math.abs(expected.getRed() - actual.getRed()) < 0.05
                        && Math.abs(expected.getGreen() - actual.getGreen()) < 0.05
                        && Math.abs(expected.getBlue() - actual.getBlue()) < 0.05
                        && actual.getOpacity() > 0.95,
                "expected " + expected + " but was " + actual);
    }
}
