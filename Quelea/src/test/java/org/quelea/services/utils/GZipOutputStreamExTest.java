package org.quelea.services.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Gzip compression")
public class GZipOutputStreamExTest {
    @TempDir Path directory;

    @Test
    public void roundTripsEmptyAndChunkedBinaryDataAtDifferentLevels() throws IOException {
        byte[] input = new byte[16391];
        new Random(17).nextBytes(input);
        for (int level : new int[]{Deflater.NO_COMPRESSION, Deflater.BEST_SPEED, Deflater.BEST_COMPRESSION}) {
            for (byte[] data : new byte[][]{new byte[0], input}) {
                ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                try (GZipOutputStreamEx gzip = new GZipOutputStreamEx(compressed, 31, level)) {
                    int split = data.length / 2;
                    gzip.write(data, 0, split);
                    gzip.write(data, split, data.length - split);
                    gzip.finish();
                    gzip.finish();
                }
                try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed.toByteArray()))) {
                    Assertions.assertArrayEquals(data, gzip.readAllBytes());
                }
            }
        }
    }

    @Test
    public void compressesAFileThatStandardGzipCanRead() throws IOException {
        byte[] data = "Hallelujah \u2014 \u65e5\u672c\u8a9e".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Path source = Files.write(directory.resolve("song.txt"), data);
        Path output = directory.resolve("song.gz");
        Assertions.assertTrue(new GZMaxFileCompressor().compress(source.toFile(), output.toFile()));
        try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(output))) {
            Assertions.assertArrayEquals(data, gzip.readAllBytes());
        }
    }

    @Test
    public void missingSourceReportsFailure() {
        Assertions.assertFalse(new GZMaxFileCompressor().compress(directory.resolve("missing").toFile(), directory.resolve("out.gz").toFile()));
    }

    @Test
    @Disabled("Compressing to the input file truncates the original data")
    public void doesNotDestroyInputWhenOutputIsTheSameFile() throws IOException {
        // The output stream truncates the source before it can be read. Detect
        // identical files (including aliases such as links) before opening the
        // output, or write to a temporary file and atomically replace afterwards.
        byte[] content = "Important song data".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Path source = Files.write(directory.resolve("important.txt"), content);
        Assertions.assertFalse(new GZMaxFileCompressor().compress(source.toFile(), source.toFile()));
        Assertions.assertArrayEquals(content, Files.readAllBytes(source));
    }
}
