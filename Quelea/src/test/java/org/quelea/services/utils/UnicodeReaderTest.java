package org.quelea.services.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UnicodeReaderTest")
public class UnicodeReaderTest {

    private static final String TEXT = "Héllo Ω 😀";
    private static final byte[] UTF8_BOM = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    private static final byte[] UTF16_BE_BOM = {(byte) 0xfe, (byte) 0xff};
    private static final byte[] UTF16_LE_BOM = {(byte) 0xff, (byte) 0xfe};
    private static final byte[] UTF32_BE_BOM = {0, 0, (byte) 0xfe, (byte) 0xff};
    private static final byte[] UTF32_LE_BOM = {(byte) 0xff, (byte) 0xfe, 0, 0};

    @Test
    @DisplayName("A BOM chooses the encoding and is not part of the text")
    public void decodesByteOrderMarks() throws IOException {
        assertDecoded(UTF8_BOM, StandardCharsets.UTF_8);
        assertDecoded(UTF16_BE_BOM, StandardCharsets.UTF_16BE);
        assertDecoded(UTF16_LE_BOM, StandardCharsets.UTF_16LE);
        assertDecoded(UTF32_BE_BOM, Charset.forName("UTF-32BE"));
    }

    @Test
    @Disabled("UTF-32LE BOM is currently detected as UTF-16LE")
    @DisplayName("UTF-32 little-endian is not mistaken for UTF-16")
    public void decodesUtf32LittleEndian() throws IOException {
        // Bible and song imports need the correct text encoding: a UTF-32LE BOM
        // starts with the same two bytes as UTF-16LE, so the current check selects
        // UTF-16LE first and produces NULs and corrupted characters. Check the
        // four-byte BOMs before the two-byte BOMs, using only bytes actually read.
        assertDecoded(UTF32_LE_BOM, Charset.forName("UTF-32LE"));
    }

    @Test
    @DisplayName("Uses the supplied encoding when there is no BOM")
    public void usesDefaultEncodingWithoutBom() throws IOException {
        byte[] bytes = "café".getBytes(StandardCharsets.ISO_8859_1);
        Assertions.assertEquals("café", read(new ByteArrayInputStream(bytes), "ISO-8859-1"));
    }

    @Test
    @DisplayName("Empty and short streams do not lose data")
    public void readsShortStreams() throws IOException {
        Assertions.assertEquals("", read(new ByteArrayInputStream(new byte[0]), "UTF-8"));
        Assertions.assertEquals("A", read(new ByteArrayInputStream(new byte[]{'A'}), "UTF-8"));
        Assertions.assertEquals("", read(new ByteArrayInputStream(UTF8_BOM), "ISO-8859-1"));
        Assertions.assertEquals("", read(new ByteArrayInputStream(UTF16_LE_BOM), "ISO-8859-1"));
    }

    @Test
    @Disabled("BOM detection currently assumes the first read fills the buffer")
    @DisplayName("Detects a BOM when the input stream returns one byte at a time")
    public void detectsBomAcrossShortReads() throws IOException {
        // InputStream.read may return fewer bytes than requested even before EOF.
        // A split BOM is then treated as text in the default encoding, corrupting
        // imports (including streams read from OpenSong archives). Accumulate up to
        // four bytes or EOF before choosing an encoding, then push back unused bytes.
        byte[] bytes = withBom(UTF8_BOM, TEXT.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream shortReads = new ByteArrayInputStream(bytes) {
            @Override
            public synchronized int read(byte[] buffer, int offset, int length) {
                return super.read(buffer, offset, Math.min(length, 1));
            }
        };
        Assertions.assertEquals(TEXT, read(shortReads, "ISO-8859-1"));
    }

    private static void assertDecoded(byte[] bom, Charset charset) throws IOException {
        byte[] bytes = withBom(bom, TEXT.getBytes(charset));
        Assertions.assertEquals(TEXT, read(new ByteArrayInputStream(bytes), "ISO-8859-1"), charset.name());
    }

    private static byte[] withBom(byte[] bom, byte[] contents) {
        byte[] bytes = Arrays.copyOf(bom, bom.length + contents.length);
        System.arraycopy(contents, 0, bytes, bom.length, contents.length);
        return bytes;
    }

    private static String read(ByteArrayInputStream input, String defaultEncoding) throws IOException {
        try (UnicodeReader reader = new UnicodeReader(input, defaultEncoding)) {
            StringBuilder result = new StringBuilder();
            char[] buffer = new char[3];
            int count;
            while ((count = reader.read(buffer, 0, buffer.length)) != -1) {
                result.append(buffer, 0, count);
            }
            return result.toString();
        }
    }
}
