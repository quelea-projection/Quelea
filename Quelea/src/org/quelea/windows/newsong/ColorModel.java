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
package org.quelea.windows.newsong;

import java.awt.Color;
import java.util.Observable;

/**
 * A custom color model. Don't want to use built in one because it causes 
 * problems with substance and Java 7.
 * @author Michael
 */
public class ColorModel extends Observable {

    private float brightness = 1.0f;
    private float hue = 0.0f;
    private float saturation = 0.0f;
    private Color color;

    /**
     * Get f within some given bound. Eg. if greater than max, return max, 
     * if less than min, return min, else return the value.
     * @param f the value.
     * @param min the minimum bound.
     * @param max the maximum bound.
     * @return f within the bound.
     */
    private float bound(float f, float min, float max) {
        if (f < min) {
            return min;
        }
        else if (f > max) {
            return max;
        }
        else {
            return f;
        }
    }

    /**
     * Get i within some given bound. Eg. if greater than max, return max, 
     * if less than min, return min, else return the value.
     * @param i the value.
     * @param min the minimum bound.
     * @param max the maximum bound.
     * @return i within the bound.
     */
    private int bound(int i, int min, int max) {
        if (i < min) {
            return min;
        }
        else if (i > max) {
            return max;
        }
        else {
            return i;
        }
    }

    /**
     * Set the brightness of the colour model between 0 and 1.
     * @param b the brightness.
     */
    public void setBrightness(float b) {
        brightness = bound(b, 0.0f, 1.0f);
        color = Color.getHSBColor(hue, saturation, brightness);
        setChanged();
    }

    /**
     * Set the hue of the colour model between 0 and 1 (exclusive.)
     * @param h the hue.
     */
    public void setHue(float h) {
        hue = bound(h, 0.0f, 0.999f);
        color = Color.getHSBColor(hue, saturation, brightness);
        setChanged();
    }

    /**
     * Set the saturation of the colour model between 0 and 1.
     * @param s the hue.
     */
    public void setSaturation(float s) {
        saturation = bound(s, 0.0f, 1.0f);
        color = Color.getHSBColor(hue, saturation, brightness);
        setChanged();
    }

    /**
     * Set the direct rgb values of this colour model.
     * @param r the red value.
     * @param g the green value.
     * @param b the blue value.
     */
    public void setRGB(int r, int g, int b) {
        r = bound(r, 0, 255);
        g = bound(g, 0, 255);
        b = bound(b, 0, 255);

        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);

        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];

        color = new Color(r, g, b);
        setChanged();
    }

    /**
     * Set the colour of this model directly.
     * @param c the colour.
     */
    public void setColor(Color c) {
        setRGB(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Called when the value of this model has changed.
     */
    protected void setChanged() {
        super.setChanged();
        notifyObservers();
    }

    /**
     * Get the current colour.
     * @return the current colour.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the current hue.
     * @return the current hue.
     */
    public float getHue() {
        return hue;
    }

    /**
     * Get the current saturation.
     * @return the current saturation.
     */
    public float getSaturation() {
        return saturation;
    }

    /**
     * Get the current brightness.
     * @return the current brightness.
     */
    public float getBrightness() {
        return brightness;
    }
}
