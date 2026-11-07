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

import com.sun.jna.Pointer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

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
    private final static int OLD_SAMPLE_BUFFER_SIZE = 2;

    private static final Field mapInfoBufferField;
    private static final Field pointerPeerField;
    private static final Field bufferAddress;
    private static final Field bufferCapacity;

    static {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            DEFAULT_CAPS = "video/x-raw, format=BGRx";
        } else {
            DEFAULT_CAPS = "video/x-raw, format=xRGB";
        }

        try {
            mapInfoBufferField = Buffer.class.getDeclaredField("mapInfo");
            mapInfoBufferField.setAccessible(true);
            pointerPeerField = Pointer.class.getDeclaredField("peer");
            pointerPeerField.setAccessible(true);
            bufferAddress = java.nio.Buffer.class.getDeclaredField("address");
            bufferAddress.setAccessible(true);
            bufferCapacity = java.nio.Buffer.class.getDeclaredField("capacity");
            bufferCapacity.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final AppSink sink;
    private final ReadOnlyObjectWrapper<WritableImage> image;
    private IntBuffer imageBuffer;
    private PixelBuffer<IntBuffer> pixelBuffer;
    private Sample activeSample;
    private Buffer activeBuffer;
    private final Queue<Sample> oldSamples;
    /**
     * The newest decoded sample that hasn't been shown yet. Only the latest
     * frame is kept: if the FX thread falls behind, superseded frames are
     * dropped instead of piling up (each one is a full native video frame).
     */
    private final AtomicReference<Sample> pendingSample = new AtomicReference<>();
    private volatile boolean disposed;
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
        oldSamples = new ArrayBlockingQueue<>(OLD_SAMPLE_BUFFER_SIZE + 1);
        sink.set("emit-signals", true);
        sink.connect((AppSink.NEW_SAMPLE) elem -> {
            Sample s = elem.pullSample();
            if (s == null) throw new IllegalStateException();
            offerSample(s);
            return FlowReturn.OK;
        });
        sink.connect((AppSink.NEW_PREROLL) appsink -> {
            Sample s = appsink.pullPreroll();
            if (s == null) throw new IllegalStateException();
            offerSample(s);
            return FlowReturn.OK;
        });
        sink.setCaps(Caps.fromString(DEFAULT_CAPS));

        imageBuffer = ByteBuffer.allocateDirect(4).asIntBuffer();
        pixelBuffer = new PixelBuffer<>(1, 1, imageBuffer, PixelFormat.getIntArgbPreInstance());
        image = new ReadOnlyObjectWrapper<>(new WritableImage(pixelBuffer));
    }

    /**
     * Property wrapping the current video frame as a JavaFX {@link Image}. The
     * Image should only be accessed on the JavaFX application thread. Use of
     * the Image when it is no longer the current value of this property may
     * cause errors or crashes.
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

    /**
     * Hand a newly decoded sample over to the FX thread. If a previous sample
     * is still waiting to be shown it is replaced (and released), so at most
     * one frame per sink is ever queued.
     */
    private void offerSample(Sample s) {
        if (disposed) {
            s.dispose();
            return;
        }
        Sample previous = pendingSample.getAndSet(s);
        if (previous != null) {
            // An update is already scheduled and will pick up the new sample.
            previous.dispose();
        } else {
            Platform.runLater(this::showPendingSample);
        }
    }

    private void showPendingSample() {
        Sample s = pendingSample.getAndSet(null);
        if (s == null) {
            return;
        }
        if (disposed) {
            s.dispose();
            return;
        }
        updateImage(s);
    }

    long lastTs = 0;
    int frames = 0;

    private void updateImage(Sample newSample) {
        long ts = System.currentTimeMillis();
        if(ts-lastTs>1000) {
//            System.out.println(frames);
            frames = 1;
            lastTs= ts;
        }
        else {
            frames++;
        }
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

        if (image.get().getWidth() != width || image.get().getHeight() != height) {
            imageBuffer = ByteBuffer.allocateDirect(width*height*4).asIntBuffer();
            pixelBuffer = new PixelBuffer<>(width, height, imageBuffer, PixelFormat.getIntArgbPreInstance());
            image.set(new WritableImage(pixelBuffer));
        }

        IntBuffer gBuffer = activeBuffer.map(false).asIntBuffer();

        try {
//            GstBufferAPI.MapInfoStruct mapInfo = (GstBufferAPI.MapInfoStruct) mapInfoBufferField.get(activeBuffer);
//            long baseAddress = (long)pointerPeerField.get(mapInfo.data);
//            long size = mapInfo.size.longValue();

            long baseAddress = bufferAddress.getLong(gBuffer);
            long size = bufferCapacity.getInt(gBuffer);

//            System.out.println(baseAddress + "  "+ size);

//            imageBuffer.rewind();

            bufferAddress.setLong(imageBuffer, baseAddress);
            bufferCapacity.setInt(imageBuffer, (int)size);
            imageBuffer.position(0);
            imageBuffer.limit((int)size);
//            System.out.println(imageBuffer.get(4140000));
//            System.out.println(gBuffer.get(4140000));
//            System.out.println(gBuffer.getClass());
//            System.out.println(imageBuffer.getClass());
//            imageBuffer.put(gBuffer);

//            imageBuffer.flip();

            pixelBuffer.updateBuffer(b -> null);


        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }

//        image.get().getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), activeBuffer.map(false), width * 4);

        if (oldSample != null) oldSamples.add(oldSample);
        if (oldBuffer != null) {
            oldBuffer.unmap();
        }
        while (oldSamples.size() > OLD_SAMPLE_BUFFER_SIZE) {
            oldSamples.remove().dispose();
        }
        if (onFrame != null) {
            onFrame.run();
        }

//        long dur = System.nanoTime() - val;
//        System.out.println("REFRESH TIME: " + dur / 1000);

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

    /**
     * Set a callback run on the FX thread each time a new frame has been
     * written to the image. The image is only valid during the callback and
     * until the next frame arrives.
     */
    void setOnFrame(Runnable onFrame) {
        this.onFrame = onFrame;
    }

    /**
     * Clear the image and stop accepting new frames. Any frames that arrive
     * afterwards are released immediately.
     */
    public void dispose() {
        disposed = true;
        clear();
    }

    private void clearImage() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX application thread");
        }
        Sample pending = pendingSample.getAndSet(null);
        if (pending != null) {
            pending.dispose();
        }
        image.set(new WritableImage(1, 1));
        if (activeBuffer != null) {
            activeBuffer.unmap();
            activeBuffer = null;
        }
        if (activeSample != null) {
            activeSample.dispose();
            activeSample = null;
        }
        while (!oldSamples.isEmpty()) {
            oldSamples.remove().dispose();
        }
    }
}
