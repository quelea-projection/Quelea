package org.quelea;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * A visual background. This may either be an image or a color.
 * @author Michael
 */
public class Background {

    private Color color;
    private BufferedImage image;

    /**
     * Create a new background that's a certain colour.
     * @param color the colour of the background.
     */
    public Background(Color color) {
        this.color = color;
    }

    /**
     * Create a new background that's a certain image.
     * @param image the background image.
     */
    public Background(BufferedImage image) {
        this.image = image;
    }

    /**
     * Get the background with a specified width and height. If this background
     * is an image then it will be scaled accordingly, if it is a colour an
     * image will be given with the specified dimension, filled with the colour.
     * @param width the width of the background.
     * @param height the height of the background.
     * @return an image containing the background with the given dimensions.
     */
    public BufferedImage getImage(int width, int height) {
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) ret.getGraphics();
        if(color==null) {
            AffineTransform scaler = AffineTransform.getScaleInstance((double) height / image.getHeight(null), (double) width / image.getHeight(null));
            g.drawRenderedImage(ret, scaler);
            return ret;
        }
        else {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
            return ret;
        }
    }
}
