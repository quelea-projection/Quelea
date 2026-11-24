package org.quelea.windows.main;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import org.quelea.services.utils.LoggerUtils;

/** A small, throttled mirror of the live display. All methods run on the FX thread. */
final class PictureInPictureMirror {
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final int MAX_WIDTH = 480;
    private static final int MAX_HEIGHT = 270;
    private static final long FRAME_INTERVAL = 100_000_000L;

    private final Supplier<? extends Node> source;
    private final ImageView imageView;
    private WritableImage buffer;
    private boolean active;
    private boolean pending;
    private boolean failureLogged;
    private long generation;
    private long lastFrame;
    private Node lastSource;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (!pending && (lastFrame == 0 || now - lastFrame >= FRAME_INTERVAL)) {
                lastFrame = now;
                capture();
            }
        }
    };

    PictureInPictureMirror(Supplier<? extends Node> source, ImageView imageView) {
        this.source = source;
        this.imageView = imageView;
    }

    void start() {
        if (!active) {
            active = true;
            generation++;
            lastFrame = 0;
            timer.start();
        }
    }

    void stop() {
        active = false;
        generation++;
        timer.stop();
        imageView.setImage(null);
        buffer = null;
        lastSource = null;
    }

    private void capture() {
        Node node = source.get();
        if (node != lastSource) {
            lastSource = node;
            generation++;
            buffer = null;
            imageView.setImage(null);
        }
        if (node == null || node.getScene() == null) {
            return;
        }
        Bounds bounds = node.getLayoutBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        if (!(width > 0 && height > 0) || !Double.isFinite(width) || !Double.isFinite(height)) {
            return;
        }
        double scale = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
        int pixelWidth = Math.max(1, (int) Math.ceil(width * scale));
        int pixelHeight = Math.max(1, (int) Math.ceil(height * scale));
        if (buffer == null || buffer.getWidth() != pixelWidth || buffer.getHeight() != pixelHeight) {
            buffer = new WritableImage(pixelWidth, pixelHeight);
        }
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX() * scale, bounds.getMinY() * scale,
                pixelWidth, pixelHeight));
        parameters.setTransform(new Scale(scale, scale));
        long request = generation;
        pending = true;
        try {
            node.snapshot(result -> {
                pending = false;
                failureLogged = false;
                if (active && request == generation) {
                    imageView.setImage(result.getImage());
                }
                return null;
            }, parameters, buffer);
        } catch (RuntimeException ex) {
            pending = false;
            if (!failureLogged) {
                LOGGER.log(Level.WARNING, "Could not update picture-in-picture preview", ex);
                failureLogged = true;
            }
        }
    }
}
