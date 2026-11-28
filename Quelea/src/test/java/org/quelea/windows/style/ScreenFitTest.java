package org.quelea.windows.style;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * Windows that are too big for the screen are shrunk and moved inside it,
 * e.g. the song editor on a small laptop screen.
 */
@DisplayName("ScreenFitTest")
public class ScreenFitTest {

    private static DisplayStage previousProjection;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-screen-fit-test").toString());
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
        Platform.setImplicitExit(false);
        onFx(() -> {
            previousProjection = QueleaApp.get().getProjectionWindow();
            QueleaApp.get().setProjectionWindow(new DisplayStage(new BoundingBox(0, 0, 1280, 720), false));
            return null;
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        onFx(() -> {
            QueleaApp.get().setProjectionWindow(previousProjection);
            return null;
        });
    }

    @Test
    public void songWindowIsKeptInsideTheScreen() throws Exception {
        SongEntryWindow window = onFx(() -> {
            SongEntryWindow w = new SongEntryWindow();
            w.resetNewSong();
            w.show();
            return w;
        });
        Thread.sleep(300);
        String problem = onFx(() -> {
            Rectangle2D screen = ScreenFit.getScreenBounds(window);
            window.setHeight(screen.getHeight() + 400);
            window.setWidth(screen.getWidth() + 400);
            window.setY(screen.getMinY() - 200);
            window.hide();
            window.show(); // fitted again when shown
            String ret = null;
            if (window.getHeight() > screen.getHeight() + 0.5 || window.getWidth() > screen.getWidth() + 0.5) {
                ret = "window is " + window.getWidth() + "x" + window.getHeight() + " on a " + screen;
            } else if (window.getY() < screen.getMinY() - 0.5 || window.getX() < screen.getMinX() - 0.5
                    || window.getY() + window.getHeight() > screen.getMaxY() + 0.5) {
                ret = "window at " + window.getX() + "," + window.getY() + " is outside " + screen;
            }
            System.out.println("Song window content min size: " + window.getScene().getRoot().minWidth(-1)
                    + "x" + window.getScene().getRoot().minHeight(-1));
            window.hide();
            return ret;
        });
        Assertions.assertNull(problem, problem);
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
}
