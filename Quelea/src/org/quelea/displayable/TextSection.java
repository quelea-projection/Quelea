package org.quelea.displayable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.quelea.Theme;
import org.quelea.utils.Utils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a section of text in a song or passage.
 * @author Michael
 */
public class TextSection {

    private String title;
    private String[] lines;
    private Theme theme;
    private boolean capitaliseFirst;

    /**
     * Create a new text section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lines the lines of the section, one line per array entry.
     */
    public TextSection(String title, String[] lines, boolean capitaliseFirst) {
        this(title, lines, capitaliseFirst, null);
    }

    /**
     * Create a new song section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lines the lines of the section, one line per array entry.
     * @param theme the theme of this song section.
     */
    public TextSection(String title, String[] lines, boolean capitaliseFirst, Theme theme) {
        this.capitaliseFirst = capitaliseFirst;
        this.title = title;
        this.lines = Arrays.copyOf(lines, lines.length);
        this.theme = theme;
    }

    /**
     * Get a representation of this section in XML format.
     * @return the section in XML format.
     */
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<section ").append("title=\"").append(getTitle()).append("\" capitalise=\"").append(shouldCapitaliseFirst()).append("\">");
        if(theme != null) {
            xml.append("<theme>");
            xml.append(theme.toDBString());
            xml.append("</theme>");
        }
        xml.append("<lyrics>");
        for (String line : getText()) {
            xml.append(Utils.escapeXML(line)).append('\n');
        }
        xml.append("</lyrics></section>");
        return xml.toString();
    }

    /**
     * Parse the given node to create a new song section.
     * @param sectionNode the section node.
     * @return the song section.
     */
    public static TextSection parseXML(Node sectionNode) {
        NamedNodeMap attributes = sectionNode.getAttributes();
        String sectionTitle = null;
        String[] lyrics = null;
        Theme theme = null;
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
            Node node = nodelist.item(i);
            if (node.getNodeName().equals("theme")) {
                theme = Theme.parseDBString(node.getTextContent());
            }
            else if (node.getNodeName().equals("lyrics")) {
                String[] rawLyrics = node.getTextContent().split("\n");
                List<String> newLyrics = new ArrayList<String>();
                for (String line : rawLyrics) {
                    if (!line.isEmpty()) {
                        newLyrics.add(line);
                    }
                }
                lyrics = newLyrics.toArray(new String[newLyrics.size()]);
            }
        }
        TextSection ret = new TextSection(sectionTitle, lyrics, capitalise);
        if (theme != null) {
            ret.setTheme(theme);
        }
        return ret;
    }

    /**
     * Get the title of the section.
     * @return the title of the section.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the lyrics of the section.
     * @return the lyrics of the section.
     */
    public String[] getText() {
        return Arrays.copyOf(lines, lines.length);
    }

    /**
     * Get the theme of the section.
     * @return the theme of the section.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Set the theme of the section.
     * @param theme the new theme.
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Arrays.deepHashCode(this.lines);
        return hash;
    }

    /**
     * Get a string representation of this song section.
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
     * @return whether this text section should capitalise the beginning of
     * every line.
     */
    public boolean shouldCapitaliseFirst() {
        return capitaliseFirst;
    }
}
