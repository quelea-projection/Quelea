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

import me.walkerknapp.devolay.Devolay;
import me.walkerknapp.devolay.DevolayFrameFormatType;
import me.walkerknapp.devolay.DevolayFrameFourCCType;
import me.walkerknapp.devolay.DevolaySender;
import me.walkerknapp.devolay.DevolayVideoFrame;
import org.quelea.services.utils.LoggerUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends frames over NDI on a thread of its own, so that a slow encoder can never
 * hold up the JavaFX thread that draws the projector output. Everything that
 * touches the NDI runtime is in here, to keep the library easy to replace.
 * <p>
 * The runtime keeps hold of a frame's pixels until the next frame is sent, so
 * there are two frames: one with NDI and one to capture into. When both are
 * busy, {@link #acquire} returns null and that capture is simply skipped.
 */
final class NdiSender implements AutoCloseable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final int FRAME_COUNT = 2;
    private static final int BYTES_PER_PIXEL = 4;

    /**
     * A frame of opaque BGRX pixels to fill and hand to {@link #submit}.
     */
    static final class Frame {

        private final int width;
        private final int height;
        private final ByteBuffer pixels;
        private final DevolayVideoFrame video;

        private Frame(int width, int height, int frameRate) {
            this.width = width;
            this.height = height;
            pixels = ByteBuffer.allocateDirect(width * height * BYTES_PER_PIXEL);
            video = new DevolayVideoFrame();
            video.setResolution(width, height);
            video.setFourCCType(DevolayFrameFourCCType.BGRX);
            video.setLineStride(width * BYTES_PER_PIXEL);
            video.setFrameRate(frameRate, 1);
            video.setAspectRatio((float) width / height);
            video.setFormatType(DevolayFrameFormatType.PROGRESSIVE);
            video.setData(pixels);
        }

        ByteBuffer getPixels() {
            pixels.clear();
            return pixels;
        }

        int getLineStride() {
            return width * BYTES_PER_PIXEL;
        }
    }

    private final DevolaySender sender;
    private final int frameRate;
    private final BlockingQueue<Frame> free = new ArrayBlockingQueue<>(FRAME_COUNT);
    private final BlockingQueue<Frame> ready = new ArrayBlockingQueue<>(1);
    private final Thread thread;
    private int allocated;

    /**
     * Start sending.
     *
     * @param name the name this source has on the network.
     * @param frameRate the frames per second to announce.
     * @throws LinkageError if the NDI runtime isn't installed or can't be used.
     */
    NdiSender(String name, int frameRate) {
        LOGGER.log(Level.INFO, "Using NDI runtime {0}", Devolay.getNDIVersion());
        this.frameRate = frameRate;
        sender = new DevolaySender(name, null, false, false);
        thread = new Thread(this::sendFrames, "ndi-sender");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @return true if anything on the network is receiving this source.
     */
    boolean hasReceivers() {
        return sender.getConnectionCount(0) > 0;
    }

    /**
     * Get a frame to capture into. Only call this from one thread.
     *
     * @param width the width in pixels.
     * @param height the height in pixels.
     * @return a frame of that size, or null if none is free right now.
     */
    Frame acquire(int width, int height) {
        Frame frame = free.poll();
        if (frame == null) {
            if (allocated == FRAME_COUNT) {
                return null;
            }
            allocated++;
            return new Frame(width, height, frameRate);
        }
        if (frame.width == width && frame.height == height) {
            return frame;
        }
        frame.video.close();
        return new Frame(width, height, frameRate);
    }

    /**
     * Send a frame from {@link #acquire}.
     *
     * @param frame the filled frame.
     */
    void submit(Frame frame) {
        if (!ready.offer(frame)) {
            free.offer(frame);
        }
    }

    private void sendFrames() {
        Frame sending = null;
        try {
            while (true) {
                Frame next = ready.take();
                sender.sendVideoFrameAsync(next.video);
                if (sending != null) {
                    free.offer(sending);
                }
                sending = next;
            }
        } catch (InterruptedException ex) {
            // Closing.
        } finally {
            sender.sendVideoFrameAsync(null); // Waits until NDI has let go of the pixels
            sender.close();
        }
    }

    /**
     * Stop sending and release the source.
     */
    @Override
    public void close() {
        thread.interrupt();
        try {
            thread.join(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
