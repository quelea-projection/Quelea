package org.quelea.services.utils;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.PluginFeature;
import org.freedesktop.gstreamer.Registry;
import org.freedesktop.gstreamer.Version;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.System.getenv;

public class GStreamerUtils {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private GStreamerUtils() {
    }

    /**
     * Initialise GStreamer. Call {@link #configurePaths()} first.
     * <p>
     * Segfault trapping is turned off: while GStreamer loads a plugin (at
     * start-up, and whenever an element from a plugin that isn't loaded yet is
     * first needed, e.g. a decoder) it installs a process-wide SIGSEGV
     * handler that exits the process. The JVM uses SIGSEGV internally (e.g. for
     * implicit null checks in compiled code) on any thread, so a JVM that
     * happened to raise one in that window was killed with "Caught a
     * segmentation fault while loading plugin file: (null)" and exit code 255.
     *
     * @param programName the program name passed to GStreamer.
     */
    public static void init(String programName) {
        Gst.init(Version.BASELINE, programName, "--gst-disable-segtrap");
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
        return findWindowsLocation(System.getenv(), Platform.is64Bit());
    }

    static String findWindowsLocation(Map<String, String> environment, boolean is64Bit) {
        List<String> options = new ArrayList<>();
        options.add(environment.get("GSTREAMER_1_0_ROOT_MSVC_X86_64"));
        options.add(environment.get("GSTREAMER_1_0_ROOT_MINGW_X86_64"));
        options.add(environment.get("GSTREAMER_1_0_ROOT_X86_64"));
        String localAppData = environment.get("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            options.add(Path.of(localAppData, "Programs", "gstreamer", "1.0", "msvc_x86_64").toString());
            options.add(Path.of(localAppData, "Programs", "gstreamer", "1.0", "mingw_x86_64").toString());
        }
        String programFiles = environment.get("ProgramFiles");
        if (programFiles != null && !programFiles.isBlank()) {
            options.add(Path.of(programFiles, "gstreamer", "1.0", "msvc_x86_64").toString());
            options.add(Path.of(programFiles, "gstreamer", "1.0", "mingw_x86_64").toString());
        }
        options.add("C:\\Program Files\\gstreamer\\1.0\\msvc_x86_64");
        options.add("C:\\Program Files\\gstreamer\\1.0\\mingw_x86_64");
        options.add("C:\\gstreamer\\1.0\\msvc_x86_64");
        options.add("C:\\gstreamer\\1.0\\mingw_x86_64");

        if (is64Bit) {
            return options.stream().filter(Objects::nonNull).filter(p -> !p.isBlank())
                    .map(p -> Path.of(p, "bin"))
                    .filter(Files::isDirectory)
                    .map(p -> p.toString() + File.separator)
                    .findFirst().orElse("");
        } else {
            return "";
        }
    }
}
