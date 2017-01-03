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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;

/**
 * Responsible for printing to PDF (using xml and xslt.)
 *
 * @author Michael
 */
public class PDFPrinter {

    /**
     * Print a PDF file.
     * @param xml the content to use for printing.
     * @param xsltfile the stylesheet to use for printing.
     * @param pdfFile the file to print to.
     * @throws IOException if anything goes wrong.
     */
    public void print(String xml, File xsltfile, File pdfFile) throws IOException {
        try {
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

            StreamSource source = new StreamSource(stream);
            StreamSource transformSource = new StreamSource(xsltfile);
            FopFactory fopFactory = FopFactory.newInstance();
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer xslfoTransformer = tranFactory.newTransformer(transformSource);
            Fop fop = fopFactory.newFop("application/pdf", foUserAgent, outStream);
            Result res = new SAXResult(fop.getDefaultHandler());
            xslfoTransformer.transform(source, res);

            OutputStream out = new java.io.FileOutputStream(pdfFile);
            out = new java.io.BufferedOutputStream(out);
            FileOutputStream str = new FileOutputStream(pdfFile);
            str.write(outStream.toByteArray());
            str.close();
            out.close();
        } catch (IOException | TransformerException | FOPException ex) {
            throw new IOException("Error printing to PDF", ex);
        }
    }

}
