/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 */
package org.quelea.windows.video;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;

import java.nio.ByteOrder;

/**
 * A wrapper connecting a GStreamer AppSink and a JavaFX Image, displaying
 * native frames without copying their pixels during playback.
 * <p>
 * Use {@link #imageProperty()} to access the JavaFX image. The Image should
 * only be used on the JavaFX application thread. Its pixels are updated while
 * video dimensions stay the same. On a stop or resize the final frame is copied
 * to owned storage, so replaced images remain valid after native cleanup.
 */
public class FXImageSink {

    private final static String DEFAULT_CAPS;

    static {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            DEFAULT_CAPS = "video/x-raw, format=BGRx";
        } else {
            DEFAULT_CAPS = "video/x-raw, format=xRGB";
        }
    }

    private final AppSink sink;
    private final ReadOnlyObjectWrapper<WritableImage> image;
    private NativeVideoImage nativeImage;
    /**
     * Passes decoded samples to the FX thread. Only the latest frame is kept:
     * if the FX thread falls behind, superseded frames are dropped instead of
     * piling up (each one is a full native video frame).
     */
    private final LatestFrameHandoff<Sample> handoff =
            new LatestFrameHandoff<>(Platform::runLater, this::updateImage, Sample::dispose);
    private Runnable onFrame;

    /**
     * Create an FXImageSink. A new AppSink element will be created that can be
     * accessed using {@link #getSinkElement()}.
     */
    public FXImageSink() {
        this(new AppSink("FXImageSink"));
    }

    /**
     * Create an FXImageSink wrapping the provided AppSink element.
     *
     * @param sink AppSink element
     */
    public FXImageSink(AppSink sink) {
        this.sink = sink;
        sink.set("emit-signals", true);
        sink.connect((AppSink.NEW_SAMPLE) elem -> {
            Sample s = elem.pullSample();
            if (s == null) throw new IllegalStateException();
            handoff.offer(s);
            return FlowReturn.OK;
        });
        sink.connect((AppSink.NEW_PREROLL) appsink -> {
            Sample s = appsink.pullPreroll();
            if (s == null) throw new IllegalStateException();
            handoff.offer(s);
            return FlowReturn.OK;
        });
        sink.setCaps(Caps.fromString(DEFAULT_CAPS));

        image = new ReadOnlyObjectWrapper<>(new WritableImage(1, 1));
    }

    /**
     * Property wrapping the current video frame as a JavaFX {@link Image}. The
     * Image should only be accessed on the JavaFX application thread. Replaced
     * images keep their own pixel storage and remain safe to render.
     *
     * @return image property for current video frame
     */
    public ReadOnlyObjectProperty<? extends Image> imageProperty() {
        return image.getReadOnlyProperty();
    }

    /**
     * Get access to the AppSink element this class wraps.
     *
     * @return AppSink element
     */
    public AppSink getSinkElement() {
        return sink;
    }

    private void updateImage(Sample newSample) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX application thread");
        }
        try {
            Structure capsStruct = newSample.getCaps().getStructure(0);
            int width = capsStruct.getInteger("width");
            int height = capsStruct.getInteger("height");
            if (width <= 0 || height <= 0) {
                return;
            }
            Buffer buffer = newSample.getBuffer();
            if (buffer == null) {
                return;
            }
            int byteCount = Math.multiplyExact(Math.multiplyExact(width, height), Integer.BYTES);
            NativeVideoImage.MappedFrame frame = NativeVideoImage.MappedFrame.map(buffer, byteCount);
            if (frame == null) {
                return;
            }
            try {
                boolean resized = nativeImage == null || !nativeImage.hasSize(width, height);
                if (resized) {
                    detachImage();
                    nativeImage = new NativeVideoImage(width, height);
                }
                nativeImage.setFrame(frame);
                frame = null; // ownership transferred to the image
                nativeImage.markDirty();
                if (resized) {
                    image.set(nativeImage.image());
                }
            } finally {
                if (frame != null) {
                    frame.close();
                }
            }
        } finally {
            newSample.dispose();
        }
        if (onFrame != null) {
            onFrame.run();
        }
    }

    /**
     * Clear the image and release pending samples. Can be called
     * from any thread, but clearing will happen asynchronously if not called on
     * JavaFX application thread.
     */
    public void clear() {
        if (Platform.isFxApplicationThread()) {
            clearImage();
        } else {
            Platform.runLater(this::clearImage);
        }
    }

    /**
     * Set a callback run on the FX thread each time a new frame has been
     * written to the image. Its pixels can change when the next frame arrives.
     */
    void setOnFrame(Runnable onFrame) {
        this.onFrame = onFrame;
    }

    /**
     * Clear the image and stop accepting new frames. Any frames that arrive
     * afterwards are released immediately.
     */
    public void dispose() {
        handoff.dispose();
        clear();
    }

    private void clearImage() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX application thread");
        }
        handoff.clear();
        detachImage();
        image.set(new WritableImage(1, 1));
    }

    private void detachImage() {
        if (nativeImage != null) {
            nativeImage.detach();
            nativeImage = null;
        }
    }
}
