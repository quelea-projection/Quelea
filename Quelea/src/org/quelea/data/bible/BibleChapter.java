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
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A chapter in the bible.
 * <p/>
 * @author Michael
 */
public final class BibleChapter implements BibleInterface, Serializable {

    private static int statId = 0;
    private final int num;
    private final Map<Integer, BibleVerse> verses;
    private transient SoftReference<String> softRefText;
    private final int id = statId++;
    private BibleBook book;

    /**
     * Create a new bible chapter.
     * <p/>
     * @param num the chapter number (or -1 if it's unknown.)
     */
    private BibleChapter(int num) {
        this.num = num;
        verses = new HashMap<>();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.num;
        hash = 67 * hash + Objects.hashCode(this.verses);
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
        final BibleChapter other = (BibleChapter) obj;
        if (this.num != other.num) {
            return false;
        }
        if (!Objects.equals(this.verses, other.verses)) {
            return false;
        }
        return true;
    }

    /**
     * Set the bible book this chapter is part of.
     * <p/>
     * @param book the book this chapter is part of.
     */
    public void setBook(BibleBook book) {
        this.book = book;
    }

    /**
     * Get the book this chapter is part of.
     * <p/>
     * @return the book this chapter is part of.
     */
    public BibleBook getBook() {
        return book;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p/>
     * @param node the XML node representing this object.
     * @param defaultNum the default chapter number if no other information is
     * available.
     * @return the object as defined by the XML.
     */
    public static BibleChapter parseXML(Node node, int defaultNum) {
        int num = -1;
        if (node.getAttributes().getNamedItem("cnumber") != null) {
            try {
                num = Integer.parseInt(node.getAttributes().getNamedItem("cnumber").getNodeValue().trim());
            } catch (NumberFormatException ex) {
            }
        } else if (node.getAttributes().getNamedItem("n") != null) {
            try {
                num = Integer.parseInt(node.getAttributes().getNamedItem("n").getNodeValue().trim());
            } catch (NumberFormatException ex) {
            }
        }
        if (num == -1) {
            num = defaultNum;
        }
        BibleChapter ret = new BibleChapter(num);
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equalsIgnoreCase("vers")
                    || list.item(i).getNodeName().equalsIgnoreCase("v")) {
                BibleVerse verse = BibleVerse.parseXML(list.item(i));
                verse.setChapter(ret);
                ret.addVerse(verse);
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this chapter.
     * <p/>
     * @return an XML representation of this chapter.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<chapter");
        if (num != -1) {
            ret.append(" cnumber=\"");
            ret.append(num);
            ret.append('\"');
        }
        ret.append(">");
        for (BibleVerse verse : verses.values()) {
            ret.append(Utils.escapeXML(verse.toXML()));
        }
        ret.append("</chapter>");
        return ret.toString();
    }

    /**
     * Return the chapter number as a string in this chapter.
     *
     * @return the chapter number as a string.
     */
    @Override
    public String toString() {
        return Integer.toString(num);
    }

    /**
     * Add a verse to this chapter.
     * <p/>
     * @param verse the verse to add.
     */
    private void addVerse(BibleVerse verse) {
        verses.put(verse.getNum(), verse);
    }

    /**
     * Get all the verses in this chapter .
     * <p/>
     * @return all the verses in the chapter.
     */
    public BibleVerse[] getVerses() {
        return verses.values().toArray(new BibleVerse[verses.size()]);
    }

    /**
     * Get a specific verse from this chapter.
     * <p/>
     * @param i the verse number to get.
     * @return the verse at the specified number, or null if it doesn't exist.
     */
    public BibleVerse getVerse(int i) {
        return verses.get(i);
    }

    /**
     * Get all the text in this chapter as a string.
     * <p/>
     * @return all the text in this chapter as a string.
     */
    public String getFullText() {
        if (softRefText == null || softRefText.get() == null) {
            StringBuilder ret = new StringBuilder();
            for (BibleVerse verse : getVerses()) {
                ret.append(verse.toString()).append(' ');
            }
            String hardText = ret.toString();
            this.softRefText = new SoftReference<>(hardText);
        }
        return softRefText.get();
    }

    /**
     * Get the unique ID for this bible chapter.
     * <p/>
     * @return the unique ID for this bible chapter.
     */
    public int getID() {
        return id;
    }

    /**
     * Get the number of this chapter (or -1 if no number has been set.)
     * <p/>
     * @return the chapter number.
     */
    @Override
    public int getNum() {
        return num;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String getText() {
        return getFullText();
    }

    @Override
    public BibleInterface getParent() {
        return getBook();
    }
}
