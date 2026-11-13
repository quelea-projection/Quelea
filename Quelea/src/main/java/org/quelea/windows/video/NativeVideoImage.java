package org.quelea.windows.video;

import com.sun.javafx.tk.quantum.QuantumToolkit;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstBufferAPI;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reuses a JavaFX image and its GPU texture while displaying mapped native frames.
 * All methods except the cleaner run on the FX thread.
 */
final class NativeVideoImage {

    private static final Cleaner CLEANER = Cleaner.create();
    private static final Field ADDRESS;

    static {
        try {
            ADDRESS = java.nio.Buffer.class.getDeclaredField("address");
            ADDRESS.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Keep the original allocation as a home for the final frame when this image
    // is retired. IntBuffer views retain this ByteBuffer, including renderer views.
    private final ByteBuffer storage;
    private final IntBuffer pixels;
    private final PixelBuffer<IntBuffer> pixelBuffer;
    private final WritableImage image;
    private final FrameLifetime lifetime = new FrameLifetime();
    private final Cleaner.Cleanable cleanable;

    NativeVideoImage(int width, int height) {
        storage = ByteBuffer.allocateDirect(Math.multiplyExact(Math.multiplyExact(width, height), Integer.BYTES))
                .order(ByteOrder.nativeOrder());
        pixels = storage.asIntBuffer();
        pixelBuffer = new PixelBuffer<>(width, height, pixels, PixelFormat.getIntArgbPreInstance());
        image = new WritableImage(pixelBuffer);
        // The cleanup action must never reference storage, pixels or this image.
        // It also covers a sink abandoned without an explicit clear/dispose.
        cleanable = CLEANER.register(storage, lifetime);
    }

    WritableImage image() {
        return image;
    }

    boolean hasSize(int width, int height) {
        return pixelBuffer.getWidth() == width && pixelBuffer.getHeight() == height;
    }

    /** Takes ownership of frame without copying its pixels. */
    void setFrame(MappedFrame frame) {
        try {
            MappedFrame previous = QuantumToolkit.runWithRenderLock(() -> {
                // The renderer may be uploading the previous address in native
                // code. Exclude that upload before changing the pointer or
                // releasing its mapping; a fixed number of delayed frames is unsafe.
                address(pixels, frame.address());
                return lifetime.frame.getAndSet(frame);
            });
            if (previous != null) {
                previous.close();
            }
        } finally {
            Reference.reachabilityFence(storage);
        }
    }

    void markDirty() {
        pixelBuffer.updateBuffer(pb -> null);
    }

    /** Freeze the last frame so old ImageViews remain valid after video cleanup. */
    void detach() {
        try {
            MappedFrame previous = QuantumToolkit.runWithRenderLock(() -> {
                if (lifetime.frame.get() == null) {
                    return null;
                }
                // This is the only pixel copy, at a stop/resize, never per frame.
                storage.asIntBuffer().put(pixels.duplicate().clear());
                address(pixels, address(storage));
                return lifetime.frame.getAndSet(null);
            });
            if (previous != null) {
                previous.close();
            }
            cleanable.clean();
        } finally {
            Reference.reachabilityFence(storage);
        }
    }

    private static long address(java.nio.Buffer buffer) {
        try {
            return ADDRESS.getLong(buffer);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void address(java.nio.Buffer buffer, long address) {
        try {
            ADDRESS.setLong(buffer, address);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class FrameLifetime implements Runnable {
        private final AtomicReference<MappedFrame> frame = new AtomicReference<>();

        @Override
        public void run() {
            MappedFrame last = frame.getAndSet(null);
            if (last != null) {
                last.close();
            }
        }
    }

    /** An independently retained GstBuffer and mapping, not a borrowed Sample. */
    static final class MappedFrame implements AutoCloseable {
        private final Buffer buffer;
        private final GstBufferAPI.MapInfoStruct mapping = new GstBufferAPI.MapInfoStruct();

        private MappedFrame(Buffer buffer) {
            this.buffer = buffer;
        }

        static MappedFrame map(Buffer buffer, int requiredBytes) {
            MappedFrame frame = new MappedFrame(buffer);
            Natives.ref(buffer);
            // Use a separate MapInfo for each frame. Preroll and the first
            // regular sample can reference the same Buffer wrapper, whose
            // Buffer.map()/unmap() methods share a single MapInfo.
            if (!GstBufferAPI.GSTBUFFER_API.gst_buffer_map(buffer, frame.mapping, GstBufferAPI.GST_MAP_READ)) {
                Natives.unref(buffer);
                return null;
            }
            if (frame.mapping.data == null || frame.mapping.size.longValue() < requiredBytes) {
                frame.close();
                return null;
            }
            return frame;
        }

        long address() {
            return com.sun.jna.Pointer.nativeValue(mapping.data);
        }

        @Override
        public void close() {
            try {
                GstBufferAPI.GSTBUFFER_API.gst_buffer_unmap(buffer, mapping);
            } finally {
                Natives.unref(buffer);
            }
        }
    }
}
