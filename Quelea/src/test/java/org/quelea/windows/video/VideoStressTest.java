/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.video;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.testutil.VideoTestSupport;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/**
 * Stress tests for the GStreamer video paths that used to crash the JVM or
 * freeze the FX thread: video theme backgrounds switched quickly in several
 * visible previews (issue #718), and short looping videos with sound, whose
 * loop seek restarts the audio output every time while the position is polled.
 * <p>
 * Each test fails as soon as the FX thread stops responding, and prints the
 * Java thread stacks. A native crash ends the test JVM instead. The run time
 * can be raised with {@code -Dquelea.test.videoStressSeconds=60}.
 * <p>
 * Needs JavaFX and GStreamer; skipped where they aren't available.
 */
@DisplayName("Video stress")
public class VideoStressTest {

    /** The FX thread is considered frozen if it doesn't answer for this long. */
    private static final long FX_HANG_MILLIS = 10_000;
    private static final long SECONDS = Long.getLong("quelea.test.videoStressSeconds", 12);

    private final List<Stage> stages = new ArrayList<>();
    private final List<LyricDrawer> drawers = new ArrayList<>();
    private final List<VidDisplay> displays = new ArrayList<>();
    private int baseline;

    @BeforeEach
    public void setUp() {
        VideoTestSupport.assumeVideoAvailable();
        baseline = VideoTestSupport.activePipelines();
    }

    @AfterEach
    public void tearDown() throws Exception {
        onFxWithin(() -> {
            drawers.forEach(d -> d.setPlayVideo(false));
            displays.forEach(VidDisplay::dispose);
            stages.forEach(Stage::close);
        }, "tear down");
        assertTrue(waitFor(() -> VideoTestSupport.activePipelines() == baseline, 10_000),
                "Every video pipeline should be released");
    }

    @Test
    @DisplayName("Switching video themes quickly in three visible previews")
    public void switchVideoThemesInVisiblePreviews(@TempDir Path dir) throws Exception {
        ThemeDTO[] themes = {
                videoTheme(Files.copy(VideoTestSupport.TEST_VIDEO.toPath(), dir.resolve("a.mp4"))),
                videoTheme(copyResource("short-with-audio.mp4", dir)),
                videoTheme(copyResource("short-with-audio-2.mp4", dir)),
        };
        List<DisplayCanvas> canvases = new ArrayList<>();
        onFxWithin(() -> {
            HBox box = new HBox();
            for (int i = 0; i < 3; i++) {
                DisplayCanvas canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
                StackPane holder = new StackPane(canvas);
                holder.setPrefSize(320, 180);
                box.getChildren().add(holder);
                LyricDrawer drawer = new LyricDrawer();
                drawer.setCanvas(canvas);
                canvases.add(canvas);
                drawers.add(drawer);
            }
            show(new Scene(box, 960, 180));
        }, "create previews");

        Random random = new Random(718);
        AtomicInteger switches = new AtomicInteger();
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(SECONDS);
        while (System.nanoTime() < end) {
            int drawer = random.nextInt(drawers.size());
            ThemeDTO theme = themes[random.nextInt(themes.length)];
            onFxWithin(() -> {
                drawers.get(drawer).setTheme(theme);
                // Force the renderer to upload the current background, like
                // a visible preview does, while frames keep being replaced.
                Node background = canvases.get(drawer).getCanvasBackground();
                if (background instanceof ImageView view && view.getImage() != null) {
                    new ImageView(view.getImage()).snapshot(null, null);
                }
            }, "switch theme " + switches.incrementAndGet());
            Thread.sleep(20 + random.nextInt(90));
        }
        for (DisplayCanvas canvas : canvases) {
            assertTrue(waitFor(() -> {
                Node background = canvas.getCanvasBackground();
                Image image = background instanceof ImageView view ? view.getImage() : null;
                return image != null && image.getWidth() > 1;
            }, 15_000), "Every preview should still show video after " + switches + " switches");
        }
    }

