package org.quelea.windows.main;

import javafx.geometry.BoundingBox;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

/** Exercises the actual projection canvas, including the operator's blackout. */
class PictureInPictureCanvasTest {
    private DisplayStage projection;
    private Stage floating;
    private PictureInPictureMirror mirror;
    private ImageView frame;

    @BeforeEach
    void setUp() throws Exception {
        FxTestSupport.assumeAvailable();
        onFx(() -> {
            // Projector disabled: this stage has never been shown.
            projection = new DisplayStage(new BoundingBox(0, 0, 640, 360), false);
            assertTrue(MainWindow.prepareHiddenPictureInPictureSource(projection));
            assertFalse(projection.isShowing());
            DisplayCanvas canvas = projection.getCanvas();
            canvas.clearNonPermanentChildren();
            Rectangle red = new Rectangle();
            red.setFill(Color.RED);
            red.widthProperty().bind(canvas.widthProperty());
            red.heightProperty().bind(canvas.heightProperty());
            canvas.getChildren().add(0, red);
            frame = new ImageView();
            floating = new Stage();
            floating.setScene(new Scene(new StackPane(frame), 320, 180));
            floating.show();
            mirror = new PictureInPictureMirror(() -> projection.getScene().getRoot(), frame);
            mirror.start();
            return null;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        onFx(() -> {
            mirror.stop();
            floating.hide();
            projection.hide();
            return null;
        });
    }

    private void awaitColor(Color expected) throws Exception {
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end) {
            boolean found = onFx(() -> frame.getImage() != null
                    && expected.equals(frame.getImage().getPixelReader().getColor(240, 135)));
            if (found) return;
            Thread.sleep(50);
        }
        fail("The live preview did not show " + expected);
    }

    @Test
    void mirrorsBlackoutFromANeverShownProjection() throws Exception {
        awaitColor(Color.RED);
        onFx(() -> { projection.getCanvas().setBlacked(true); return null; });
        awaitColor(Color.BLACK);
        onFx(() -> { projection.getCanvas().setBlacked(false); return null; });
        awaitColor(Color.RED);
        onFx(() -> {
            assertFalse(projection.isShowing(), "opening a preview must not show the projector");
            assertFalse(MainWindow.prepareHiddenPictureInPictureSource(projection), "layout is only needed once");
            return null;
        });
    }

    @Test
    void mirrorsTheProjectorTestPatternAndReturnsToTheLiveContent() throws Exception {
        awaitColor(Color.RED);
        onFx(() -> {
            var pattern = new javafx.scene.image.WritableImage(8, 8);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) pattern.getPixelWriter().setColor(x, y, Color.LIME);
            }
            projection.setTestImage(pattern, false);
            return null;
        });
        awaitColor(Color.LIME);
        onFx(() -> { projection.setTestImage(null, false); return null; });
        awaitColor(Color.RED);
    }
}
