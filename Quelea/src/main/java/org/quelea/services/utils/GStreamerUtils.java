package org.quelea.services.utils;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import org.freedesktop.gstreamer.PluginFeature;
import org.freedesktop.gstreamer.Registry;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.System.getenv;

public class GStreamerUtils {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final String DEFAULT_WINDOWS_PATH = "C:\\gstreamer\\1.0\\msvc_x86_64";

    private GStreamerUtils() {
    }

    /**
     * Sometimes the default plugin priorities cause issues on some systems.
     * <p>
     * This method will reorganise plugin priorities for maximum compatibility.
     * It can be overridden by various properties defined in quelea.properties.
     */
    public static void setFeaturePriorities() {
        if (QueleaProperties.get().getDisableDirectShowForWVC1()) {
            try (var feature = Registry.get().lookupFeature("dshowvdec_wvc1")) {
                if (feature != null) {
                    feature.setRank(PluginFeature.Rank.NONE.intValue());
                }
            }
        }
    }

    /**
     * Configures paths to the GStreamer libraries. On Windows queries various
     * GStreamer environment variables, and then sets up the PATH environment
     * variable. On macOS, adds the location to jna.library.path (macOS binaries
     * link to each other). On both, the gstreamer.path system property can be
     * used to override. On Linux, assumes GStreamer is in the path already.
     */
    public static void configurePaths() {
        System.setProperty("jna.debug_load", "true");
        if (Platform.isWindows()) {
            LOGGER.log(Level.INFO, "Detected Windows");
            String gstPath = System.getProperty("gstreamer.path", findWindowsLocation());
            LOGGER.log(Level.INFO, "gst path is " + gstPath);
            if (!gstPath.isEmpty()) {
                String systemPath = getenv("PATH");
                if (systemPath == null || systemPath.trim().isEmpty()) {
                    Kernel32.INSTANCE.SetEnvironmentVariable("PATH", gstPath);
                } else {
                    Kernel32.INSTANCE.SetEnvironmentVariable("PATH", gstPath + File.pathSeparator + systemPath);
                }
            }
        } else if (Platform.isMac()) {
            LOGGER.log(Level.INFO, "Detected Mac OS");
            String gstPath = System.getProperty("gstreamer.path", "/Library/Frameworks/GStreamer.framework/Libraries/");
            LOGGER.log(Level.INFO, "gst path is " + gstPath);
            if (!gstPath.isEmpty()) {
                String jnaPath = System.getProperty("jna.library.path", "").trim();
                if (jnaPath.isEmpty()) {
                    System.setProperty("jna.library.path", gstPath);
                } else {
                    System.setProperty("jna.library.path", jnaPath + File.pathSeparator + gstPath);
                }
            }
        } else if (getenv("SNAP") != null) {
            LOGGER.log(Level.INFO, "Detected Snap Linux");
            System.setProperty("jna.tmpdir", System.getProperty("java.io.tmpdir"));

            String gstPath = new File(getenv("SNAP"), System.getProperty("gstreamer.path", "/usr/lib/x86_64-linux-gnu/")).getAbsolutePath();
            LOGGER.log(Level.INFO, "gst path is " + gstPath);

            if (!gstPath.isEmpty()) {
                String jnaPath = System.getProperty("jna.library.path", "").trim();
                LOGGER.log(Level.INFO, "JNA path is: " + jnaPath);
                if (jnaPath.isEmpty()) {
                    System.setProperty("jna.library.path", gstPath);
                    LOGGER.log(Level.INFO, "JNA path was empty, is now: " + System.getProperty("jna.library.path"));
                } else {
                    System.setProperty("jna.library.path", jnaPath + File.pathSeparator + gstPath);
                    LOGGER.log(Level.INFO, "JNA path is now: " + System.getProperty("jna.library.path"));
                }
            }
            LOGGER.log(Level.INFO, "jna.library.path is: " + System.getProperty("jna.library.path"));
        }
    }

    /**
     * Query over a stream of possible environment variables for GStreamer
     * location, filtering on the first non-null result, and adding \bin\ to the
     * value.
     *
     * @return location or empty string
     */
    private static String findWindowsLocation() {
        if (Platform.is64Bit()) {
            return Stream.of(getenv("GSTREAMER_1_0_ROOT_MSVC_X86_64"), getenv("GSTREAMER_1_0_ROOT_MINGW_X86_64"), getenv("GSTREAMER_1_0_ROOT_X86_64"), DEFAULT_WINDOWS_PATH).filter(Objects::nonNull).map(p -> p.endsWith("\\") ? p + "bin\\" : p + "\\bin\\").filter(p -> Files.exists(Path.of(p))).findFirst().orElse("");
        } else {
            return "";
        }
    }
}
