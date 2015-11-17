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
package org.quelea.services.utils;

import java.io.Serializable;
import java.util.Objects;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 *
 * @author Michael
 */
public class SerializableFont implements Serializable {

    private String family;
    private String name;
    private double size;
    private String style;

    public SerializableFont(Font font) {
        family = font.getFamily();
        name = font.getName();
        size = font.getSize();
        style = font.getStyle();
    }
    
    public boolean isBold() {
        return style.toLowerCase().contains("bold");
    }
    
    public boolean isItalic() {
        return style.toLowerCase().contains("italic");
    }

    public Font getFont() {
        Font ret;
        if(isBold() && isItalic()) {
            ret = Font.font(family, FontWeight.BOLD, FontPosture.ITALIC, size);
        }
        else if(isBold()) {
            ret = Font.font(family, FontWeight.BOLD, FontPosture.REGULAR, size);
        }
        else if(isItalic()) {
            ret = Font.font(family, FontWeight.NORMAL, FontPosture.ITALIC, size);
        }
        else {
            ret = Font.font(family, size);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.family);
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.size) ^ (Double.doubleToLongBits(this.size) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final SerializableFont other = (SerializableFont) obj;
        if(!Objects.equals(this.family, other.family)) {
            return false;
        }
        if(Double.doubleToLongBits(this.size) != Double.doubleToLongBits(other.size)) {
            return false;
        }
        return true;
    }

}
