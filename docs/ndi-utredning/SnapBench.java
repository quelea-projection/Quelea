import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Measures what a Quelea-like 1080p canvas costs to capture on the FX thread.
 * Scene content mimics the live projection: a full-frame PixelBuffer image that
 * is rewritten every pulse (as FXImageSink does for video) plus lyric text.
 */
public class SnapBench extends Application {

    record Mode(String name, int w, int h, int fps, boolean capture) {}

    static final int SRC_W = 1920, SRC_H = 1080, SECONDS = 6;
    static final List<Mode> MODES = List.of(
            new Mode("warmup (discard)", SRC_W, SRC_H, 0, false),
            new Mode("baseline (no capture)", SRC_W, SRC_H, 0, false),
            new Mode("1080p @ 30", 1920, 1080, 30, true),
            new Mode("1080p @ 60", 1920, 1080, 60, true),
            new Mode("720p  @ 30", 1280, 720, 30, true),
            new Mode("1080p @ 10", 1920, 1080, 10, true),
            new Mode("baseline again", SRC_W, SRC_H, 0, false));

    IntBuffer videoPixels;
    PixelBuffer<IntBuffer> pixelBuffer;
    StackPane canvas;
    int modeIdx = -1;
    long modeStart, lastCapture, lastPulse;
    int pulses, captures, tick;
    final List<Double> snapMs = new ArrayList<>(), readMs = new ArrayList<>(), gapMs = new ArrayList<>();
    WritableImage target;
    ByteBuffer out;
    SnapshotParameters params;
    int lastPixel, changedFrames;

    @Override
    public void start(Stage stage) {
        videoPixels = ByteBuffer.allocateDirect(SRC_W * SRC_H * 4).order(java.nio.ByteOrder.nativeOrder()).asIntBuffer();
        pixelBuffer = new PixelBuffer<>(SRC_W, SRC_H, videoPixels, PixelFormat.getIntArgbPreInstance());
        ImageView video = new ImageView(new WritableImage(pixelBuffer));
        Text lyric = new Text("Amazing grace, how sweet the sound\nThat saved a wretch like me");
        lyric.setFont(Font.font(72));
        lyric.setFill(Color.WHITE);
        canvas = new StackPane(video, lyric);
        canvas.setMinSize(SRC_W, SRC_H);
        canvas.setPrefSize(SRC_W, SRC_H);
        canvas.setMaxSize(SRC_W, SRC_H);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(new javafx.scene.Group(canvas), SRC_W, SRC_H, Color.BLACK));
        stage.setX(0);
        stage.setY(0);
        stage.show();
        if (Boolean.getBoolean("hidden")) {
            // Quelea keeps its main window open while the projection stage is hidden.
            Stage mainWindow = new Stage();
            mainWindow.setScene(new Scene(new StackPane(new Text("main window")), 300, 200));
            mainWindow.show();
            stage.hide();
        }
        System.out.println("pipeline=" + com.sun.prism.GraphicsPipeline.getPipeline().getClass().getSimpleName()
                + " window=" + canvas.getWidth() + "x" + canvas.getHeight());
        new AnimationTimer() {
            @Override public void handle(long now) { pulse(now); }
        }.start();
    }

    void nextMode(long now) {
        if (modeIdx >= 0) report();
        modeIdx++;
        if (modeIdx >= MODES.size()) {
            Platform.exit();
            return;
        }
        Mode m = MODES.get(modeIdx);
        modeStart = now;
        lastCapture = 0;
        lastPulse = 0;
        pulses = 0;
        captures = 0;
        snapMs.clear();
        readMs.clear();
        gapMs.clear();
        target = new WritableImage(m.w(), m.h());
        out = ByteBuffer.allocateDirect(m.w() * m.h() * 4);
        params = new SnapshotParameters();
        params.setFill(Color.BLACK);
        if (m.w() != SRC_W) params.setTransform(new Scale((double) m.w() / SRC_W, (double) m.h() / SRC_H));
    }

    void pulse(long now) {
        if (modeIdx >= MODES.size()) return; // a last pulse can arrive after Platform.exit()
        if (modeIdx < 0 || now - modeStart > SECONDS * 1_000_000_000L) {
            nextMode(now);
            if (modeIdx >= MODES.size()) return;
        }
        Mode m = MODES.get(modeIdx);
        if (lastPulse != 0) gapMs.add((now - lastPulse) / 1e6);
        lastPulse = now;
        pulses++;
        int t = ++tick;
        pixelBuffer.updateBuffer(b -> {
            int color = 0xFF000000 | ((t * 3) & 0xFF) << 16 | ((t * 5) & 0xFF) << 8 | ((t * 7) & 0xFF);
            int[] row = new int[SRC_W];
            Arrays.fill(row, color);
            for (int y = 0; y < SRC_H; y += 1) {
                videoPixels.put(y * SRC_W, row);
            }
            return null;
        });
        if (m.capture() && now - lastCapture >= 1_000_000_000L / m.fps() - 2_000_000L) {
            lastCapture = now;
            long a = System.nanoTime();
            canvas.snapshot(params, target);
            long b = System.nanoTime();
            out.clear();
            target.getPixelReader().getPixels(0, 0, m.w(), m.h(), PixelFormat.getByteBgraPreInstance(), out, m.w() * 4);
            long c = System.nanoTime();
            snapMs.add((b - a) / 1e6);
            readMs.add((c - b) / 1e6);
            captures++;
            int bgra = out.getInt(0);
            if (bgra != lastPixel) { changedFrames++; lastPixel = bgra; }
        }
    }

    static double pct(List<Double> v, double p) {
        if (v.isEmpty()) return 0;
        double[] s = v.stream().mapToDouble(Double::doubleValue).sorted().toArray();
        return s[(int) Math.min(s.length - 1, Math.floor(p * s.length))];
    }

    void report() {
        Mode m = MODES.get(modeIdx);
        double busy = (snapMs.stream().mapToDouble(d -> d).sum() + readMs.stream().mapToDouble(d -> d).sum()) / (SECONDS * 1000.0) * 100;
        System.out.println("  distinct captured frames=" + changedFrames + " of " + captures + " lastPixelBGRA=" + Integer.toHexString(lastPixel));
        changedFrames = 0;
        System.out.printf("%-22s pulses/s=%5.1f  cap/s=%5.1f  snapshot p50=%6.2f p95=%6.2f ms  readback p50=%6.2f p95=%6.2f ms  pulseGap p95=%6.1f max=%6.1f ms  fxBusy=%4.1f%%%n",
                m.name(), pulses / (double) SECONDS, captures / (double) SECONDS,
                pct(snapMs, .5), pct(snapMs, .95), pct(readMs, .5), pct(readMs, .95),
                pct(gapMs, .95), pct(gapMs, 1), busy);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
