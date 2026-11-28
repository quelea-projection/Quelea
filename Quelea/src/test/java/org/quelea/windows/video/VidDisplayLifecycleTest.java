package org.quelea.windows.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.testutil.VideoTestSupport;

import static org.quelea.testutil.VideoTestSupport.activePipelines;
import static org.quelea.testutil.VideoTestSupport.nativePipelineMark;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;
import static org.quelea.testutil.VideoTestSupport.waitForNativePipelines;

/** Player lifecycle tests use GStreamer; the playback test uses the bundled video. */
@DisplayName("VidDisplay lifecycle")
public class VidDisplayLifecycleTest {

    private VidDisplay display;
    private Path temporaryVideo;
    private int baseline;
    private long nativeMark;

    @BeforeEach
    public void setUp() {
        VideoTestSupport.assumeVideoAvailable();
        baseline = activePipelines();
        nativeMark = nativePipelineMark();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (display != null) {
            onFx(display::dispose);
            Assertions.assertTrue(waitFor(() -> activePipelines() == baseline, 5_000),
                    "The test player must not leave a pipeline active");
            Assertions.assertTrue(waitForNativePipelines(nativeMark, 20_000),
                    "Every native pipeline must be shut down");
        }
        if (temporaryVideo != null) {
            Assertions.assertTrue(waitFor(() -> {
                try {
                    Files.deleteIfExists(temporaryVideo);
                    return true;
                } catch (IOException ex) {
                    return false;
                }
            }, 10_000), "The player must release its source file");
        }
    }

    @Test
    @DisplayName("Disposing a video-only player is idempotent")
    public void disposeIsIdempotent() throws Exception {
        onFx(() -> {
            display = new VidDisplay(false);
            Assertions.assertEquals(baseline + 1, activePipelines());
            display.setURI(VideoTestSupport.TEST_VIDEO.toURI());
            display.setLoop(true);
            Assertions.assertEquals(VideoTestSupport.TEST_VIDEO.toURI(), display.getUri());
            Assertions.assertTrue(display.isLoop());

            display.dispose();
            display.dispose();
            Assertions.assertEquals(baseline, activePipelines());
            Assertions.assertNull(display.getUri());
            Assertions.assertEquals(-1, display.getDuration());
            Assertions.assertDoesNotThrow(() -> {
                display.play();
                display.pause();
                display.stop();
                display.seek(0.5);
            });
        });
    }

    @Test
    @DisplayName("Repeatedly replacing video-only players releases every pipeline")
    public void repeatedPlayerReplacement() throws Exception {
        onFx(() -> {
            for (int i = 0; i < 8; i++) {
                display = new VidDisplay(false);
                Assertions.assertEquals(baseline + 1, activePipelines(), "player " + i);
                display.dispose();
                Assertions.assertEquals(baseline, activePipelines(), "player " + i);
            }
        });
    }

    @Test
    @DisplayName("A video with a non-ASCII filename can play")
    public void playsUnicodeFilename(@TempDir Path directory) throws Exception {
        temporaryVideo = Files.copy(VideoTestSupport.TEST_VIDEO.toPath(), directory.resolve("video café #1.mp4"));
        CompletableFuture<Image> frame = new CompletableFuture<>();
        onFx(() -> {
            display = new VidDisplay(false);
            display.setOnFrame(() -> frame.complete(display.imageProperty().get()));
            display.setURI(temporaryVideo.toUri());
            Assertions.assertEquals(temporaryVideo.toUri(), display.getUri());
            display.play();
        });
        Image image = frame.get(20, TimeUnit.SECONDS);
        Assertions.assertTrue(image.getWidth() > 1 && image.getHeight() > 1);
    }

    @Test
    @DisplayName("Position and end-of-video callbacks arrive on the FX thread")
    public void callbacksRunOnFxThread(@TempDir Path directory) throws Exception {
        temporaryVideo = copyResource("short-with-audio.mp4", directory);
        CompletableFuture<Boolean> finished = new CompletableFuture<>();
        AtomicInteger positions = new AtomicInteger();
        AtomicBoolean positionsOnFx = new AtomicBoolean(true);
        onFx(() -> {
            display = new VidDisplay(false);
            display.setOnPosChanged(position -> {
                positions.incrementAndGet();
                positionsOnFx.compareAndSet(true, Platform.isFxApplicationThread());
            });
            display.setOnFinished(() -> finished.complete(Platform.isFxApplicationThread()));
            display.setURI(temporaryVideo.toUri());
            display.play();
        });
        Assertions.assertTrue(finished.get(20, TimeUnit.SECONDS), "onFinished must run on the FX thread");
        Assertions.assertTrue(positions.get() > 0, "Positions should be reported while playing");
        Assertions.assertTrue(positionsOnFx.get(), "Positions must be reported on the FX thread");
    }

