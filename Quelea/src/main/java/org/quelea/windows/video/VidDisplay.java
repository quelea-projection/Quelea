package org.quelea.windows.video;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.elements.PlayFlags;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.quelea.services.utils.GStreamerInitState;

import java.net.URI;
import java.util.EnumSet;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VidDisplay {

    private static int idCounter = 0;
    /** Number of displays holding a native pipeline that hasn't been disposed. */
    private static final AtomicInteger ACTIVE_PIPELINES = new AtomicInteger();
    /**
     * Pipelines are shut down off the FX thread: setting a pipeline to NULL can
     * block (on macOS the FX thread is also the AppKit main thread, which some
     * GStreamer elements need during shutdown), and that must never freeze the UI.
     */
    private static final ExecutorService CLEANUP_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Video pipeline cleanup");
        t.setDaemon(true);
        return t;
    });

    private volatile PlayBin playBin;
    private FXImageSink fxImageSink;
    private final int id;
    private boolean loop;
    private URI uri;
    private Consumer<Double> posChanged;
    private Runnable onFinished;
    private Timeline posTimer;
    private boolean disposed;
    private static final Image BLANK_IMG;

    static {
        BLANK_IMG = new WritableImage(1, 1);
        ((WritableImage) BLANK_IMG).getPixelWriter().setColor(0, 0, Color.BLACK);
    }

    public VidDisplay() {
        this(true);
    }

    /**
     * @param withAudio false to decode video only - no audio decoder or audio
     * device is set up at all.
     */
    VidDisplay(boolean withAudio) {
        id = idCounter++;
        posChanged = d -> {
        };
        onFinished = () -> {
        };
        if(GStreamerInitState.INIT_SUCCESS) {
            fxImageSink = new FXImageSink();
            playBin = new PlayBin("playbin " + id);
            ACTIVE_PIPELINES.incrementAndGet();
            playBin.setVideoSink(fxImageSink.getSinkElement());
            if (!withAudio) {
                Set<PlayFlags> flags = EnumSet.noneOf(PlayFlags.class);
                flags.addAll(playBin.getFlags());
                flags.remove(PlayFlags.AUDIO);
                flags.remove(PlayFlags.TEXT);
                playBin.setFlags(flags);
            }
            playBin.getBus().connect((Bus.EOS) source -> {
                PlayBin pb = playBin;
                if (pb == null) {
                    return;
                }
                if (loop) {
                    pb.seekSimple(Format.TIME, EnumSet.of(SeekFlags.FLUSH), 0);
                } else {
                    onFinished.run();
                }
            });
            // Only runs while playing (see play()/stop()). A permanently running
            // timeline would be referenced by the JavaFX toolkit forever, keeping
            // this display - and its native pipeline - from ever being collected.
            posTimer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                if (playBin == null) {
                    return;
                }
                long dur = playBin.queryDuration(Format.TIME);
                long pos = playBin.queryPosition(Format.TIME);
                if (dur > 0) {
                    double relPos = (double) pos / dur;
                    posChanged.accept(relPos);
                }
            }));
            posTimer.setCycleCount(Animation.INDEFINITE);
        }
    }

    /**
     * Get the number of video displays that currently hold a native GStreamer
     * pipeline, i.e. that have been created and not yet disposed. Useful for
     * diagnostics and tests: every display that is no longer needed should be
     * disposed, otherwise its pipeline keeps decoding in the background.
     *
     * @return the number of live pipelines.
     */
    public static int getActivePipelineCount() {
        return ACTIVE_PIPELINES.get();
    }

    public ReadOnlyObjectProperty<? extends Image> imageProperty() {
        if (fxImageSink == null) {
            return new ReadOnlyObjectWrapper<>(BLANK_IMG);
        }
        return fxImageSink.imageProperty();
    }

    public void play() {
        if (playBin != null) {
            playBin.play();
            posTimer.play();
        }
    }

    public void pause() {
        if (playBin != null) {
            playBin.pause();
        }
    }

    public void stop() {
        if (playBin != null) {
            posTimer.stop();
            playBin.stop();
            fxImageSink.clear();
        }
    }

    /**
     * Stop playback and release the native GStreamer pipeline. The display
     * can't be used again after this has been called.
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (playBin != null) {
            final PlayBin pb = playBin;
            playBin = null;
            ACTIVE_PIPELINES.decrementAndGet();
            posTimer.stop();
            fxImageSink.dispose();
            CLEANUP_EXECUTOR.execute(() -> {
                pb.stop();
                pb.dispose();
            });
        }
        uri = null;
    }

    public void setURI(URI uri) {
        if (!Objects.equals(this.uri, uri)) {
            this.uri = uri;
            if (playBin != null) {
                playBin.setURI(uri);
            }
        }
    }

    public void setVolume(double volume) {
        if (playBin != null) {
            playBin.setVolume(volume);
        }
    }

    public URI getUri() {
        return uri;
    }

    public boolean isLoop() {
        return loop;
    }

    public int getId() {
        return id;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setOnPosChanged(Consumer<Double> callback) {
        this.posChanged = callback;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    /**
     * @return the duration in nanoseconds, or a value <= 0 if not yet known.
     */
    long getDuration() {
        PlayBin pb = playBin;
        return pb == null ? -1 : pb.queryDuration(Format.TIME);
    }

    void setOnFrame(Runnable onFrame) {
        if (fxImageSink != null) {
            fxImageSink.setOnFrame(onFrame);
        }
    }

    public void seek(double seekPos) {
        if (playBin == null) {
            return;
        }
        long dur = playBin.queryDuration(Format.TIME);
        if (dur > 0) {
            playBin.seekSimple(Format.TIME, EnumSet.of(SeekFlags.FLUSH), (long) (seekPos * dur));
        }
    }
}
