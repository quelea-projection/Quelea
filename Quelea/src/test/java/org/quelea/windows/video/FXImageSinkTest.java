package org.quelea.windows.video;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSrc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.onFx;

/** Exercises the native frame lifetime without needing particular video codecs. */
public class FXImageSinkTest {

    private FXImageSink sink;
    private Pipeline pipeline;
    private AppSrc source;

    @BeforeEach
    public void setUp() throws Exception {
        assumeVideoAvailable();
        onFx(() -> {
            sink = new FXImageSink();
            sink.getSinkElement().set("sync", false);
            source = new AppSrc("frames");
            source.set("is-live", true);
            pipeline = new Pipeline();
            // Not addMany: gst1-java passes gst_bin_add_many's varargs without
            // the terminating NULL, so it reads past them and can crash the JVM
            // (seen as SIGSEGV/SIGABRT in gst_bin_add_many on Apple Silicon).
            pipeline.add(source);
            pipeline.add(sink.getSinkElement());
            source.link(sink.getSinkElement());
            pipeline.play();
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (pipeline != null) {
            pipeline.stop();
            onFx(sink::dispose);
            pipeline.dispose();
        }
    }

    @Test
    public void retiredFramesCanStillBeRenderedAfterResolutionChangesAndDisposal() throws Exception {
        Image first = frame(640, 480, 0xff123456);
        Image second = frame(320, 240, 0xffabcdef);
        Image third = frame(800, 600, 0xff345678);
        // More than the old sink's two retained native samples: the first frame
        // must survive even after its GstBuffer has been released.
        frame(800, 600, 0xff56789a);
        frame(800, 600, 0xff789abc);
        pipeline.stop();
        onFx(sink::dispose);
        onFx(() -> {
            assertRenderedColour(first, 0xff123456);
            assertRenderedColour(second, 0xffabcdef);
            assertRenderedColour(third, 0xff789abc);
        });
    }

    @Test
    public void sameSizeFramesUpdateTheImageAndPlaybackCanRestartAfterClear() throws Exception {
        Image first = frame(64, 48, 0xff123456);
        Image updated = frame(64, 48, 0xffabcdef);
        assertSame(first, updated, "An image should be reused while video dimensions stay the same");
        onFx(() -> assertRenderedColour(updated, 0xffabcdef));
        onFx(sink::clear);
        onFx(() -> {
            assertEquals(1, sink.imageProperty().get().getWidth());
            assertRenderedColour(updated, 0xffabcdef);
        });
        Image restarted = frame(64, 48, 0xff345678);
        onFx(() -> {
            assertRenderedColour(restarted, 0xff345678);
            assertRenderedColour(updated, 0xffabcdef);
        });
    }

    private Image frame(int width, int height, int argb) throws Exception {
        CompletableFuture<Image> received = new CompletableFuture<>();
        onFx(() -> sink.setOnFrame(() -> {
            Image image = sink.imageProperty().get();
            // The first buffer may arrive as both preroll and a regular sample.
            // Only complete this request once its own frame has been delivered.
            if (image.getWidth() == width && image.getHeight() == height
                    && image.getPixelReader().getArgb(0, 0) == argb) {
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
        while (pixels.hasRemaining()) {
            pixels.put(argb);
        }
        buffer.unmap();
        assertEquals(FlowReturn.OK, source.pushBuffer(buffer));
        return received.get(10, TimeUnit.SECONDS);
    }

    private static void assertRenderedColour(Image image, int argb) {
        // Snapshot forces the JavaFX renderer to upload the pixels, including
        // images it could still hold while a video theme is being replaced.
        WritableImage rendered = new ImageView(image).snapshot(null, null);
        assertEquals(argb, rendered.getPixelReader().getArgb(
                (int) image.getWidth() / 2, (int) image.getHeight() / 2));
    }
}
