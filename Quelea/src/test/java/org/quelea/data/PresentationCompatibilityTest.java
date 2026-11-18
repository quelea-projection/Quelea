package org.quelea.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.hslf.usermodel.HSLFAutoShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.powerpoint.PPTPresentation;
import org.quelea.data.powerpoint.PPTXPresentation;
import org.quelea.data.powerpoint.Presentation;
import org.quelea.services.print.PdfPageRenderer;

/** Smoke tests for reading and rendering files created by presentation libraries. */
class PresentationCompatibilityTest {
    @TempDir
    Path tempDir;

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
            Assumptions.assumeTrue(started.await(15, TimeUnit.SECONDS), "JavaFX toolkit did not start");
        } catch (IllegalStateException ex) {
            // Already started by another test.
            Platform.setImplicitExit(false);
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
    }

    @Test
    void rendersPptxSlidesInOrder() throws Exception {
        Path file = tempDir.resolve("slides.pptx");
        try (XMLSlideShow show = new XMLSlideShow(); OutputStream out = Files.newOutputStream(file)) {
            show.setPageSize(new Dimension(720, 540));
            for (Color color : new Color[]{Color.RED, Color.BLUE}) {
                XSLFSlide slide = show.createSlide();
                XSLFAutoShape shape = slide.createAutoShape();
                shape.setShapeType(ShapeType.RECT);
                shape.setAnchor(new Rectangle2D.Double(0, 0, 720, 540));
                shape.setFillColor(color);
                shape.setLineColor(color);
            }
            show.write(out);
        }
        assertSlides(new PPTXPresentation(file.toString()));
    }

    @Test
    void rendersPptSlidesInOrder() throws Exception {
        Path file = tempDir.resolve("slides.ppt");
        try (HSLFSlideShow show = new HSLFSlideShow(); OutputStream out = Files.newOutputStream(file)) {
            show.setPageSize(new Dimension(720, 540));
            for (Color color : new Color[]{Color.RED, Color.BLUE}) {
                HSLFSlide slide = show.createSlide();
                HSLFAutoShape shape = slide.createAutoShape();
                shape.setShapeType(ShapeType.RECT);
                shape.setAnchor(new Rectangle2D.Double(0, 0, 720, 540));
                shape.setFillColor(color);
                shape.setLineColor(color);
            }
            show.write(out);
        }
        assertSlides(new PPTPresentation(file.toString()));
    }

    private static void assertSlides(Presentation presentation) {
        Assertions.assertEquals(2, presentation.getSlides().length);
        assertCenterColor(presentation.getSlide(0).getImage(), javafx.scene.paint.Color.RED);
        assertCenterColor(presentation.getSlide(1).getImage(), javafx.scene.paint.Color.BLUE);
    }

    private static void assertCenterColor(Image image, javafx.scene.paint.Color expected) {
        Assertions.assertTrue(image.getWidth() > 0);
        Assertions.assertTrue(image.getHeight() > 0);
        javafx.scene.paint.Color actual = image.getPixelReader().getColor((int) image.getWidth() / 2, (int) image.getHeight() / 2);
        Assertions.assertEquals(expected.getRed(), actual.getRed(), 0.05);
        Assertions.assertEquals(expected.getGreen(), actual.getGreen(), 0.05);
        Assertions.assertEquals(expected.getBlue(), actual.getBlue(), 0.05);
        Assertions.assertEquals(1.0, actual.getOpacity(), 0.05);
    }

    @Test
    void rendersPdfPagesInOrder() throws Exception {
        Path file = tempDir.resolve("pages.pdf");
        try (PDDocument document = new PDDocument()) {
            for (Color color : new Color[]{Color.RED, Color.BLUE}) {
                PDPage page = new PDPage(new PDRectangle(100, 100));
                document.addPage(page);
                try (PDPageContentStream contents = new PDPageContentStream(document, page)) {
                    contents.setNonStrokingColor(color);
                    contents.addRect(0, 0, 100, 100);
                    contents.fill();
                }
            }
            document.save(file.toFile());
        }
        PdfPageRenderer.Pages pages = PdfPageRenderer.render(file.toFile(), 72);
        Assertions.assertEquals(2, pages.pageCount());
        Assertions.assertEquals(2, pages.pages().size());
        Assertions.assertEquals(Color.RED.getRGB(), pages.pages().get(0).getRGB(50, 50));
        Assertions.assertEquals(Color.BLUE.getRGB(), pages.pages().get(1).getRGB(50, 50));
    }
}
