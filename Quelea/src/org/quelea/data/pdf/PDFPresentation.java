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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * A PDF presentation that can be displayed.
 *
 * @author Arvid
 */
public class PDFPresentation implements Pdf {

    private PdfSlide[] slides;
    public String file;

    /**
     * Create a presentation from a file.
     *
     * @param file the PDF containing the presentation.
     */
    public PDFPresentation(String file) throws IOException {
        this.file = file;
        slides = makeSlides();
    }

    /**
     * Get the presentation slide at the given index in the PDF.
     *
     * @param index the index of the slide.
     * @return the slide at the given index.
     */
    @Override
    public PdfSlide getSlide(int index) {
        return slides[index];
    }

    /**
     * Get all the slides in the presentation.
     *
     * @return all the slides.
     */
    @Override
    public PdfSlide[] getSlides() {
        return slides;
    }

    /**
     * Make the slides that go in this PDF, this is what takes time and should
     * only be done once.
     *
     * @return all the slides.
     */
    private PdfSlide[] makeSlides() throws IOException {
        File pdf = new File(file);
        PDDocument document = PDDocument.load(pdf.getAbsoluteFile());
        Path f = Files.createTempDirectory(null);
        f.toFile().deleteOnExit();
        ArrayList<PdfSlide> ret = new ArrayList<>();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            ret.add(new PdfSlide(i + 1, pdfRenderer));
        }
        document.close();
        return ret.toArray(new PdfSlide[ret.size()]);
    }
}
