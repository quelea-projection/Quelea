package org.quelea.powerpoint;

import java.awt.image.BufferedImage;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 *
 * @author Michael
 */
public class PresentationSlide {

    private BufferedImage image;

    public PresentationSlide(Slide slide) {
        SlideShow slideshow = slide.getSlideShow();
        image = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        slide.draw(image.createGraphics());
    }

    public BufferedImage getImage() {
        return image;
    }
}
