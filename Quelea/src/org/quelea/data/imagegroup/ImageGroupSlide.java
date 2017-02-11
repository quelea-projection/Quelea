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
package org.quelea.data.imagegroup;

import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;

/**
 * A slide in an image group.
 *
 * @author Arvid, based on PresentationSlide
 */
public class ImageGroupSlide {

    private final Image image;


    /**
     * Create a new image group slide.
     *
     * @param numSlide slide number
     * @param image the name of the file
     */
    public ImageGroupSlide(int numSlide, File image) throws IOException {
        this.image = new Image(image.toURI().toURL().toString());
    }

    /**
     * Get the image from this slide.
     *
     * @return the image of this slide.
     */
    public final Image getImage() {
        return image;
    }
}
