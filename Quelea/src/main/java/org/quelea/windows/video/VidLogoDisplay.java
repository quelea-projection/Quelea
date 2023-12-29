package org.quelea.windows.video;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.quelea.services.utils.GStreamerInitState;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;

public class VidLogoDisplay {

    public static final VidLogoDisplay INSTANCE = new VidLogoDisplay();


    private PlayBin playBin;
    private FXImageSink fxImageSink;
    private URI uri;
    private static final Image BLANK_IMG;

    static {
        BLANK_IMG = new WritableImage(1, 1);
        ((WritableImage) BLANK_IMG).getPixelWriter().setColor(0, 0, Color.BLACK);
    }

    private VidLogoDisplay() {
        if (GStreamerInitState.INIT_SUCCESS) {
            fxImageSink = new FXImageSink();
            playBin = new PlayBin("playbin logo");
            playBin.setVideoSink(fxImageSink.getSinkElement());
            playBin.getBus().connect((Bus.EOS) source -> {
                playBin.seekSimple(Format.TIME, EnumSet.of(SeekFlags.FLUSH), 0);
            });
            playBin.setVolume(0);
        }
    }

    public ReadOnlyObjectProperty<? extends Image> imageProperty() {
        if (fxImageSink == null) {
            return new ReadOnlyObjectWrapper<>(BLANK_IMG);
        }
        return fxImageSink.imageProperty();
    }


    public void setURI(URI uri) {
        if (!Objects.equals(this.uri, uri)) {
            this.uri = uri;
            if (playBin != null) {
                playBin.stop();
                fxImageSink.clear();
                playBin.setURI(uri);
                playBin.play();
            }
        }
    }


}
