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
package org.quelea.data.displayable;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.data.ThemeDTO;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.Utils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a section of text in a song or passage.
 *
 * @author Michael
 */
public class TextSection implements Serializable {

    private final String title;
    private final String[] lines;
    private final String[] smallLines;
    private ThemeDTO theme;
    private ThemeDTO tempTheme;
    private final boolean capitaliseFirst;
    
    public TextSection(TextSection orig) {
        this.title = orig.title;
        this.lines = orig.lines;
        this.smallLines = orig.smallLines;
        this.theme = orig.theme;
        this.tempTheme = orig.tempTheme;
        this.capitaliseFirst = orig.capitaliseFirst;
    }

    /**
     * Create a new text section with the specified title and lyrics.
     *
     * @param title the title of the section.
     * @param lines the lines of the section, one line per array entry.
     * @param smallLines the lines to be displayed in the bottom left of the
     * canvas for this text section
     * @param capitaliseFirst true if the first character of each line should be
     * a capital, false otherwise.
     */
    public TextSection(String title, String[] lines, String[] smallLines, boolean capitaliseFirst) {
        this(title, lines, smallLines, capitaliseFirst, null, null);
    }

    /**
     * Create a new song section with the specified title and lyrics.
     *
     * @param title the title of the section.
     * @param lines the lines of the section, one line per array entry.
     * @param smallLines the lines to be displayed in the bottom left of the
     * canvas for this text section
     * @param capitaliseFirst true if the first character of each line should be
     * a capital, false otherwise.
     * @param theme the theme of this song section.
     * @param tempTheme the tempTheme of this song section.
     */
    public TextSection(String title, String[] lines, String[] smallLines, boolean capitaliseFirst, ThemeDTO theme, ThemeDTO tempTheme) {
        this.capitaliseFirst = capitaliseFirst;
        this.title = title;
        this.lines = Arrays.copyOf(lines, lines.length);
        if (smallLines == null) { //Guard against NPE
            smallLines = new String[0];
        }
        this.smallLines = Arrays.copyOf(smallLines, smallLines.length);
        this.theme = theme;
        this.tempTheme = tempTheme;
    }

