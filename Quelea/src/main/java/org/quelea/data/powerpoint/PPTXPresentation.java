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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 * A presentation that uses the newer PPTX file format.
 *
 * @author Michael
 */
public class PPTXPresentation implements Presentation {

    private XMLSlideShow slideshow;
    private PresentationSlide[] slides;

    /**
     * Create a new XML presentation.
     *
     * @param file the file containing the presentation.
     */
    public PPTXPresentation(String file) throws IOException {
        slideshow = new XMLSlideShow(new FileInputStream(new File(file)));
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
        List<XSLFSlide> lSlides = slideshow.getSlides();
        ArrayList<PresentationSlide> ret = new ArrayList<>();
        for (int i = 0; i < lSlides.size(); i++) {
            if (lSlides.get(i) != null) {
                ret.add(new PresentationSlide(lSlides.get(i), i + 1));
            }
        }
        return ret.toArray(new PresentationSlide[ret.size()]);
    }
}
