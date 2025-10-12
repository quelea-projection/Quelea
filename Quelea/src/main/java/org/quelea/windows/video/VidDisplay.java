package org.quelea.windows.video;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.quelea.services.utils.GStreamerInitState;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Consumer;

public class VidDisplay {

    private static int idCounter = 0;

    private PlayBin playBin;
    private FXImageSink fxImageSink;
    private final int id;
    private boolean loop;
    private URI uri;
    private Consumer<Double> posChanged;
    private Runnable onFinished;
    private static final Image BLANK_IMG;

    static {
        BLANK_IMG = new WritableImage(1, 1);
        ((WritableImage) BLANK_IMG).getPixelWriter().setColor(0, 0, Color.BLACK);
    }

    public VidDisplay() {
        id = idCounter++;
        posChanged = d -> {
        };
        onFinished = () -> {
        };
        if(GStreamerInitState.INIT_SUCCESS) {
            fxImageSink = new FXImageSink();
            playBin = new PlayBin("playbin " + id);
            playBin.setVideoSink(fxImageSink.getSinkElement());
            playBin.getBus().connect((Bus.EOS) source -> {
                if (loop) {
                    playBin.seekSimple(Format.TIME, EnumSet.of(SeekFlags.FLUSH), 0);
                } else {
                    onFinished.run();
                }
            });
            Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                long dur = playBin.queryDuration(Format.TIME);
                long pos = playBin.queryPosition(Format.TIME);
                if (dur > 0) {
                    double relPos = (double) pos / dur;
                    posChanged.accept(relPos);
                }
            }));
            timer.setCycleCount(Animation.INDEFINITE);
            timer.play();
        }
    }

    public ReadOnlyObjectProperty<? extends Image> imageProperty() {
        if (fxImageSink == null) {
            return new ReadOnlyObjectWrapper<>(BLANK_IMG);
        }
        return fxImageSink.imageProperty();
    }

    public void play() {
        if (playBin != null) {
            playBin.play();
        }
    }

    public void pause() {
        if (playBin != null) {
            playBin.pause();
        }
    }

    public void stop() {
        if (playBin != null) {
            playBin.stop();
            fxImageSink.clear();
        }
    }

    public void setURI(URI uri) {
        if (!Objects.equals(this.uri, uri)) {
            this.uri = uri;
            if (playBin != null) {
                playBin.setURI(uri);
            }
        }
    }

    public void setVolume(double volume) {
        if (playBin != null) {
            playBin.setVolume(volume);
        }
    }

    public URI getUri() {
        return uri;
    }

    public boolean isLoop() {
        return loop;
    }

    public int getId() {
        return id;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setOnPosChanged(Consumer<Double> callback) {
        this.posChanged = callback;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void seek(double seekPos) {
        long dur = playBin.queryDuration(Format.TIME);
        if (dur > 0) {
            playBin.seekSimple(Format.TIME, EnumSet.of(SeekFlags.FLUSH), (long) (seekPos * dur));
        }
    }
}
