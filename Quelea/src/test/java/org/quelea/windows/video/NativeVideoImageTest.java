package org.quelea.windows.video;

import com.sun.javafx.tk.quantum.QuantumToolkit;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.freedesktop.gstreamer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.onFx;

public class NativeVideoImageTest {

    @BeforeEach
    public void setUp() {
        assumeVideoAvailable();
    }

    @Test
    public void mappingsOfTheSameBufferHaveIndependentLifetimes() {
        try (Buffer buffer = buffer(0xff123456)) {
            int refs = buffer.getRefCount();
            try (var first = NativeVideoImage.MappedFrame.map(buffer, 4)) {
                assertNotNull(first);
                assertEquals(refs + 1, buffer.getRefCount());
                try (var second = NativeVideoImage.MappedFrame.map(buffer, 4)) {
                    assertNotNull(second);
                    assertEquals(first.address(), second.address(), "Mapping must not copy the native pixels");
                    assertEquals(refs + 2, buffer.getRefCount());
                }
                assertEquals(refs + 1, buffer.getRefCount());
            }
            assertEquals(refs, buffer.getRefCount());
        }
    }

    @Test
    public void replacingAFrameWaitsForRenderingAndDetachReleasesTheLastBuffer() throws Exception {
        try (Buffer first = buffer(0xff123456); Buffer next = buffer(0xffabcdef)) {
            NativeVideoImage video = onFx(() -> new NativeVideoImage(1, 1));
            onFx(() -> {
                video.setFrame(NativeVideoImage.MappedFrame.map(first, 4));
                video.markDirty();
            });
            assertEquals(2, first.getRefCount());

            CountDownLatch rendering = new CountDownLatch(1);
            CountDownLatch finishRendering = new CountDownLatch(1);
            Thread renderer = new Thread(() -> QuantumToolkit.runWithRenderLock(() -> {
                rendering.countDown();
                try {
                    finishRendering.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }), "Video frame lifetime test");
            renderer.setDaemon(true);
            renderer.start();
            CompletableFuture<Void> published = new CompletableFuture<>();
            CountDownLatch publishing = new CountDownLatch(1);
            try {
                assertTrue(rendering.await(5, TimeUnit.SECONDS));
                Platform.runLater(() -> {
                    try {
                        publishing.countDown();
                        video.setFrame(NativeVideoImage.MappedFrame.map(next, 4));
                        video.markDirty();
                        published.complete(null);
                    } catch (Throwable ex) {
                        published.completeExceptionally(ex);
                    }
                });
                assertTrue(publishing.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> published.get(100, TimeUnit.MILLISECONDS));
                assertEquals(2, first.getRefCount(), "The frame being uploaded must still be retained");
            } finally {
                finishRendering.countDown();
                renderer.join(5_000);
            }
            try {
                published.get(5, TimeUnit.SECONDS);
                assertEquals(1, first.getRefCount(), "The old mapping should be released after the upload");
                assertEquals(2, next.getRefCount());
                Image retired = onFx(video::image);
                onFx(video::detach);
                assertEquals(1, next.getRefCount(), "Detach must release native memory without waiting for GC");
                onFx(() -> assertEquals(0xffabcdef, retired.getPixelReader().getArgb(0, 0)));
            } finally {
                onFx(video::detach);
            }
        }
    }

    private static Buffer buffer(int argb) {
        Buffer buffer = new Buffer(4);
        buffer.map(true).order(ByteOrder.nativeOrder()).putInt(argb);
        buffer.unmap();
        return buffer;
    }
}
