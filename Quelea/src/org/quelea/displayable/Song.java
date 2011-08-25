package org.quelea.displayable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.Background;
import org.quelea.SongDatabase;
import org.quelea.Theme;
import org.quelea.utils.LineTypeChecker;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A song that contains a number of sections (verses, choruses, etc.)
 * @author Michael
 */
public class Song implements TextDisplayable, Searchable, Comparable<Song> {

    public static class Builder {

        private Song song;

        public Builder(String title, String author) {
            song = new Song(title, author);
        }

        public Builder id(int id) {
            song.id = id;
            return this;
        }

        public Builder ccli(String str) {
            if (str == null) {
                str = "";
            }
            song.ccli = str;
            return this;
        }

        public Builder year(String str) {
            if (str == null) {
                str = "";
            }
            song.year = str;
            return this;
        }

        public Builder publisher(String str) {
            if (str == null) {
                str = "";
            }
            song.publisher = str;
            return this;
        }

        public Builder tags(String str) {
            if (str == null) {
                song.tags = new String[0];
            }
            else {
                song.tags = str.split(";");
            }
            return this;
        }

        public Builder tags(String[] arr) {
            if (arr == null) {
                arr = new String[0];
            }
            song.tags = arr;
            return this;
        }

        public Builder tags(Theme theme) {
            song.theme = theme;
            return this;
        }

        public Builder lyrics(String lyrics) {
            song.setLyrics(lyrics);
            return this;
        }

        public Builder copyright(String copyright) {
            if (copyright == null) {
                copyright = "";
            }
            song.copyright = copyright;
            return this;
        }
        
        public Builder key(String key) {
            if (key == null) {
                key = "";
            }
            song.key = key;
            return this;
        }
        
        public Builder info(String info) {
            if (info == null) {
                info = "";
            }
            song.info = info;
            return this;
        }

        public Song get() {
            return song;
        }

    }
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String title;
    private String author;
    private String ccli;
    private String year;
    private String publisher;
    private String copyright;
    private String key;
    private String info;
    private String[] tags;
    private List<TextSection> sections;
    private Theme theme;
    private int id;
    private SoftReference<String> searchLyrics;

    /**
     * Copy constructor - creates a shallow copy.
     * @param song the song to copy to create the new song.
     */
    public Song(Song song) {
        this.title = song.title;
        this.author = song.author;
        this.sections = song.sections;
        this.theme = song.theme;
        this.id = song.id;
        this.searchLyrics = song.searchLyrics;
        this.ccli = song.ccli;
        this.year = song.year;
        this.publisher = song.publisher;
        this.copyright = song.copyright;
        this.key = song.key;
        this.info = song.info;
        this.tags = song.tags;
    }

    /**
     * Create a new, empty song.
     * @param title  the title of the song.
     * @param author the author of the song.
     */
    public Song(String title, String author) {
        this(title, author, new Theme(Theme.DEFAULT_FONT, Theme.DEFAULT_FONT_COLOR, Theme.DEFAULT_BACKGROUND));
    }

    /**
     * Create a new, empty song.
     * @param title  the title of the song.
     * @param author the author of the song.
     * @param theme  the theme of the song.
     */
    public Song(String title, String author, Theme theme) {
        id = -1;
        this.title = title;
        this.author = author;
        this.theme = theme;
        sections = new ArrayList<>();
    }

    /**
     * Try and give this song an ID based on the ID in the database. If this can't be done, leave it as -1.
     */
    public void matchID() {
        if (id == -1) {
            for (Song song : SongDatabase.get().getSongs()) {
                if (this.title.equals(song.title)) {
                    id = song.getID();
                }
            }
        }
    }

    /**
     * Get the unique ID of the song.
     * @return the ID of the song.
     */
    public int getID() {
        return id;
    }

