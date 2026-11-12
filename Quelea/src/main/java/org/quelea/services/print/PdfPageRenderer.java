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
package org.quelea.services.print;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Renders the pages of a generated PDF to images, for the print preview.
 */
public final class PdfPageRenderer {

    /**
     * Pages beyond this are not rendered (the preview says how many pages
     * there are in all), so a huge schedule can't use up all the memory.
     */
    public static final int MAX_PAGES = 60;

    private PdfPageRenderer() {
        throw new AssertionError();
    }

    /**
     * The rendered pages of a PDF file.
     *
     * @param pages the rendered pages (at most MAX_PAGES).
     * @param pageCount the number of pages in the document.
     */
    public record Pages(List<BufferedImage> pages, int pageCount) {
    }

    /**
     * Render the pages of a PDF file.
     *
     * @param pdf the PDF file.
     * @param dpi the resolution to render at.
     * @return the rendered pages.
     * @throws IOException if the file couldn't be read.
     */
    public static Pages render(File pdf, float dpi) throws IOException {
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int count = document.getNumberOfPages();
            List<BufferedImage> pages = new ArrayList<>();
            for (int i = 0; i < Math.min(count, MAX_PAGES); i++) {
                pages.add(renderer.renderImageWithDPI(i, dpi, ImageType.RGB));
            }
            return new Pages(pages, count);
        }
    }
}
