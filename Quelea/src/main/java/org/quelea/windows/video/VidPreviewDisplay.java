package org.quelea.windows.video;

import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.quelea.services.utils.GStreamerInitState;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.library.VideoListPanel;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VidPreviewDisplay {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private PlayBin playBin;

    public VidPreviewDisplay() {
        if (GStreamerInitState.INIT_SUCCESS) {
            playBin = new PlayBin("playbin preview");
            playBin.setVideoSink(Gst.parseLaunch("fakesink"));
        }
    }

    public synchronized Image getPreviewImg(URI uri) {
        if (uri == null || playBin == null) {
            return VideoListPanel.BLANK;
        }

        LOGGER.log(Level.INFO, "PROCESSING URI " + uri);
        playBin.setURI(uri);
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
            Structure capsStruct = s.getCaps().getStructure(0);
            int width = capsStruct.getInteger("width");
            int height = capsStruct.getInteger("height");
            Buffer activeBuffer = s.getBuffer();
            PixelBuffer<ByteBuffer> pixelBuffer = new PixelBuffer<>(width, height,
                    activeBuffer.map(false), PixelFormat.getByteBgraPreInstance());
            WritableImage img = new WritableImage(pixelBuffer);
            playBin.stop();
            playBin.getState();
            return img;
        }
    }

}
