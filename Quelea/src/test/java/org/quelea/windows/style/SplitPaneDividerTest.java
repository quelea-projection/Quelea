package org.quelea.windows.style;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import java.util.function.Supplier;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;

/**
 * The split pane dividers must stay thick enough to grab with the mouse once
 * Quelea's stylesheets are applied (a 0px divider can't be dragged at all.)
 */
@DisplayName("SplitPaneDividerTest")
public class SplitPaneDividerTest {

    private static final double MIN_GRAB_THICKNESS = 5;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-divider-test").toString());
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
        // Keep the toolkit running when the first test hides its window
        Platform.setImplicitExit(false);
    }

    @Test
    public void verticalDividerCanBeGrabbed() throws Exception {
        assertGrabbable(Orientation.VERTICAL);
    }

    @Test
    public void horizontalDividerCanBeGrabbed() throws Exception {
        assertGrabbable(Orientation.HORIZONTAL);
    }

    private static void assertGrabbable(Orientation orientation) throws Exception {
        SplitPane split = onFx(() -> {
            SplitPane pane = new SplitPane(new StackPane(), new StackPane());
            pane.setOrientation(orientation);
            Scene scene = new Scene(new BorderPane(pane), 400, 600);
            StyleManager.apply(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            return pane;
        });
        Thread.sleep(500); // let a layout pulse run
        double thickness = onFx(() -> {
            Node divider = split.lookup(".split-pane-divider");
            Bounds bounds = divider.getLayoutBounds();
            split.getScene().getWindow().hide();
            return orientation == Orientation.VERTICAL ? bounds.getHeight() : bounds.getWidth();
        });
        Assertions.assertTrue(thickness >= MIN_GRAB_THICKNESS,
                orientation + " divider is only " + thickness + "px thick");
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
