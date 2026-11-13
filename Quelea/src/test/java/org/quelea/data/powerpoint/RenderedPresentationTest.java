package org.quelea.data.powerpoint;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Slides from a PowerPoint-made PDF, and the cache they are kept in.
 */
@DisplayName("RenderedPresentation")
public class RenderedPresentationTest {

    private static boolean fx;

    @BeforeAll
    public static void setUp() throws Exception {
        File home = Files.createTempDirectory("quelea-test-home").toFile();
        home.deleteOnExit();
        QueleaProperties.init(home.getAbsolutePath());
        try {
            CompletableFuture<Void> started = new CompletableFuture<>();
            Platform.startup(() -> started.complete(null));
            started.get(30, TimeUnit.SECONDS);
            fx = true;
        } catch (IllegalStateException alreadyStarted) {
            fx = true;
        } catch (Throwable ex) {
            fx = false;
        }
        if (fx) {
            Platform.setImplicitExit(false);
        }
    }

    @Test
    @DisplayName("Every page becomes a slide 1080 pixels high, whatever the page size")
    public void rendersPages() throws Exception {
        assumeTrue(fx, "needs JavaFX");
        File pdf = File.createTempFile("deck", ".pdf");
        pdf.deleteOnExit();
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage(new PDRectangle(960, 540)));   // 16:9
            doc.addPage(new PDPage(new PDRectangle(720, 540)));   // 4:3
            doc.addPage(new PDPage(new PDRectangle(1920, 1080))); // 16:9, large
            doc.save(pdf);
        }
        RenderedPresentation p = new RenderedPresentation(pdf);
        assertEquals(3, p.getSlides().length);
        assertEquals(1080, (int) p.getSlide(0).getImage().getHeight());
        assertEquals(1920, (int) p.getSlide(0).getImage().getWidth());
        assertEquals(1440, (int) p.getSlide(1).getImage().getWidth());
        assertEquals(1080, (int) p.getSlide(2).getImage().getHeight());
    }

    @Test
    @DisplayName("The cache is keyed on the file contents, not its name or place")
    public void cacheKey() throws Exception {
        File a = File.createTempFile("deck-a", ".pptx");
        File b = File.createTempFile("deck-b other name", ".pptx");
        File c = File.createTempFile("deck-c", ".pptx");
        for (File f : new File[]{a, b, c}) {
            f.deleteOnExit();
        }
        Files.writeString(a.toPath(), "same contents");
        Files.writeString(b.toPath(), "same contents");
        Files.writeString(c.toPath(), "other contents");
        assertEquals(RenderedPresentation.cacheFile(a), RenderedPresentation.cacheFile(b));
        assertNotEquals(RenderedPresentation.cacheFile(a), RenderedPresentation.cacheFile(c));
        assertEquals(new File(QueleaProperties.get().getQueleaUserHome(), "powerpoint-cache"),
                RenderedPresentation.cacheFile(a).getParentFile());
    }
}
