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
package org.quelea.data.pdf;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

/**
 * A slide in a PDF presentation.
 *
 * @author Arvid
 */
public class PdfSlide {

    private BufferedImage thumbnail;
    private BufferedImage originalImage;
    private final File outputfile;
    private final File thumbnailFile;
    private final int SMALL_SIZE = 200;
    private final int BIG_SIZE = 1920;

    /**
     * Create a new PDF slide.
     *
     * @param numSlide slide number
     * @param pdfRenderer the renderer of the file
     */
    public PdfSlide(int numSlide, PDFRenderer pdfRenderer) throws IOException {
        originalImage = resizeImage(pdfRenderer.renderImageWithDPI(numSlide - 1, 200, ImageType.RGB), BIG_SIZE, BIG_SIZE);
        outputfile = File.createTempFile("slide" + numSlide, ".png");
        outputfile.deleteOnExit();
        ImageIO.write(originalImage, "png", outputfile);

        thumbnail = resizeImage(originalImage, SMALL_SIZE, SMALL_SIZE);
        thumbnailFile = File.createTempFile("thumb" + numSlide, ".png");
        thumbnailFile.deleteOnExit();
        ImageIO.write(thumbnail, "png", thumbnailFile);
        
        originalImage.flush();
        originalImage = null;
        thumbnail.flush();
        thumbnail = null;
    }

    /**
     * Get the image from this slide.
     *
     * @return the image of this slide.
     */
    public final Image getImage() {
        return new Image("file:" + outputfile.getAbsolutePath());
    }

    /**
     * Get the thumbnail of this slide.
     *
     * @return the image of this slide.
     */
    public final Image getThumbnail() {
        return new Image("file:" + thumbnailFile.getAbsolutePath());
    }

    /**
     * Scale image to a smaller size of this slide.
     *
     * @param originalImage source image to scale
     * @param width desired width
     * @param height desired height
     * @return the resixed image
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        int finalw = width;
        int finalh = height;
        double factor;
        if (originalImage.getWidth() > originalImage.getHeight()) {
            factor = ((double) originalImage.getHeight() / (double) originalImage.getWidth());
            finalh = (int) (finalw * factor);
        } else {
            factor = ((double) originalImage.getWidth() / (double) originalImage.getHeight());
            finalw = (int) (finalh * factor);
        }

        BufferedImage scaledImage = new BufferedImage(finalw, finalh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, finalw, finalh, null);
        g2.dispose();
        return scaledImage;
    }
}
