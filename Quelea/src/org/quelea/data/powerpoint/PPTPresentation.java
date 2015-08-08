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
package org.quelea.data.powerpoint;

import java.io.IOException;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 * A presentation that can be displayed. At the moment represents a powerpoint
 * presentation though other formats may be supported in future.
 *
 * @author Michael
 */
public class PPTPresentation implements Presentation {

    private SlideShow slideshow;
    private PresentationSlide[] slides;

    /**
     * Create a presentation from a file.
     *
     * @param file the file containing the presentation.
     */
    public PPTPresentation(String file) throws IOException {
        slideshow = new SlideShow(new HSLFSlideShow(file));
        slides = makeSlides();
    }

    /**
     * Get the presentation slide at the given index in the presentation.
     *
     * @param index the index of the slide.
     * @return the slide at the given index.
     */
    @Override
    public PresentationSlide getSlide(int index) {
        return slides[index];
    }

    /**
     * Get all the slides in the presentation.
     *
     * @return all the slides.
     */
    @Override
    public PresentationSlide[] getSlides() {
        return slides;
    }

    /**
     * Make the slides that go in this presentation, this is what takes time and
     * should only be done once.
     *
     * @return all the slides.
     */
    private PresentationSlide[] makeSlides() {
        Slide[] lSlides = slideshow.getSlides();
        PresentationSlide[] ret = new PresentationSlide[lSlides.length];
        for (int i = 0; i < lSlides.length; i++) {
            ret[i] = new PresentationSlide(lSlides[i], i + 1);
        }
        return ret;
    }
}
