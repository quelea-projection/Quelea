package org.quelea.windows.video;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VideoUriTest {
    @Test
    void fileUrisEncodeUnicodeWithoutDoubleEncodingEscapedCharacters() {
        URI original = URI.create("file:///C:/video/café%20%23100%25.mp4");
        URI converted = VideoUri.forGStreamer(original);
        Assertions.assertEquals("/C:/video/caf%C3%A9%20%23100%25.mp4", converted.getRawPath());
        Assertions.assertEquals(original.getPath(), converted.getPath());
    }

    @Test
    void fileUrisPreserveDecomposedUnicode() {
        URI original = URI.create("file:///tmp/cafe\u0301.mp4");
        URI converted = VideoUri.forGStreamer(original);
        Assertions.assertEquals("/tmp/cafe%CC%81.mp4", converted.getRawPath());
        Assertions.assertEquals(original.getPath(), converted.getPath());
    }

    @Test
    void nonFileUrisAndNullAreUnchanged() {
        URI stream = URI.create("https://example.com/vidéo?q=café%20%23");
        Assertions.assertEquals(stream.toString(), VideoUri.forGStreamer(stream).toString());
        Assertions.assertNull(VideoUri.forGStreamer(null));
    }
}
