package org.quelea.windows.video;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.elements.PlayFlags;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.quelea.services.utils.GStreamerInitState;
import org.quelea.services.utils.LoggerUtils;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plays a video into a JavaFX image.
 * <p>
 * All methods must be called on the FX thread, and none of them waits for
 * GStreamer: every GStreamer call (state changes, seeks and position queries)
 * runs on a thread of its own for each pipeline. A pipeline's state change can
 * block for a long time, or for ever: on macOS, stopping or starting an audio
 * output can deadlock inside CoreAudio (osxaudiosink changes the AudioUnit on
 * CoreAudio's IO thread while another thread holds the AudioUnit's lock and
 * waits for the IO thread). On the FX thread, which is also the macOS main
 * thread, that froze the whole application.
 * <p>
 * Audio outputs are only started and stopped as often as needed: a looping
 * video loops with segment seeks, which don't flush the pipeline, instead of a
 * flushing seek back to the start (which pauses and restarts the audio output)
 * at the end of every pass. Before an audio output is stopped, or a playing
 * video with sound is seeked, the pipeline is paused and the output is given
 * time to settle (see {@link #AUDIO_SETTLE_MILLIS}).
 * <p>
 * Each pipeline only goes from NULL to playing and back to NULL once:
 * {@link #stop()} and a change of URI release the current pipeline, and the
 * next {@link #play()} or {@link #pause()} creates a new one. Reusing one
 * PlayBin for another file (stop, new URI, play) while frames and state
 * changes were still in flight aborted the JVM in
 * {@code gst_play_sink_do_reconfigure} in stress tests.
 */
public class VidDisplay {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    /** Number of displays that haven't been disposed (each may hold a native pipeline). */
    private static final AtomicInteger ACTIVE_PIPELINES = new AtomicInteger();
    private static final long POSITION_INTERVAL_MILLIS = 100;
    /** How long to wait for a state change, e.g. for a paused pipeline to preroll. */
    private static final long STATE_TIMEOUT_SECONDS = 5;
    /**
     * How long a paused audio output is left alone before it is stopped or
     * restarted. osxaudiosink finishes pausing on CoreAudio's IO thread, in the
     * next IO cycle (a few milliseconds); starting or stopping the output
     * before then can deadlock CoreAudio, and with it every audio output of
     * the process.
     */
    private static final long AUDIO_SETTLE_MILLIS = 100;
    private static final Image BLANK_IMG;

    static {
        BLANK_IMG = new WritableImage(1, 1);
        ((WritableImage) BLANK_IMG).getPixelWriter().setColor(0, 0, Color.BLACK);
    }

    private final int id;
    private final boolean withAudio;
    private final boolean available;
    private final ReadOnlyObjectWrapper<Image> image;
    /** The current pipeline, or null if none has been needed since the last stop. */
    private Player player;
    private int playerCount;
    private volatile boolean loop;
    private URI uri;
    private Double volume;
    private Consumer<Double> posChanged;
    private Runnable onFinished;
    private Runnable onFrame;
    private boolean disposed;

    /**
     * Create a display that plays video and sound.
     */
    public VidDisplay() {
        this(true);
    }

    /**
     * @param withAudio false to decode video only - no audio decoder or audio
     * device is set up at all. Use this for anything that shouldn't be heard,
     * such as theme backgrounds and previews.
     */
    public VidDisplay(boolean withAudio) {
        id = ID_COUNTER.getAndIncrement();
        this.withAudio = withAudio;
        posChanged = d -> {
        };
        onFinished = () -> {
        };
        available = GStreamerInitState.INIT_SUCCESS;
        if (available) {
            image = new ReadOnlyObjectWrapper<>(new WritableImage(1, 1));
            ACTIVE_PIPELINES.incrementAndGet();
        } else {
            image = new ReadOnlyObjectWrapper<>(BLANK_IMG);
        }
    }

    /**
     * Get the number of video displays that can currently hold a native
     * GStreamer pipeline, i.e. that have been created and not yet disposed.
     * Useful for diagnostics and tests: every display that is no longer needed
     * should be disposed, otherwise its pipeline keeps decoding in the
     * background.
     *
     * @return the number of live displays.
     */
    public static int getActivePipelineCount() {
        return ACTIVE_PIPELINES.get();
    }

    public ReadOnlyObjectProperty<? extends Image> imageProperty() {
        return image.getReadOnlyProperty();
    }

    public void play() {
        Player p = player();
        if (p != null) {
            p.play();
        }
    }

    public void pause() {
        Player p = player();
        if (p != null) {
            p.pause();
        }
    }

    /**
     * Stop playback and release the current pipeline. Playing again starts
     * from the beginning, with a new pipeline.
     */
    public void stop() {
        releasePlayer();
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
        releasePlayer();
        if (available) {
            ACTIVE_PIPELINES.decrementAndGet();
        }
        uri = null;
    }

    public void setURI(URI uri) {
        if (!Objects.equals(this.uri, uri)) {
            this.uri = uri;
            // Never point an existing PlayBin at another file.
            releasePlayer();
        }
    }

    public void setVolume(double volume) {
        this.volume = volume;
        if (player != null) {
            player.setVolume(volume);
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

    /**
     * @param callback called on the FX thread with the relative position
     * (0-1) while playing.
     */
    public void setOnPosChanged(Consumer<Double> callback) {
        this.posChanged = callback;
    }

    /**
     * @param onFinished called on the FX thread when a video that doesn't loop
     * reaches its end.
     */
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    /**
     * @return the last known duration in nanoseconds, or a value <= 0 if not
     * (yet) known. Doesn't ask GStreamer, so it never blocks.
     */
    long getDuration() {
        return player == null ? -1 : player.duration;
    }

    /**
     * Ask GStreamer for the duration without blocking the FX thread.
     *
     * @param onResult called on the FX thread with the duration in
     * nanoseconds, or a value <= 0 if it isn't known.
     */
    void queryDuration(LongConsumer onResult) {
        if (player == null || !player.started) {
            Platform.runLater(() -> onResult.accept(-1));
        } else {
            player.queryDuration(onResult);
        }
    }

    /**
     * @param onFrame called on the FX thread each time a new frame has been
     * written to the image.
     */
    void setOnFrame(Runnable onFrame) {
        this.onFrame = onFrame;
    }

    /**
     * @param seekPos the position to seek to, relative to the duration (0-1).
     */
    public void seek(double seekPos) {
        if (player != null) {
            player.seek(seekPos);
        }
    }

    /** The current pipeline, created if needed; null if there can't be one. */
    private Player player() {
        if (player == null && available && !disposed) {
            player = new Player(uri);
        }
        return player;
    }

    private void releasePlayer() {
        if (player != null) {
            Player p = player;
            player = null;
            p.release();
            image.set(new WritableImage(1, 1));
        }
    }

    /**
     * One PlayBin, its video sink and the thread that talks to it. Methods are
     * called on the FX thread unless their comment says otherwise.
     */
    private final class Player {

        private final PlayBin playBin;
        private final FXImageSink sink;
        private final ScheduledThreadPoolExecutor thread;
        /** True once the pipeline has been asked to leave the NULL state. */
        private boolean started;
        private ScheduledFuture<?> positionPoll;
        private final AtomicBoolean positionPending = new AtomicBoolean();
        private final AtomicReference<Double> pendingSeek = new AtomicReference<>();
        // Only used on the pipeline thread:
        private boolean playing;
        private boolean everPlayed;
        private volatile double position;
        private volatile boolean released;
        private volatile long duration = -1;

        Player(URI uri) {
            String name = "playbin " + id + "." + (++playerCount);
            sink = new FXImageSink();
            sink.setOnFrame(() -> {
                if (player == this && onFrame != null) {
                    onFrame.run();
                }
            });
            playBin = new PlayBin(name);
            playBin.setVideoSink(sink.getSinkElement());
            if (!withAudio) {
                Set<PlayFlags> flags = EnumSet.noneOf(PlayFlags.class);
                flags.addAll(playBin.getFlags());
                flags.remove(PlayFlags.AUDIO);
                flags.remove(PlayFlags.TEXT);
                playBin.setFlags(flags);
            } else if (volume != null) {
                playBin.setVolume(volume);
            }
            if (uri != null) {
                playBin.setURI(VideoUri.forGStreamer(uri));
            }
            // Bus messages arrive on gst1-java's single dispatch thread, shared
            // by all pipelines; don't block that with a seek either.
            playBin.getBus().connect((Bus.EOS) source -> run(this::endOfStream));
            playBin.getBus().connect((Bus.SEGMENT_DONE) (source, format, position) -> run(this::segmentDone));
            playBin.getBus().connect((Bus.ERROR) (source, code, message)
                    -> LOGGER.log(Level.WARNING, "Video error in {0}: {1}", new Object[]{name, message}));
            thread = new ScheduledThreadPoolExecutor(1, r -> {
                Thread t = new Thread(r, "Video " + name);
                t.setDaemon(true);
                return t;
            });
            thread.setRemoveOnCancelPolicy(true);
            image.bind(sink.imageProperty());
        }

        void play() {
            started = true;
            run(this::startPlaying);
            if (positionPoll == null) {
                try {
                    positionPoll = thread.scheduleWithFixedDelay(this::pollPosition,
                            POSITION_INTERVAL_MILLIS, POSITION_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException alreadyReleased) {
                    // Nothing to poll.
                }
            }
        }

        void pause() {
            started = true;
            run(() -> {
                playBin.pause();
                playing = false;
            });
        }

        void setVolume(double volume) {
            if (withAudio) {
                run(() -> playBin.setVolume(volume));
            }
        }

        void seek(double seekPos) {
            if (!started) {
                return;
            }
            // Seeks can come quickly while a slider is dragged: only the
            // latest one that hasn't been done yet is done.
            if (pendingSeek.getAndSet(seekPos) == null) {
                run(() -> {
                    Double pos = pendingSeek.getAndSet(null);
                    long dur = playBin.queryDuration(Format.TIME);
                    if (pos != null && dur > 0) {
                        duration = dur;
                        flushingSeek((long) (pos * dur));
                    }
                });
            }
        }

        void queryDuration(LongConsumer onResult) {
            run(() -> {
                long dur = playBin.queryDuration(Format.TIME);
                if (dur > 0) {
                    duration = dur;
                }
                Platform.runLater(() -> onResult.accept(dur));
            });
        }

        /** Called on the pipeline thread. */
        private void startPlaying() {
            if (!everPlayed && loop) {
                // Start in segment mode, so the end of each pass is a
                // SEGMENT_DONE message instead of EOS. Seek while paused, so
                // no audio output is running yet.
                playBin.pause();
                playBin.getState(STATE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                long position = Math.max(0, playBin.queryPosition(Format.TIME));
                segmentSeek(EnumSet.of(SeekFlags.FLUSH, SeekFlags.SEGMENT), position);
            }
            everPlayed = true;
            playBin.play();
            playing = true;
        }

        /** Called on the pipeline thread, at the end of a video that isn't in segment mode. */
        private void endOfStream() {
            if (loop) {
                // Looping was turned on while playing: go back to the start,
                // and loop in segment mode from now on.
                flushingSeek(0);
            } else {
                finished();
            }
        }

        /** Called on the pipeline thread, at the end of a pass in segment mode. */
        private void segmentDone() {
            if (loop) {
                // Not flushing: playback carries on seamlessly from the start,
                // without pausing or restarting the audio output.
                segmentSeek(EnumSet.of(SeekFlags.SEGMENT), 0);
            } else {
                finished();
            }
        }

        private void finished() {
            Platform.runLater(() -> {
                if (player == this) {
                    onFinished.run();
                }
            });
        }

        /**
         * A flushing seek; one in segment mode if looping. Called on the
         * pipeline thread. Flushing pauses a running audio output, and the
         * data that follows restarts it straight away, so a video with sound
         * is paused and given time to settle first.
         */
        private void flushingSeek(long position) {
            boolean resume = playing;
            if (withAudio && everPlayed) {
                pauseAndSettle();
            }
            Set<SeekFlags> flags = loop ? EnumSet.of(SeekFlags.FLUSH, SeekFlags.SEGMENT) : EnumSet.of(SeekFlags.FLUSH);
            segmentSeek(flags, position);
            if (resume && withAudio) {
                playBin.getState(STATE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                playBin.play();
            }
        }

        /** Seek to a position, playing on to the end. Called on the pipeline thread. */
        private void segmentSeek(Set<SeekFlags> flags, long position) {
            playBin.seek(1.0, Format.TIME, flags, SeekType.SET, position, SeekType.NONE, -1);
        }

        /** Pause, and give an audio output time to settle. Called on the pipeline thread. */
        private void pauseAndSettle() {
            playBin.pause();
            playBin.getState(STATE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            try {
                Thread.sleep(AUDIO_SETTLE_MILLIS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        /** Called on the pipeline thread. */
        private void pollPosition() {
            if (released) {
                return;
            }
            long dur = playBin.queryDuration(Format.TIME);
            long pos = playBin.queryPosition(Format.TIME);
            if (dur > 0) {
                duration = dur;
                position = (double) pos / dur;
                // At most one update waits for the FX thread at a time.
                if (positionPending.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        positionPending.set(false);
                        if (player == this) {
                            posChanged.accept(position);
                        }
                    });
                }
            }
        }

        /** Run a task on the pipeline thread, unless the pipeline has been released. */
        private void run(Runnable task) {
            try {
                thread.execute(() -> {
                    if (!released) {
                        task.run();
                    }
                });
            } catch (RejectedExecutionException alreadyReleased) {
                // The pipeline is gone; nothing to do.
            }
        }

        /**
         * Stop showing frames and shut the pipeline down on its own thread.
         * The sink copies the last frame into memory of its own first, so
         * images still on screen stay valid after GStreamer frees its buffers.
         */
        void release() {
            released = true;
            if (positionPoll != null) {
                positionPoll.cancel(false);
            }
            image.unbind();
            sink.dispose();
            try {
                thread.execute(() -> {
                    try {
                        if (withAudio && everPlayed) {
                            // Going straight from playing to NULL stops the
                            // audio output right after pausing it.
                            pauseAndSettle();
                        }
                        playBin.stop();
                    } finally {
                        playBin.dispose();
                    }
                });
            } catch (RejectedExecutionException ex) {
                LOGGER.log(Level.WARNING, "Couldn't release video pipeline", ex);
            }
            thread.shutdown();
        }
    }
}
