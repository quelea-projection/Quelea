package org.quelea.data.pdf;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.testutil.FxTestSupport;

@DisplayName("PDF presentation")
public class PdfPresentationTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-pdf-test").toString());
        }
    }

    @Test
    public void rendersAllPagesInOrderWithTheirAspectRatios() throws Exception {
        FxTestSupport.assumeAvailable();
        Path path = directory.resolve("slides.pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage(new PDRectangle(288, 144)));
            document.addPage(new PDPage(new PDRectangle(144, 288)));
            document.save(path.toFile());
        }
        Pdf pdf = new PdfFactory().getPresentation(path.toFile());
        Assertions.assertNotNull(pdf);
        Assertions.assertEquals(2, pdf.getSlides().length);
        Assertions.assertEquals(2.0, pdf.getSlide(0).getImage().getWidth() / pdf.getSlide(0).getImage().getHeight(), 0.01);
        Assertions.assertEquals(0.5, pdf.getSlide(1).getImage().getWidth() / pdf.getSlide(1).getImage().getHeight(), 0.01);
        Assertions.assertFalse(pdf.getSlide(0).getThumbnail().isError());
    }

    @Test
    public void rejectsNonPdfFiles() throws Exception {
        Assertions.assertNull(new PdfFactory().getPresentation(directory.resolve("slides.txt").toFile()));
    }

    @Test
    public void extremePageAspectRatiosStillProduceAtLeastOnePixelPerDimension() throws Exception {
        FxTestSupport.assumeAvailable();
        Path path = directory.resolve("extreme.pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage(new PDRectangle(1, 4000)));
            document.addPage(new PDPage(new PDRectangle(4000, 1)));
            document.save(path.toFile());
        }
        Pdf pdf = new PdfFactory().getPresentation(path.toFile());
        for (PdfSlide slide : pdf.getSlides()) {
            Assertions.assertTrue(slide.getImage().getWidth() >= 1);
            Assertions.assertTrue(slide.getImage().getHeight() >= 1);
            Assertions.assertFalse(slide.getThumbnail().isError());
        }
    }
}
