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
package org.quelea.windows.options;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;

/**
 * The enum used to represent text position on the lyrics panel.
 * <p/>
 * @author Michael
 */
public enum TextPosition {

    TOP(LabelGrabber.INSTANCE.getLabel("top.text.position"), Pos.TOP_CENTER), MIDDLE(LabelGrabber.INSTANCE.getLabel("middle.text.position"), Pos.CENTER), BOTTOM(LabelGrabber.INSTANCE.getLabel("bottom.text.position"), Pos.BOTTOM_CENTER);
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String description;
    private Pos layoutPos;

    public static TextPosition parseTextPosition(String position) {

        if(position.equals(LabelGrabber.INSTANCE.getLabel("top.text.position"))) {
            return TOP;
        }
        else if(position.equals(LabelGrabber.INSTANCE.getLabel("middle.text.position"))) {
            return MIDDLE;
        }
        else if(position.equals(LabelGrabber.INSTANCE.getLabel("bottom.text.position"))) {
            return BOTTOM;
        }
        LOGGER.log(Level.WARNING, "Unrecognised text position: {0}. Options are {1}, {2}, {3}", new Object[]{position, LabelGrabber.INSTANCE.getLabel("top.text.position"), LabelGrabber.INSTANCE.getLabel("middle.text.position"), LabelGrabber.INSTANCE.getLabel("bottom.text.position")});
        return null;
    }

    private TextPosition(String description, Pos layoutPos) {
        this.description = description;
        this.layoutPos = layoutPos;
    }

    public Pos getLayouPos() {
        return layoutPos;
    }

    @Override
    public String toString() {
        return description;
    }
}
