package org.quelea.windows.main;

import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

class PictureInPictureCompositionTest {
    @Test
    void presentationSlideIsBehindLiveProjectionOverlays() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1];
        Stage[] floating = new Stage[1];
        Rectangle[] blackout = new Rectangle[1];
        try {
            onFx(() -> {
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 400, 300));
                main[0].show();
                blackout[0] = new Rectangle(640, 360, Color.BLACK);
                blackout[0].setOpacity(0);
                StackPane projection = new StackPane(blackout[0]);
                projection.setStyle("-fx-background-color: transparent;");
                new Scene(projection, 640, 360);
                projection.resize(640, 360);
                projection.layout();
                WritableImage slide = new WritableImage(8, 8);
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) slide.getPixelWriter().setColor(x, y, Color.LIME);
                }
                PictureInPictureWindow pip = new PictureInPictureWindow(main[0], () -> projection, () -> slide, () -> { }, () -> null);
                pip.toggle();
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != main[0])
                        .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE
                                .getLabel("picture.in.picture.title").equals(w.getTitle())).findFirst().orElseThrow();
                PictureInPictureLogoTest.hover(floating[0], true);
                return null;
            });
            awaitVisibleColor(floating[0], Color.LIME);
            onFx(() -> { blackout[0].setOpacity(1); return null; });
            awaitVisibleColor(floating[0], Color.BLACK);
        } finally {
            onFx(() -> { if (main[0] != null) main[0].hide(); return null; });
        }
    }

    private static void awaitVisibleColor(Stage stage, Color expected) throws Exception {
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end) {
            boolean matches = onFx(() -> {
                var root = stage.getScene().getRoot();
                var image = root.snapshot(null, null);
                return expected.equals(image.getPixelReader().getColor(
                        (int) image.getWidth() / 2, (int) image.getHeight() / 2));
            });
            if (matches) return;
            Thread.sleep(50);
        }
        String observed = onFx(() -> {
            var root = stage.getScene().getRoot();
            var image = root.snapshot(null, null);
            return "pixel=" + image.getPixelReader().getColor((int) image.getWidth() / 2, (int) image.getHeight() / 2)
                    + ", root=" + root.getBoundsInLocal() + ", showing=" + stage.isShowing() + ", nodes=" + describe(root);
        });
        fail("The floating window did not paint " + expected + "; " + observed);
    }

    private static String describe(javafx.scene.Node node) {
        String result = node.getClass().getSimpleName() + "=" + node.getBoundsInParent();
        if (node instanceof javafx.scene.Parent parent) {
            for (var child : parent.getChildrenUnmodifiable()) result += " [" + describe(child) + "]";
        }
        return result;
    }
}
