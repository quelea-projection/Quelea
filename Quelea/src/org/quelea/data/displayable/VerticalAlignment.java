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
 * Used for describing the position the vertical alignment of text can take.
 *
 * @author Greg
 */
public enum VerticalAlignment {

    TOP(LabelGrabber.INSTANCE.getLabel("top")),
    CENTRE(LabelGrabber.INSTANCE.getLabel("centre")),
    BOTTOM(LabelGrabber.INSTANCE.getLabel("bottom"));

    private String friendlyString;
    /**
     * Creates the enumeration for holding vertical alignment settings.
     * @param friendlyString The "friendly" string to show for this enum
     */
    VerticalAlignment(String friendlyString) {
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
     * Parse a VerticalAlignment based on its friendly string.
     *
     * @param val the friendly string.
     * @return the text alignment object.
     */
    public static VerticalAlignment parse(String val) {
        if (val.equals(LabelGrabber.INSTANCE.getLabel("top"))) {
            return VerticalAlignment.TOP;
        } else if (val.equals(LabelGrabber.INSTANCE.getLabel("bottom"))) {
            return VerticalAlignment.BOTTOM;
        } else if (val.equals(LabelGrabber.INSTANCE.getLabel("centre"))) {
            return VerticalAlignment.CENTRE;
        } else {
            return null;
        }
    }
}
