package org.quelea.windows.main;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

class PictureInPictureWebTest {
    @Test
    void hidingAVisibleProjectionKeepsItsWebPageLiveAndRestoresIt() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1];
        Stage[] projection = new Stage[1];
        WebView[] web = new WebView[1];
        StackPane[] projectionRoot = new StackPane[1];
        PictureInPictureWindow[] pip = new PictureInPictureWindow[1];
        boolean[] projectionShownBeforeWebRestore = {false};
        try {
            onFx(() -> {
                web[0] = new WebView();
                projectionRoot[0] = new StackPane(web[0]);
                projectionRoot[0].setStyle("-fx-background-color: transparent;");
                projection[0] = new Stage();
                projection[0].setScene(new Scene(projectionRoot[0], 400, 225));
                projection[0].show();
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 400, 225));
                main[0].show();
                pip[0] = new PictureInPictureWindow(main[0], () -> {
                    if (!projection[0].isShowing() && !pip[0].containsWebView(web[0])) {
                        pip[0].setWebView(web[0]);
                    }
                    return projectionRoot[0];
                }, () -> null, () -> {
                    if (pip[0].containsWebView(web[0])) {
                        projectionShownBeforeWebRestore[0] = projection[0].isShowing();
                        pip[0].setWebView(null);
                        projectionRoot[0].getChildren().add(web[0]);
                    }
                }, () -> projection[0]);
                web[0].getEngine().loadContent("<html><body style='margin:0;background:red'></body></html>");
                return null;
            });
            waitUntil(() -> web[0].getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED);
            waitUntil(() -> matches(web[0], Color.RED));
            onFx(() -> {
                pip[0].toggle();
                assertFalse(projection[0].isShowing());
                return null;
            });
            waitUntil(() -> pip[0].containsWebView(web[0]));
            onFx(() -> { web[0].getEngine().executeScript("document.body.style.background = 'blue'"); return null; });
            waitUntil(() -> matches(web[0], Color.BLUE));
            onFx(() -> {
                pip[0].toggle();
                assertTrue(projection[0].isShowing());
                assertTrue(projectionShownBeforeWebRestore[0]);
                assertSame(projectionRoot[0], web[0].getParent());
                return null;
            });
        } finally {
            onFx(() -> {
                if (main[0] != null) main[0].hide();
                if (projection[0] != null) projection[0].hide();
                return null;
            });
        }
    }

    @Test
    void temporarilyHostsTheSameWebViewWhileItsMainWindowIsMinimised() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1];
        WebView[] web = new WebView[1];
        PictureInPictureWindow[] pip = new PictureInPictureWindow[1];
        StackPane[] original = new StackPane[1];
        try {
            onFx(() -> {
                web[0] = new WebView();
                original[0] = new StackPane(web[0]);
                main[0] = new Stage();
                main[0].setScene(new Scene(original[0], 400, 225));
                main[0].show();
                StackPane projectionOverlays = new StackPane();
                projectionOverlays.setStyle("-fx-background-color: transparent;");
                new Scene(projectionOverlays, 400, 225);
                projectionOverlays.resize(400, 225);
                projectionOverlays.layout();
                pip[0] = new PictureInPictureWindow(main[0], () -> projectionOverlays, () -> null,
                        () -> {
                            if (pip[0].containsWebView(web[0])) {
                                pip[0].setWebView(null);
                                original[0].getChildren().add(web[0]);
                            }
                        }, () -> null);
                web[0].getEngine().loadContent("<html><body style='margin:0;background:red'></body></html>");
                return null;
            });
            waitUntil(() -> web[0].getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED);
            waitUntil(() -> matches(web[0], Color.RED));
            onFx(() -> {
                pip[0].toggle();
                pip[0].setWebView(web[0]);
                assertTrue(main[0].isIconified());
                assertTrue(pip[0].containsWebView(web[0]));
                web[0].getEngine().executeScript("document.body.style.background = 'blue'");
                return null;
            });
            waitUntil(() -> matches(web[0], Color.BLUE));
            onFx(() -> {
                PictureInPictureLogoTest.hover((Stage) web[0].getScene().getWindow(), true);
                return null;
            });
            waitUntil(() -> {
                var image = web[0].getScene().getRoot().snapshot(null, null);
                return Color.BLUE.equals(image.getPixelReader().getColor(
                        (int) image.getWidth() / 2, (int) image.getHeight() / 2));
            });
            Thread.sleep(500); // Minimise/restore requests are asynchronous on Linux.
            onFx(() -> {
                pip[0].toggle();
                assertSame(original[0], web[0].getParent());
                assertFalse(pip[0].containsWebView(web[0]));
                return null;
            });
            waitUntil(() -> !main[0].isIconified());
        } finally {
            onFx(() -> {
                if (main[0] != null) main[0].hide();
                return null;
            });
        }
    }

    private static boolean matches(WebView web, Color color) {
        var image = web.snapshot(null, null);
        return color.equals(image.getPixelReader().getColor(
                (int) image.getWidth() / 2, (int) image.getHeight() / 2));
    }

    private static void waitUntil(java.util.function.BooleanSupplier condition) throws Exception {
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end) {
            if (onFx(condition::getAsBoolean)) return;
            Thread.sleep(50);
        }
        fail("Web page did not reach the expected state");
    }
}
