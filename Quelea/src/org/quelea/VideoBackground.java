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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

/**
 * A background consisting of a moving video.
 *
 * @author Michael
 */
public class VideoBackground implements Background {

    private String vidLocation;

    /**
     * Create a new video background.
     *
     * @param vidLocation the location in the "vid" folder of the video.
     */
    public VideoBackground(String vidLocation) {
        this.vidLocation = vidLocation;
    }

    /**
     * Get the location, relative to the "vid" folder, of this video.
     *
     * @return the location, relative to the "vid" folder, of this video.
     */
    public String getVideoLocation() {
        return vidLocation;
    }

    /**
     * Get the video file used for this background.
     *
     * @return the video file.
     */
    public File getVideoFile() {
        return new File(new File("vid"), vidLocation.trim());
    }

    /**
     * Get an empty image. TODO: Update to return a preview of the video?
     *
     * @param width the width of the image to return.
     * @param height the height of the image to return.
     * @param key key to potentially use for caching in future. At present just
     * pass null.
     * @return an empty image of the given dimensions.
     */
    @Override
    public BufferedImage getImage(int width, int height, String key) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Get the DB string of this particular colour background.
     *
     * @return the DB string.
     */
    @Override
    public String toDBString() {
        StringBuilder ret = new StringBuilder();
        ret.append("$backgroundvideo:").append(getVideoLocation());
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
        final VideoBackground other = (VideoBackground) obj;
        if(!Objects.equals(this.vidLocation, other.vidLocation)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.vidLocation);
        return hash;
    }
}
