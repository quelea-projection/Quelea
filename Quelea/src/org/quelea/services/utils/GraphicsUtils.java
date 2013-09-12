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
package org.quelea.services.utils;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Responsible for drawing things with a Graphics2D that aren't supported in 
 * standard methods. Like drawing a string with an outline.
 * @author Michael
 */
public class GraphicsUtils {

    private Graphics graphics;

    /**
     * Create a new graphics utils object.
     * @param graphics the graphics to use for the modifications.
     */
    public GraphicsUtils(Graphics graphics) {
        this.graphics = graphics;
    }

    /**
     * Get the inverse color of the one currently in use.
     * @return the inverse color of the one currently in use.
     */
    public Color getInverseColor() {
        int r = 255 - graphics.getColor().getRed();
        int g = 255 - graphics.getColor().getGreen();
        int b = 255 - graphics.getColor().getBlue();
        return new Color(r, g, b);
    }

    /**
     * Draw a string with an outline.
     * @param str the string to draw.
     * @param x the x position of the string.
     * @param y the y position of the string.
     * @param outlineColor the colour of the outline.
     * @param outlineThickness the thickness of the outline (in pixels.)
     */
    public void drawStringWithOutline(String str, int x, int y, Color outlineColor, int outlineThickness) {
        if (outlineThickness > 0) {
            Color originalColor = graphics.getColor();
            graphics.setColor(outlineColor);
            graphics.drawString(str, shiftWest(x, outlineThickness), shiftNorth(y, outlineThickness));
            graphics.drawString(str, shiftWest(x, outlineThickness), shiftSouth(y, outlineThickness));
            graphics.drawString(str, shiftEast(x, outlineThickness), shiftNorth(y, outlineThickness));
            graphics.drawString(str, shiftEast(x, outlineThickness), shiftSouth(y, outlineThickness));
            graphics.setColor(originalColor);
        }
        graphics.drawString(str, x, y);
    }
    
    /**
     * Draw a string with a shadow.
     * @param str the string to draw.
     * @param x the x position of the string.
     * @param y the y position of the string.
     * @param shadowColor the colour of the shadow.
     */
    public void drawStringWithShadow(String str, int x, int y, Color shadowColor) {
        Color originalColor = graphics.getColor();
        graphics.setColor(shadowColor);
        graphics.drawString(str, shiftEast(x, 2), shiftSouth(y, 2));
        graphics.setColor(originalColor);
        graphics.drawString(str, x, y);
    }

    /**
     * Used for outline measurements. Shift north a certain distance.
     * @param p the initial point.
     * @param distance the distance to shift.
     * @return the point after shifting.
     */
    private int shiftNorth(int p, int distance) {
        return (p - distance);
    }

    /**
     * Used for outline measurements. Shift south a certain distance.
     * @param p the initial point.
     * @param distance the distance to shift.
     * @return the point after shifting.
     */
    private int shiftSouth(int p, int distance) {
        return (p + distance);
    }

    /**
     * Used for outline measurements. Shift east a certain distance.
     * @param p the initial point.
     * @param distance the distance to shift.
     * @return the point after shifting.
     */
    private int shiftEast(int p, int distance) {
        return (p + distance);
    }

    /**
     * Used for outline measurements. Shift west a certain distance.
     * @param p the initial point.
     * @param distance the distance to shift.
     * @return the point after shifting.
     */
    private int shiftWest(int p, int distance) {
        return (p - distance);
    }
}