    @Test
    @DisplayName("Stopping releases the pipeline; playing another file starts a new one")
    public void stopAndPlayAnotherFile(@TempDir Path directory) throws Exception {
        Path first = copyResource("short-with-audio.mp4", directory);
        temporaryVideo = copyResource("short-with-audio-2.mp4", directory);
        onFx(() -> {
            display = new VidDisplay(false);
            display.setLoop(true);
            display.setURI(first.toUri());
            display.play();
        });
        Assertions.assertTrue(waitFor(() -> display.imageProperty().get().getWidth() > 1, 20_000),
                "The first video should play");
        Image firstFrame = onFx(() -> {
            Image frame = display.imageProperty().get();
            display.stop();
            Assertions.assertEquals(1, display.imageProperty().get().getWidth(), "Stopping clears the image");
            display.setURI(temporaryVideo.toUri());
            display.play();
            return frame;
        });
        Assertions.assertTrue(waitFor(() -> display.imageProperty().get().getWidth() > 1
                && display.imageProperty().get() != firstFrame, 20_000), "The second video should play");
        Assertions.assertEquals(baseline + 1, activePipelines());
        // The first file must be let go of once its pipeline is gone.
        Assertions.assertTrue(waitFor(() -> {
            try {
                Files.delete(first);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }, 10_000), "The stopped pipeline must release its file");
    }

    @Test
    @DisplayName("Resuming works when a loop pass ends just after pausing")
    public void loopPassEndsWhilePaused(@TempDir Path directory) throws Exception {
        temporaryVideo = copyResource("short-with-audio.mp4", directory);
        AtomicInteger frames = new AtomicInteger();
        onFx(() -> {
            // With sound, like a looping video item in the live panel.
            display = new VidDisplay();
            display.setLoop(true);
            display.setOnFrame(frames::incrementAndGet);
            display.setURI(temporaryVideo.toUri());
            display.play();
        });
        Assertions.assertTrue(waitFor(() -> frames.get() > 0, 20_000), "The video should play");
        onFx(() -> {
            // GStreamer reported the end of a pass while playing, but it's
            // only handled after the pause: the next pass must not be started
            // with a seek that blocks in the paused state.
            display.pause();
            display.simulateSegmentDone();
        });
        Thread.sleep(500);
        int paused = frames.get();
        onFx(display::play);
        Assertions.assertTrue(waitFor(() -> frames.get() > paused + 5, 10_000),
                "Playback should resume after the pause");
    }

    @Test
    public void turningLoopOffEarlyFinishesTheCurrentPass(@TempDir Path directory) throws Exception {
        checkFinalPass(directory, 1.0 / 3);
    }

    @Test
    public void turningLoopOffLateFinishesTheCurrentPass(@TempDir Path directory) throws Exception {
        checkFinalPass(directory, 2.0 / 3);
    }

    private void checkFinalPass(Path directory, double toggleBrightness) throws Exception {
        // Six seconds: black opening, dark grey at 1 s, light grey at 5.4 s,
        // white ending at 5.8 s. Observe rendered frames, independent of how
        // far ahead of the sinks the demuxer posts SEGMENT_DONE.
        Path clip = copyResource("loop-markers.mp4", directory);
        CompletableFuture<Void> finished = new CompletableFuture<>();
        onFx(() -> {
            display = new VidDisplay();
            display.setURI(clip.toUri());
            display.setLoop(true);
            display.setOnFrame(() -> {
                double brightness = frameBrightness();
                if (display.isLoop() && Math.abs(brightness - toggleBrightness) < 0.05) {
                    display.setLoop(false);
                } else if (!display.isLoop() && brightness < 0.1) {
                    finished.completeExceptionally(new AssertionError("Playback repeated the opening after Loop was disabled"));
                }
            });
            display.setOnFinished(() -> {
                if (!display.isLoop() && frameBrightness() > 0.9) {
                    finished.complete(null);
                } else {
                    finished.completeExceptionally(new AssertionError("Playback finished before showing the white ending"));
                }
                display.stop(); // Same action as the live controls.
            });
            display.play();
        });
        finished.get(20, TimeUnit.SECONDS);
    }

    private double frameBrightness() {
        return display.imageProperty().get().getPixelReader().getColor(0, 0).getBrightness();
    }

    private static Path copyResource(String name, Path directory) throws IOException {
        try (InputStream in = VidDisplayLifecycleTest.class.getResourceAsStream(name)) {
            Assertions.assertNotNull(in, name);
            Path target = directory.resolve(name);
            Files.copy(in, target);
            return target;
        }
    }
}
