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
package org.quelea.powerpoint;

import java.io.IOException;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 * An XML presentation that's not yet supported.
 * TODO: Support and implement properly
 * @author Michael
 */
public class XMLPresentation {

    private XMLSlideShow slideshow;
    private PresentationSlide[] slides;

    /**
     * Create a new XML presentation.
     * @param file the file containing the presentation.
     */
    public XMLPresentation(String file) {
        try {
            slideshow = new XMLSlideShow(new XSLFSlideShow(file));
        }
        catch (IOException ex) {
            throw new RuntimeException("Couldn't find " + file, ex);
        }
        catch (Exception ex) {
            throw new RuntimeException("Error creating presentation");
        }
        slides = makeSlides();
    }

    /**
     * Get the presentation slide at the given index in the presentation.
     * @param index the index of the slide.
     * @return the slide at the given index.
     */
    public PresentationSlide getSlide(int index) {
        return slides[index];
    }

    /**
     * Get all the slides in the presentation.
     * @return all the slides.
     */
    public PresentationSlide[] getSlides() {
        return slides;
    }

    /**
     * Make the slides that go in this presentation, this is what takes time
     * and should only be done once.
     * @return all the slides.
     */
    private PresentationSlide[] makeSlides() {
        XSLFSlide[] lSlides = slideshow.getSlides();
        PresentationSlide[] ret = new PresentationSlide[lSlides.length];
        for (int i = 0; i < lSlides.length; i++) {
//            ret[i] = new PresentationSlide(lSlides[i]);
        }
        return ret;
    }
}
