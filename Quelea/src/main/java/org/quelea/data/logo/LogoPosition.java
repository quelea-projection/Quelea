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
package org.quelea.data.logo;

import org.quelea.services.languages.LabelGrabber;

/**
 * Where on the screen the logo is anchored. The name of each constant is what
 * gets stored in the properties file, so don't rename them.
 *
 * @author Arvid
 */
public enum LogoPosition {

    TOP_LEFT(0, 0),
    TOP_CENTER(0.5, 0),
    TOP_RIGHT(1, 0),
    CENTER_LEFT(0, 0.5),
    CENTER(0.5, 0.5),
    CENTER_RIGHT(1, 0.5),
    BOTTOM_LEFT(0, 1),
    BOTTOM_CENTER(0.5, 1),
    BOTTOM_RIGHT(1, 1);

    private final double horizontal;
    private final double vertical;

    LogoPosition(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    /**
     * @return 0 for left, 0.5 for centre and 1 for right.
     */
    public double getHorizontal() {
        return horizontal;
    }

    /**
     * @return 0 for top, 0.5 for centre and 1 for bottom.
     */
    public double getVertical() {
        return vertical;
    }

    /**
     * @return the key of this position's label in the language files.
     */
    public String getLabelKey() {
        return "logo.position." + name().toLowerCase().replace('_', '.');
    }

    /**
     * @return the (translated) name of this position, as shown to the user.
     */
    public String getLabel() {
        return LabelGrabber.INSTANCE.getLabel(getLabelKey());
    }

    /**
     * Find the position with the given (translated) label.
     *
     * @param label the label, as returned by {@link #getLabel()}.
     * @return the matching position, or null if there isn't one.
     */
    public static LogoPosition fromLabel(String label) {
        for (LogoPosition position : values()) {
            if (position.getLabel().equals(label)) {
                return position;
            }
        }
        return null;
    }

    /**
     * Parse a position as stored in the properties file, falling back to
     * {@link #CENTER} (the behaviour before this setting existed) for anything
     * unknown.
     *
     * @param value the stored value, may be null.
     * @return the position.
     */
    public static LogoPosition parse(String value) {
        if (value != null) {
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                // Fall through to the default
            }
        }
        return CENTER;
    }
}
