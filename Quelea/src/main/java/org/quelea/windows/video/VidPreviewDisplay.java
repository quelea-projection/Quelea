package org.quelea.windows.video;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Gst;
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

public class VidPreviewDisplay {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private PlayBin playBin;

    public VidPreviewDisplay() {
        if (GStreamerInitState.INIT_SUCCESS) {
            playBin = new PlayBin("playbin preview");
            playBin.setVideoSink(Gst.parseLaunch("fakesink"));
            // Only a frame is needed: don't open an audio output at all.
            Set<PlayFlags> flags = EnumSet.noneOf(PlayFlags.class);
            flags.addAll(playBin.getFlags());
            flags.remove(PlayFlags.AUDIO);
            flags.remove(PlayFlags.TEXT);
            playBin.setFlags(flags);
        }
    }

    public synchronized Image getPreviewImg(URI uri) {
        if (uri == null || playBin == null) {
            return VideoListPanel.BLANK;
        }

        LOGGER.log(Level.INFO, "PROCESSING URI " + uri);
        playBin.setURI(VideoUri.forGStreamer(uri));
        playBin.getState();
        playBin.pause();
        playBin.getState();
        playBin.getState();
        Sample s = playBin.emit(Sample.class, "convert-sample", Caps.fromString("video/x-raw,format=BGRA"));
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
                    // Copy the pixels: the sample's memory is freed with the
                    // sample, while the image lives on in the UI.
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

}
