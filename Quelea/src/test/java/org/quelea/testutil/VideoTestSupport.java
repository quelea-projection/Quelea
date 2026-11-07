package org.quelea.testutil;

import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.paint.Color;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Version;
import org.junit.jupiter.api.Assumptions;
import org.quelea.data.ColourBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.utils.GStreamerInitState;
import org.quelea.services.utils.GStreamerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.video.VidDisplay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Shared setup for tests that need the JavaFX toolkit and GStreamer. Tests
 * using it are skipped (not failed) where either isn't available, e.g. on a
 * headless build server without GStreamer installed.
 */
public final class VideoTestSupport {

    /** A short video that ships with Quelea. */
    public static final File TEST_VIDEO = new File("vid/abstract blue.mp4");

    private static Boolean available;
    private static String unavailableReason;

    private VideoTestSupport() {
    }

    /**
     * Start JavaFX and GStreamer (once per test run), or skip the calling test
     * if that isn't possible here.
     */
    public static synchronized void assumeVideoAvailable() {
        if (available == null) {
            available = init();
        }
        Assumptions.assumeTrue(available, () -> "Video tests skipped: " + unavailableReason);
    }

    private static boolean init() {
        if (!TEST_VIDEO.isFile()) {
            unavailableReason = "test video " + TEST_VIDEO.getAbsolutePath() + " not found";
            return false;
        }
        try {
            File userHome = Files.createTempDirectory("quelea-test-home").toFile();
            userHome.deleteOnExit();
            QueleaProperties.init(userHome.getAbsolutePath());
        } catch (IOException ex) {
            unavailableReason = "couldn't create a temporary user home: " + ex;
            return false;
        }
        try {
            CompletableFuture<Void> started = new CompletableFuture<>();
            Platform.startup(() -> started.complete(null));
            started.get(30, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            // Fine: another test started the toolkit already.
        } catch (Throwable ex) {
            unavailableReason = "JavaFX couldn't be started (no display?): " + ex;
            return false;
        }
        // Tests open and close windows; closing the last one mustn't shut the
        // toolkit down for the tests that run after it. Set this even if
        // another test started the toolkit.
        Platform.setImplicitExit(false);
        try {
            GStreamerUtils.configurePaths();
            Gst.init(Version.BASELINE, "QueleaTest");
            GStreamerInitState.INIT_SUCCESS = true;
        } catch (Throwable ex) {
            unavailableReason = "GStreamer couldn't be initialised: " + ex;
            return false;
        }
        return true;
    }

    /**
     * Run the given code on the FX thread and wait for its result.
     */
    public static <T> T onFx(Callable<T> callable) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(callable.call());
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }

    /**
     * Run the given code on the FX thread and wait for it to finish.
     */
    public static void onFx(Runnable runnable) throws Exception {
        onFx(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Wait until a condition (checked on the FX thread) holds.
     *
     * @return true if it held before the timeout.
     */
    public static boolean waitFor(BooleanSupplier condition, long timeoutMillis) throws Exception {
        long end = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < end) {
            if (onFx(condition::getAsBoolean)) {
                return true;
            }
            Thread.sleep(50);
        }
        return onFx(condition::getAsBoolean);
    }

    /**
     * Register a (never shown) projection window, which some UI components
     * use for their aspect ratio. Must be called on the FX thread.
     */
    public static void ensureProjectionWindow() {
        if (QueleaApp.get().getProjectionWindow() == null) {
            QueleaApp.get().setProjectionWindow(new DisplayStage(new BoundingBox(0, 0, 1280, 720), false));
        }
    }

    /**
     * @return the number of video pipelines that are currently alive.
     */
    public static int activePipelines() {
        return VidDisplay.getActivePipelineCount();
    }

    /**
     * A theme with the test video as its background and the given shadow
     * radius, like the theme editor produces while the shadow slider is moved.
     */
    public static ThemeDTO videoTheme(double shadowRadius) {
        ThemeDTO d = ThemeDTO.DEFAULT_THEME;
        return new ThemeDTO(d.getSerializableFont(), d.getFontPaint(), d.getSerializableFont(),
                d.getTranslateFontPaint(), new VideoBackground(TEST_VIDEO.getAbsolutePath(), 0, false),
                new org.quelea.services.utils.SerializableDropShadow(Color.BLACK, 0, 0, shadowRadius, 0.1, true),
                false, false, false, false, -1, 0);
    }

    /**
     * A theme with a plain colour background.
     */
    public static ThemeDTO colourTheme() {
        ThemeDTO d = ThemeDTO.DEFAULT_THEME;
        return new ThemeDTO(d.getSerializableFont(), d.getFontPaint(), d.getSerializableFont(),
                d.getTranslateFontPaint(), new ColourBackground(Color.DARKGREEN),
                d.getShadow(), false, false, false, false, -1, 0);
    }
}
