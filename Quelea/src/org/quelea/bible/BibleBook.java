package org.quelea.bible;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A book in the bible.
 * @author Michael
 */
public final class BibleBook {

    private int bookNumber;
    private String bookName;
    private final List<BibleChapter> chapters;

    /**
     * Create a new book.
     */
    private BibleBook() {
        chapters = new ArrayList<BibleChapter>();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * @param info the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleBook parseXML(Node node) {
        BibleBook ret = new BibleBook();
        if (node.getAttributes().getNamedItem("bnumber") != null) {
            ret.bookNumber = Integer.parseInt(node.getAttributes().getNamedItem("bnumber").getNodeValue());
        }
        if (node.getAttributes().getNamedItem("bname") != null) {
            ret.bookName = node.getAttributes().getNamedItem("bname").getNodeValue();
        }
        else {
            ret.bookName = node.getAttributes().getNamedItem("n").getNodeValue();
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equalsIgnoreCase("chapter")
                    || list.item(i).getNodeName().equalsIgnoreCase("c")) {
                ret.addChapter(BibleChapter.parseXML(list.item(i)));
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this book.
     * @return an XML representation of this book.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<biblebook bnumber=\"");
        ret.append(bookNumber);
        ret.append("\" bname=\"");
        ret.append(bookName);
        ret.append("\">");
        for (BibleChapter chapter : chapters) {
            ret.append(chapter.toXML());
        }
        ret.append("</biblebook>");
        return ret.toString();
    }

    /**
     * Get the name of the book.
     * @return the book's name.
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Get the name of the book.
     * @return the book's name.
     */
    @Override
    public String toString() {
        return bookName;
    }

    /**
     * Get the number of the book.
     * @return the book's number.
     */
    public int getBookNumber() {
        return bookNumber;
    }

    /**
     * Add a chapter to this book.
     * @param chapter the chapter to add.
     */
    public void addChapter(BibleChapter chapter) {
        chapters.add(chapter);
    }

    /**
     * Get a specific chapter from this book.
     * @param i the chapter number to get.
     * @return the chapter at the specified number, or null if it doesn't
     * exist.
     */
    public BibleChapter getChapter(int i) {
        if (i < chapters.size() && i >= 0) {
            return chapters.get(i);
        }
        else {
            return null;
        }
    }

    /**
     * Get all the chapters in this book.
     * @return all the chapters in the book.
     */
    public BibleChapter[] getChapters() {
        return chapters.toArray(new BibleChapter[chapters.size()]);
    }
}
