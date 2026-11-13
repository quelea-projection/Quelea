/*
 * This file is part of Quelea, free projection software for churches.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.ndi;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mirrors the projection canvas to an NDI source that OBS and similar software
 * can receive. Nothing is captured while nobody is receiving, so an unused
 * source costs the projector output nothing.
 * <p>
 * Call everything here on the JavaFX thread.
 */
public final class NdiOutput {

    public static final NdiOutput INSTANCE = new NdiOutput();

    /** Largest frame to send, also the size used when there's no projector. */
    static final int MAX_WIDTH = 1920;
    static final int MAX_HEIGHT = 1080;

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private final AtomicBoolean capturing = new AtomicBoolean();
    private final ChangeListener<Boolean> showingListener = (ov, wasShowing, showing) -> sizeHiddenCanvas();
    private NdiSender sender;
    private ScheduledExecutorService scheduler;
    private DisplayStage stage;
    private WritableImage image;
    private String runningName;
    private int runningFrameRate;

    private NdiOutput() {
    }

    /**
     * The pixel size to send for a canvas: as sharp as the screen shows it, but
     * no larger than {@link #MAX_WIDTH} by {@link #MAX_HEIGHT}.
     *
     * @param width the canvas width in JavaFX units.
     * @param height the canvas height in JavaFX units.
     * @param outputScale the screen's pixels per JavaFX unit.
     * @return the width and height, both even, or null if the canvas is empty.
     */
    static int[] frameSize(double width, double height, double outputScale) {
        if (width < 2 || height < 2) {
            return null;
        }
        double scale = Math.min(outputScale, Math.min(MAX_WIDTH / width, MAX_HEIGHT / height));
        return new int[]{(int) (width * scale) & ~1, (int) (height * scale) & ~1};
    }

    /**
     * Start, stop or restart the output to match the current settings.
     */
    public void applySettings() {
        QueleaProperties properties = QueleaProperties.get();
        boolean enabled = properties.getNdiOutputEnabled();
        String name = properties.getNdiSourceName();
        int frameRate = properties.getNdiFrameRate();
        if (enabled && sender != null && name.equals(runningName) && frameRate == runningFrameRate) {
            return;
        }
        stop();
        if (enabled) {
            start(name, frameRate);
        }
    }

    private void start(String name, int frameRate) {
        try {
            sender = new NdiSender(name, frameRate);
        } catch (LinkageError ex) {
            // Devolay reports a missing runtime from its static initialiser
            LOGGER.log(Level.WARNING, "NDI runtime not available", ex);
            Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("ndi.runtime.missing.title"),
                    LabelGrabber.INSTANCE.getLabel("ndi.runtime.missing.text"));
            return;
        }
        runningName = name;
        runningFrameRate = frameRate;
        stage = QueleaApp.get().getProjectionWindow();
        stage.showingProperty().addListener(showingListener);
        sizeHiddenCanvas();
        scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "ndi-capture");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::tick, 0, 1_000_000_000L / frameRate, TimeUnit.NANOSECONDS);
        LOGGER.log(Level.INFO, "NDI output \"{0}\" started at {1} fps", new Object[]{name, frameRate});
    }

    /**
     * Stop the output. Safe to call when it isn't running.
     */
    public void stop() {
        if (sender == null) {
            return;
        }
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        stage.showingProperty().removeListener(showingListener);
        sender.close();
        sender = null;
        image = null;
        capturing.set(false);
        LOGGER.log(Level.INFO, "NDI output stopped");
    }

    /**
     * Without a projector the window is never shown, which leaves the canvas
     * with no size at all, so give it a standard one to draw at.
     */
    private void sizeHiddenCanvas() {
        Region root = (Region) stage.getScene().getRoot();
        if (stage.isShowing()) {
            root.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            root.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        } else if (stage.getCanvas().getWidth() < 2) {
            root.setMinSize(MAX_WIDTH, MAX_HEIGHT);
            root.setPrefSize(MAX_WIDTH, MAX_HEIGHT);
            root.setMaxSize(MAX_WIDTH, MAX_HEIGHT);
            root.resize(MAX_WIDTH, MAX_HEIGHT);
            root.layout();
        }
    }

    /**
     * Runs on the scheduler thread, so that an idle source doesn't wake the
     * JavaFX thread at all.
     */
    private void tick() {
        try {
            if (capturing.compareAndSet(false, true)) {
                if (sender.hasReceivers()) {
                    Platform.runLater(this::capture);
                } else {
                    capturing.set(false);
                }
            }
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "NDI tick failed", ex); // Must not escape, or the schedule stops
            capturing.set(false);
        }
    }

    private void capture() {
        NdiSender.Frame frame = null;
        try {
            if (sender != null) {
                DisplayCanvas canvas = stage.getCanvas();
                double width = canvas.getWidth();
                double height = canvas.getHeight();
                int[] size = frameSize(width, height, stage.getOutputScaleX());
                frame = size == null ? null : sender.acquire(size[0], size[1]);
                if (frame != null) {
                    snapshot(canvas, frame, size[0], size[1], width, height);
                    return;
                }
            }
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "NDI capture failed", ex);
        }
        capturing.set(false);
    }

    private void snapshot(DisplayCanvas canvas, NdiSender.Frame frame, int pixelWidth, int pixelHeight, double width, double height) {
        if (image == null || image.getWidth() != pixelWidth || image.getHeight() != pixelHeight) {
            image = new WritableImage(pixelWidth, pixelHeight);
        }
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.BLACK);
        parameters.setTransform(new Scale(pixelWidth / width, pixelHeight / height));
        // The callback form joins the next pulse instead of forcing a render of its own
        canvas.snapshot(result -> {
            try {
                if (sender != null) {
                    result.getImage().getPixelReader().getPixels(0, 0, pixelWidth, pixelHeight,
                            PixelFormat.getByteBgraPreInstance(), frame.getPixels(), frame.getLineStride());
                    sender.submit(frame);
                }
            } finally {
                capturing.set(false);
            }
            return null;
        }, parameters, image);
    }
}
