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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * A slide in a PDF presentation.
 *
 * @author Arvid, based on PresentationSlide
 */
public class PdfSlide {

    private final WritableImage image;


    /**
     * Create a new PDF slide.
     *
     * @param numSlide slide number
     * @param pdf the name of the file
     */
    public PdfSlide(int numSlide, String pdf) throws IOException {
        BufferedImage originalImage;
        PDDocument document = PDDocument.load(new File(pdf));
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        originalImage = pdfRenderer.renderImageWithDPI(numSlide - 1, 300, ImageType.RGB);
        Graphics2D g2 = originalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
