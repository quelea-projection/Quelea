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
package org.quelea.windows.main;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.newsong.ThemePanel;
import org.quelea.windows.video.VideoStills;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders theme thumbnails (e.g. for the remote control) on an off-screen
 * canvas, so no window has to be shown to produce them. Video backgrounds are
 * shown as a still frame (see {@link VideoStills}); no video keeps playing.
 */
public final class ThemeThumbnails {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final int WIDTH = 200;
    public static final int HEIGHT = 150;
    private static final long VIDEO_STILL_TIMEOUT_SECONDS = 10;

    /** Created lazily and reused; only touched on the FX thread. */
    private static StackPane root;
    private static DisplayCanvas canvas;
    private static LyricDrawer drawer;

    private ThemeThumbnails() {
    }

    /**
     * Render a thumbnail of the given theme with sample lyrics. Must not be
     * called on the FX thread: for a video theme this waits (up to a timeout)
     * for a still frame of the video to be grabbed.
     *
     * @param theme the theme to render.
     * @return the thumbnail.
     */
    public static Image render(ThemeDTO theme) {
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Mustn't be called on the FX thread");
        }
        if (theme.getBackground() instanceof VideoBackground) {
            File videoFile = ((VideoBackground) theme.getBackground()).getVideoFile();
            CompletableFuture<Object> stillLoaded = new CompletableFuture<>();
            Platform.runLater(() -> VideoStills.load(videoFile).thenAccept(stillLoaded::complete));
            try {
                stillLoaded.get(VIDEO_STILL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                LOGGER.log(Level.INFO, "Timed out waiting for a still of {0}", videoFile);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Couldn't get a still of " + videoFile, ex);
            }
        }
        AtomicReference<Image> ret = new AtomicReference<>();
        Utils.fxRunAndWait(() -> ret.set(renderOnFxThread(theme)));
        return ret.get();
    }

    private static Image renderOnFxThread(ThemeDTO theme) {
        if (canvas == null) {
            canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            root = new StackPane(canvas);
            // The scene is never shown; it only gives the canvas a fixed size.
            new Scene(root, WIDTH, HEIGHT);
            drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
            drawer.setPlayVideo(false);
        }
        root.applyCss();
        root.layout();
        drawer.setTheme(theme);
        drawer.setText(ThemePanel.SAMPLE_LYRICS, new String[0], new String[0], false, -1);
        WritableImage image = new WritableImage(WIDTH, HEIGHT);
        canvas.snapshot(new SnapshotParameters(), image);
        return image;
    }
}
