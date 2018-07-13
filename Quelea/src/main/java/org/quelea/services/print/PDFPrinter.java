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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Responsible for printing to PDF (using xml and xslt.)
 *
 * @author Michael
 */
public class PDFPrinter {

    private static FopFactory fopFactory;
    private static FopFactoryBuilder builder;
    private static FOUserAgent foUserAgent;
    private static TransformerFactory tranFactory;

    /**
     * Print a PDF file.
     *
     * @param xml the content to use for printing.
     * @param xsltfile the stylesheet to use for printing.
     * @param pdfFile the file to print to.
     * @throws IOException if anything goes wrong.
     */
    @SuppressWarnings("unchecked")
    public void print(String xml, File xsltfile, File pdfFile) throws IOException {
        try {
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            StreamSource source = new StreamSource(stream);
            StreamSource transformSource = new StreamSource(xsltfile);
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            Configuration cfg = cfgBuilder.buildFromFile(new File("fopcfg.xml"));

            if (builder == null) {
                builder = new FopFactoryBuilder(new URI("."));
                builder.setConfiguration(cfg);
                fopFactory = builder.build();
                foUserAgent = fopFactory.newFOUserAgent();
                tranFactory = TransformerFactory.newInstance();
            }

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            Transformer xslfoTransformer = tranFactory.newTransformer(transformSource);

            Fop fop = fopFactory.newFop("application/pdf", foUserAgent, outStream);
            Result res = new SAXResult((DefaultHandler) fop.getDefaultHandler());
            xslfoTransformer.transform(source, res);

            try (FileOutputStream str = new FileOutputStream(pdfFile)) {
                str.write(outStream.toByteArray());
            }
        } catch (IOException | URISyntaxException | TransformerException | ConfigurationException | SAXException ex) {
            throw new IOException("Error printing to PDF", ex);
        }
    }
}
