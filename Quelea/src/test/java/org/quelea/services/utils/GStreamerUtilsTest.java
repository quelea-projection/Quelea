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

    @Test
    void initialisesGStreamerWithoutItsSegfaultHandler() {
        // GStreamer's handler, installed while it loads plugins, would exit the
        // JVM on the SIGSEGVs the JVM raises (and handles) itself.
        org.quelea.testutil.VideoTestSupport.assumeVideoAvailable();
        Assertions.assertEquals(0, gstreamerLibrary().getFunction("gst_segtrap_is_enabled").invokeInt(new Object[0]));
    }

    /**
     * The library gst1-java uses, found the way it finds it. The macOS framework
     * also has a separate copy under another name, with its own state.
     */
    private static com.sun.jna.NativeLibrary gstreamerLibrary() {
        UnsatisfiedLinkError failure = null;
        for (String name : new String[]{"gstreamer-1.0.0", "gstreamer-1.0-0", "gstreamer-1.0"}) {
            try {
                return com.sun.jna.NativeLibrary.getInstance(name);
            } catch (UnsatisfiedLinkError ex) {
                failure = ex;
            }
        }
        throw failure;
    }
}
