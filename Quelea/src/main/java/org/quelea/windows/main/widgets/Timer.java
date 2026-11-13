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

import java.time.LocalTime;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.quelea.data.ThemeDTO;
import org.quelea.windows.timer.CountdownTimes;

/**
 * A simple JavaFX countdown timer. It either counts down a fixed duration, or
 * counts down to a time of day by following the clock.
 * <p>
 * @author Ben
 */
public class Timer extends Text {

    private final int duration;
    private final LocalTime target;
    private boolean paused;
    private boolean reset;
    private int count;
    private Timeline timeline;
    private String pretext = "";
    private String posttext = "";
    private Pos textPosition;
    private ThemeDTO theme;

    /**
     * Create a timer that counts down a fixed duration.
     *
     * @param seconds the duration in seconds.
     * @param pretext the text before the countdown.
     * @param posttext the text after the countdown.
     */
    public Timer(int seconds, String pretext, String posttext) {
        this(seconds, null, pretext, posttext);
    }

    /**
     * Create a timer that counts down to a time of day.
     *
     * @param target the time of day the timer reaches 00:00.
     * @param pretext the text before the countdown.
     * @param posttext the text after the countdown.
     */
    public Timer(LocalTime target, String pretext, String posttext) {
        this(-1, target, pretext, posttext);
    }

    private Timer(int seconds, LocalTime target, String pretext, String posttext) {
        this.duration = seconds;
        this.target = target;
        this.pretext = pretext;
        this.posttext = posttext;
        paused = true;
        bindToTime();
        setOpacity(1);
    }

    private int remaining() {
        if (target != null) {
            return CountdownTimes.secondsUntil(target, LocalTime.now());
        }
        return Math.max(0, duration - count);
    }

    private void showRemaining() {
        setText(pretext + CountdownTimes.formatRemaining(remaining()) + posttext);
    }

    private void bindToTime() {
        showRemaining();
        // A clock based timer ticks more often so it changes close to the second
        Duration tick = target == null ? Duration.seconds(1) : Duration.millis(200);
        timeline = new Timeline(new KeyFrame(tick, actionEvent -> {
            if (reset) {
                count = 0;
                reset = false;
            } else if (!paused && target == null) {
                count++;
            }
            if (!paused || target == null) {
                showRemaining();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void pause() {
        paused = true;
    }

    public void play() {
        if (timeline.getStatus() != Timeline.Status.RUNNING) {
            count = 0;
            reset = false;
            showRemaining();
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
        return pretext + CountdownTimes.formatRemaining(target == null ? duration : remaining()) + posttext;
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