    @Test
    @DisplayName("Short looping videos with sound while the position is polled")
    public void loopShortVideosWithSound(@TempDir Path dir) throws Exception {
        Path[] videos = {copyResource("short-with-audio.mp4", dir), copyResource("short-with-audio-2.mp4", dir)};
        AtomicInteger positions = new AtomicInteger();
        int[] frames = new int[4];
        onFxWithin(() -> {
            HBox box = new HBox();
            for (int i = 0; i < frames.length; i++) {
                // With sound, like the live video panel: every loop restarts
                // the audio output, and the position is polled meanwhile.
                VidDisplay display = new VidDisplay();
                int index = i;
                display.setOnFrame(() -> frames[index]++);
                display.setLoop(true);
                display.setOnPosChanged(p -> positions.incrementAndGet());
                display.setURI(videos[i % videos.length].toUri());
                ImageView view = new ImageView();
                view.imageProperty().bind(display.imageProperty());
                view.setFitWidth(240);
                view.setPreserveRatio(true);
                box.getChildren().add(view);
                displays.add(display);
            }
            show(new Scene(box, 960, 180));
            displays.forEach(VidDisplay::play);
        }, "start players");

        Random random = new Random(42);
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(SECONDS);
        int ticks = 0;
        int[] lastFrames = new int[frames.length];
        long[] lastProgress = new long[frames.length];
        java.util.Arrays.fill(lastProgress, System.nanoTime());
        while (System.nanoTime() < end) {
            // A pipeline that deadlocked natively stops delivering frames,
            // even if the FX thread is fine.
            int[] now = onFxWithin(frames::clone, "count frames");
            for (int i = 0; i < frames.length; i++) {
                if (now[i] != lastFrames[i]) {
                    lastFrames[i] = now[i];
                    lastProgress[i] = System.nanoTime();
                } else if (System.nanoTime() - lastProgress[i] > TimeUnit.MILLISECONDS.toNanos(FX_HANG_MILLIS)) {
                    String stacks = threadStacks();
                    holdForInspection();
                    fail("Player " + i + " stopped showing frames after " + now[i] + " (pid "
                            + ProcessHandle.current().pid() + ")" + stacks);
                }
            }
            VidDisplay display = displays.get(random.nextInt(displays.size()));
            int action = random.nextInt(10);
            onFxWithin(() -> {
                // Mostly let them loop, sometimes seek or restart like an operator.
                if (action == 0) {
                    display.seek(random.nextDouble());
                } else if (action == 1) {
                    display.stop();
                    display.play();
                }
            }, "tick " + ++ticks);
            Thread.sleep(50 + random.nextInt(100));
        }
        assertTrue(positions.get() > 0, "Position updates should have been delivered");
    }

    private void show(Scene scene) {
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stages.add(stage);
    }

    private static ThemeDTO videoTheme(Path video) {
        ThemeDTO d = ThemeDTO.DEFAULT_THEME;
        return new ThemeDTO(d.getSerializableFont(), d.getFontPaint(), d.getTranslateSerializableFont(),
                d.getTranslateFontPaint(), new VideoBackground(video.toString(), false), d.getShadow(),
                false, false, false, false, -1, 0);
    }

    private static Path copyResource(String name, Path dir) throws IOException {
        try (InputStream in = VideoStressTest.class.getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Missing test resource " + name);
            }
            Path target = dir.resolve(name);
            Files.copy(in, target);
            return target;
        }
    }

    /**
     * Run on the FX thread and wait, failing with the thread stacks if the FX
     * thread doesn't get to it in time (it's blocked, typically in native code).
     */
    private static void onFxWithin(Runnable runnable, String what) throws Exception {
        onFxWithin(() -> {
            runnable.run();
            return null;
        }, what);
    }

    private static <T> T onFxWithin(java.util.concurrent.Callable<T> callable, String what) throws Exception {
        CompletableFuture<T> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                done.complete(callable.call());
            } catch (Throwable ex) {
                done.completeExceptionally(ex);
            }
        });
        try {
            return done.get(FX_HANG_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException hung) {
            String stacks = threadStacks();
            holdForInspection();
            return fail("The FX thread didn't respond within " + FX_HANG_MILLIS + " ms (" + what + ", pid "
                    + ProcessHandle.current().pid() + ")" + stacks);
        }
    }

    /**
     * With -Dquelea.test.videoStressHoldFile=path, write this JVM's pid to that
     * file and wait, so native stacks can be sampled (e.g. with macOS
     * {@code sample}) before the test fails and the JVM exits.
     */
    private static void holdForInspection() {
        String holdFile = System.getProperty("quelea.test.videoStressHoldFile");
        if (holdFile != null) {
            try {
                Files.writeString(Path.of(holdFile), Long.toString(ProcessHandle.current().pid()));
                Thread.sleep(20_000);
            } catch (IOException | InterruptedException ex) {
                // Just fail without waiting.
            }
        }
    }

    private static String threadStacks() {
        StringBuilder stacks = new StringBuilder();
        for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
            String name = e.getKey().getName();
            if (name.contains("JavaFX") || name.startsWith("Video") || name.contains("gstreamer")) {
                stacks.append('\n').append(name).append(':');
                for (StackTraceElement el : e.getValue()) {
                    stacks.append("\n    at ").append(el);
                }
            }
        }
        return stacks.toString();
    }
}
