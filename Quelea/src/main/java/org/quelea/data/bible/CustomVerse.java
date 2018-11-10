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

import org.quelea.services.utils.Utils;

import java.util.ArrayList;

/**
 * A customized verse from the bible.
 *
 * @author Arvid
 */
public final class CustomVerse extends BibleVerse {
    private BibleVerse bibleVerse;
    private String verse = "";
    private ArrayList<Bible> bibles = new ArrayList<>();

    public CustomVerse(BibleVerse bibleVerse) {
        this.bibleVerse = bibleVerse;
        verse = bibleVerse.getText();
    }

    public void setBibles(ArrayList<Bible> bibles) {
        this.bibles.clear();
        this.bibles.addAll(bibles);
    }

    public ArrayList<Bible> getBibles() {
        return bibles;
    }

    @Override
    public String getText() {
        return verse;
    }

    @Override
    public String getVerseText() {
        return getText();
    }

    @Override
    public BibleChapter getChapter() {
        return bibleVerse.getChapter();
    }

    @Override
    public int getNum() {
        return bibleVerse.getNum();
    }

    @Override
    public String getName() {
        return bibleVerse.getName();
    }

    @Override
    public int getChapterNum() {
        return bibleVerse.getChapterNum();
    }

    @Override
    public BibleInterface getParent() {
        return bibleVerse.getParent();
    }

    public BibleVerse getBibleVerse() {
        return bibleVerse;
    }

    public void setBibleVerse(BibleVerse bibleVerse) {
        this.bibleVerse = bibleVerse;
    }

    public void setVerseText(String text) {
        this.verse = text;
    }

    /**
     * Generate an XML representation of this verse.
     *
     * @return an XML representation of this verse.
     */
    @Override
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<vers cnumber=\"");
        ret.append(getChapterNum());
        ret.append("\" vnumber=\"");
        ret.append(getNum());
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
        return getNum() + " " + verse;
    }

}