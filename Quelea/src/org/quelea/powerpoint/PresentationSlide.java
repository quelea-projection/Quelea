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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hslf.model.Slide;
import org.quelea.Application;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricCanvas;

/**
 * A slide in a powerpoint presentation.
 * @author Michael
 */
public class PresentationSlide {

    private BufferedImage image;
    private Map<Dimension, SoftReference<BufferedImage>> cache;

    {
        cache = new HashMap<>();
    }

    public PresentationSlide(Slide slide) {
        org.apache.poi.hslf.usermodel.SlideShow slideshow = slide.getSlideShow();
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
        cache.put(d, new SoftReference<>(ret));
        return ret;
    }
}
