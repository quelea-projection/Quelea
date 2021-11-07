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
package org.quelea.data.bible;

import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.UnicodeReader;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A bible containing a number of books as well as some information.
 * <p/>
 * @author Michael
 */
public final class Bible implements BibleInterface, Serializable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final String name;
    private BibleInfo information;
    private final List<BibleBook> books;
    private String filePath;

    /**
     * Create a new bible.
     * <p/>
     * @param name the name of the bible.
     */
    private Bible(String name) {
        books = new ArrayList<>();
        this.name = name;
    }
    
    /**
     * @return the path of the file this bible has been read from on null if n.a.
     */
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.name);
        hash = 19 * hash + Objects.hashCode(this.information);
        hash = 19 * hash + Objects.hashCode(this.books);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bible other = (Bible) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.information, other.information)) {
            return false;
        }
        if (!Objects.equals(this.books, other.books)) {
            return false;
        }
        return true;
    }

    /**
     * Parse a bible from a specified bible and return it as an object.
     * <p/>
     * @param file the file where the XML bible is stored.
     * @return the bible as a java object, or null if an error occurred.
     */
    public static Bible parseBible(final File file) {
        LOGGER.log(Level.INFO, "Parsing bible: " + file.getAbsolutePath());
        try {
            if (file.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new UnicodeReader(new FileInputStream(file), Utils.getEncoding(file))));
                NodeList list = doc.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getNodeName().equalsIgnoreCase("xmlbible")
                            || list.item(i).getNodeName().equalsIgnoreCase("bible")) {
                        return parseXML(list.item(i), Utils.getFileNameWithoutExtension(file.getName()));
                    }
                    else if (list.item(i).getNodeName().equalsIgnoreCase("osis")) {
                        NodeList innerList = list.item(i).getChildNodes();
                        for (int j = 0; j < innerList.getLength(); j++) {
                            if (innerList.item(j).getNodeName().equalsIgnoreCase("osisText")) {
                                return parseXML(innerList.item(j), Utils.getFileNameWithoutExtension(file.getName()));
                            }
                        }
                    }
                }
                LOGGER.log(Level.WARNING, "Couldn''t parse the bible {0} because I couldn''t find any <bible> or <xmlbible> root tags :-(", file);
                return null;
            } else {
                LOGGER.log(Level.WARNING, "Couldn''t parse the bible {0} because the file doesn''t exist!", file);
                return null;
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the bible " + file, ex);
            return null;
        }
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p/>
     * @param node the XML node representing this object.
     * @param defaultName the name of the bible if none is specified in the XML
     * file.
     * @return the object as defined by the XML.
     */
    public static Bible parseXML(Node node, String defaultName) {
        String name = "";
        if (node.getAttributes().getNamedItem("biblename") != null) {
            name = node.getAttributes().getNamedItem("biblename").getNodeValue();
        } else if (node.getAttributes().getNamedItem("name") != null) {
            name = node.getAttributes().getNamedItem("name").getNodeValue();
        } else {
            name = defaultName;
        }
        Bible ret = new Bible(name);
        List<Node> list = toList(node.getChildNodes());
        list = list.stream().flatMap(n -> {
                    if (n.getNodeName().equalsIgnoreCase("testament")) {
                        return toList(n.getChildNodes()).stream();
                    } else {
                        return Stream.of(n);
                    }
                })
                .filter(item -> ret.isBibleBookNode(item))
                .collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            Node item = list.get(i);
            if (item.getNodeName().equalsIgnoreCase("information")) {
                ret.information = BibleInfo.parseXML(item);
            } else if (ret.isBibleBookNode(item)) {
                BibleBook book = BibleBook.parseXML(item, i, BibleBookNameUtil.getBookNameForIndex(i, list.size()));
                book.setBible(ret);
                ret.addBook(book);
            }
        }
        LOGGER.log(Level.INFO, "Parsed bible: {0}. Contains {1} books.", new Object[]{ret.getName(), list.size()});
        return ret;
    }

    private boolean isBibleBookNode(Node n) {
        return n.getNodeName().equalsIgnoreCase("biblebook")
                || n.getNodeName().equalsIgnoreCase("b")
                || n.getNodeName().equalsIgnoreCase("book")
                || (n.getNodeName().equalsIgnoreCase("div") && n.getAttributes().getNamedItem("type") != null && n.getAttributes().getNamedItem("type").getNodeValue().equals("book"));
    }

    private static List<Node> toList(NodeList nodeList) {
        List<Node> ret = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            ret.add(nodeList.item(i));
        }
        return ret;
    }

    /**
     * Generate an XML representation of this bible.
     * <p/>
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
     * <p/>
     * @return the bibleinfo object providing general information about the
     * bible.
     */
    public BibleInfo getInformation() {
        return information;
    }

    /**
     * The name of the bible.
     * <p/>
     * @return the name of the bible.
     */
    public String getBibleName() {
        return name;
    }

    /**
     * Get a summary of the bible.
     * <p/>
     * @return a summary of the bible.
     */
    @Override
    public String toString() {
        String abbrev = Utils.getAbbreviation(getName());
        StringBuilder ret = new StringBuilder(getName());
        if (!(getName().contains("(") || getName().contains(")")) && abbrev.length() > 1) {
            ret.append(" (").append(abbrev).append(")");
        }
        return ret.toString();
    }

    /**
     * Add a book to the bible.
     * <p/>
     * @param book the book to add.
     */
    public void addBook(BibleBook book) {
        books.add(book);
    }

    /**
     * Get all the books currently contained within this bible.
     * <p/>
     * @return all the books in the bible.
     */
    public BibleBook[] getBooks() {
        return books.toArray(new BibleBook[books.size()]);
    }

    @Override
    public int getNum() {
        return -1;
    }

    @Override
    public String getText() {
        return toString();
    }

    @Override
    public String getName() {
        return getBibleName();
    }

    @Override
    public BibleInterface getParent() {
        return null;
    }
}
