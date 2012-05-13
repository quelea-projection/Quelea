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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.quelea.utils.Utils;

/**
 * A background consisting of a static image.
 *
 * @author Michael
 */
public class ImageBackground implements Background {

    private String imageLocation;
    private BufferedImage originalImage;
    private Map<String, BufferedImage> cacheMap = new HashMap<>();

    /**
     * Create a new background that's a certain image.
     *
     * @param imageLocation the location of the background image.
     * @param originalImage the original image to use.
     */
    public ImageBackground(String imageLocation, BufferedImage originalImage) {
        this.imageLocation = imageLocation;
        this.originalImage = originalImage;
    }

    /**
     * Get the background with a specified width and height. If this background
     * is an image then it will be scaled accordingly, if it is a colour an
     * image will be given with the specified dimension, filled with the colour.
     *
     * @param width the width of the background.
     * @param height the height of the background.
     * @param key the key to use for the image in the cache.
     * @return an image containing the background with the given dimensions.
     */
    @Override
    public BufferedImage getImage(int width, int height, String key) {
        if(key != null && cacheMap.get(key) != null) {
            BufferedImage cacheImage = cacheMap.get(key);
            if(cacheImage.getWidth() == width && cacheImage.getHeight() == height) {
                return cacheImage;
            }
        }
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) ret.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        if(originalImage == null && getImageFile().isFile()) {
            originalImage = Utils.getImage(getImageFile().getAbsolutePath());
        }
        if(originalImage == null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
        }
        else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(originalImage, 0, 0, width, height, null);
        }
        if(key != null) {
            cacheMap.put(key, ret);
        }
        return ret;
    }

    /**
     * Get the image background file.
     *
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
     *
     * @return the current image location of the background.
     */
    public String getImageLocation() {
        return imageLocation;
    }

    /**
     * Get the DB string of this particular image background.
     *
     * @return the DB string.
     */
    @Override
    public String toDBString() {
        StringBuilder ret = new StringBuilder();
        ret.append("$backgroundimage:").append(getImageLocation());
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
        final ImageBackground other = (ImageBackground) obj;
        if(!Objects.equals(this.imageLocation, other.imageLocation)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.imageLocation);
        return hash;
    }
}
