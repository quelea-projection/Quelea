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
package org.quelea.services.notice;

import java.util.Objects;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;

/**
 * A notice to be displayed on the bottom of the main projection screen.
 * @author Michael
 */
public class Notice {
    
    private SerializableColor color;
    private SerializableFont font;
    private String text;
    private int times;

    /**
     * Create a new notice.
     * @param str the notice text.
     * @param times the number of times to display the notice.
     */
    public Notice(String str, int times, SerializableColor color, SerializableFont font) {
        this.text = str;
        this.times = times;
        this.color = color;
        this.font = font;
    }
    
    /**
     * Copy attributes from one notice to this notice.
     * @param other the other notice.
     */
    public void copyAttributes(Notice other) {
        this.text = other.text;
        this.times = other.times;
        this.color = other.color;
        this.font = other.font;
    }
    
    /**
     * Get the notice text.
     * @return the notice text.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the number of times this notice should be displayed.
     * @return the number of times this notice should be displayed.
     */
    public int getTimes() {
        return times;
    }
    
    /**
     * Decrement the times - call after the notice has been displayed once.
     */
    public void decrementTimes() {
        times--;
    }

    /**
     * Set the notice text.
     * @param text the notice text.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Set the number of times this notice should display.
     * @param times the number of times this notice should display.
     */
    public void setTimes(int times) {
        this.times = times;
    }

    public SerializableColor getColor() {
        return color;
    }

    public void setColor(SerializableColor color) {
        this.color = color;
    }

    public SerializableFont getFont() {
        return font;
    }

    public void setFont(SerializableFont font) {
        this.font = font;
    }
    
    /**
     * Convert to a string.
     * @return the notice text.
     */
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.color);
        hash = 11 * hash + Objects.hashCode(this.font);
        hash = 11 * hash + Objects.hashCode(this.text);
        hash = 11 * hash + this.times;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Notice other = (Notice) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.font, other.font)) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this.times != other.times) {
            return false;
        }
        return true;
    }

}