    /**
     * Set the unique ID of this song.
     * @param id the id of the song.
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Get the title of this song.
     * @return the title of this song.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the song.
     * @param title the new song title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the author of this song.
     * @return the author of the song.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author of the song.
     * @param author the new song author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean supportClear() {
        return true;
    }

    public String getCcli() {
        return ccli;
    }

    public String getPublisher() {
        return publisher;
    }

    public String[] getTags() {
        return tags;
    }

    public String getTagsAsString() {
        if(tags==null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(tags.length*5);
        for (int i = 0; i < tags.length; i++) {
            ret.append(tags[i]);
            if (i != tags.length - 1) {
                ret.append("; ");
            }
        }
        return ret.toString();
    }

    public String getYear() {
        return year;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getKey() {
        return key;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public void setCcli(String ccli) {
        this.ccli = ccli;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setTags(String tags) {
        this.tags = tags.split(";");
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /**
     * Get all the lyrics to this song as a string. This can be parsed using the setLyrics() method.
     * @param chords true if any chords should be included, false otherwise.
     * @return the lyrics to this song.
     */
    public String getLyrics(boolean chords) {
        StringBuilder ret = new StringBuilder();
        for (TextSection section : sections) {
            if (section.getTitle() != null && !section.getTitle().equals("")) {
                ret.append(section.getTitle()).append("\n");
            }
            for (String line : section.getText(chords)) {
                ret.append(line).append("\n");
            }
            ret.append("\n");
        }
        return ret.toString().trim();
    }

    /**
     * Set the lyrics to this song as a string. This will erase any sections currently in the song and parse the given
     * lyrics into a number of song sections.
     * @param lyrics the lyrics to set as this song's lyrics.
     */
    public void setLyrics(String lyrics) {
        sections.clear();
        lyrics = lyrics.replaceAll("\n\n+", "\n\n");
        for (String section : lyrics.split("\n\n")) {
            String[] sectionLines = section.split("\n");
            String[] newLyrics = section.split("\n");
            String sectionTitle = "";
            if (sectionLines.length == 0) {
                continue;
            }
            if (new LineTypeChecker(sectionLines[0]).getLineType() == LineTypeChecker.Type.TITLE) {
                sectionTitle = sectionLines[0];
                newLyrics = new String[sectionLines.length - 1];
                System.arraycopy(sectionLines, 1, newLyrics, 0, newLyrics.length);
            }
            String[] smallLines = new String[]{
                title,
                author
            };
            sections.add(new TextSection(sectionTitle, newLyrics, smallLines, true));
        }
        searchLyrics = null;
    }

    /**
     * Add a section to this song.
     * @param section the section to add.
     */
    public void addSection(TextSection section) {
        if (section.getTheme() == null) {
            section.setTheme(theme);
        }
        sections.add(section);
        searchLyrics = null;
    }

    /**
     * Add a number of sections to this song.
     * @param sections the sections to add.
     */
    public void addSections(TextSection[] sections) {
        for (TextSection section : sections) {
            addSection(section);
        }
    }

    /**
     * Get an array of all the sections in this song.
     * @return the song sections.
     */
    public TextSection[] getSections() {
        return sections.toArray(new TextSection[sections.size()]);
    }

    /**
     * Determine whether this song matches a particular search. In doing so this method will check for whether the given
     * soft reference to the search lyrics is null or contains null, if it does then new search lyrics will be
     * generated. This acts as a type of cache to speed up searching.
     * @param s the search term.
     * @return true if the song matches, false otherwise.
     */
    public boolean search(String s) {
        if (searchLyrics == null || searchLyrics.get() == null) {
            searchLyrics = new SoftReference<>(stripPunctuation(getLyrics(false).replace("\n", " ")).toLowerCase());
        }
        return title.toLowerCase().contains(s)
                || searchLyrics.get().contains(stripPunctuation(s));
    }

    /**
     * Strip punctuation from the given string.
     * @param s the string to use to strip punctuation from
     * @return the "stripped" string.
     */
    private static String stripPunctuation(String s) {
        s = s.replaceAll("[ ]+", " ");
        StringBuilder ret = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    /**
     * Get a representation of this song in XML format.
     * @return the song in XML format.
     */
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<song>");
        xml.append("<title>");
        xml.append(Utils.escapeXML(title));
        xml.append("</title>");
        xml.append("<author>");
        xml.append(Utils.escapeXML(author));
        xml.append("</author>");
        xml.append("<lyrics>");
        for (TextSection section : sections) {
            xml.append(section.getXML());
        }
        xml.append("</lyrics>");
        xml.append("</song>");
        return xml.toString();
    }

