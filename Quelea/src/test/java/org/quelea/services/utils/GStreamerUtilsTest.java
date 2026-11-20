package org.quelea.services.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class GStreamerUtilsTest {
    @TempDir
    Path temp;

    @Test
    void findsNewPerUserInstallationWithoutEnvironmentVariable() throws Exception {
        Path bin = Files.createDirectories(temp.resolve("Programs/gstreamer/1.0/msvc_x86_64/bin"));
        Assertions.assertEquals(bin + File.separator,
                GStreamerUtils.findWindowsLocation(Map.of("LOCALAPPDATA", temp.toString()), true));
    }

    @Test
    void findsNewSystemInstallationOnNonDefaultDrive() throws Exception {
        Path bin = Files.createDirectories(temp.resolve("gstreamer/1.0/msvc_x86_64/bin"));
        Assertions.assertEquals(bin + File.separator,
                GStreamerUtils.findWindowsLocation(Map.of("ProgramFiles", temp.toString()), true));
    }

    @Test
    void preservesExplicitEnvironmentPathPrecedence() throws Exception {
        Path explicit = Files.createDirectories(temp.resolve("custom/bin"));
        Files.createDirectories(temp.resolve("Programs/gstreamer/1.0/msvc_x86_64/bin"));
        Assertions.assertEquals(explicit + File.separator, GStreamerUtils.findWindowsLocation(Map.of(
                "LOCALAPPDATA", temp.toString(),
                "GSTREAMER_1_0_ROOT_MSVC_X86_64", temp.resolve("custom").toString()), true));
    }

    @Test
    void ignoresMissingOrBlankEnvironmentPathsAndKeeps32BitBehavior() throws Exception {
        Path bin = Files.createDirectories(temp.resolve("Programs/gstreamer/1.0/mingw_x86_64/bin"));
        Map<String, String> environment = Map.of("LOCALAPPDATA", temp.toString(),
                "GSTREAMER_1_0_ROOT_MSVC_X86_64", " ",
                "GSTREAMER_1_0_ROOT_X86_64", temp.resolve("missing").toString());
        Assertions.assertEquals(bin + File.separator, GStreamerUtils.findWindowsLocation(environment, true));
        Assertions.assertEquals("", GStreamerUtils.findWindowsLocation(environment, false));
    }
}
