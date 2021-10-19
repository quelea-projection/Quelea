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

import java.util.Calendar;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.services.utils.QueleaProperties;

/**
 * A simple JavaFX clock.
 * <p>
 * @author Michael
 */
public class Clock extends Text {

    public Clock() {
        setFontSize(50);
        setFill(QueleaProperties.get().getStageLyricsColor());
        bindToTime();
        setOpacity(0.8);
    }

    public void setFontSize(double fontSize) {
        setFont(Font.font("Noto Sans", FontWeight.BOLD, FontPosture.REGULAR, fontSize));
        setFill(QueleaProperties.get().getStageChordColor());
    }

    private void bindToTime() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), (ActionEvent actionEvent) -> {
                    Calendar time = Calendar.getInstance();
                    String hourString;
                    boolean s24h = QueleaProperties.get().getUse24HourClock();

                    // Get user visibility setting. There is only one clock at the moment but if there are
                    // ever more clocks we will need to tell the constructor which clock we are looking at
                    // to get the correct property
                    boolean sShowClock = QueleaProperties.get().getStageShowClock();

                    // Only bother updating the text is we are actually showing the clock
                    if( sShowClock ) {
                        if (s24h) {
                            hourString = pad(2, "0", time.get(Calendar.HOUR_OF_DAY) + "");
                        } else {
                            hourString = pad(2, "0", time.get(Calendar.HOUR) + "");
                        }
                        String minuteString = pad(2, "0", time.get(Calendar.MINUTE) + "");
                        String text1 = hourString + ":" + minuteString;
                        if (!s24h) {
                            text1 += (time.get(Calendar.AM_PM) == Calendar.AM) ? " AM" : " PM";
                        }
                        setText(text1);
                    }

                    // Set visibility after updating the text as otherwise we might get a slight glitch when turning
                    // the clock from hidden to visible!
                    setVisible( sShowClock );
                }),
                new KeyFrame(Duration.seconds(1))
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
    private static String pad(int fieldWidth, String padChar, String s) {
        StringBuilder sb = new StringBuilder();
        var paddingAmount = Math.max(0,fieldWidth-s.length());

        sb.append(padChar.repeat(paddingAmount));
        sb.append(s);

        return sb.toString();
    }
}
