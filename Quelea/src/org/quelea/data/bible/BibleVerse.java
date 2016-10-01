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
import java.util.Objects;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;

/**
 * A verse in the bible.
 *
 * @author Michael
 */
public final class BibleVerse implements BibleInterface, Serializable {

    private String verse;
    private int num;
    private BibleChapter chapter;
    private int chapterNum;

    /**
     * For internal use only.
     */
    private BibleVerse() {
        //For internal use
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.verse);
        hash = 97 * hash + this.num;
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
        final BibleVerse other = (BibleVerse) obj;
        if (!Objects.equals(this.verse, other.verse)) {
            return false;
        }
        if (this.num != other.num) {
            return false;
        }
        return true;
    }

    /**
     * Set the chapter this verse is part of.
     *
     * @param chapter the chapter this verse is part of.
     */
    void setChapter(BibleChapter chapter) {
        this.chapter = chapter;
        this.chapterNum = chapter.getNum();
    }

    /**
     * Get the chapter this verse is part of.
     *
     * @return the chapter this verse is part of.
     */
    public BibleChapter getChapter() {
        return chapter;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleVerse parseXML(Node node) {
        BibleVerse ret = new BibleVerse();
        if (node.getAttributes().getNamedItem("cnumber") != null) {
            ret.setChapterNum(Integer.parseInt(node.getAttributes().getNamedItem("cnumber").getTextContent()));
            ret.setChapter(BibleChapter.parseXML(node, ret.getChapterNum()));
        } 
        if (node.getAttributes().getNamedItem("vnumber") == null) {
            ret.num = Integer.parseInt(node.getAttributes().getNamedItem("n").getNodeValue().trim());
        } else {
            ret.num = Integer.parseInt(node.getAttributes().getNamedItem("vnumber").getNodeValue().trim());
        }
        ret.verse = node.getTextContent();
        return ret;
    }

    /**
     * Generate an XML representation of this verse.
     *
     * @return an XML representation of this verse.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<vers cnumber=\"");
        ret.append(chapterNum);
        ret.append("\" vnumber=\"");
        ret.append(num);
        ret.append("\">");
        ret.append(Utils.escapeXML(verse));
        ret.append("</vers>");
        return ret.toString();
    }

    /**
     * Get this verse as a string.
     *
     * @return this verse as a string.
     */
    @Override
    public String toString() {
        return num + " " + verse;
    }

    /**
     * Get the textual content of the verse.
     *
     * @return the textual content of the verse.
     */
    public String getVerseText() {
        return verse;
    }

    @Override
    public String getName() {
        return getNum() + " " + getText();
    }

    @Override
    public String getText() {
        return getVerseText();
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public BibleInterface getParent() {
        return getChapter();
    }

    private void setChapterNum(int num) {
        chapterNum = num;
    }

    public int getChapterNum() {
        return chapterNum;
    }
}
