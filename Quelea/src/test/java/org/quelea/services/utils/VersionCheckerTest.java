package org.quelea.services.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VersionCheckerTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-version-checker").toString());
        }
    }

    @Test
    void readsVersionFromUrlWithEscapedFilename() throws Exception {
        Path page = Files.writeString(directory.resolve("version & details.html"), "<p>Latest version: 2027.0</p>");
        Version version = new VersionChecker(page.toUri().toString()).getLatestVersion();
        Assertions.assertNotNull(version);
        Assertions.assertEquals("2027.0", version.getVersionString());
    }

    @Test
    void invalidUrlReturnsNoVersion() {
        Assertions.assertNull(new VersionChecker("not a valid URI").getLatestVersion());
    }
}
