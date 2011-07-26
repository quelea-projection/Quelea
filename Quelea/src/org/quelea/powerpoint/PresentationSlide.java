package org.quelea.powerpoint;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.quelea.Application;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricCanvas;

/**
 *
 * @author Michael
 */
public class PresentationSlide {

    private BufferedImage image;
    private Map<Dimension, SoftReference<BufferedImage>> cache;

    public PresentationSlide(Slide slide) {
        cache = new HashMap<Dimension, SoftReference<BufferedImage>>();
        SlideShow slideshow = slide.getSlideShow();
        image = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        slide.draw(image.createGraphics());
        LyricCanvas lc = Application.get().getLyricWindow().getCanvas();
        getImage(lc.getWidth(), lc.getHeight()); //just for the cache
    }

    public BufferedImage getImage(int width, int height) {
        Dimension d = new Dimension(width, height);
        SoftReference<BufferedImage> cacheReference = cache.get(d);
        if (cacheReference != null) {
            BufferedImage cacheImage = cacheReference.get();
            if (cacheImage != null) {
                return cacheImage;
            }
        }
        BufferedImage ret = Utils.resizeImage(image, width, height);
        cache.put(d, new SoftReference<BufferedImage>(ret));
        return ret;
    }
}
