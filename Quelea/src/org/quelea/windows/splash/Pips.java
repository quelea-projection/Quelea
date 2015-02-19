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
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A group of text that's just "..." - makes it more extensible if we wish to
 * animate these at any point in the future.
 * <p/>
 * @author Michael
 */
public class Pips extends Group {

    public Pips(Font font, Paint paint) {
        double width = Toolkit.getToolkit().getFontLoader().getFontMetrics(font).computeStringWidth(".");
        for(int i = 0; i < 3; i++) {
            Text pip = new Text(".");
            pip.setFill(paint);
            pip.setFont(font);
            pip.setLayoutX(i * width);
            getChildren().add(pip);
        }
    }
}
