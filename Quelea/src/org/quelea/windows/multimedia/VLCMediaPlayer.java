/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.multimedia;

import com.sun.jna.Memory;
import java.nio.ByteBuffer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

/**
 *
 * @author Michael
 */
public class VLCMediaPlayer extends Canvas {

    private final PixelWriter pixelWriter;
    private final WritablePixelFormat<ByteBuffer> pixelFormat;
    private final DirectMediaPlayerComponent mediaPlayerComponent;

    public VLCMediaPlayer() {
        pixelWriter = getGraphicsContext2D().getPixelWriter();
        pixelFormat = PixelFormat.getByteBgraInstance();
        mediaPlayerComponent = new TestMediaPlayerComponent();
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                refresh();
            }
        });
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                refresh();
            }
        });
    }

    private void refresh() {
        if(mediaPlayerComponent.getMediaPlayer().isPlaying()) {
            long pos = mediaPlayerComponent.getMediaPlayer().getTime();
            mediaPlayerComponent.getMediaPlayer().stop();
            mediaPlayerComponent.getMediaPlayer().play();
            mediaPlayerComponent.getMediaPlayer().setTime(pos);
        }
    }

    /**
     * Implementation of a direct rendering media player component that renders
     * the video to a JavaFX canvas.
     */
    private class TestMediaPlayerComponent extends DirectMediaPlayerComponent {

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
            Memory nativeBuffer = nativeBuffers[0];
            ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
            pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
        }

        public TestMediaPlayerComponent() {
            super(new TestBufferFormatCallback());
        }
    }

    /**
     * Callback to get the buffer format to use for video playback.
     */
    private class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            int width = (int) getWidth();
            int height = (int) getHeight();
            if(width <= 10) {
                width = 10;
            }
            if(height <= 10) {
                height = 10;
            }
            return new RV32BufferFormat(width, height);
        }
    }

    public void stop() {
        if(mediaPlayerComponent.getMediaPlayer().isPlaying()) {
            mediaPlayerComponent.getMediaPlayer().stop();
        }
    }

    public boolean isPlaying() {
        return mediaPlayerComponent.getMediaPlayer().isPlaying();
    }

    public void dispose() {
        mediaPlayerComponent.getMediaPlayer().release();
    }

    public void setRepeat(boolean repeat) {
        mediaPlayerComponent.getMediaPlayer().setRepeat(repeat);
    }

    public void play() {
        mediaPlayerComponent.getMediaPlayer().play();
    }

    public void pause() {
        mediaPlayerComponent.getMediaPlayer().pause();
    }

    public void load(String file) {
        mediaPlayerComponent.getMediaPlayer().prepareMedia(file);
    }
    
    public long getCurrentTime() {
        return mediaPlayerComponent.getMediaPlayer().getTime();
    }
    
    public long getTotalTime() {
        return mediaPlayerComponent.getMediaPlayer().getLength();
    }
}
