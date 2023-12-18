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
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper connecting a GStreamer AppSink and a JavaFX Image, making use of
 * {@link PixelBuffer} to directly access the native GStreamer pixel data.
 * <p>
 * Use {@link #imageProperty()} to access the JavaFX image. The Image should
 * only be used on the JavaFX application thread, and is only valid while it is
 * the current property value. Using the Image when it is no longer the current
 * property value may cause errors or crashes.
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
    private final ReadOnlyObjectWrapper<Image> image;
    private Sample activeSample;
    private Buffer activeBuffer;
    private List<Sample> oldSamples;

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
        oldSamples = new ArrayList<>();
        sink.set("emit-signals", true);
        sink.connect((AppSink.NEW_SAMPLE) elem -> {
            Sample s = elem.pullSample();
            if (s == null) throw new IllegalStateException();
            Platform.runLater(() -> updateImage(s));
            return FlowReturn.OK;
        });
        sink.connect((AppSink.NEW_PREROLL) appsink -> {
            Sample s = appsink.pullPreroll();
            if (s == null) throw new IllegalStateException();
            Platform.runLater(() -> updateImage(s));
            return FlowReturn.OK;
        });
        sink.setCaps(Caps.fromString(DEFAULT_CAPS));
        image = new ReadOnlyObjectWrapper<>();
    }

    /**
     * Property wrapping the current video frame as a JavaFX {@link Image}. The
     * Image should only be accessed on the JavaFX application thread. Use of
     * the Image when it is no longer the current value of this property may
     * cause errors or crashes.
     *
     * @return image property for current video frame
     */
    public ReadOnlyObjectProperty<Image> imageProperty() {
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
        if (newSample == null) {
            throw new RuntimeException();
        }
        Sample oldSample = activeSample;
        Buffer oldBuffer = activeBuffer;

        activeSample = newSample;
        Structure capsStruct = newSample.getCaps().getStructure(0);
        int width = capsStruct.getInteger("width");
        int height = capsStruct.getInteger("height");
        activeBuffer = newSample.getBuffer();

        PixelBuffer<ByteBuffer> pixelBuffer = new PixelBuffer<>(width, height,
                activeBuffer.map(false), PixelFormat.getByteBgraPreInstance());
        WritableImage img = new WritableImage(pixelBuffer);
        image.set(img);

        if (oldSample != null) oldSamples.add(oldSample);
        if (oldBuffer != null) {
            oldBuffer.unmap();
        }

        while (oldSamples.size() > 2) {
            oldSamples.remove(0).dispose();
        }

    }

    /**
     * Clear any image and dispose of underlying native buffers. Can be called
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

    private void clearImage() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX application thread");
        }
        image.set(null);
        if (activeBuffer != null) {
            activeBuffer.unmap();
            activeBuffer = null;
        }
        if (activeSample != null) {
            activeSample.dispose();
            activeSample = null;
        }
    }
}
