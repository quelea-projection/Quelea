/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.multimedia;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Michael
 */
public class MultimediaControls extends StackPane {

    private static final Image PLAY_IMAGE = new Image("file:C:\\users\\michael\\desktop\\png\\play.png");
    private static final Image PAUSE_IMAGE = new Image("file:C:\\users\\michael\\desktop\\png\\pause.png");
    private static final Image STOP_IMAGE = new Image("file:C:\\users\\michael\\desktop\\png\\stop.png");
    private static final Image PLAY_IMAGE_DISABLE = new Image("file:C:\\users\\michael\\desktop\\png\\playdisable.png");
    private static final Image PAUSE_IMAGE_DISABLE = new Image("file:C:\\users\\michael\\desktop\\png\\pausedisable.png");
    private static final Image STOP_IMAGE_DISABLE = new Image("file:C:\\users\\michael\\desktop\\png\\stopdisable.png");
    private boolean playpause;
    private EventHandler<ActionEvent> playEvent;
    private EventHandler<ActionEvent> pauseEvent;
    private EventHandler<ActionEvent> stopEvent;
    private ImageView playButton;
    private ImageView stopButton;
    private Slider posSlider;
    private boolean disableControls;

    public MultimediaControls() {
        Rectangle rect = new Rectangle(230, 100);
        Stop[] stops = new Stop[]{new Stop(0, Color.BLACK), new Stop(1, Color.GREY)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        rect.setOpacity(0.8);
        rect.setFill(lg1);
        rect.setArcHeight(20);
        rect.setArcWidth(20);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(5.0);
        ds.setOffsetX(5.0);
        ds.setColor(Color.GRAY);
        ds.setInput(new Reflection(5, 0.4, 0.3, 0));
        rect.setEffect(ds);
        getChildren().add(rect);

        playButton = new ImageView(PLAY_IMAGE);
        setButtonParams(playButton);
        playButton.setTranslateX(-30);
        getChildren().add(playButton);
        playButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(!disableControls) {
                    playpause = !playpause;
                    if(playpause) {
                        playButton.setImage(PAUSE_IMAGE);
                        playEvent.handle(new ActionEvent());
                    }
                    else {
                        playButton.setImage(PLAY_IMAGE);
                        pauseEvent.handle(new ActionEvent());
                    }
                }
            }
        });

        stopButton = new ImageView(STOP_IMAGE);
        stopButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(!disableControls) {
                    playButton.setImage(PLAY_IMAGE);
                    playpause = false;
                    posSlider.setValue(0);
                    stopEvent.handle(new ActionEvent());
                }
            }
        });
        setButtonParams(stopButton);
        stopButton.setTranslateX(30);
        getChildren().add(stopButton);

        posSlider = new Slider(0, 1, 0);
        posSlider.setTranslateY(30);
        posSlider.setPrefWidth(rect.getWidth() - 20);
        posSlider.setMaxWidth(rect.getWidth() - 20);
        getChildren().add(posSlider);
        setOnPlay(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                VLCWindow.INSTANCE.setRepeat(false);
                VLCWindow.INSTANCE.play();
            }
        });
        setOnPause(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                VLCWindow.INSTANCE.pause();
            }
        });
        setOnStop(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                VLCWindow.INSTANCE.stop();
            }
        });
    }

    public void loadMultimedia(String path) {
        VLCWindow.INSTANCE.stop();
        VLCWindow.INSTANCE.load(path);
    }

    public void setDisableControls(boolean disable) {
        this.disableControls = disable;
        if(disable) {
            if(!playpause) {
                playButton.setImage(PLAY_IMAGE_DISABLE);
            }
            else {
                playButton.setImage(PAUSE_IMAGE_DISABLE);
            }
            stopButton.setImage(STOP_IMAGE_DISABLE);
        }
        else {
            if(!playpause) {
                playButton.setImage(PLAY_IMAGE);
            }
            else {
                playButton.setImage(PAUSE_IMAGE);
            }
            stopButton.setImage(STOP_IMAGE);
        }
        posSlider.setDisable(disable);
    }

    public void reset() {
        stopEvent.handle(new ActionEvent());
    }

    private void setButtonParams(final ImageView button) {
        button.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(!disableControls) {
                    button.setEffect(new Glow(0.5));
                }
            }
        });
        button.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                button.setEffect(null);
            }
        });
        button.setFitWidth(50);
        button.setPreserveRatio(true);
        button.setTranslateY(-10);
    }

    public final void setOnPlay(EventHandler<ActionEvent> event) {
        this.playEvent = event;
    }

    public final void setOnPause(EventHandler<ActionEvent> event) {
        this.pauseEvent = event;
    }

    public final void setOnStop(EventHandler<ActionEvent> event) {
        this.stopEvent = event;
    }
}
