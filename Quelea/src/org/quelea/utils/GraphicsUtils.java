package org.quelea.utils;

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

    private int shiftNorth(int p, int distance) {
        return (p - distance);
    }

    private int shiftSouth(int p, int distance) {
        return (p + distance);
    }

    private int shiftEast(int p, int distance) {
        return (p + distance);
    }

    private int shiftWest(int p, int distance) {
        return (p - distance);
    }
}
