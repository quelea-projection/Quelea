package org.quelea.services.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
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
    @DisplayName("UTF-32 little-endian is not mistaken for UTF-16")
    public void decodesUtf32LittleEndian() throws IOException {
        assertDecoded(UTF32_LE_BOM, Charset.forName("UTF-32LE"));
        try (UnicodeReader reader = new UnicodeReader(shortReads(withBom(UTF32_LE_BOM, TEXT.getBytes("UTF-32LE"))), "UTF-8")) {
            Assertions.assertEquals(Charset.forName("UTF-32LE"), Charset.forName(reader.getEncoding()));
        }
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
        Assertions.assertEquals("", read(new ByteArrayInputStream(UTF16_BE_BOM), "ISO-8859-1"));
        Assertions.assertEquals("", read(new ByteArrayInputStream(UTF16_LE_BOM), "ISO-8859-1"));
        Assertions.assertEquals("", read(new ByteArrayInputStream(UTF32_BE_BOM), "ISO-8859-1"));
        Assertions.assertEquals("", read(new ByteArrayInputStream(UTF32_LE_BOM), "ISO-8859-1"));
        byte[] incompleteBom = {(byte) 0xef, (byte) 0xbb};
        Assertions.assertEquals(new String(incompleteBom, StandardCharsets.ISO_8859_1),
                read(new ByteArrayInputStream(incompleteBom), "ISO-8859-1"));
        byte[] incompleteUtf32Bom = {0, 0, (byte) 0xfe};
        Assertions.assertEquals(new String(incompleteUtf32Bom, StandardCharsets.ISO_8859_1),
                read(shortReads(incompleteUtf32Bom), "ISO-8859-1"));
    }

    @Test
    @DisplayName("Detects a BOM when the input stream returns one byte at a time")
    public void detectsBomAcrossShortReads() throws IOException {
        assertDecodedWithShortReads(UTF8_BOM, StandardCharsets.UTF_8);
        assertDecodedWithShortReads(UTF16_BE_BOM, StandardCharsets.UTF_16BE);
        assertDecodedWithShortReads(UTF16_LE_BOM, StandardCharsets.UTF_16LE);
        assertDecodedWithShortReads(UTF32_BE_BOM, Charset.forName("UTF-32BE"));
        assertDecodedWithShortReads(UTF32_LE_BOM, Charset.forName("UTF-32LE"));
        Assertions.assertEquals(TEXT, read(shortReads(TEXT.getBytes(StandardCharsets.UTF_8)), "UTF-8"));
    }

    private static void assertDecodedWithShortReads(byte[] bom, Charset charset) throws IOException {
        Assertions.assertEquals(TEXT, read(shortReads(withBom(bom, TEXT.getBytes(charset))), "ISO-8859-1"), charset.name());
    }

    private static ByteArrayInputStream shortReads(byte[] bytes) {
        ByteArrayInputStream shortReads = new ByteArrayInputStream(bytes) {
            @Override
            public synchronized int read(byte[] buffer, int offset, int length) {
                return super.read(buffer, offset, Math.min(length, 1));
            }
        };
        return shortReads;
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
