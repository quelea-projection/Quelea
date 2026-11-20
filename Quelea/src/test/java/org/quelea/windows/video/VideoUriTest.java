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

    @Test
    void fileUriConversionPreservesNetworkShareAndSupplementaryCharacters() {
        URI original = URI.create("file://server/share/\u97f3\u697d/\ud83c\udfb5%20clip.mp4");
        URI converted = VideoUri.forGStreamer(original);
        Assertions.assertEquals("server", converted.getHost());
        Assertions.assertEquals("/share/%E9%9F%B3%E6%A5%BD/%F0%9F%8E%B5%20clip.mp4", converted.getRawPath());
        Assertions.assertEquals(original.getPath(), converted.getPath());
        Assertions.assertEquals(converted, VideoUri.forGStreamer(converted));
    }

    @Test
    void fileUriConversionPreservesQueryFragmentAndEscapedPercentSigns() {
        URI original = URI.create("FILE:///tmp/r\u00e9sum\u00e9%25.mp4?x=%23#part");
        URI converted = VideoUri.forGStreamer(original);
        Assertions.assertEquals("/tmp/r%C3%A9sum%C3%A9%25.mp4", converted.getRawPath());
        Assertions.assertEquals("x=%23", converted.getRawQuery());
        Assertions.assertEquals("part", converted.getRawFragment());
        Assertions.assertEquals(original.getPath(), converted.getPath());
    }
}
