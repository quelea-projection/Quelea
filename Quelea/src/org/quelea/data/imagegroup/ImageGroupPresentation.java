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
import java.util.ArrayList;

/**
 * An image group that can be displayed.
 *
 * @author Arvid, based on PresentationDisplayable
 */
public class ImageGroupPresentation implements ImageGroup {

    private ImageGroupSlide[] slides;
    public File[] files;

    /**
     * Create a presentation from a file.
     *
     * @param files the images
     */
    public ImageGroupPresentation(File[] files) throws IOException {
        this.files = files;
        slides = makeSlides();
    }

    /**
     * Get the presentation slide at the given index in the image group.
     *
     * @param index the index of the slide.
     * @return the slide at the given index.
     */
    @Override
    public ImageGroupSlide getSlide(int index) {
        return slides[index];
    }

    /**
     * Get all the slides in the presentation.
     *
     * @return all the slides.
     */
    @Override
    public ImageGroupSlide[] getSlides() {
        return slides;
    }

    /**
     * Make the slides that go in this image group, this is what takes time and
     * should only be done once.
     *
     * @return all the slides.
     */
    private ImageGroupSlide[] makeSlides() throws IOException {
        ArrayList<ImageGroupSlide> ret = new ArrayList<>();
        int i = 0;
        for (File f : files) {
            ret.add(new ImageGroupSlide(i + 1, f));
            i++;
        }
        return ret.toArray(new ImageGroupSlide[ret.size()]);
    }
}
