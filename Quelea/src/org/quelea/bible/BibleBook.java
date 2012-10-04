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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import org.quelea.utils.Utils;
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
    private SoftReference<String> softRefText;
    private List<Integer> caretPosList;
    private Bible bible;

    /**
     * Create a new book.
     */
    private BibleBook() {
        chapters = new ArrayList<>();
        caretPosList = new ArrayList<>();
    }
    
    /**
     * Set the bible this book is part of.
     *
     * @param bible the bible this book is part of.
     */
    void setBible(Bible bible) {
        this.bible = bible;
    }
    
    /**
     * Get the text of this chapter as nicely formatted HTML.
     * @return the text of this chapter.
     */
    public String getText() {
        String hardText = "";
        if(softRefText == null || softRefText.get() == null) {
            caretPosList.clear();
            int pos = 0;
            StringBuilder ret = new StringBuilder(1000);
            for(BibleChapter chapter : getChapters()) {
                caretPosList.add(pos);
                String numStr = Integer.toString(chapter.getNum());
                pos += numStr.length();
                ret.append("Chapter ").append(numStr);
                ret.append("\n");
                for(BibleVerse verse : chapter.getVerses()) {
                    String verseText = verse.getText();
                    pos += verseText.length();
                    ret.append(verseText).append(' ');
                }
                ret.append("\n");
            }
            hardText = ret.toString();
            this.softRefText = new SoftReference<>(hardText);
        }
        return hardText;
    }
    
    /**
     * Get the caret index of the chapter when used with the getHTML() method.
     * @param num the number of the chapter in which to get the index.
     * @return 
     */
    public int getCaretIndexOfChapter(int num) {
        return caretPosList.get(num-1);
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
     * Parse some XML representing this object and return the object it represents.
     * @param node the XML node representing this object.
     * @param defaultBookNum the default book number if none is available on the XML file.
     * @return the object as defined by the XML.
     */
    public static BibleBook parseXML(Node node, int defaultBookNum) {
        BibleBook ret = new BibleBook();
        if (node.getAttributes().getNamedItem("bnumber") != null) {
            ret.bookNumber = Integer.parseInt(node.getAttributes().getNamedItem("bnumber").getNodeValue());
        }
        else {
            ret.bookNumber = defaultBookNum;
        }
        if (node.getAttributes().getNamedItem("bname") != null) {
            ret.bookName = node.getAttributes().getNamedItem("bname").getNodeValue();
        }
        else {
            if (node.getAttributes().getNamedItem("n") != null) {
                ret.bookName = node.getAttributes().getNamedItem("n").getNodeValue();
            }
            else {
                ret.bookName = "Book " + ret.bookNumber;
            }
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
     * @return the chapter at the specified number, or null if it doesn't exist.
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
