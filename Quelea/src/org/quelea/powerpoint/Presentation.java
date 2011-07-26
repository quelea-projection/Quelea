package org.quelea.powerpoint;

import java.io.IOException;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 *
 * @author Michael
 */
public class Presentation {

    private SlideShow slideshow;
    private PresentationSlide[] slides;

    public Presentation(String file) {
        try {
            slideshow = new SlideShow(new HSLFSlideShow(file));
            slides = makeSlides();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't find " + file, ex);
        }
    }

    public PresentationSlide getSlide(int index) {
        return slides[index];
    }

    public PresentationSlide[] getSlides() {
        return slides;
    }

    private PresentationSlide[] makeSlides() {
        Slide[] lSlides = slideshow.getSlides();
        PresentationSlide[] ret = new PresentationSlide[lSlides.length];
        for (int i = 0; i < lSlides.length; i++) {
            ret[i] = new PresentationSlide(lSlides[i]);
        }
        return ret;
    }

}
