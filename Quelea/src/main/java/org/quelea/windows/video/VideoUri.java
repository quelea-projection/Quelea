package org.quelea.windows.video;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Prepares video locations for the GStreamer Java bindings. */
final class VideoUri {
    private static final String HEX = "0123456789ABCDEF";

    private VideoUri() {
    }

    static URI forGStreamer(URI uri) {
        // The bindings pass a file URI's raw path to native code. Encode only
        // non-ASCII bytes; URI.toASCIIString() normalizes Unicode and can change
        // which file is selected on filesystems that distinguish those names.
        if (uri == null || !"file".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        StringBuilder encoded = new StringBuilder();
        for (byte value : uri.toString().getBytes(StandardCharsets.UTF_8)) {
            int unsigned = value & 0xff;
            if (unsigned < 0x80) {
                encoded.append((char) unsigned);
            } else {
                encoded.append('%').append(HEX.charAt(unsigned >>> 4)).append(HEX.charAt(unsigned & 0x0f));
            }
        }
        return URI.create(encoded.toString());
    }
}
