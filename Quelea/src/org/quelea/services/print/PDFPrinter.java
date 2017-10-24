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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Responsible for printing to PDF (using xml and xslt.)
 *
 * @author Michael
 */
public class PDFPrinter {

    private static Object fopFactory;
    private static Object fopFactoryBuilderObj;
    private static Object foUserAgent;
    private static TransformerFactory tranFactory;
    private static Class fopFactoryBuilderCls;
    private static Class fopFactoryCls;
    private static Class fopCls;
    private static Class foUserAgentCls;
    private static Class configurationCls;

    /**
     * Print a PDF file.
     *
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
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            Configuration cfg = cfgBuilder.buildFromFile(new File("fopcfg.xml"));

            /*
            Yuk, I know. The issue is here that Apache FOP hasn't been updated to use PDFBox 2.x, it's stuck on 1.8.
            We can't include both by default because that causes all sorts of classloader issues, so exclude 1.8
            from the default classpath, load it manually and then reflect around it here.
             */

            if (fopFactoryBuilderObj == null) {
                CustomClassLoader clsLoader = new CustomClassLoader(new java.net.URL[]{
                    new java.net.URL("file:lib/fop-2.1.jar"),
                    new java.net.URL("file:lib/pdfbox-app-1.8.13.jar")
                });
                fopFactoryBuilderCls = clsLoader.loadClass("org.apache.fop.apps.FopFactoryBuilder");
                fopFactoryCls = clsLoader.loadClass("org.apache.fop.apps.FopFactory");
                fopCls = clsLoader.loadClass("org.apache.fop.apps.Fop");
                foUserAgentCls = clsLoader.loadClass("org.apache.fop.apps.FOUserAgent");
                configurationCls = clsLoader.loadClass("org.apache.avalon.framework.configuration.Configuration");
                fopFactoryBuilderObj = fopFactoryBuilderCls.getConstructor(URI.class).newInstance(new URI("."));
                fopFactoryBuilderObj = fopFactoryBuilderCls.getMethod("setConfiguration", configurationCls).invoke(fopFactoryBuilderObj, cfg);
                fopFactory = fopFactoryBuilderCls.getMethod("build").invoke(fopFactoryBuilderObj);
                foUserAgent = fopFactoryCls.getMethod("newFOUserAgent").invoke(fopFactory);
                tranFactory = TransformerFactory.newInstance();
            }

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            Transformer xslfoTransformer = tranFactory.newTransformer(transformSource);

            Object fop = fopFactoryCls.getMethod("newFop", String.class, foUserAgentCls, OutputStream.class).invoke(fopFactory, "application/pdf", foUserAgent, outStream);
            Result res = new SAXResult((DefaultHandler) fopCls.getMethod("getDefaultHandler").invoke(fop));
            xslfoTransformer.transform(source, res);

            OutputStream out = new java.io.FileOutputStream(pdfFile);
            out = new java.io.BufferedOutputStream(out);
            FileOutputStream str = new FileOutputStream(pdfFile);
            str.write(outStream.toByteArray());
            str.close();
            out.close();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | URISyntaxException | TransformerException | ConfigurationException | SAXException ex) {
            throw new IOException("Error printing to PDF", ex);
        }
    }
}

class CustomClassLoader extends ClassLoader {

    private final ChildClassLoader childClassLoader;

    public CustomClassLoader(URL[] classpath) {
        super(Thread.currentThread().getContextClassLoader());
        childClassLoader = new ChildClassLoader(classpath, new DetectClass(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }

    private static class ChildClassLoader extends URLClassLoader {

        private DetectClass realParent;

        public ChildClassLoader(URL[] urls, DetectClass realParent) {
            super(urls, null);
            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                Class<?> loaded = super.findLoadedClass(name);
                if (loaded != null) {
                    return loaded;
                }
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                return realParent.loadClass(name);
            }
        }
    }

    private static class DetectClass extends ClassLoader {

        public DetectClass(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }
}
