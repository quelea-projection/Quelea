package org.quelea.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.quelea.Theme;
import org.quelea.Utils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a section of a song, eg. a verse, chorus or bridge.
 * @author Michael
 */
public class SongSection {

    private String title;
    private String[] lyrics;
    private Theme theme;

    /**
     * Create a new song section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lyrics the lyrics of the section, one line per array entry.
     */
    public SongSection(String title, String[] lyrics) {
        this(title, lyrics, null);
    }

    /**
     * Create a new song section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lyrics the lyrics of the section, one line per array entry.
     * @param theme the theme of this song section.
     */
    public SongSection(String title, String[] lyrics, Theme theme) {
        this.title = title;
        this.lyrics = Arrays.copyOf(lyrics, lyrics.length);
        this.theme = theme;
    }

    /**
     * Get a representation of this section in XML format.
     * @return the section in XML format.
     */
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<section ").append("title=\"").append(getTitle()).append("\">");
        xml.append("<theme>");
        xml.append(theme.toDBString());
        xml.append("</theme>");
        xml.append("<lyrics>");
        for (String line : getLyrics()) {
            xml.append(Utils.escapeXML(line)).append('\n');
        }
        xml.append("</lyrics></section>");
        return xml.toString();
    }

    public static SongSection parseXML(Node sectionNode) {
        try {
            NamedNodeMap attributes = sectionNode.getAttributes();
            String sectionTitle = null;
            String[] lyrics = null;
            Theme theme = null;
            if (attributes != null) {
                Node titleNode = attributes.getNamedItem("title");
                if (titleNode != null) {
                    sectionTitle = titleNode.getTextContent();
                }
            }
            NodeList nodelist = sectionNode.getChildNodes();
            for(int i=0 ; i<nodelist.getLength() ; i++) {
                Node node = nodelist.item(i);
                if(node.getNodeName().equals("theme")) {
                    theme = Theme.parseDBString(node.getTextContent());
                }
                else if(node.getNodeName().equals("lyrics")) {
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
            SongSection ret = new SongSection(sectionTitle, lyrics);
            if(theme != null) {
                ret.setTheme(theme);
            }
            return ret;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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
    public String[] getLyrics() {
        return Arrays.copyOf(lyrics, lyrics.length);
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

    /**
     * Get a string representation of this song section.
     * @return a string representation.
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(title).append('\n');
        for (String str : lyrics) {
            ret.append(str).append('\n');
        }
        return ret.toString();
    }
}
