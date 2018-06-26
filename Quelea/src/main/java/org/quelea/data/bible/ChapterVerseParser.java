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

/**
 * Parses an input string into from / to chapter / verse. This class accepts an input string that's base 1 (i.e. 1:1 is
 * the first verse of the first chapter) but for ease of transition gives output in base 0 (so the above input would
 * give all values returning as 0.)
 * @author Michael
 */
public class ChapterVerseParser {

    private int fromChapter = -1;
    private int fromVerse = -1;
    private int toChapter = -1;
    private int toVerse = -1;

    /**
     * Create the parser and parse the given string. Strings must be in the format fromchapter:fromverse-tochapter:toverse
     * - the entire "to" section can be omitted if it's just one verse. The tochapter can also be left out if the from
     * and to chapters are the same.
     * @param str the string to parse.
     */
    public ChapterVerseParser(String str) {
        try {
            parseFull(str.trim());
        }
        catch (Exception ex) {
            //Just ignore, invalid input
        }
    }

    /**
     * Get the starting chapter.
     * @return the starting chapter.
     */
    public int getFromChapter() {
        return fromChapter - 1;
    }

    /**
     * Get the starting verse.
     * @return the starting verse.
     */
    public int getFromVerse() {
        if (fromVerse == -1) {
            return 0;
        }
        else {
            return fromVerse;
        }
    }

    /**
     * Get the ending chapter.
     * @return the ending chapter.
     */
    public int getToChapter() {
        if (toChapter == -1) {
            return fromChapter - 1;
        }
        else {
            return toChapter - 1;
        }
    }

    /**
     * Get the ending verse.
     * @return the ending verse.
     */
    public int getToVerse() {
        if (toVerse == -1) {
            return fromVerse;
        }
        else {
            return toVerse;
        }
    }

    /**
     * Parse a full string (mustn't have whitespace, call trim() first)
     * @param str the string to parse.
     */
    private void parseFull(String str) {
        if (str.endsWith(":")) {
            str = str.substring(0, str.length() - 1);
        }
        else if (str.endsWith("-")) {
            str += "1000";
        }
        if (str.charAt(str.length() - 1) != '-' && str.contains("-")) {
            String toStr = str.substring(str.indexOf('-') + 1);
            parseToStr(toStr);
            str = str.substring(0, str.indexOf('-'));
        }
        parseFromStr(str);
    }

    /**
     * Parse the part of the string that sets the "to" chapter and verse.
     * @param str the string to parse.
     */
    private void parseToStr(String str) {
        toChapter = getChapterTo(str);
        toVerse = getVerseTo(str);
    }

    /**
     * Parse the part of the string that sets the "from" chapter and verse.
     * @param str the string to parse.
     */
    private void parseFromStr(String str) {
        fromChapter = getChapterFrom(str);
        fromVerse = getVerseFrom(str);
    }

    /**
     * Get the "from chapter" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private int getChapterFrom(String str) {
        str = str.trim();
        if (str.contains(":")) {
            str = str.split(":")[0].trim();
        }
        return Integer.parseInt(str);
    }

    /**
     * Get the "from verse" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private int getVerseFrom(String str) {
        str = str.trim();
        if (str.contains(":")) {
            return Integer.parseInt(str.split(":")[1].trim());
        }
        else {
            return -1;
        }
    }

    /**
     * Get the "to chapter" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private int getChapterTo(String str) {
        str = str.trim();
        if (str.contains(":")) {
            return Integer.parseInt(str.split(":")[0].trim());
        }
        else {
            return -1;
        }
    }

    /**
     * Get the "to verse" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private int getVerseTo(String str) {
        str = str.trim();
        if (str.contains(":")) {
            str = str.split(":")[1].trim();
        }
        return Integer.parseInt(str);
    }
}
