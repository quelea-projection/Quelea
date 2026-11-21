package org.quelea.services.utils;

import javafx.geometry.Dimension2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SVG logos are rendered with Batik, sharply, at whatever size is asked for.
 * Doesn't need JavaFX.
 */
@DisplayName("SvgRasterizer")
public class SvgRasterizerTest {

    @TempDir
    Path dir;

    /**
     * A 2:1 image: left half red, right half blue.
     */
    private String twoColourSvg(String sizeAttributes) throws IOException {
        Path file = dir.resolve("logo" + System.nanoTime() + ".svg");
        Files.writeString(file, "<svg xmlns=\"http://www.w3.org/2000/svg\" " + sizeAttributes + ">"
                + "<rect x=\"0\" y=\"0\" width=\"100\" height=\"100\" fill=\"#ff0000\"/>"
                + "<rect x=\"100\" y=\"0\" width=\"100\" height=\"100\" fill=\"#0000ff\"/>"
                + "</svg>", StandardCharsets.UTF_8);
        return file.toUri().toString();
    }

    @Test
    @DisplayName("Recognises SVG file names")
    public void isSvg() {
        assertTrue(SvgRasterizer.isSvg("file:/x/logo.svg"));
        assertTrue(SvgRasterizer.isSvg("C:\\logos\\LOGO.SVG"));
        assertTrue(SvgRasterizer.isSvg("logo.svgz"));
        assertFalse(SvgRasterizer.isSvg("logo.png"));
        assertFalse(SvgRasterizer.isSvg(null));
    }

    @Test
    @DisplayName("Natural size comes from width/height, else the viewBox")
    public void intrinsicSize() throws IOException {
        assertEquals(new Dimension2D(200, 100),
                SvgRasterizer.getIntrinsicSize(twoColourSvg("viewBox=\"0 0 200 100\"")));
        assertEquals(new Dimension2D(200, 100),
                SvgRasterizer.getIntrinsicSize(twoColourSvg("width=\"100%\" height=\"100%\" viewBox=\"0 0 200 100\"")));
        assertEquals(new Dimension2D(50, 25),
                SvgRasterizer.getIntrinsicSize(twoColourSvg("width=\"50\" height=\"25\" viewBox=\"0 0 200 100\"")));
        assertEquals(new Dimension2D(200, 100),
                SvgRasterizer.getIntrinsicSize(twoColourSvg("width=\"200px\" height=\"100px\"")));
    }

    @Test
    @DisplayName("Renders at exactly the requested size, with a crisp edge")
    public void rendersSharplyAtTargetSize() throws IOException {
        // Natural size is tiny (20x10), but we want it big: it must not be a
        // blurry upscale of a small bitmap.
        String uri = twoColourSvg("width=\"20\" height=\"10\" viewBox=\"0 0 200 100\"");
        BufferedImage image = SvgRasterizer.renderToBufferedImage(uri, 1920, 960);
        assertEquals(1920, image.getWidth());
        assertEquals(960, image.getHeight());
        assertEquals(0xFFFF0000, image.getRGB(10, 480));
        assertEquals(0xFF0000FF, image.getRGB(1910, 480));
        // The edge between red and blue is right at the middle, with no blur
        assertEquals(0xFFFF0000, image.getRGB(958, 480));
        assertEquals(0xFF0000FF, image.getRGB(961, 480));
    }

    @Test
    @DisplayName("Keeps the aspect ratio, leaving the rest transparent")
    public void keepsAspectRatio() throws IOException {
        String uri = twoColourSvg("viewBox=\"0 0 200 100\"");
        BufferedImage image = SvgRasterizer.renderToBufferedImage(uri, 400, 400);
        // 400x200 drawing centred vertically in a 400x400 image
        assertEquals(0, image.getRGB(200, 10) >>> 24, "above the drawing should be transparent");
        assertEquals(0xFFFF0000, image.getRGB(100, 200));
        assertEquals(0xFF0000FF, image.getRGB(300, 200));
    }

    @Test
    @DisplayName("A broken SVG gives an IOException, not a crash")
    public void brokenSvg() throws IOException {
        Path file = dir.resolve("broken.svg");
        Files.writeString(file, "<svg xmlns=\"http://www.w3.org/2000/svg\"><rect", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> SvgRasterizer.getIntrinsicSize(file.toUri().toString()));
        assertThrows(IOException.class, () -> SvgRasterizer.renderToBufferedImage(file.toUri().toString(), 10, 10));
        assertThrows(IOException.class, () -> SvgRasterizer.getIntrinsicSize(dir.resolve("missing.svg").toUri().toString()));
    }
}
