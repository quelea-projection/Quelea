package org.quelea.windows.video;

import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSrc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.onFx;

/** Exercises native video frames through the renderer of an attached scene. */
public class VideoSceneRenderingTest {
    private FXImageSink sink;
    private Pipeline pipeline;
    private AppSrc source;
    private Stage stage;
    private ImageView view;

    @BeforeEach
    public void setUp() throws Exception {
        assumeVideoAvailable();
        onFx(() -> {
            sink = new FXImageSink();
            sink.getSinkElement().set("sync", false);
            source = new AppSrc("scene-frames");
            source.set("is-live", true);
            pipeline = new Pipeline();
            pipeline.addMany(source, sink.getSinkElement());
            assertTrue(source.link(sink.getSinkElement()));

            view = new ImageView();
            view.setFitWidth(80);
            view.setFitHeight(60);
            view.setSmooth(false);
            view.imageProperty().bind(sink.imageProperty());
            stage = new Stage();
            stage.setScene(new Scene(new StackPane(view), 80, 60));
            stage.show();
            pipeline.play();
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (pipeline != null) {
            pipeline.stop();
            onFx(() -> {
                if (view != null) {
                    view.imageProperty().unbind();
                }
                if (sink != null) {
                    sink.dispose();
                }
                if (stage != null) {
                    stage.close();
                }
            });
            pipeline.dispose();
        }
    }

    @Test
    public void changingAndResizingFramesUpdatesAnAttachedScene() throws Exception {
        int[] firstColours = {0xffff0000, 0xff00ff00, 0xff0000ff, 0xffffffff};
        int[] nextColours = {0xff17395b, 0xffabcdef, 0xffa34211, 0xff2468ac};
        int[] resizedColours = {0xff008888, 0xff880088, 0xff888800, 0xff555555};

        Image first = frame(64, 48, firstColours);
        assertSceneColours(firstColours);
        Image next = frame(64, 48, nextColours);
        assertSame(first, next, "Same-size frames should reuse the image and update its texture");
        assertSceneColours(nextColours);

        frame(96, 72, resizedColours);
        assertSceneColours(resizedColours);
        // The image retired during resize must retain the last frame, even
        // after its GStreamer mapping has been released.
        onFx(() -> {
            WritableImage retired = new ImageView(first).snapshot(null, null);
            assertEquals(nextColours[0], retired.getPixelReader().getArgb(16, 12));
            assertEquals(nextColours[3], retired.getPixelReader().getArgb(48, 36));
        });
    }

    private Image frame(int width, int height, int[] colours) throws Exception {
        CompletableFuture<Image> received = new CompletableFuture<>();
        onFx(() -> sink.setOnFrame(() -> {
            Image image = sink.imageProperty().get();
            if (image.getWidth() == width && image.getHeight() == height
                    && image.getPixelReader().getArgb(width / 4, height / 4) == colours[0]) {
                received.complete(image);
            }
        }));
        String format = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "BGRx" : "xRGB";
        try (Caps caps = Caps.fromString("video/x-raw,format=" + format
                + ",width=" + width + ",height=" + height + ",framerate=30/1")) {
            source.setCaps(caps);
        }
        Buffer buffer = new Buffer(width * height * Integer.BYTES);
        var pixels = buffer.map(true).order(ByteOrder.nativeOrder()).asIntBuffer();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels.put(colours[(y >= height / 2 ? 2 : 0) + (x >= width / 2 ? 1 : 0)]);
            }
        }
        buffer.unmap();
        assertEquals(FlowReturn.OK, source.pushBuffer(buffer));
        return received.get(10, TimeUnit.SECONDS);
    }

    private void assertSceneColours(int[] colours) throws Exception {
        onFx(() -> {
            WritableImage rendered = stage.getScene().snapshot(null);
            int left = (int) rendered.getWidth() / 4;
            int right = (int) rendered.getWidth() * 3 / 4;
            int top = (int) rendered.getHeight() / 4;
            int bottom = (int) rendered.getHeight() * 3 / 4;
            assertEquals(colours[0], rendered.getPixelReader().getArgb(left, top));
            assertEquals(colours[1], rendered.getPixelReader().getArgb(right, top));
            assertEquals(colours[2], rendered.getPixelReader().getArgb(left, bottom));
            assertEquals(colours[3], rendered.getPixelReader().getArgb(right, bottom));
        });
    }
}
