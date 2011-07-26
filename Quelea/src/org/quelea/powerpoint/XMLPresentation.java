package org.quelea.powerpoint;

import java.io.IOException;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 *
 * @author Michael
 */
public class XMLPresentation {

    private XMLSlideShow slideshow;
    private PresentationSlide[] slides;

    public XMLPresentation(String file) {
        try {
            slideshow = new XMLSlideShow(new XSLFSlideShow(file));
        }
        catch (IOException ex) {
            throw new RuntimeException("Couldn't find " + file, ex);
        }
        catch(Exception ex) {
            throw new RuntimeException("Error creating presentation");
        }
        slides = makeSlides();
    }

    public PresentationSlide getSlide(int index) {
        return slides[index];
    }

    public PresentationSlide[] getSlides() {
        return slides;
    }

    private PresentationSlide[] makeSlides() {
        XSLFSlide[] lSlides = slideshow.getSlides();
        PresentationSlide[] ret = new PresentationSlide[lSlides.length];
        for (int i = 0; i < lSlides.length; i++) {
//            ret[i] = new PresentationSlide(lSlides[i]);
        }
        return ret;
    }

}