    /**
     * Get a representation of this section in XML format.
     *
     * @return the section in XML format.
     */
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<section ").append("title=\"").append(getTitle()).append("\" capitalise=\"").append(shouldCapitaliseFirst()).append("\">");
        if (theme != null) {
            xml.append("<theme>");
            xml.append(Utils.escapeXML(theme.asString()));
            xml.append("</theme>");
        }
        if (smallLines != null) {
            xml.append("<smalllines>");
            for (String line : smallLines) {
                try {
                    xml.append(new String(Utils.escapeXML(line).getBytes("UTF8"), "UTF-8")).append('\n');
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(TextSection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            xml.append("</smalllines>");
        }
        xml.append("<lyrics>");
        for (String line : getText(true, true)) {
            try {
                xml.append(new String(Utils.escapeXML(line).getBytes("UTF8"), "UTF-8")).append('\n');
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TextSection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        xml.append("</lyrics></section>");
        return xml.toString();
    }

    /**
     * Parse the given node to create a new song section.
     *
     * @param sectionNode the section node.
     * @return the song section.
     */
    public static TextSection parseXML(Node sectionNode) {
        NamedNodeMap attributes = sectionNode.getAttributes();
        String sectionTitle = null;
        String[] lyrics = null;
        String[] smallLines = null;
        ThemeDTO theme = null;
        boolean capitalise = true;

        if (attributes != null) {
            Node titleNode = attributes.getNamedItem("title");
            if (titleNode != null) {
                sectionTitle = titleNode.getTextContent();
            }
            Node capitaliseNode = attributes.getNamedItem("capitalise");
            if (capitaliseNode != null) {
                capitalise = Boolean.parseBoolean(capitaliseNode.getTextContent());
            }
        }
        NodeList nodelist = sectionNode.getChildNodes();
        for (int i = 0; i < nodelist.getLength(); i++) {
            //try {
            Node node = nodelist.item(i);
            switch (node.getNodeName()) {
                case "theme":
                    theme = ThemeDTO.fromString(node.getTextContent());
                    break;
                case "lyrics":
                    //String[] rawLyrics = new String(node.getTextContent().getBytes(), "UTF-8").split("\n");
                    String[] rawLyrics = node.getTextContent().split("\n");
                    List<String> newLyrics = new ArrayList<>();
                    for (String line : rawLyrics) {
                        if (!line.isEmpty()) {
                            newLyrics.add(line);
                        }
                    }
                    lyrics = newLyrics.toArray(new String[newLyrics.size()]);
                    break;
                case "smalllines":
                    //String[] rawSmallLines = new String(node.getTextContent().getBytes(), "UTF-8").split("\n");
                    String[] rawSmallLines = node.getTextContent().split("\n");
                    List<String> newSmallLines = new ArrayList<>();
                    for (String line : rawSmallLines) {
                        if (!line.isEmpty()) {
                            newSmallLines.add(line);
                        }
                    }
                    smallLines = newSmallLines.toArray(new String[newSmallLines.size()]);
                    break;
            }
            //} catch (UnsupportedEncodingException ex) {
            //    Logger.getLogger(TextSection.class.getName()).log(Level.SEVERE, null, ex);
            //}
        }
        TextSection ret = new TextSection(sectionTitle, lyrics, smallLines, capitalise);
        if (theme != null) {
            ret.setTheme(theme);
        }
        return ret;
    }

    /**
     * Get the title of the section.
     *
     * @return the title of the section.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the lyrics of the section.
     *
     * @param chords true if any chords should be included in the text (if
     * present), false otherwise.
     * @param comments true if any comments should be included, false otherwise.
     * @return the lyrics of the section.
     */
    public String[] getText(boolean chords, boolean comments) {
        List<String> ret = new ArrayList<>(lines.length);
        for (String str : lines) {
            if (chords) {
                if (comments) {
                    ret.add(str);
                } else {
                    ret.add(removeComments(str));
                }
            } else {
                if (new LineTypeChecker(str).getLineType() != LineTypeChecker.Type.CHORDS) {
                    if (comments) {
                        ret.add(str);
                    } else {
                        ret.add(removeComments(str));
                    }
                }
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    /**
     * Remove comments from a string.
     *
     * @param line the line to remove comments from.
     * @return the string without comments.
     */
    private String removeComments(String line) {
        line = trimFromEnd(line);
        if (line.toLowerCase().endsWith("//lyrics")) {
            return line.substring(0, line.indexOf("//lyrics"));
        }
        if (line.toLowerCase().endsWith("//chords")) {
            return line.substring(0, line.indexOf("//chords"));
        }
        if (line.toLowerCase().endsWith("//title")) {
            return line.substring(0, line.indexOf("//title"));
        }
        return line;
    }

    /**
     * Trim whitespace from the end of the string (but not the start.)
     *
     * @param str the string to trim.
     * @return the trimmed string.
     */
    private String trimFromEnd(String str) {
        int pos = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != ' ') {
                pos = i + 1;
                break;
            }
        }
        return str.substring(0, pos);
    }

    /**
     * Get the small text of the section.
     *
     * @return the small text of the section.
     */
    public String[] getSmallText() {
        return Arrays.copyOf(smallLines, smallLines.length);
    }

    /**
     * Get the theme of the section.
     *
     * @return the theme of the section.
     */
    public ThemeDTO getTheme() {
        return theme;
    }

    /**
     * Get the temporary theme of the section.
     *
     * @return the temporary theme of the section, or null if none has been set.
     */
    public ThemeDTO getTempTheme() {
        return tempTheme;
    }

    /**
     * Set the temporary theme of the section.
     *
     * @param tempTheme the temporary theme.
     */
    public void setTempTheme(ThemeDTO tempTheme) {
        this.tempTheme = tempTheme;
    }

    /**
     * Set the theme of the section.
     *
     * @param theme the new theme.
     */
    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
    }

    /**
     * Determine if this text section is equal to another object.
     *
     * @param obj the other object
     * @return true if it's equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextSection other = (TextSection) obj;
        if (!Arrays.deepEquals(this.lines, other.lines)) {
            return false;
        }
        return true;
    }

    /**
     * Generate a hashcode of this text section.
     *
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Arrays.deepHashCode(this.lines);
        return hash;
    }

    /**
     * Get a string representation of this song section.
     *
     * @return a string representation.
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(title).append('\n');
        for (String str : lines) {
            ret.append(str).append('\n');
        }
        return ret.toString();
    }

    /**
     * Determine whether the first word of each line should be a capital (if
     * Quelea allows it.)
     *
     * @return whether this text section should capitalise the beginning of
     * every line.
     */
    public boolean shouldCapitaliseFirst() {
        return capitaliseFirst;
    }
}
