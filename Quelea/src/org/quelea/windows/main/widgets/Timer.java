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
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.services.utils.QueleaProperties;

/**
 * A simple JavaFX countdown timer.
 * <p>
 * @author Michael
 */
public class Timer extends Text {

    private final int minutes;
    private final int seconds;

    public Timer(int minutes, int seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
        setFontSize(50);
        setFill(QueleaProperties.get().getStageLyricsColor());
        bindToTime();
        setOpacity(1);
    }

    public void setFontSize(double fontSize) {
        setFont(Font.font("Noto Sans", FontWeight.BOLD, FontPosture.REGULAR, fontSize));
        setFill(QueleaProperties.get().getStageChordColor());
    }

    private void bindToTime() {
        int starttime = minutes*60+seconds;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>() {
                            private int count = 0;
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                count++;
                                int remaining = starttime - count;
                                if(remaining >= 0) {
                                setText(((remaining/60)>9?"":"0") + (int) Math.floor(remaining / 60) + ":" + ((remaining%60)>9?"":"0") + remaining % 60);
                                }
                            }
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Creates a string left padded to the specified width with the supplied
     * padding character.
     * <p>
     * @param fieldWidth the length of the resultant padded string.
     * @param padChar a character to use for padding the string.
     * @param s the string to be padded.
     * @return the padded string.
     */
    private static String pad(int fieldWidth, char padChar, String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < fieldWidth; i++) {
            sb.append(padChar);
        }
        sb.append(s);

        return sb.toString();
    }

}
