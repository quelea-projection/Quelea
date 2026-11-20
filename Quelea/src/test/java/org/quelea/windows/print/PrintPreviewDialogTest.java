package org.quelea.windows.print;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.print.SchedulePDFPrinter;
import org.quelea.services.print.SchedulePrintContent;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.print.PrintPreviewDialog.Choice;

/**
 * Checks that the print preview generates and switches between the PDFs of
 * its options (without showing the window or printing anything.)
 */
@DisplayName("PrintPreviewDialogTest")
public class PrintPreviewDialogTest {

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-print-preview-test").toString());
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
    }

    private static <R> R onFx(Supplier<R> supplier) throws Exception {
        CompletableFuture<R> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(supplier.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }

    /**
     * Wait until the dialog's preview is ready (or failed, after the timeout.)
     */
    private static void awaitReady(PrintPreviewDialog<?> dialog) throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (dialog.readyProperty().get()) {
                ready.countDown();
            }
            dialog.readyProperty().addListener((obs, was, isReady) -> {
                if (isReady) {
                    ready.countDown();
                }
            });
        });
        Assertions.assertTrue(ready.await(60, TimeUnit.SECONDS), "The preview never became ready");
    }

    private static Schedule schedule() {
        Schedule schedule = new Schedule();
        schedule.add(new SongDisplayable.Builder("Amazing Grace", "John Newton")
                .lyrics("Verse 1\nG          C\nAmazing grace how sweet the sound").get());
        schedule.add(new SongDisplayable.Builder("Great Is Thy Faithfulness", "Thomas Chisholm")
                .lyrics("Verse 1\nGreat is thy faithfulness").get());
        return schedule;
    }

    @Test
    public void previewFollowsTheChoice() throws Exception {
        Schedule schedule = schedule();
        SchedulePDFPrinter printer = new SchedulePDFPrinter();
        List<Choice<SchedulePrintContent>> choices = List.of(
                new Choice<>(SchedulePrintContent.OVERVIEW, "overview",
                        pdf -> printer.printXML(schedule.getPrintXML(SchedulePrintContent.OVERVIEW), pdf)),
                new Choice<>(SchedulePrintContent.LYRICS, "lyrics",
                        pdf -> printer.printXML(schedule.getPrintXML(SchedulePrintContent.LYRICS), pdf)));
        PrintPreviewDialog<SchedulePrintContent> dialog = onFx(()
                -> new PrintPreviewDialog<>("Print", "Order of Service", choices, SchedulePrintContent.LYRICS));
        awaitReady(dialog);
        Assertions.assertEquals(3, (int) onFx(dialog::getReadyPageCount));
        File lyricsPdf = onFx(dialog::getReadyFile);
        Assertions.assertTrue(lyricsPdf.isFile());
        Assertions.assertTrue(lyricsPdf.getName().startsWith("Order of Service"), lyricsPdf.getName());

        boolean readyAfterSelect = onFx(() -> {
            dialog.select(0);
            return dialog.readyProperty().get();
        });
        Assertions.assertFalse(readyAfterSelect, "Not ready while the new preview is prepared");
        awaitReady(dialog);
        Assertions.assertEquals(1, (int) onFx(dialog::getReadyPageCount));
        Assertions.assertNotEquals(lyricsPdf, onFx(dialog::getReadyFile));
    }

    @Test
    public void failureKeepsPrintDisabled() throws Exception {
        List<Choice<String>> choices = List.of(new Choice<>("broken", "broken", pdf -> {
            throw new IOException("Expected test failure");
        }));
        PrintPreviewDialog<String> dialog = onFx(() -> new PrintPreviewDialog<>("Print", "x", choices, "broken"));
        // Give the background thread time to fail
        Thread.sleep(1000);
        Assertions.assertFalse(onFx(() -> dialog.readyProperty().get()));
        Assertions.assertNull(onFx(dialog::getReadyFile));
    }

    @Test
    public void filePrefixes() {
        Assertions.assertEquals("print", PrintPreviewDialog.toFilePrefix(null));
        Assertions.assertEquals("printab", PrintPreviewDialog.toFilePrefix("a/b"));
        Assertions.assertEquals("Amazing Grace ", PrintPreviewDialog.toFilePrefix("Amazing Grace"));
        Assertions.assertEquals("Hello world ", PrintPreviewDialog.toFilePrefix("Hello: world?"));
    }
}
