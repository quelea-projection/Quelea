package org.quelea.windows.main;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

class PictureInPictureMirrorTest {
    private Stage sourceStage;
    private Stage mirrorStage;
    private Rectangle content;
    private ImageView image;
    private PictureInPictureMirror mirror;

    @BeforeEach
    void setUp() throws Exception {
        FxTestSupport.assumeFxAvailable();
        onFx(() -> {
            content = new Rectangle(800, 450, Color.RED);
            StackPane source = new StackPane(content);
            sourceStage = new Stage();
            sourceStage.setScene(new Scene(source, 800, 450));
            sourceStage.show();
            image = new ImageView();
            mirrorStage = new Stage();
            mirrorStage.setScene(new Scene(new StackPane(image), 320, 180));
            mirrorStage.show();
            mirror = new PictureInPictureMirror(() -> source, image);
            mirror.start();
            return null;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        onFx(() -> {
            mirror.stop();
            sourceStage.close();
            mirrorStage.close();
            return null;
        });
    }

    private void awaitColor(Color expected) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            boolean match = onFx(() -> {
                Image frame = image.getImage();
                return frame != null && expected.equals(frame.getPixelReader().getColor(
                        (int) frame.getWidth() / 2, (int) frame.getHeight() / 2));
            });
            if (match) return;
            Thread.sleep(50);
        }
        fail("Preview did not show " + expected);
    }

    @Test
    void mirrorsChangesWithABoundedImage() throws Exception {
        awaitColor(Color.RED);
        onFx(() -> {
            assertEquals(480, image.getImage().getWidth());
            assertEquals(270, image.getImage().getHeight());
            content.setFill(Color.BLUE);
            return null;
        });
        awaitColor(Color.BLUE);
        Image rendered = onFx(() -> image.snapshot(null, null));
        assertEquals(Color.BLUE, rendered.getPixelReader().getColor(
                (int) rendered.getWidth() / 2, (int) rendered.getHeight() / 2),
                "updated pixels must actually be painted by the ImageView");
    }

    @Test
    void continuesWhileSourceWindowIsHidden() throws Exception {
        awaitColor(Color.RED);
        onFx(() -> {
            sourceStage.hide();
            content.setFill(Color.BLUE);
            return null;
        });
        awaitColor(Color.BLUE);
    }

    @Test
    void stoppingAndRestartingReleasesAndRefreshesTheFrame() throws Exception {
        awaitColor(Color.RED);
        onFx(() -> {
            mirror.stop();
            assertNull(image.getImage());
            content.setFill(Color.BLUE);
            mirror.start();
            return null;
        });
        awaitColor(Color.BLUE);
    }

    @Test
    void changingOrRemovingTheSourceDoesNotLeaveAStalePreview() throws Exception {
        AtomicReference<javafx.scene.Node> selected = new AtomicReference<>();
        onFx(() -> {
            mirror.stop();
            selected.set(content);
            mirror = new PictureInPictureMirror(selected::get, image);
            mirror.start();
            return null;
        });
        awaitColor(Color.RED);
        onFx(() -> { selected.set(null); return null; });
        long end = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < end && onFx(image::getImage) != null) Thread.sleep(50);
        assertNull(onFx(image::getImage));
        onFx(() -> { content.setFill(Color.BLUE); selected.set(content); return null; });
        awaitColor(Color.BLUE);
    }

    @Test
    void limitsCaptureRateAndStopsWorkAfterClosing() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        onFx(() -> {
            mirror.stop();
            mirror = new PictureInPictureMirror(() -> { requests.incrementAndGet(); return content; }, image);
            mirror.start();
            return null;
        });
        awaitColor(Color.RED);
        int initial = requests.get();
        Thread.sleep(1_200);
        int count = requests.get() - initial;
        assertTrue(count >= 2 && count <= 14, "capture count in 1.2 seconds: " + count);
        onFx(() -> { mirror.stop(); return null; });
        int stopped = requests.get();
        Thread.sleep(300);
        assertEquals(stopped, requests.get(), "a closed preview must not keep capturing");
    }
}
