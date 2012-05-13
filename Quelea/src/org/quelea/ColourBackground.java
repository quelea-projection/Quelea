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
package org.quelea;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * A background of a single colour.
 *
 * @author Michael
 */
public class ColourBackground implements Background {

    private Color colour;

    /**
     * Create a new background that's a certain colour.
     *
     * @param colour the colour of the background.
     */
    public ColourBackground(Color colour) {
        this.colour = colour;
    }

    /**
     * Get the current colour of this background, or null if the background is
     * currently an image.
     *
     * @return the colour of the background.
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Get this colour background with a specific dimension.
     * @param width the width of the image to return.
     * @param height the width of the image to return.
     * @param key not used (just leave as null).
     * @return a BufferedImage in the given dimensions of the background's
     * colour.
     */
    @Override
    public BufferedImage getImage(int width, int height, String key) {
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = ret.getGraphics();
        graphics.setColor(colour);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return ret;
    }

    /**
     * Get the DB string of this particular colour background.
     * @return the DB string.
     */
    @Override
    public String toDBString() {
        StringBuilder ret = new StringBuilder();
        ret.append("$backgroundcolour:").append(getColour());
        return ret.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ColourBackground other = (ColourBackground) obj;
        if(!Objects.equals(this.colour, other.colour)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.colour);
        return hash;
    }
}
