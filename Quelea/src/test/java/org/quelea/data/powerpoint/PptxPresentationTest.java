package org.quelea.data.powerpoint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.testutil.FxTestSupport;

@DisplayName("PowerPoint previews")
public class PptxPresentationTest {
    @TempDir Path directory;

    @Test
    public void rendersBothSlidesOfARealPresentation() throws Exception {
        FxTestSupport.assumeAvailable();
        Path file = directory.resolve("slides.pptx");
        try (XMLSlideShow presentation = new XMLSlideShow()) {
            presentation.createSlide();
            presentation.createSlide();
            try (var out = Files.newOutputStream(file)) {
                presentation.write(out);
            }
        }
        Presentation presentation = new PresentationFactory().getPresentation(file.toFile());
        Assertions.assertEquals(2, presentation.getSlides().length);
        Assertions.assertEquals(1080, presentation.getSlide(0).getImage().getHeight());
        Assertions.assertEquals(1080, presentation.getSlide(1).getImage().getHeight());
    }

    @Test
    @Disabled("PowerPoint previews reuse scale factors from an earlier presentation")
    public void previewOfNative1080HeightDoesNotReuseScaleFromPreviousPresentation() throws Exception {
        // A previously opened file can make a later native-height presentation
        // render with the wrong size or crop. Track scale per presentation (and
        // initialize it to 1 when no scaling is needed), rather than sharing
        // mutable static factors between unrelated files.
        FxTestSupport.assumeAvailable();
        Path smaller = createPresentation("small.pptx", 960, 540);
        Path nativeSize = createPresentation("native.pptx", 1920, 1080);
        new PPTXPresentation(smaller.toString());
        var image = new PPTXPresentation(nativeSize.toString()).getSlide(0).getImage();
        var inside = image.getPixelReader().getColor((int) (image.getWidth() * 0.1), 100);
        var outside = image.getPixelReader().getColor((int) (image.getWidth() * 0.4), 100);
        Assertions.assertTrue(inside.getRed() > 0.8 && inside.getGreen() < 0.2, "The rectangle should be red");
        Assertions.assertFalse(outside.getRed() > 0.8 && outside.getGreen() < 0.2, "The rectangle must not extend beyond its original width");
    }

    private Path createPresentation(String name, int width, int height) throws Exception {
        Path file = directory.resolve(name);
        try (XMLSlideShow slideshow = new XMLSlideShow()) {
            slideshow.setPageSize(new Dimension(width, height));
            var shape = slideshow.createSlide().createAutoShape();
            shape.setShapeType(ShapeType.RECT);
            shape.setAnchor(new Rectangle2D.Double(0, 0, width / 4.0, height / 4.0));
            shape.setFillColor(Color.RED);
            try (var out = Files.newOutputStream(file)) {
                slideshow.write(out);
            }
        }
        return file;
    }
}
