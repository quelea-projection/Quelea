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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * A slide in a powerpoint presentation.
 *
 * @author Michael
 */
public class PresentationSlide {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private WritableImage image;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    /**
     * The scale of each presentation. Rendering the first slide resizes the
     * presentation's page to {@link #HEIGHT} pixels high, so the scale must be
     * remembered for its other slides - but per presentation, otherwise a deck
     * that is 1080 high already would use the previous deck's scale (or 0 =
     * blank slides if it is the first one loaded).
     */
    private static final Map<SlideShow<?, ?>, Double> SCALES = new WeakHashMap<>();

    /**
     * Create a slide from an image rendered elsewhere (by PowerPoint itself,
     * see {@link RenderedPresentation}).
     *
     * @param image the slide.
     */
    public PresentationSlide(WritableImage image) {
        this.image = image;
    }

    /**
     * Create a new presentation slide.
     *
     * @param slide the underlying apache POI slide.
     */
    public PresentationSlide(Slide slide, int numSlide) {
        SlideShow slideshow = slide.getSlideShow();
        double scale = scaleFor(slideshow);
        BufferedImage originalImage = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            g2.setTransform(AffineTransform.getScaleInstance(scale, scale));
            slide.draw(g2);
        } catch (Exception ex) {
            if (QueleaProperties.get().getUsePP()) {
                LOGGER.log(Level.INFO, "Couldn't use library to generate thumbnail, using default");
                draw(g2, originalImage.getWidth(), originalImage.getHeight(), numSlide);
            } else {
                throw ex;
            }
        }
        image = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
        SwingFXUtils.toFXImage(originalImage, image);
        originalImage.flush();
        originalImage = null;
    }

    /**
     * Create a new presentation slide.
     *
     * @param slide the underlying apache POI slide.
     */
    public PresentationSlide(XSLFSlide slide, int numSlide) {
        org.apache.poi.xslf.usermodel.XMLSlideShow slideshow = slide.getSlideShow();
        double scale = scaleFor(slideshow);
        BufferedImage originalImage = new BufferedImage((int) slideshow.getPageSize().getWidth(), (int) slideshow.getPageSize().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            g2.setTransform(AffineTransform.getScaleInstance(scale, scale));
            slide.draw(g2);
        } catch (NullPointerException ex) {
            if (QueleaProperties.get().getUsePP()) {
                LOGGER.log(Level.INFO, "Couldn't use library to generate thumbnail, using default");
                draw(g2, originalImage.getWidth(), originalImage.getHeight(), numSlide);
            } else {
                throw ex;
            }
        }
        image = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
        SwingFXUtils.toFXImage(originalImage, image);
    }

    /**
     * Get the scale for this presentation, resizing its page to
     * {@link #HEIGHT} pixels high the first time.
     *
     * @param slideshow the presentation.
     * @return the factor to draw its shapes with.
     */
    private static double scaleFor(SlideShow<?, ?> slideshow) {
        synchronized (SCALES) {
            Double scale = SCALES.get(slideshow);
            if (scale == null) {
                Dimension size = slideshow.getPageSize();
                if (Math.abs(size.getHeight() - HEIGHT) > 0.1) {
                    int adjustWidth = (int) ((HEIGHT / size.getHeight()) * size.getWidth());
                    scale = HEIGHT / size.getHeight();
                    slideshow.setPageSize(new Dimension(adjustWidth, HEIGHT));
                } else {
                    scale = 1.0;
                }
                SCALES.put(slideshow, scale);
            }
            return scale;
        }
    }

    private void draw(Graphics2D graphics, int width, int height, int num) {
        String slideText = LabelGrabber.INSTANCE.getLabel("preview.failed");
        graphics.setColor(new Color(174, 167, 159));
        graphics.fillRect(0, 0, width, height);
        graphics.setFont(new Font("Calibri", Font.PLAIN, 1000));
        graphics.setColor(new Color(250, 250, 250));
        while (graphics.getFontMetrics().stringWidth(slideText) > width - 1000) {
            graphics.setFont(new Font("Calibri", Font.PLAIN, graphics.getFont().getSize() - 2));
        }
        graphics.drawString(slideText, 10, height / 2 - graphics.getFontMetrics().getHeight() / 4);
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
