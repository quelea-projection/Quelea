package org.quelea.windows.presentation.powerpoint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Drives the real Microsoft PowerPoint through the whole chain (service,
 * bridge, AppleScript). It opens a full screen slide show, so it only runs
 * when asked for: {@code ./gradlew test -Dquelea.test.powerpoint=true}.
 * Only commands that need no Accessibility permission are used.
 */
@DisplayName("PowerPoint (live, macOS)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PowerPointLiveTest {

    private static PowerPointService service;
    private static File deck;

    @BeforeAll
    public static void setUp() throws Exception {
        assumeTrue(Boolean.getBoolean("quelea.test.powerpoint"), "set -Dquelea.test.powerpoint=true to run");
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("mac"), "macOS only");
        assumeTrue(MacPowerPointBackend.isInstalled(), "needs Microsoft PowerPoint");
        File dir = Files.createTempDirectory("quelea ppt test å").toFile();
        deck = new File(dir, "Live \"test\" deck.pptx");
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.setPageSize(new Dimension(960, 540));
            for (int i = 1; i <= 4; i++) {
                XSLFSlide slide = ppt.createSlide();
                slide.getBackground().setFillColor(new Color(30 * i, 80, 160));
                XSLFTextBox box = slide.createTextBox();
                box.setAnchor(new Rectangle(100, 150, 760, 200));
                box.addNewTextParagraph().addNewTextRun().setText("Slide " + i);
            }
            try (FileOutputStream out = new FileOutputStream(deck)) {
                ppt.write(out);
            }
        }
        service = new PowerPointService(MacPowerPointBackend.create(), Runnable::run);
        service.setErrorHandler(null);
    }

    @AfterAll
    public static void tearDown() {
        if (service != null) {
            service.shutdown();
        }
    }

    private static SlideState await(java.util.concurrent.CompletableFuture<SlideState> f) throws Exception {
        return f.get(90, TimeUnit.SECONDS);
    }

    @Test
    @Order(1)
    @DisplayName("Opens the show and steps through it")
    public void slideShow() throws Exception {
        javafx.geometry.Rectangle2D main = new javafx.geometry.Rectangle2D(0, 0, 1000, 700);
        SlideState s = await(service.open(deck, main)); // a screen area, as Quelea passes it
        assertEquals(new SlideState(1, 4, SlideState.Mode.RUNNING), s);
        assertEquals(2, await(service.next()).getSlide());
        assertEquals(3, await(service.next()).getSlide());
        assertEquals(2, await(service.previous()).getSlide());
        assertEquals(4, await(service.gotoSlide(4)).getSlide()); // last: no typing needed
        assertEquals(3, await(service.gotoSlide(3)).getSlide()); // one back
        assertEquals(1, await(service.gotoSlide(1)).getSlide()); // first
        assertEquals(3, await(service.gotoSlide(3)).getSlide()); // arbitrary: stepped there
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            await(service.refresh());
        }
        long perCall = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) / 10;
        System.out.println("PowerPoint state round trip: " + perCall + " ms");
        assertTrue(perCall < 200, "the bridge should answer quickly, took " + perCall + " ms");
        SlideState closed = await(service.close());
        assertTrue(!closed.isShowing(), "show should be closed: " + closed);
    }

    @Test
    @Order(2)
    @DisplayName("Exports a PDF of the presentation")
    public void exportPdf() throws Exception {
        File pdf = File.createTempFile("export", ".pdf");
        pdf.delete();
        pdf.deleteOnExit();
        int slides = service.exportPdf(deck, pdf).get(3, TimeUnit.MINUTES);
        assertEquals(4, slides);
        try (PDDocument doc = PDDocument.load(pdf)) {
            assertEquals(4, doc.getNumberOfPages());
        }
    }
}
