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

import java.io.File;
import java.io.IOException;
import org.quelea.data.displayable.SongDisplayable;

/**
 * Responsible for exporting a song to a PDF file.
 *
 * @author Michael
 */
public class SongPDFPrinter extends PDFPrinter {

    public static SongPDFPrinter INSTANCE = new SongPDFPrinter();

    private SongPDFPrinter() {
    }

    public void print(SongDisplayable song, File pdfFile, boolean includeTranslations) throws IOException {
        printXML(song.getPrintXML(includeTranslations), pdfFile);
    }

    /**
     * Write song print XML (see SongDisplayable.getPrintXML()) to a PDF file.
     *
     * @param printXML the print XML of the song.
     * @param pdfFile the file to write.
     * @throws IOException if the PDF couldn't be generated.
     */
    public void printXML(String printXML, File pdfFile) throws IOException {
        print(printXML, new File("songformat.xsl"), pdfFile);
    }

}
