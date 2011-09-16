/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
 *
 * @author Michael
 */
public class Notice {
    
    private String text;
    private int times;

    public Notice(String str, int times) {
        this.text = str;
        this.times = times;
    }
    
    public void copyAttributes(Notice other) {
        this.text = other.text;
        this.times = other.times;
    }
    
    public String getText() {
        return text;
    }

    public int getTimes() {
        return times;
    }
    
    public void decrementTimes() {
        times--;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimes(int times) {
        this.times = times;
    }
    
    public String toString() {
        return text;
    }

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

    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.text);
        hash = 47 * hash + this.times;
        return hash;
    }
    
}
