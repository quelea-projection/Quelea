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

import org.quelea.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * A chapter in the bible.
 *
 * @author Michael
 */
public final class BibleChapter {

    private final int num;
    private final List<BibleVerse> verses;
    private String text;
    private int id = -1;

    /**
     * Create a new bible chapter.
     *
     * @param num the chapter number (or -1 if it's unknown.)
     */
    private BibleChapter(int num) {
        this.num = num;
        verses = new ArrayList<>();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleChapter parseXML(Node node) {
        int num = -1;
        if(node.getAttributes().getNamedItem("cnumber") != null) {
            num = Integer.parseInt(node.getAttributes().getNamedItem("cnumber").getNodeValue());
        }
        BibleChapter ret = new BibleChapter(num);
        NodeList list = node.getChildNodes();
        for(int i = 0; i < list.getLength(); i++) {
            if(list.item(i).getNodeName().equalsIgnoreCase("vers")
                    || list.item(i).getNodeName().equalsIgnoreCase("v")) {
                ret.addVerse(BibleVerse.parseXML(list.item(i)));
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this chapter.
     *
     * @return an XML representation of this chapter.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<chapter");
        if(num != -1) {
            ret.append(" cnumber=\"");
            ret.append(num);
            ret.append('\"');
        }
        ret.append(">");
        for(BibleVerse verse : verses) {
            ret.append(Utils.escapeXML(verse.toXML()));
        }
        ret.append("</chapter>");
        return ret.toString();
    }

    /**
     * Add a verse to this chapter.
     *
     * @param verse the verse to add.
     */
    private void addVerse(BibleVerse verse) {
        verses.add(verse);
    }

    /**
     * Get all the verses in this chapter .
     *
     * @return all the verses in the chapter.
     */
    public BibleVerse[] getVerses() {
        return verses.toArray(new BibleVerse[verses.size()]);
    }

    /**
     * Get a specific verse from this chapter.
     *
     * @param i the verse number to get.
     * @return the verse at the specified number, or null if it doesn't exist.
     */
    public BibleVerse getVerse(int i) {
        if(i < verses.size() && i >= 0) {
            return verses.get(i);
        }
        else {
            return null;
        }
    }

    /**
     * Get all the text in this chapter as a string.
     *
     * @return all the text in this chapter as a string.
     */
    public String getText() {
        if(text==null) {
            StringBuilder ret = new StringBuilder();
            for(BibleVerse verse : getVerses()) {
                ret.append(verse.getText()).append(' ');
            }
            text = ret.toString();
            id = text.hashCode();
        }
        return text;
    }
    
    /**
     * Get the unique ID for this bible chapter.
     * @return the unique ID for this bible chapter.
     */
    public int getID() {
        if(text==null) {
            getText(); //Initialises id
        }
        return id;
    }

    /**
     * Get the number of this chapter (or -1 if no number has been set.)
     *
     * @return the chapter number.
     */
    public int getNum() {
        return num;
    }
}
