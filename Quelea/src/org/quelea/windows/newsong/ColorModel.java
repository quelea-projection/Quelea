package org.quelea.windows.newsong;


import java.awt.Color;
import java.util.Observable;

public class ColorModel extends Observable {

    private float brightness = 1.0f;
    private float hue = 0.0f;
    private float saturation = 0.0f;
    private Color color;

    public ColorModel() {
        super();
    }

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

    public void setBrightness(float b) {
        brightness = bound(b, 0.0f, 1.0f);

        color = Color.getHSBColor(hue, saturation, brightness);
        setChanged();
    }

    public void setHue(float h) {
        hue = bound(h, 0.0f, 0.999f);

        color = Color.getHSBColor(hue, saturation, brightness);
        setChanged();
    }

    public void setSaturation(float s) {
        saturation = bound(s, 0.0f, 1.0f);

        color = Color.getHSBColor(hue, saturation, brightness);
        setChanged();
    }

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

    public void setColor(Color c) {
        setRGB(c.getRed(), c.getGreen(), c.getBlue());
    }

    protected void setChanged() {
        super.setChanged();
        notifyObservers();
    }

    public Color getColor() {
        return color;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }
}