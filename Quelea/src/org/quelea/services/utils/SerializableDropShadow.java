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
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * A class that wraps DropShadow to make it serializable.
 * @author Michael
 */
public class SerializableDropShadow implements Serializable {
    
    private SerializableColor color;
    private double xOffset;
    private double yOffset;
    
    public SerializableDropShadow(Color color, double xOffset, double yOffset) {
        this.color = new SerializableColor(color);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public Color getColor() {
        return color.getColor();
    }

    public double getOffsetX() {
        return xOffset;
    }

    public double getOffsetY() {
        return yOffset;
    }
    
    @Override
    public String toString() {
        return getColor().toString() + " X: " + xOffset + " Y: " + yOffset;
    }
    
    public DropShadow getDropShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(getColor());
        shadow.setOffsetX(getOffsetX());
        shadow.setOffsetY(getOffsetY());
        return shadow;
    }
    
}
