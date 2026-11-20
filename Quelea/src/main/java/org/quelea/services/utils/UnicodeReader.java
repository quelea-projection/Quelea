package org.quelea.services.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class UnicodeReader extends Reader {

    private static final int BOM_SIZE = 4;
    private final InputStreamReader reader;

    /**
     * Construct UnicodeReader
     *
     * @param in Input stream.
     * @param defaultEncoding Default encoding to be used if BOM is not found,
     * or <code>null</code> to use system default encoding.
     * @throws IOException If an I/O error occurs.
     */
    public UnicodeReader(InputStream in, String defaultEncoding) throws IOException {
        byte[] bom = new byte[BOM_SIZE];
        String encoding = defaultEncoding;
        int bomLength = 0;
        PushbackInputStream pushbackStream = new PushbackInputStream(in, BOM_SIZE);
        int n = 0;
        while (n < bom.length) {
            int count = pushbackStream.read(bom, n, bom.length - n);
            if (count < 0) {
                break;
            }
            if (count == 0) {
                int next = pushbackStream.read();
                if (next < 0) {
                    break;
                }
                bom[n++] = (byte) next;
            } else {
                n += count;
            }
        }

        // Check four-byte marks before UTF-16LE, whose prefix is shared by UTF-32LE.
        if (n >= 4 && bom[0] == 0 && bom[1] == 0 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF) {
            encoding = "UTF-32BE";
            bomLength = 4;
        } else if (n >= 4 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == 0 && bom[3] == 0) {
            encoding = "UTF-32LE";
            bomLength = 4;
        } else if (n >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            encoding = "UTF-8";
            bomLength = 3;
        } else if (n >= 2 && bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
            encoding = "UTF-16BE";
            bomLength = 2;
        } else if (n >= 2 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            encoding = "UTF-16LE";
            bomLength = 2;
        }

        // Put back all bytes after the BOM, or all bytes if no BOM was found.
        if (n > bomLength) {
            pushbackStream.unread(bom, bomLength, n - bomLength);
        }

        // Use given encoding.
        if (encoding == null) {
            reader = new InputStreamReader(pushbackStream);
        } else {
            reader = new InputStreamReader(pushbackStream, encoding);
        }
    }

    public String getEncoding() {
        return reader.getEncoding();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
