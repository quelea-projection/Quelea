package org.quelea.services.utils;

import com.sun.jna.Library;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import org.freedesktop.gstreamer.lowlevel.GFunctionMapper;
import org.freedesktop.gstreamer.lowlevel.GNative;
import org.freedesktop.gstreamer.lowlevel.GTypeMapper;
import org.freedesktop.gstreamer.lowlevel.GstAPI;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class GStreamerUtils {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private GStreamerUtils() {
    }

    /**
     * Configures paths to the GStreamer libraries. On Windows queries various
     * GStreamer environment variables, and then sets up the PATH environment
     * variable. On macOS, adds the location to jna.library.path (macOS binaries
     * link to each other). On both, the gstreamer.path system property can be
     * used to override. On Linux, assumes GStreamer is in the path already.
     */
    public static void configurePaths() {
        if (Platform.isWindows()) {
            String gstPath = System.getProperty("gstreamer.path", findWindowsLocation());
            if (!gstPath.isEmpty()) {
                String systemPath = System.getenv("PATH");
                if (systemPath == null || systemPath.trim().isEmpty()) {
                    Kernel32.INSTANCE.SetEnvironmentVariable("PATH", gstPath);
                } else {
                    Kernel32.INSTANCE.SetEnvironmentVariable("PATH", gstPath
                            + File.pathSeparator + systemPath);
                }
            }
        } else if (Platform.isMac()) {
            String gstPath = System.getProperty("gstreamer.path",
                    "/Library/Frameworks/GStreamer.framework/Libraries/");
            if (!gstPath.isEmpty()) {
                String jnaPath = System.getProperty("jna.library.path", "").trim();
                if (jnaPath.isEmpty()) {
                    System.setProperty("jna.library.path", gstPath);
                } else {
                    System.setProperty("jna.library.path", jnaPath + File.pathSeparator + gstPath);
                }
            }
        } else if (Platform.isLinux()) {
            LOGGER.log(Level.INFO, "Detected Linux");
            String gstPath = System.getProperty("gstreamer.path",
                    "/usr/lib/x86_64-linux-gnu/gstreamer-1.0/");

            gstPath = new File(System.getenv("SNAP"), gstPath).getAbsolutePath();

            GNative.loadLibrary("gstreaner-1.0", GstAPI.class, options);

            System.out.println(System.getenv("SNAP"));
            System.out.println(new File(System.getenv("SNAP")).exists());
            System.out.println(new File(gstPath).exists());

            LOGGER.log(Level.INFO, "GStreamer path is: " + System.getProperty("gstreamer.path"));
            LOGGER.log(Level.INFO, "GStreamer path with default is: " + gstPath);
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

    private static final Map<String, Object> options = new HashMap<String, Object>() {{
        put(Library.OPTION_TYPE_MAPPER, new GTypeMapper());
        put(Library.OPTION_FUNCTION_MAPPER, new GFunctionMapper());
    }};

    /**
     * Query over a stream of possible environment variables for GStreamer
     * location, filtering on the first non-null result, and adding \bin\ to the
     * value.
     *
     * @return location or empty string
     */
    static String findWindowsLocation() {
        if (Platform.is64Bit()) {
            return Stream.of("GSTREAMER_1_0_ROOT_MSVC_X86_64",
                            "GSTREAMER_1_0_ROOT_MINGW_X86_64",
                            "GSTREAMER_1_0_ROOT_X86_64")
                    .map(System::getenv)
                    .filter(Objects::nonNull)
                    .map(p -> p.endsWith("\\") ? p + "bin\\" : p + "\\bin\\")
                    .findFirst().orElse("");
        } else {
            return "";
        }
    }
}
