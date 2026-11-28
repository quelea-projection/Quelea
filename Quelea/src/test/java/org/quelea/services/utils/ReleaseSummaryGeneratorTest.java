package org.quelea.services.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReleaseSummaryGeneratorTest {

    @Test
    void hashesFileContentsRegardlessOfFilename(@TempDir Path directory) throws Exception {
        Path first = directory.resolve("quelea-windows.exe");
        Path second = directory.resolve("quelea-mac.zip");
        Files.writeString(first, "abc", StandardCharsets.UTF_8);
        Files.writeString(second, "abc", StandardCharsets.UTF_8);

        String expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
        assertEquals(expected, ReleaseSummaryGenerator.sha256(first));
        assertEquals(expected, ReleaseSummaryGenerator.sha256(second));

        Files.writeString(first, "", StandardCharsets.UTF_8);
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                ReleaseSummaryGenerator.sha256(first));
    }
}
