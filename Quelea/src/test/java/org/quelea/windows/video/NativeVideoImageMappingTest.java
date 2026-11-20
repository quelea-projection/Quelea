package org.quelea.windows.video;

import com.sun.jna.Pointer;
import java.nio.ByteOrder;
import org.freedesktop.gstreamer.Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.VideoTestSupport;

/** Checks native frame mapping without needing a particular video codec. */
@DisplayName("Native video frame mapping")
public class NativeVideoImageMappingTest {

    @BeforeEach
    public void setUp() {
        VideoTestSupport.assumeVideoAvailable();
    }

    @Test
    @DisplayName("An undersized native frame is rejected without retaining its buffer")
    public void rejectsShortBuffer() {
        try (Buffer buffer = pixelBuffer(0xff123456)) {
            int refs = buffer.getRefCount();
            Assertions.assertNull(NativeVideoImage.MappedFrame.map(buffer, 2 * Integer.BYTES));
            Assertions.assertEquals(refs, buffer.getRefCount(), "Rejecting a frame must release its reference");
            try (var valid = NativeVideoImage.MappedFrame.map(buffer, Integer.BYTES)) {
                Assertions.assertNotNull(valid, "A rejected mapping must leave the buffer usable");
                Assertions.assertEquals(refs + 1, buffer.getRefCount());
            }
            Assertions.assertEquals(refs, buffer.getRefCount());
        }
    }

    @Test
    @DisplayName("Mapped pixels use the native byte order and survive until released")
    public void mapsPixelInNativeByteOrder() {
        int pixel = 0xff123456;
        try (Buffer buffer = pixelBuffer(pixel);
                var frame = NativeVideoImage.MappedFrame.map(buffer, Integer.BYTES)) {
            Assertions.assertNotNull(frame);
            Assertions.assertEquals(pixel, new Pointer(frame.address()).getInt(0));
        }
    }

    private static Buffer pixelBuffer(int argb) {
        Buffer buffer = new Buffer(Integer.BYTES);
        buffer.map(true).order(ByteOrder.nativeOrder()).putInt(argb);
        buffer.unmap();
        return buffer;
    }
}
