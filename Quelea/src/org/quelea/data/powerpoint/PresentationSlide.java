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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;

/**
 * A slide in a powerpoint presentation.
 *
 * @author Michael
 */
public class PresentationSlide {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private WritableImage image;

    /**
     * Create a new presentation slide.
     *
     * @param slide the underlying apache POI slide.
     */
    public PresentationSlide(Slide slide, int numSlide) {
        org.apache.poi.hslf.usermodel.SlideShow slideshow = slide.getSlideShow();
        BufferedImage originalImage = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        try {
            slide.draw(g2);
//        } catch (Exception ex) {
//            LOGGER.log(Level.INFO, "Couldn't use library to generate thumbnail, using default", ex);
//            draw(g2, originalImage.getWidth(), originalImage.getHeight(), numSlide);
//        }
        image = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
        SwingFXUtils.toFXImage(originalImage, image);
    }

    /**
     * Create a new presentation slide.
     *
     * @param slide the underlying apache POI slide.
     */
    public PresentationSlide(XSLFSlide slide, int numSlide) {
        org.apache.poi.xslf.usermodel.XMLSlideShow slideshow = slide.getSlideShow();
        BufferedImage originalImage = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        try {
            slide.draw(g2);
//        } catch (Exception ex) {
//            LOGGER.log(Level.INFO, "Couldn't use library to generate thumbnail, using default", ex);
//            draw(g2, originalImage.getWidth(), originalImage.getHeight(), numSlide);
//        }
        image = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
        SwingFXUtils.toFXImage(originalImage, image);
    }
    
    private void draw(Graphics2D graphics, int width, int height, int num) {
        String slideText = LabelGrabber.INSTANCE.getLabel("slide.text") + " " + num;
        graphics.setColor(new Color(174, 167, 159));
        graphics.fillRect(0, 0, width, height);
        graphics.setFont(new Font("Calibri", 0, 1000));
        graphics.setColor(new Color(250, 250, 250));
        while(graphics.getFontMetrics().stringWidth(slideText)>width-200) {
            graphics.setFont(new Font("Calibri", 0, graphics.getFont().getSize()-2));
        }
        graphics.drawString(slideText, 100, height/2 - graphics.getFontMetrics().getHeight()/4);
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
