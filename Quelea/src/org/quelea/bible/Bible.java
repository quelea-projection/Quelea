/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.bible;

import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bible containing a number of books as well as some information.
 * @author Michael
 */
public final class Bible {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final String name;
    private BibleInfo information;
    private final List<BibleBook> books;

    /**
     * Create a new bible.
     * @param name the name of the bible.
     */
    private Bible(String name) {
        books = new ArrayList<>();
        this.name = name;
    }

    /**
     * Parse a bible from a specified bible and return it as an object.
     * @param file the file where the XML bible is stored.
     * @return the bible as a java object, or null if an error occurred.
     */
    public static Bible parseBible(File file) {
        try {
            if (file.exists()) {
                InputStream fis = new FileInputStream(file);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(fis);
                NodeList list = doc.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getNodeName().equalsIgnoreCase("xmlbible")
                            || list.item(i).getNodeName().equalsIgnoreCase("bible")) {
                        return parseXML(list.item(i));
                    }
                }
            }
            return null;
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the bible", ex);
            return null;
        }
    }

    /**
     * Parse some XML representing this object and return the object it represents.
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static Bible parseXML(Node node) {
        String name = "";
        if (node.getAttributes().getNamedItem("biblename") != null) {
            name = node.getAttributes().getNamedItem("biblename").getNodeValue();
        }
        Bible ret = new Bible(name);
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equalsIgnoreCase("information")) {
                ret.information = BibleInfo.parseXML(list.item(i));
            }
            else if (list.item(i).getNodeName().equalsIgnoreCase("biblebook")
                    || list.item(i).getNodeName().equalsIgnoreCase("b")) {
                ret.addBook(BibleBook.parseXML(list.item(i)));
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this bible.
     * @return an XML representation of this bible.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<xmlbible xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"zef2005.xsd\" version=\"2.0.1.18\" status=\"v\" biblename=\"");
        ret.append(Utils.escapeXML(name));
        ret.append("\" type=\"x-bible\" revision=\"0\">");
        if (information != null) {
            ret.append(information.toXML());
        }
        for (BibleBook book : books) {
            ret.append(book.toXML());
        }
        ret.append("</xmlbible>");
        return ret.toString();
    }

    /**
     * Get general information about this bible.
     * @return the bibleinfo object providing general information about the bible.
     */
    public BibleInfo getInformation() {
        return information;
    }

    /**
     * The name of the bible.
     * @return the name of the bible.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a summary of the bible.
     * @return a summary of the bible.
     */
    @Override
    public String toString() {
        return getName() + " (" + Utils.getAbbreviation(getName()) + ")";
    }

    /**
     * Add a book to the bible.
     * @param book the book to add.
     */
    public void addBook(BibleBook book) {
        books.add(book);
    }

    /**
     * Get all the books currently contained within this bible.
     * @return all the books in the bible.
     */
    public BibleBook[] getBooks() {
        return books.toArray(new BibleBook[books.size()]);
    }
}
