package org.quelea.windows.image;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.imagegroup.ImageGroupDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.pdf.PdfDrawer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pictures (images, PDF pages, presentation slides) that don't have the
 * projector's aspect ratio must be letterboxed in black on the projector, not
 * left see-through.
 */
@DisplayName("Picture letterboxing")
public class PictureLetterboxTest {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;

    private DisplayStage previousProjection;
    private DisplayStage previousStage;
    private DisplayStage projection;

    @BeforeAll
    public static void startFx() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-letterbox-test").toString());
        }
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
    }

    @BeforeEach
    public void setUp() throws Exception {
        onFx(() -> {
            previousProjection = QueleaApp.get().getProjectionWindow();
            previousStage = QueleaApp.get().getStageWindow();
            projection = new DisplayStage(new BoundingBox(0, 0, WIDTH, HEIGHT), false);
            QueleaApp.get().setProjectionWindow(projection);
            if (previousStage == null) {
                QueleaApp.get().setStageWindow(new DisplayStage(new BoundingBox(0, 0, WIDTH, HEIGHT), true));
            }
            // Lay the projector canvas out at a known size, off screen.
            DisplayCanvas canvas = projection.getCanvas();
            ((StackPane) canvas.getParent()).getChildren().remove(canvas);
            Scene scene = new Scene(new StackPane(canvas), WIDTH, HEIGHT);
            scene.setFill(null);
            layout(canvas);
            return null;
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        onFx(() -> {
            QueleaApp.get().setProjectionWindow(previousProjection);
            QueleaApp.get().setStageWindow(previousStage);
            return null;
        });
    }

    @Test
    @DisplayName("A square image on a 16:9 projector is centred on black")
    public void imageIsLetterboxedInBlack() throws Exception {
        assertLetterboxedInBlack(ImageDrawer::new);
    }

    @Test
    @DisplayName("A square PDF page on a 16:9 projector is centred on black")
    public void pdfPageIsLetterboxedInBlack() throws Exception {
        assertLetterboxedInBlack(PdfDrawer::new);
    }

    @Test
    @DisplayName("A square image group slide on a 16:9 projector is centred on black")
    public void imageGroupSlideIsLetterboxedInBlack() throws Exception {
        assertLetterboxedInBlack(ImageGroupDrawer::new);
    }

    @Test
    @DisplayName("The black goes away when the picture is cleared, e.g. for a song")
    public void blackGoesAwayWhenCleared() throws Exception {
        WritableImage snapshot = onFx(() -> {
            DisplayCanvas canvas = projection.getCanvas();
            ImageDrawer drawer = new ImageDrawer();
            drawer.setCanvas(canvas);
            drawer.draw(new ImageDisplayable(Utils.getImageFromColour(Color.WHITE)));
            drawer.clear();
            return snapshot(canvas);
        });
        assertEquals(0, snapshot.getPixelReader().getColor(5, HEIGHT / 2).getOpacity(), 0.01,
                "Without a picture the projector canvas should be transparent again");
    }

    @Test
    @DisplayName("Previews keep the preview backdrop instead of black")
    public void previewsKeepTheirBackdrop() throws Exception {
        StackPane box = new StackPane();
        DisplayableDrawer.setPictureBackdrop(box, true);
        assertTrue(box.getStyleClass().contains(DisplayableDrawer.PREVIEW_BACKDROP_STYLE_CLASS));
        assertTrue(box.getBackground() == null);
        DisplayableDrawer.setPictureBackdrop(box, false);
        assertFalse(box.getStyleClass().contains(DisplayableDrawer.PREVIEW_BACKDROP_STYLE_CLASS));
        assertEquals(Color.BLACK, box.getBackground().getFills().get(0).getFill());
    }

    private void assertLetterboxedInBlack(Supplier<DisplayableDrawer> drawerFactory) throws Exception {
        WritableImage snapshot = onFx(() -> {
            DisplayCanvas canvas = projection.getCanvas();
            DisplayableDrawer drawer = drawerFactory.get();
            drawer.setCanvas(canvas);
            drawer.draw(new ImageDisplayable(squareWhiteImage()));
            return snapshot(canvas);
        });
        PixelReader pixels = snapshot.getPixelReader();
        // The square picture is 180x180, centred: x 70..250.
        assertColour(Color.BLACK, pixels.getColor(5, HEIGHT / 2), "left bar");
        assertColour(Color.BLACK, pixels.getColor(WIDTH - 5, HEIGHT / 2), "right bar");
        assertColour(Color.WHITE, pixels.getColor(WIDTH / 2, HEIGHT / 2), "centre");
        assertColour(Color.WHITE, pixels.getColor(75, HEIGHT / 2), "left edge of the picture");
        assertColour(Color.WHITE, pixels.getColor(245, HEIGHT / 2), "right edge of the picture");
        assertColour(Color.BLACK, pixels.getColor(65, HEIGHT / 2), "just left of the picture");
        assertColour(Color.BLACK, pixels.getColor(255, HEIGHT / 2), "just right of the picture");
    }

    private static Image squareWhiteImage() {
        WritableImage image = new WritableImage(100, 100);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                image.getPixelWriter().setColor(x, y, Color.WHITE);
            }
        }
        return image;
    }

    private static void assertColour(Color expected, Color actual, String where) {
        assertEquals(1, actual.getOpacity(), 0.01, where + " should be opaque, was " + actual);
        assertEquals(expected.getRed(), actual.getRed(), 0.05, where + " was " + actual);
        assertEquals(expected.getGreen(), actual.getGreen(), 0.05, where + " was " + actual);
        assertEquals(expected.getBlue(), actual.getBlue(), 0.05, where + " was " + actual);
    }

    private static WritableImage snapshot(Node node) {
        layout(node);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return node.snapshot(params, null);
    }

    private static void layout(Node node) {
        Node root = node.getScene().getRoot();
        root.applyCss();
        ((StackPane) root).layout();
    }

    private static <T> T onFx(java.util.concurrent.Callable<T> callable) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(callable.call());
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }
}
