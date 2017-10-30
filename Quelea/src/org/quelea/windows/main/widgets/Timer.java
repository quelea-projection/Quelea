/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.widgets;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.quelea.data.ThemeDTO;

/**
 * A simple JavaFX countdown timer.
 * <p>
 * @author Ben
 */
public class Timer extends Text {

    private final int minutes;
    private final int seconds;
    private boolean paused;
    private boolean reset;
    private Timeline timeline;
    private String pretext = "";
    private String posttext = "";
    private Pos textPosition;
    private ThemeDTO theme;

    public Timer(int seconds, String pretext, String posttext) {
        this.minutes = (int) Math.floor(seconds / 60);
        this.seconds = seconds % 60;
        this.pretext = pretext;
        this.posttext = posttext;
        setText(pretext + (minutes > 9 ? "" : "0") + minutes + ":" + (seconds > 9 ? "" : "0") + seconds + posttext);
        bindToTime();
        setOpacity(1);
        paused = true;
    }

    private void bindToTime() {
        int starttime = minutes * 60 + seconds;
        setText(pretext + (minutes > 9 ? "" : "0") + minutes + ":" + (seconds > 9 ? "" : "0") + seconds + posttext);
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>() {
                            private int count = 0;

                            @Override
                            public void handle(ActionEvent actionEvent) {
                                if (!paused) {
                                    count++;
                                }
                                if (reset) {
                                    count = 0;
                                    reset = false;
                                }
                                int remaining = starttime - count;
                                if (remaining >= 0) {
                                    setText(pretext + ((remaining / 60) > 9 ? "" : "0") + (int) Math.floor(remaining / 60) + ":" + ((remaining % 60) > 9 ? "" : "0") + remaining % 60 + posttext);
                                }
                            }
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void pause() {
        paused = true;
    }

    public void play() {
        if (timeline.getStatus() != Timeline.Status.RUNNING) {
            setText(pretext + (minutes > 9 ? "" : "0") + minutes + ":" + (seconds > 9 ? "" : "0") + seconds + posttext);
            timeline.play();
        }
        paused = false;
    }

    public void reset() {
        reset = true;
    }
    
    public void stop() {
        reset = true;
        timeline.stop();
        setText("");
    }

    @Override
    public String toString() {
        return pretext + (minutes > 9 ? "" : "0") + minutes + ":" + (seconds > 9 ? "" : "0") + seconds + posttext;
    }

    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
        this.setEffect(theme.getShadow().getDropShadow());
        this.setFont(theme.getFont());
        this.setFill(theme.getFontPaint());
        this.textPosition = DisplayPositionSelector.getPosFromIndex(theme.getTextPosition());
        this.setTextAlignment(alignmentFromIndex(theme.getTextAlignment()));
    }

    public Pos getTextPosition() {
        return textPosition;
    }

    public void setFontSize(double pickFontSize) {
        setFont(Font.font(theme.getFont().getFamily(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                pickFontSize));

    }

    private TextAlignment alignmentFromIndex(int index) {
        switch (index) {
            case -1:
                return TextAlignment.LEFT;
            case 0:
                return TextAlignment.CENTER;
            case 1:
                return TextAlignment.RIGHT;
        }
        return TextAlignment.CENTER;
    }

    public void synchronise(Timer timer) {
        timer.reset();
        this.reset();
        timer.play();
        this.play();
    }
}
