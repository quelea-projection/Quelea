package org.quelea.services.notice;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.widgets.DisplayPreview;

/**
 * A notice shown on the projection screen is shown on the live preview in
 * the main window too, at the same time and scaled to the preview.
 */
@DisplayName("NoticeMirrorTest")
public class NoticeMirrorTest {

    private static final double PROJECTION_WIDTH = 1280;
    private static DisplayStage previousProjection;
    private static DisplayStage projection;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-notice-mirror-test").toString());
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
            projection = new DisplayStage(new BoundingBox(0, 0, PROJECTION_WIDTH, 720), false);
            QueleaApp.get().setProjectionWindow(projection);
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
    public void previewShowsTheNoticeScaledAndInStep() throws Exception {
        DisplayCanvas[] canvases = onFx(() -> {
            DisplayCanvas live = new DisplayCanvas(false, null, DisplayCanvas.Priority.MID);
            Stage liveStage = new Stage();
            liveStage.setScene(new Scene(new StackPane(live), PROJECTION_WIDTH, 720, Color.BLACK));
            liveStage.show();

            DisplayCanvas preview = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            Stage previewStage = new Stage();
            previewStage.setScene(new Scene(new DisplayPreview(preview), 500, 320));
            previewStage.show();

            live.getNoticeDrawer().addMirror(preview.getNoticeDrawer());
            live.getNoticeDrawer().addNotice(new Notice("The car park is full", 1,
                    new SerializableColor(Color.WHITE), new SerializableFont(Font.font("Arial", 50))));
            return new DisplayCanvas[]{live, preview};
        });
        Thread.sleep(2500);
        double[] result = onFx(() -> {
            Text liveText = findText(canvases[0].getNoticeDrawer().getOverlay());
            Text previewText = findText(canvases[1].getNoticeDrawer().getOverlay());
            Assertions.assertNotNull(liveText, "the notice should be on the projection screen");
            Assertions.assertNotNull(previewText, "the notice should be on the live preview too");
            Assertions.assertEquals(liveText.getText(), previewText.getText());
            double liveX = ((HBox) liveText.getParent()).getTranslateX();
            double previewX = ((HBox) previewText.getParent()).getTranslateX();
            StackPane layer = (StackPane) previewText.getParent().getParent();
            Scale scale = (Scale) layer.getTransforms().get(0);
            saveSnapshot(canvases[1], "notice-mirror-preview.png");
            return new double[]{liveX, previewX, scale.getX(), canvases[1].getWidth()};
        });
        Assertions.assertEquals(result[0], result[1], 40, "the preview should scroll in step with the projection");
        Assertions.assertTrue(result[0] < PROJECTION_WIDTH, "the notice should have started scrolling in");
        Assertions.assertEquals(result[3] / PROJECTION_WIDTH, result[2], 0.001, "the notice should be scaled to the preview");
        Assertions.assertTrue(result[2] < 0.5, "the preview is much smaller than the projection");
    }

    private static Text findText(Node node) {
        if (node instanceof Text) {
            return (Text) node;
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                Text found = findText(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void saveSnapshot(Node node, String name) {
        try {
            File dir = new File("build/test-snapshots");
            dir.mkdirs();
            ImageIO.write(SwingFXUtils.fromFXImage(node.snapshot(null, null), null), "png", new File(dir, name));
        } catch (Exception ex) {
            // Only for looking at
        }
    }

    private static <T> T onFx(Supplier<T> task) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(task.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(20, TimeUnit.SECONDS);
    }
}
