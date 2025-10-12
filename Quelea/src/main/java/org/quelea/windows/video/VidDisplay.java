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
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.GhostPad;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.Registry;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.quelea.services.utils.GStreamerInitState;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Cross-platform video display for JavaFX using GStreamer.
 * Automatically selects GPU-based YUV→RGB conversion (D3D11, OpenGL, VAAPI)
 * with CPU fallback if unavailable.
 */
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
            playBin = new PlayBin("playbin-" + id);

            // Build the best GPU-accelerated sink available
            Bin sinkBin = buildHardwareAwareSink(fxImageSink);
            playBin.setVideoSink(sinkBin);

            // Handle end-of-stream and looping
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

    /**
     * Build a hardware-accelerated GPU→CPU sink for FXImageSink.
     * Works with D3D11 (Windows), OpenGL (Linux/macOS), VAAPI (Linux), or software fallback.
     */
    private Bin buildHardwareAwareSink(FXImageSink fxImageSink) {
        Registry registry = Registry.get();

        // 1. Choose GPU converter
        Element converter;
        Element downloader = null;

        if (registry.findPlugin("d3d11") != null) {
            converter = ElementFactory.make("d3d11convert", "gpuConvert");
            downloader = ElementFactory.make("d3d11download", "downloader");
            System.out.println("[VidDisplay] Using D3D11 GPU conversion");
        } else if (registry.findPlugin("opengl") != null) {
            converter = ElementFactory.make("glcolorconvert", "gpuConvert");
            downloader = ElementFactory.make("gldownload", "downloader");
            System.out.println("[VidDisplay] Using OpenGL GPU conversion");
        } else if (registry.findPlugin("vaapi") != null) {
            converter = ElementFactory.make("vaapipostproc", "gpuConvert");
            System.out.println("[VidDisplay] Using VAAPI GPU conversion");
        } else {
            converter = ElementFactory.make("videoconvert", "gpuConvert");
            System.out.println("[VidDisplay] Using software videoconvert");
        }

        // 2. Caps filter to match FXImageSink expectations
        Element capsFilter = ElementFactory.make("capsfilter", "capsFilter");
        Caps caps = Caps.fromString("video/x-raw,format=BGRx");
        capsFilter.setCaps(caps);

        // 3. Get FXImageSink AppSink
        AppSink appSink = fxImageSink.getSinkElement();
        appSink.set("max-buffers", 10);
        appSink.set("drop", true);

        // 4. Create bin and add elements
        Bin sinkBin = new Bin("fxSinkBin");
        if (downloader != null) sinkBin.addMany(converter, downloader, capsFilter, appSink);
        else sinkBin.addMany(converter, capsFilter, appSink);

        // 5. Link elements
        if (downloader != null) {
            converter.link(downloader);
            downloader.link(capsFilter);
        } else {
            converter.link(capsFilter);
        }
        capsFilter.link(appSink);

        // 6. Add ghost pad for bin
        Pad sinkPad = converter.getStaticPad("sink"); // first element’s sink pad
        if (sinkPad != null) {
            GhostPad ghostPad = new GhostPad("sink", sinkPad);
            sinkBin.addPad(ghostPad);
        }

        return sinkBin;
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
