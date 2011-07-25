package org.quelea;

import org.quelea.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A visual background. This may either be an image or a colour.
 * @author Michael
 */
public class Background {

    private Color colour;
    private String imageLocation;
    private BufferedImage originalImage;

    /**
     * Create a new background that's a certain colour.
     * @param colour the colour of the background.
     */
    public Background(Color colour) {
        this.colour = colour;
    }

    /**
     * Create a new background that's a certain image.
     * @param imageLocation the location of the background image.
     */
    public Background(String imageLocation, BufferedImage originalImage) {
        this.imageLocation = imageLocation;
        this.originalImage = originalImage;
    }

    /**
     * Get the background with a specified width and height. If this background is an image then it will be scaled
     * accordingly, if it is a colour an image will be given with the specified dimension, filled with the colour.
     * @param width  the width of the background.
     * @param height the height of the background.
     * @return an image containing the background with the given dimensions.
     */
    public BufferedImage getImage(int width, int height) {
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) ret.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        if(colour == null) {
            if(originalImage==null) {
                originalImage = Utils.getImage(getImageFile().getAbsolutePath());
            }
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(originalImage, 0, 0, width, height, null);
            return ret;
        }
        else {
            g.setColor(colour);
            g.fillRect(0, 0, width, height);
            return ret;
        }
    }

    /**
     * Get the current colour of this background, or null if the background is currently an image.
     * @return the colour of the background.
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Get the image background file.
     * @return the file representing the image background, or null if the image background is a colour.
     */
    public File getImageFile() {
        if(imageLocation == null) {
            return null;
        }
        else {
            return new File(new File("img").getAbsolutePath(), imageLocation);
        }
    }

    /**
     * Get the current image location of this background, or null if the background is currently a colour.
     * @return the current image location of the background.
     */
    public String getImageLocation() {
        return imageLocation;
    }

    /**
     * Determine whether this background is an image.
     * @return true if the background is an image, false if its a colour.
     */
    public boolean isImage() {
        return imageLocation != null;
    }

    /**
     * Determine whether this background is a colour.
     * @return true if the background is a colour, false if its an image.
     */
    public boolean isColour() {
        return colour != null;
    }

    /**
     * Generate a hashcode for this background.
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.colour != null ? this.colour.hashCode() : 0);
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
        if(this.colour != other.colour && (this.colour == null || !this.colour.equals(other.colour))) {
            return false;
        }
        if(this.imageLocation != other.imageLocation && (this.imageLocation == null || !this.imageLocation.equals(other.imageLocation))) {
            return false;
        }
        if(this.originalImage != other.originalImage && (this.originalImage == null || !this.originalImage.equals(other.originalImage))) {
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
        if(colour == null) {
            ret.append("image: ");
            ret.append(imageLocation);
        }
        else {
            ret.append("colour: ");
            ret.append(colour);
        }
        return ret.toString();
    }
}