    /**
     * Parse a song in XML format and return the song object.
     * @param xml the xml string to parse.
     * @return the song, or null if an error occurs.
     */
    public static Song parseXML(String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return parseXML(doc.getFirstChild());
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            return null;
        }
    }

    /**
     * Parse a song in XML format and return the song object.
     * @param inputStream the input stream containing the xml.
     * @return the song, or null if an error occurs.
     */
    public static Song parseXML(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return parseXML(doc.getChildNodes().item(0));
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        }
    }

    /**
     * Parse a song in XML format and return the song object.
     * @param song the song node to parse.
     * @return the song, or null if an error occurs.
     */
    public static Song parseXML(Node song) {
        NodeList list = song.getChildNodes();
        String title = "";
        String author = "";
        List<TextSection> songSections = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equals("title")) {
                title = node.getTextContent();
            }
            if (node.getNodeName().equals("author")) {
                author = node.getTextContent();
            }
            if (node.getNodeName().equals("lyrics")) {
                NodeList sections = node.getChildNodes();
                for (int j = 0; j < sections.getLength(); j++) {
                    Node sectionNode = sections.item(j);
                    if (sectionNode.getNodeName().equals("section")) {
                        songSections.add(TextSection.parseXML(sectionNode));
                    }
                }
            }
        }
        Song ret = new Song(title, author, new Theme(Theme.DEFAULT_FONT, Theme.DEFAULT_FONT_COLOR, Theme.DEFAULT_BACKGROUND));
        for (TextSection section : songSections) {
            ret.addSection(section);
        }
        return ret;
    }

    /**
     * Generate a hashcode for this song.
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 29 * hash + (this.author != null ? this.author.hashCode() : 0);
        hash = 29 * hash + (this.sections != null ? this.sections.hashCode() : 0);
        hash = 29 * hash + (this.theme != null ? this.theme.hashCode() : 0);
        return hash;
    }

    /**
     * Determine whether this song equals another object.
     * @param obj the other object.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Song)) {
            return false;
        }
        final Song other = (Song) obj;
        if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
            return false;
        }
        if ((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
            return false;
        }
        if (this.sections != other.sections && (this.sections == null || !this.sections.equals(other.sections))) {
            return false;
        }
        if (this.theme != other.theme && (this.theme == null || !this.theme.equals(other.theme))) {
            return false;
        }
        return true;
    }

    /**
     * Compare this song to another song, first by title and then by author.
     * @param other the other song.
     * @return 1 if this song is greater than the other song, 0 if they're the same, and -1 if this is less than the
     *         other song.
     */
    public int compareTo(Song other) {
        int result = getTitle().compareToIgnoreCase(other.getTitle());
        if (result == 0) {
            if (getAuthor() != null && other.getAuthor() != null) {
                result = getAuthor().compareToIgnoreCase(other.getAuthor());
            }
            if (result == 0 && getLyrics(false) != null && other.getLyrics(false) != null) {
                result = getLyrics(false).compareTo(other.getLyrics(false));
            }
        }
        return result;
    }

    /**
     * Get a string representation of this song.
     * @return a string representation of the song.
     */
    @Override
    public String toString() {
        return getXML();
    }

    /**
     * Get the preview icon of this song.
     * @return the song's preview icon.
     */
    public Icon getPreviewIcon() {
        return Utils.getImageIcon("icons/lyrics.png");
    }

    /**
     * Get the preview text of this song.
     * @return the song's preview text.
     */
    public String getPreviewText() {
        return "<html>" + getTitle() + "<br/><i>" + getAuthor() + "</i></html>";
    }

    /**
     * Remove any duplicate sections in this song.
     */
    public void removeDuplicateSections() {
        Utils.removeDuplicateWithOrder(sections);
    }

    /**
     * Get all the files used by this song.
     * @return all the files used by this song.
     */
    public Collection<File> getResources() {
        Set<File> ret = new HashSet<>();
        for (TextSection section : getSections()) {
            Theme sectionTheme = section.getTheme();
            if (sectionTheme != null) {
                Background background = sectionTheme.getBackground();
                if (background.getImageLocation() != null) {
                    ret.add(background.getImageFile());
                }
            }
        }
        return ret;
    }

    @Override
    public String getPrintText() {
        return "Song: " + getTitle() + " (" + getAuthor() + ")";
    }
}
