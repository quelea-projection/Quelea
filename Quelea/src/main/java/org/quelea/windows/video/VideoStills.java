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

import javafx.animation.PauseTransition;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.quelea.services.utils.GStreamerInitState;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Still images of video backgrounds, for previews that aren't on screen (and
 * for theme thumbnails sent to the remote control). Each still is grabbed once
 * by a short-lived, video-only player that is disposed as soon as it has a
 * frame, so nothing keeps decoding in the background. Stills are cached per
 * video file.
 * <p>
 * All methods must be called on the FX thread.
 */
public final class VideoStills {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    /** Stills are only used for small previews, so don't keep full-size frames. */
    private static final double MAX_WIDTH = 640;
    /** Grab the frame this far into the video (or 10% in, if shorter) to skip fade-ins. */
    private static final long GRAB_POSITION_NANOS = 1_000_000_000L;
    private static final Duration GRAB_TIMEOUT = Duration.seconds(8);

    /** Empty optional = grabbing failed, so it isn't retried on every redraw. */
    private static final Map<String, Optional<Image>> CACHE = new HashMap<>();
    private static final Map<String, CompletableFuture<Optional<Image>>> PENDING = new HashMap<>();
    /** Grabs run one at a time, so a schedule full of video themes doesn't start them all at once. */
    private static final Queue<File> QUEUE = new ArrayDeque<>();
    private static boolean grabbing;

    private VideoStills() {
    }

    /**
     * Get a still image of the given video if one has already been grabbed.
     *
     * @param videoFile the video.
     * @return the still, or null if it isn't available (yet).
     */
    public static Image getCached(File videoFile) {
        Utils.checkFXThread();
        if (videoFile == null) {
            return null;
        }
        Optional<Image> cached = CACHE.get(key(videoFile));
        return cached == null ? null : cached.orElse(null);
    }

    /**
     * Get a still image of the given video, grabbing one if necessary.
     *
     * @param videoFile the video.
     * @return a future completed on the FX thread with the still, or with an
     * empty optional if no still could be grabbed.
     */
    public static CompletableFuture<Optional<Image>> load(File videoFile) {
        Utils.checkFXThread();
        if (!GStreamerInitState.INIT_SUCCESS || videoFile == null || !videoFile.isFile()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        String key = key(videoFile);
        Optional<Image> cached = CACHE.get(key);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        CompletableFuture<Optional<Image>> pending = PENDING.get(key);
        if (pending == null) {
            pending = new CompletableFuture<>();
            PENDING.put(key, pending);
            QUEUE.add(videoFile);
            grabNext();
        }
        return pending;
    }

    private static String key(File videoFile) {
        // Include the modification time so a replaced file gets a new still.
        return videoFile.getAbsolutePath() + "@" + videoFile.lastModified();
    }

    private static void grabNext() {
        if (grabbing || QUEUE.isEmpty()) {
            return;
        }
        grabbing = true;
        File videoFile = QUEUE.remove();
        String key = key(videoFile);

        VidDisplay vid = new VidDisplay(false);
        PauseTransition timeout = new PauseTransition(GRAB_TIMEOUT);
        Image[] still = new Image[1];
        boolean[] seeked = new boolean[1];
        boolean[] done = new boolean[1];

        Runnable finish = () -> {
            if (done[0]) {
                return;
            }
            done[0] = true;
            timeout.stop();
            vid.setOnFrame(null);
            vid.dispose();
            if (still[0] == null) {
                LOGGER.log(Level.INFO, "Couldn't grab a still image from {0}", videoFile);
            }
            Optional<Image> result = Optional.ofNullable(still[0]);
            CACHE.put(key, result);
            grabbing = false;
            CompletableFuture<Optional<Image>> pending = PENDING.remove(key);
            if (pending != null) {
                pending.complete(result);
            }
            grabNext();
        };

        vid.setOnFrame(() -> {
            if (done[0]) {
                return;
            }
            Image frame = copyFrame(vid.imageProperty().get());
            if (frame != null) {
                still[0] = frame;
            }
            long duration = vid.getDuration();
            if (!seeked[0] && duration > 0) {
                // Keep the first frame as a fallback, then move a little way in.
                seeked[0] = true;
                vid.seek(Math.min(0.1, (double) GRAB_POSITION_NANOS / duration));
                return;
            }
            finish.run();
        });
        timeout.setOnFinished(e -> finish.run());
        timeout.play();
        vid.setURI(videoFile.toURI());
        vid.pause(); // Prerolling in PAUSED delivers a frame without playing.
    }

    /**
     * Copy a frame into a normal image: the video frame shares its memory with
     * the native buffer, which goes away when the player is disposed.
     */
    private static Image copyFrame(Image frame) {
        if (frame == null || frame.getWidth() <= 1 || frame.getHeight() <= 1) {
            return null;
        }
        ImageView view = new ImageView(frame);
        view.setPreserveRatio(true);
        if (frame.getWidth() > MAX_WIDTH) {
            view.setFitWidth(MAX_WIDTH);
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.BLACK);
        return view.snapshot(params, null);
    }
}
