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

import javafx.geometry.Pos;

/**
 * The enum used to represent text position on the lyrics panel.
 * @author Michael
 */
public enum TextPosition {
    
    TOP("Top", Pos.TOP_CENTER), MIDDLE("Middle", Pos.CENTER), BOTTOM("Bottom", Pos.BOTTOM_CENTER);
    
    private String description;
    private Pos layoutPos;
    
    public static TextPosition parseTextPosition(String position) {
        switch(position) {
            case "Top":return TOP;
            case "Middle":return MIDDLE;
            case "Bottom":return BOTTOM;
            default:return null;
        }
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
