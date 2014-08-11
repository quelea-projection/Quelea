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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 * A slide in a powerpoint presentation.
 *
 * @author Michael
 */
public class PresentationSlide {

    private WritableImage image;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static double scaleWidth = 0;
    private static double scaleHeight = 0;

    /**
     * Create a new presentation slide.
     *
     * @param slide the underlying apache POI slide.
     */
    public PresentationSlide(Slide slide) {
        org.apache.poi.hslf.usermodel.SlideShow slideshow = slide.getSlideShow();
        if (Math.abs(slideshow.getPageSize().getHeight() - HEIGHT) > 0.1) {
            int adjustHeight = HEIGHT;
            int adjustWidth = (int) ((adjustHeight / slideshow.getPageSize().getHeight()) * slideshow.getPageSize().getWidth());
            scaleWidth = (double) adjustWidth / slideshow.getPageSize().getWidth();
            scaleHeight = (double) adjustHeight / slideshow.getPageSize().getHeight();
            slideshow.setPageSize(new Dimension(adjustWidth, adjustHeight));
        }
        BufferedImage originalImage = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setTransform(AffineTransform.getScaleInstance(scaleWidth, scaleHeight));
        slide.draw(g2);
        image = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
        SwingFXUtils.toFXImage(originalImage, image);
    }

    /**
     * Create a new presentation slide.
     *
     * @param slide the underlying apache POI slide.
     */
    public PresentationSlide(XSLFSlide slide) {
        org.apache.poi.xslf.usermodel.XMLSlideShow slideshow = slide.getSlideShow();
        if (Math.abs(slideshow.getPageSize().getHeight() - HEIGHT) > 0.1) {
            int adjustHeight = HEIGHT;
            int adjustWidth = (int) ((adjustHeight / slideshow.getPageSize().getHeight()) * slideshow.getPageSize().getWidth());
            scaleWidth = (double) adjustWidth / slideshow.getPageSize().getWidth();
            scaleHeight = (double) adjustHeight / slideshow.getPageSize().getHeight();
            slideshow.setPageSize(new Dimension(adjustWidth, adjustHeight));
        }
        BufferedImage originalImage = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setTransform(AffineTransform.getScaleInstance(scaleWidth, scaleHeight));
        slide.draw(g2);
        image = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
        SwingFXUtils.toFXImage(originalImage, image);
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
