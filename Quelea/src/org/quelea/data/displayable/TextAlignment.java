/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.data.displayable;

import org.quelea.services.languages.LabelGrabber;

/**
 * Used for describing the position the alignment of text can take.
 *
 * @author mjrb5
 */
public enum TextAlignment {

    LEFT(LabelGrabber.INSTANCE.getLabel("left")),
    CENTRE(LabelGrabber.INSTANCE.getLabel("centre"));
//    RIGHT(LabelGrabber.INSTANCE.getLabel("right"));
    
    private String friendlyString;

    TextAlignment(String friendlyString) {
        this.friendlyString = friendlyString;
    }

    /**
     * Get the "friendly" string to show for this enum constant.
     *
     * @return the "friendly" string to show for this enum constant.
     */
    public String toFriendlyString() {
        return friendlyString;
    }

    /**
     * Parse a TextAlignment based on its friendly string.
     *
     * @param val the friendly string.
     * @return the text alignment object.
     */
    public static TextAlignment parse(String val) {
        if(val.equals(LabelGrabber.INSTANCE.getLabel("left"))) {
            return TextAlignment.LEFT;
        }
//        else if(val.equals(LabelGrabber.INSTANCE.getLabel("right"))) {
//            return TextAlignment.RIGHT;
//        }
        else if(val.equals(LabelGrabber.INSTANCE.getLabel("centre"))) {
            return TextAlignment.CENTRE;
        }
        else {
            return null;
        }
    }
}
