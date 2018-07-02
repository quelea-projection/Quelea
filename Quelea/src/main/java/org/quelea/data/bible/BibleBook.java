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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A book in the bible.
 *
 * @author Michael
 */
public final class BibleBook implements BibleInterface, Serializable {

    private int bookNumber;
    private String bookName;
    private final List<BibleChapter> chapters;
    private String htmlText;
    private List<Integer> caretPosList;
    private Bible bible;
    private String bsname;

    /**
     * Create a new book.
     */
    private BibleBook() {
        chapters = new ArrayList<>();
        caretPosList = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.bookNumber;
        hash = 43 * hash + Objects.hashCode(this.bookName);
        hash = 43 * hash + Objects.hashCode(this.chapters);
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
        final BibleBook other = (BibleBook) obj;
        if (this.bookNumber != other.bookNumber) {
            return false;
        }
        if (!Objects.equals(this.bookName, other.bookName)) {
            return false;
        }
        if (!Objects.equals(this.chapters, other.chapters)) {
            return false;
        }
        return true;
    }

    /**
     * Set the bible this book is part of.
     *
     * @param bible the bible this book is part of.
     */
    public void setBible(Bible bible) {
        this.bible = bible;
    }

    /**
     * Get the text of this chapter as nicely formatted HTML.
     *
     * @return the text of this chapter.
     */
    public String getFullText() {
        if (htmlText == null) {
            caretPosList.clear();
            int pos = 0;
            StringBuilder ret = new StringBuilder(1000);
            for (BibleChapter chapter : getChapters()) {
                caretPosList.add(pos);
                String numStr = Integer.toString(chapter.getNum());
                pos += numStr.length();
                ret.append("Chapter ").append(numStr);
                ret.append("\n");
                for (BibleVerse verse : chapter.getVerses()) {
                    String verseText = verse.getText();
                    pos += verseText.length();
                    ret.append(verseText).append(' ');
                }
                ret.append("\n");
            }
            htmlText = ret.toString();
        }
        return htmlText;
    }

    /**
     * Get the caret index of the chapter when used with the getHTML() method.
     *
     * @param num the number of the chapter in which to get the index.
     * @return
     */
    public int getCaretIndexOfChapter(int num) {
        return caretPosList.get(num - 1);
    }

    /**
     * Get the bible this book is part of.
     *
     * @return the bible this book is part of.
     */
    public Bible getBible() {
        return bible;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @param defaultBookNum the default book number if none is available on the
     * XML file.
     * @return the object as defined by the XML.
     */
    public static BibleBook parseXML(Node node, int defaultBookNum) {
        BibleBook ret = new BibleBook();
        if (node.getAttributes().getNamedItem("bnumber") != null) {
            ret.bookNumber = Integer.parseInt(node.getAttributes().getNamedItem("bnumber").getNodeValue().trim());
        } else {
            ret.bookNumber = defaultBookNum;
        }
        
        if (node.getAttributes().getNamedItem("bname") != null) {
            ret.bookName = node.getAttributes().getNamedItem("bname").getNodeValue();
        } else if (node.getAttributes().getNamedItem("n") != null) {
            ret.bookName = node.getAttributes().getNamedItem("n").getNodeValue();
        } else if (node.getAttributes().getNamedItem("name") != null) {
            ret.bookName = node.getAttributes().getNamedItem("name").getNodeValue();
        } else {
            ret.bookName = "Book " + ret.bookNumber;
        }
        
        if (node.getAttributes().getNamedItem("bsname") != null) {
            ret.bsname = node.getAttributes().getNamedItem("bsname").getNodeValue();
        } else {
            ret.bsname = ret.bookName;
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equalsIgnoreCase("chapter")
                    || list.item(i).getNodeName().equalsIgnoreCase("c")) {
                BibleChapter chapter = BibleChapter.parseXML(list.item(i), i);
                chapter.setBook(ret);
                ret.addChapter(chapter);
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this book.
     *
     * @return an XML representation of this book.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<biblebook bnumber=\"");
        ret.append(bookNumber);
        ret.append("\" bname=\"");
        ret.append(Utils.escapeXML(bookName));
        ret.append("\">");
        for (BibleChapter chapter : chapters) {
            ret.append(chapter.toXML());
        }
        ret.append("</biblebook>");
        return ret.toString();
    }

    /**
     * Get the name of the book.
     *
     * @return the book's name.
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Get the name of the book.
     *
     * @return the book's name.
     */
    @Override
    public String toString() {
        return bookName;
    }

    /**
     * Get the number of the book.
     *
     * @return the book's number.
     */
    public int getBookNumber() {
        return bookNumber;
    }

    /**
     * Add a chapter to this book.
     *
     * @param chapter the chapter to add.
     */
    public void addChapter(BibleChapter chapter) {
        chapters.add(chapter);
    }

    /**
     * Get a specific chapter from this book.
     *
     * @param i the chapter number to get.
     * @return the chapter at the specified number, or null if it doesn't exist.
     */
    public BibleChapter getChapter(int i) {
        if (i < chapters.size() && i >= 0) {
            return chapters.get(i);
        } else {
            return null;
        }
    }

    /**
     * Get all the chapters in this book.
     *
     * @return all the chapters in the book.
     */
    public BibleChapter[] getChapters() {
        return chapters.toArray(new BibleChapter[chapters.size()]);
    }

    @Override
    public int getNum() {
        return getBookNumber();
    }

    @Override
    public String getName() {
        return getBookName();
    }

    public String getBSName() {
        return bsname;
    }

    @Override
    public String getText() {
        return getFullText();
    }

    @Override
    public BibleInterface getParent() {
        return getBible();
    }
}
