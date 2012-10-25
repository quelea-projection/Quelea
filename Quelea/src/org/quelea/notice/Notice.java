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
package org.quelea.notice;

import java.util.Objects;

/**
 * A notice to be displayed on the bottom of the main projection screen.
 * @author Michael
 */
public class Notice {
    
    private String text;
    private int times;

    /**
     * Create a new notice.
     * @param str the notice text.
     * @param times the number of times to display the notice.
     */
    public Notice(String str, int times) {
        this.text = str;
        this.times = times;
    }
    
    /**
     * Copy attributes from one notice to this notice.
     * @param other the other notice.
     */
    public void copyAttributes(Notice other) {
        this.text = other.text;
        this.times = other.times;
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
    
    /**
     * Convert to a string.
     * @return the notice text.
     */
    public String toString() {
        return text;
    }

    /**
     * Determine if this notice equals another object.
     * @param obj the other object.
     * @return true if the objects are equal, false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Notice other = (Notice) obj;
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this.times != other.times) {
            return false;
        }
        return true;
    }

    /**
     * Get a hashcode for this notice.
     * @return the hashcode.
     */
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.text);
        hash = 47 * hash + this.times;
        return hash;
    }
    
}
