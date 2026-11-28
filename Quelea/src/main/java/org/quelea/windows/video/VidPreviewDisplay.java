package org.quelea.windows.video;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.Fraction;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.elements.PlayFlags;
import org.quelea.services.utils.GStreamerInitState;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.library.VideoListPanel;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Grabs a still frame of a video, e.g. for the video library and search.
 */
public class VidPreviewDisplay {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private PlayBin playBin;
    private Element videoSink;

    public VidPreviewDisplay() {
        if (GStreamerInitState.INIT_SUCCESS) {
            playBin = new PlayBin("playbin preview");
            videoSink = Gst.parseLaunch("fakesink");
            playBin.setVideoSink(videoSink);
            // Only a frame is needed: don't open an audio output at all.
            Set<PlayFlags> flags = EnumSet.noneOf(PlayFlags.class);
            flags.addAll(playBin.getFlags());
            flags.remove(PlayFlags.AUDIO);
            flags.remove(PlayFlags.TEXT);
            playBin.setFlags(flags);
        }
    }

    /**
     * Get a frame near the start of a video, scaled by GStreamer to fit the
     * given size, keeping its aspect ratio (see {@link #fitSize}). Only the
     * scaled frame is copied into the image, so the Java heap never holds a
     * full-size frame, however large the video; GStreamer itself still
     * decodes, and briefly converts, the frame at full size.
     *
     * @param uri the video.
     * @param maxWidth the largest width the image should have, in pixels.
     * @param maxHeight the largest height the image should have, in pixels.
     * @return the frame; {@link VideoListPanel#UNSUPPORTED} if it can't be
     * decoded, or {@link VideoListPanel#BLANK} if there's no URI or GStreamer
     * isn't available.
     */
    public synchronized Image getPreviewImg(URI uri, int maxWidth, int maxHeight) {
        if (uri == null || playBin == null) {
            return VideoListPanel.BLANK;
        }

        LOGGER.log(Level.INFO, "PROCESSING URI " + uri);
        playBin.setURI(VideoUri.forGStreamer(uri));
        playBin.getState();
        playBin.pause();
        playBin.getState();
        playBin.getState();
        Sample s = playBin.emit(Sample.class, "convert-sample", Caps.fromString(frameCaps(maxWidth, maxHeight)));
        if (s == null) {
            playBin.stop();
            playBin.getState();
            return VideoListPanel.UNSUPPORTED;
        } else {
            WritableImage img;
            try {
                Structure capsStruct = s.getCaps().getStructure(0);
                int width = capsStruct.getInteger("width");
                int height = capsStruct.getInteger("height");
                Buffer activeBuffer = s.getBuffer();
                ByteBuffer pixels = activeBuffer.map(false);
                try {
                    // Copy the (already scaled) pixels: the sample's memory is
                    // freed with the sample, while the image lives on in the UI.
                    img = new WritableImage(width, height);
                    img.getPixelWriter().setPixels(0, 0, width, height,
                            PixelFormat.getByteBgraPreInstance(), pixels, width * 4);
                } finally {
                    activeBuffer.unmap();
                }
            } finally {
                s.dispose();
            }
            playBin.stop();
            playBin.getState();
            return img;
        }
    }

    /**
     * The caps to grab the frame with: BGRA, scaled to fit the given size if
     * the video's size is known (it is once the pipeline has prerolled).
     */
    private String frameCaps(int maxWidth, int maxHeight) {
        String caps = "video/x-raw,format=BGRA";
        Pad pad = videoSink.getStaticPad("sink");
        Caps current = pad == null ? null : pad.getCurrentCaps();
        if (current == null || current.isEmpty()) {
            return caps;
        }
        Structure video = current.getStructure(0);
        if (!video.hasIntField("width") || !video.hasIntField("height")) {
            return caps;
        }
        double pixelAspect = 1;
        if (video.hasField("pixel-aspect-ratio")) {
            Fraction par = video.getFraction("pixel-aspect-ratio");
            if (par.getNumerator() > 0 && par.getDenominator() > 0) {
                pixelAspect = par.toDouble();
            }
        }
        int[] size = fitSize(video.getInteger("width"), video.getInteger("height"), pixelAspect, maxWidth, maxHeight);
        return caps + ",width=" + size[0] + ",height=" + size[1] + ",pixel-aspect-ratio=1/1";
    }

    /**
     * The size, in square pixels, of a video frame scaled to fit a box. A
     * frame that fits already isn't scaled up, but non-square pixels are made
     * square, so the width can grow (720x576 with 64:45 pixels is 1024x576).
     *
     * @param width the frame's width, in the video's pixels.
     * @param height the frame's height.
     * @param pixelAspect the width of a video pixel relative to its height.
     * @param maxWidth the box's width.
     * @param maxHeight the box's height.
     * @return the width and height, as shown: never larger than the box, nor
     * than the frame shown with square pixels.
     */
    static int[] fitSize(int width, int height, double pixelAspect, int maxWidth, int maxHeight) {
        double displayWidth = width * pixelAspect;
        double scale = Math.min(1, Math.min(maxWidth / displayWidth, maxHeight / (double) height));
        return new int[]{
                Math.max(1, (int) Math.round(displayWidth * scale)),
                Math.max(1, (int) Math.round(height * scale))
        };
    }
}
