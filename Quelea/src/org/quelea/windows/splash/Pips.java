/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.splash;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.quelea.services.utils.Utils;

/**
 * A group of text that's just "..." - makes it more extensible if we wish to
 * animate these at any point in the future.
 * <p/>
 * @author Michael
 */
public class Pips extends Group {

    private final int HEIGHT_OF_BOUNCE=4;
    private final int MILLI_TIMEOUT_SPEED= 40; //slower is higher number //this is the speed at which each pip moves
    private final int OFFSET=150; //this is the time offset between each pip animation
    private final int TIME_BEFORE_RESTART=1000; //before the pip does it's thing again
    public Pips(Font font, Paint paint) {
        double width = Toolkit.getToolkit().getFontLoader().getFontMetrics(font).computeStringWidth(".");
        for (int i = 0; i < 3; i++) {
            final Text pip = new Text(".");
            pip.setFill(paint);
            pip.setFont(font);
            pip.setLayoutX(i * width);
            getChildren().add(pip);
            final int finI = i;
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {

                    Runnable upAndDown = new Runnable() {

                        @Override
                        public void run() {
                            int yOffset = 0;
                            double initPipY = pip.getLayoutY();
                            for (yOffset = 0; yOffset <= HEIGHT_OF_BOUNCE; yOffset++) {
                                final int finY = yOffset;
                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        pip.setLayoutY(pip.getLayoutY() - finY);
                                    }
                                });
                                Utils.sleep(MILLI_TIMEOUT_SPEED);
                            }
                            Utils.sleep(MILLI_TIMEOUT_SPEED);
                            for (yOffset =0; yOffset <=HEIGHT_OF_BOUNCE; yOffset++) {
                                final int finY = yOffset;
                               Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        pip.setLayoutY(pip.getLayoutY() + finY);
                                    }
                                });
                                Utils.sleep(MILLI_TIMEOUT_SPEED);
                            }

                        }
                    };
                    Utils.sleep(finI * OFFSET);
                    while (pip.isVisible()) {
                        upAndDown.run();
                        Utils.sleep(TIME_BEFORE_RESTART);

                    }

                }
            });
            t.start();
        }
    }
}
