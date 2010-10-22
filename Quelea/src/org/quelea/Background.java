package org.quelea;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A visual background. This may either be an image or a color.
 * @author Michael
 */
public class Background {

    private Color color;
    private String imageLocation;

    /**
     * Create a new background that's a certain colour.
     * @param color the colour of the background.
     */
    public Background(Color color) {
        this.color = color;
    }

    /**
     * Create a new background that's a certain image.
     * @param imageLocation the location of the background image.
     */
    public Background(String imageLocation) {
        this.imageLocation = imageLocation;
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
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        if(color == null) {
            g.drawImage(Utils.getImage(new File(new File("img").getAbsolutePath(),imageLocation).getAbsolutePath()), 0, 0, width, height, null);
            return ret;
        }
        else {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
            return ret;
        }
    }

    /**
     * Generate a hashcode for this background.
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.color != null ? this.color.hashCode() : 0);
        hash = 59 * hash + (this.imageLocation != null ? this.imageLocation.hashCode() : 0);
        return hash;
    }

    /**
     * Determine whether this background is equal to another object.
     * @param obj the other object.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final Background other = (Background) obj;
        if(this.color != other.color && (this.color == null || !this.color.equals(other.color))) {
            return false;
        }
        if(this.imageLocation != other.imageLocation && (this.imageLocation == null || !this.imageLocation.equals(other.imageLocation))) {
            return false;
        }
        return true;
    }

    /**
     * Get some information about this background.
     * @return information about the background in a string format.
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Background: ");
        if(color == null) {
            ret.append("image: ");
            ret.append(imageLocation);
        }
        else {
            ret.append("colour: ");
            ret.append(color);
        }
        return ret.toString();
    }
}
