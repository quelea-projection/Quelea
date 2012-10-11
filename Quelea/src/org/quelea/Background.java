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
package org.quelea;

import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.quelea.utils.Utils;

/**
 * A visual background. This may either be an image or a colour.
 * <p/>
 * @author Michael
 */
public class Background {

    private Color colour;
    private String imageLocation;
    private Image originalImage;

    /**
     * Create a new background that's a certain colour.
     * <p/>
     * @param colour the colour of the background.
     */
    public Background(Color colour) {
        this.colour = colour;
        this.originalImage = Utils.getImageFromColour(colour);
    }

    /**
     * Create a new background that's a certain image.
     * <p/>
     * @param imageLocation the location of the background image.
     */
    public Background(String imageLocation) {
        this.imageLocation = imageLocation;
        originalImage = new Image("file:" + imageLocation);
    }

    /**
     * Get the background image.
     */
    public Image getImage() {
        return originalImage;
    }

    /**
     * Get the current colour of this background, or null if the background is
     * currently an image.
     * <p/>
     * @return the colour of the background.
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Get the image background file.
     * <p/>
     * @return the file representing the image background, or null if the image
     * background is a colour.
     */
    public File getImageFile() {
        if(imageLocation == null) {
            return null;
        }
        else {
            return new File(new File("img"), imageLocation.trim());
        }
    }

    /**
     * Get the current image location of this background, or null if the
     * background is currently a colour.
     * <p/>
     * @return the current image location of the background.
     */
    public String getImageLocation() {
        return imageLocation;
    }

    /**
     * Determine whether this background is an image.
     * <p/>
     * @return true if the background is an image, false if its a colour.
     */
    public boolean isImage() {
        return imageLocation != null;
    }

    /**
     * Determine whether this background is a colour.
     * <p/>
     * @return true if the background is a colour, false if its an image.
     */
    public boolean isColour() {
        return colour != null;
    }

    /**
     * Generate a hashcode for this background.
     * <p/>
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
     * <p/>
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
        if(this.imageLocation == null) {
            return false;
        }
//        if(this.originalImage != other.originalImage && (this.originalImage == null || !this.originalImage.equals(other.originalImage))) {
//            return false;
//        }
        return true;
    }

    /**
     * Get some information about this background.
     * <p/>
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
