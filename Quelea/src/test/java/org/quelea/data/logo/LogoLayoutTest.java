package org.quelea.data.logo;

import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LogoLayout")
public class LogoLayoutTest {

    private static final double EPS = 1e-9;

    private static void assertBounds(double x, double y, double w, double h, Rectangle2D actual) {
        assertEquals(x, actual.getMinX(), EPS, "x");
        assertEquals(y, actual.getMinY(), EPS, "y");
        assertEquals(w, actual.getWidth(), EPS, "width");
        assertEquals(h, actual.getHeight(), EPS, "height");
    }

    @Test
    @DisplayName("Default settings fit the logo to the screen, centred, like before")
    public void defaultFitsAndCentres() {
        // Square logo on a 16:9 screen: full height, centred horizontally
        assertBounds(420, 0, 1080, 1080, LogoLayout.computeBounds(1920, 1080, 500, 500, LogoSettings.DEFAULT));
        // Wide logo: full width, centred vertically
        assertBounds(0, 340, 1920, 400, LogoLayout.computeBounds(1920, 1080, 960, 200, LogoSettings.DEFAULT));
        // Small images are scaled up to fit, as the old ImageView did
        assertBounds(0, 0, 1920, 1080, LogoLayout.computeBounds(1920, 1080, 16, 9, LogoSettings.DEFAULT));
    }

    @Test
    @DisplayName("Null settings mean the defaults")
    public void nullSettings() {
        assertEquals(LogoLayout.computeBounds(1920, 1080, 500, 500, LogoSettings.DEFAULT),
                LogoLayout.computeBounds(1920, 1080, 500, 500, null));
    }

    @Test
    @DisplayName("Size scales the box the logo has to fit in")
    public void size() {
        LogoSettings quarter = LogoSettings.DEFAULT.withSize(0.25);
        // Box is 480x270, square logo fits as 270x270, centred
        assertBounds(825, 405, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, quarter));
    }

    @Test
    @DisplayName("Corners and edges anchor the logo there")
    public void positions() {
        LogoSettings s = LogoSettings.DEFAULT.withSize(0.25);
        assertBounds(0, 0, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.TOP_LEFT)));
        assertBounds(825, 0, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.TOP_CENTER)));
        assertBounds(1650, 0, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.TOP_RIGHT)));
        assertBounds(0, 405, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.CENTER_LEFT)));
        assertBounds(1650, 405, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.CENTER_RIGHT)));
        assertBounds(0, 810, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.BOTTOM_LEFT)));
        assertBounds(825, 810, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.BOTTOM_CENTER)));
        assertBounds(1650, 810, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s.withPosition(LogoPosition.BOTTOM_RIGHT)));
    }

    @Test
    @DisplayName("Offsets shift the logo by a fraction of the screen size")
    public void offsets() {
        LogoSettings s = new LogoSettings(0.25, LogoPosition.TOP_RIGHT, -0.05, 0.1);
        // 5% of 1920 = 96 to the left, 10% of 1080 = 108 down
        assertBounds(1650 - 96, 108, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s));
        LogoSettings centred = new LogoSettings(0.25, LogoPosition.CENTER, 0.01, -0.02);
        assertBounds(825 + 19.2, 405 - 21.6, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, centred));
    }

    @Test
    @DisplayName("The logo is always kept on the screen")
    public void keptOnScreen() {
        LogoSettings s = new LogoSettings(0.25, LogoPosition.TOP_RIGHT, 0.3, -0.3);
        assertBounds(1650, 0, 270, 270, LogoLayout.computeBounds(1920, 1080, 500, 500, s));
        LogoSettings full = new LogoSettings(1, LogoPosition.CENTER, 0.5, 0.5);
        // Square logo filling the height can still move sideways, but not down
        assertBounds(840, 0, 1080, 1080, LogoLayout.computeBounds(1920, 1080, 500, 500, full));
    }

    @Test
    @DisplayName("Nothing to show gives empty bounds")
    public void empty() {
        assertBounds(0, 0, 0, 0, LogoLayout.computeBounds(0, 1080, 500, 500, LogoSettings.DEFAULT));
        assertBounds(0, 0, 0, 0, LogoLayout.computeBounds(1920, 1080, 0, 0, LogoSettings.DEFAULT));
        assertBounds(0, 0, 0, 0, LogoLayout.computeBounds(1920, 1080, Double.NaN, 5, LogoSettings.DEFAULT));
    }

    @Test
    @DisplayName("Settings are clamped to sensible values")
    public void clamping() {
        LogoSettings s = new LogoSettings(7, null, -3, Double.NaN);
        assertEquals(LogoSettings.MAX_SIZE, s.size(), EPS);
        assertEquals(LogoPosition.CENTER, s.position());
        assertEquals(LogoSettings.MIN_OFFSET, s.offsetX(), EPS);
        assertEquals(0, s.offsetY(), EPS);
        assertEquals(LogoSettings.MIN_SIZE, new LogoSettings(0, LogoPosition.CENTER, 0, 0).size(), EPS);
    }

    @Test
    @DisplayName("Stored positions are parsed leniently")
    public void parsePosition() {
        assertEquals(LogoPosition.BOTTOM_RIGHT, LogoPosition.parse("BOTTOM_RIGHT"));
        assertEquals(LogoPosition.BOTTOM_RIGHT, LogoPosition.parse(" bottom_right "));
        assertEquals(LogoPosition.CENTER, LogoPosition.parse(null));
        assertEquals(LogoPosition.CENTER, LogoPosition.parse("somewhere"));
    }
}
