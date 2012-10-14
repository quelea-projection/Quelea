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
package org.quelea.displayable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 *
 * @author Michael
 */
public class Song implements TextDisplayable, Comparable<Song>, Printable {

    /**
     * The builder responsible for building this song.
     */
    public static class Builder {

        private Song song;

        /**
         * Create a new builder with the required fields.
         *
         * @param title the title of the song.
         * @param author the author of the song.
         */
        public Builder(String title, String author) {
            song = new Song(title, author);
        }

        /**
         * Set the id of the song.
         *
         * @param id the song's id.
         * @return this builder.
         */
        public Builder id(int id) {
            song.id = id;
            return this;
        }

        /**
         * Set the ccli number of the song.
         *
         * @param ccli the song's ccli number.
         * @return this builder.
         */
        public Builder ccli(String ccli) {
            if(ccli == null) {
                ccli = "";
            }
            song.ccli = ccli;
            return this;
        }

        /**
         * Set the year of the song.
         *
         * @param year the song's year.
         * @return this builder.
         */
        public Builder year(String year) {
            if(year == null) {
                year = "";
            }
            song.year = year;
            return this;
        }

        /**
         * Set the publisher of the song.
         *
         * @param publisher the song's publisher.
         * @return this builder.
         */
        public Builder publisher(String publisher) {
            if(publisher == null) {
                publisher = "";
            }
            song.publisher = publisher;
            return this;
        }

        /**
         * Set the tags of this song..
         *
         * @param tags the song's tags.
         * @return this builder.
         */
        public Builder tags(String tags) {
            if(tags == null) {
                song.tags = new String[0];
            }
            else {
                song.tags = tags.split(";");
            }
            return this;
        }

        /**
         * Set the tags of this song..
         *
         * @param tags the song's tags.
         * @return this builder.
         */
        public Builder tags(String[] tags) {
            if(tags == null) {
                tags = new String[0];
            }
            song.tags = tags;
            return this;
        }

        /**
         * Set the theme of this song..
         *
         * @param theme the song's theme.
         * @return this builder.
         */
        public Builder theme(Theme theme) {
            song.theme = theme;
            return this;
        }

        /**
         * Set the lyrics of this song..
         *
         * @param lyrics the song's tags.
         * @return this builder.
         */
        public Builder lyrics(String lyrics) {
            song.setLyrics(lyrics);
            return this;
        }

        /**
         * Set the copyright info of this song..
         *
         * @param copyright the song's copyright info.
         * @return this builder.
         */
        public Builder copyright(String copyright) {
            if(copyright == null) {
                copyright = "";
            }
            song.copyright = copyright;
            return this;
        }

        /**
         * Set the key of this song..
         *
         * @param key the song's key.
         * @return this builder.
         */
        public Builder key(String key) {
            if(key == null) {
                key = "";
            }
            song.key = key;
            return this;
        }

        /**
         * Set the capo of this song..
         *
         * @param capo the song's capo.
         * @return this builder.
         */
        public Builder capo(String capo) {
            if(capo == null) {
                capo = "";
            }
            song.capo = capo;
            return this;
        }

        /**
         * Set the info string of this song..
         *
         * @param info the song's information field.
         * @return this builder.
         */
        public Builder info(String info) {
            if(info == null) {
                info = "";
            }
            song.info = info;
            return this;
        }

        /**
         * Get the song from this builder with all the fields set appropriately.
         *
         * @return the song.
         */
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
    private String capo;
    private String info;
    private String[] tags;
    private List<TextSection> sections;
    private Theme theme;
    private int id;
    private boolean printChords;
    private String lastSearch;

    /**
     * Copy constructor - creates a shallow copy.
     *
     * @param song the song to copy to create the new song.
     */
    public Song(Song song) {
        this.title = song.title;
        this.author = song.author;
        this.sections = song.sections;
        this.theme = song.theme;
        this.id = song.id;
        this.ccli = song.ccli;
        this.year = song.year;
        this.publisher = song.publisher;
        this.copyright = song.copyright;
        this.key = song.key;
        this.info = song.info;
        this.tags = song.tags;
        this.capo = song.capo;
        this.lastSearch = song.lastSearch;
    }

    /**
     * Create a new, empty song.
     *
     * @param title the title of the song.
     * @param author the author of the song.
     */
    public Song(String title, String author) {
        this(title, author, new Theme(Theme.DEFAULT_FONT, Theme.DEFAULT_FONT_COLOR, Theme.DEFAULT_BACKGROUND));
    }

    /**
     * Create a new, empty song.
     *
     * @param title the title of the song.
     * @param author the author of the song.
     * @param theme the theme of the song.
     */
    public Song(String title, String author, Theme theme) {
        id = -1;
        this.title = title;
        this.author = author;
        this.theme = theme;
        sections = new ArrayList<>();
    }

    /**
     * Try and give this song an ID based on the ID in the database. If this
     * can't be done, leave it as -1.
     */
    public void matchID() {
        if(id == -1) {
            for(Song song : SongDatabase.get().getSongs()) {
                if(this.title.equals(song.title)) {
                    id = song.getID();
                }
            }
        }
    }

    /**
     * Determine whether this song contains any lines of chords.
     *
     * @return true if it contains chords, false otherwise.
     */
    public boolean hasChords() {
        String[] lyrics = getLyrics(true, true).split("\n");
        for(String line : lyrics) {
            if(new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the unique ID of the song.
     *
     * @return the ID of the song.
     */
    public int getID() {
        return id;
    }

    /**
     * Set the unique ID of this song.
     *
     * @param id the id of the song.
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Get the title of this song.
     *
     * @return the title of this song.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the song.
     *
     * @param title the new song title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the author of this song.
     *
     * @return the author of the song.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author of the song.
     *
     * @param author the new song author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Return true because songs can be cleared.
     *
     * @return true, always.
     */
    @Override
    public boolean supportClear() {
        return true;
    }

    /**
     * Get the CCLI number of this song.
     *
     * @return the CCLI number of this song.
     */
    public String getCcli() {
        return ccli;
    }

    /**
     * Get the publisher of this song.
     *
     * @return the publisher of this song.
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Get the tags of this song.
     *
     * @return the tags of this song.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Get the tags of this song as a single string delimited by semicolons.
     *
     * @return the tags of this song.
     */
    public String getTagsAsString() {
        if(tags == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(tags.length * 5);
        for(int i = 0; i < tags.length; i++) {
            ret.append(tags[i]);
            if(i != tags.length - 1) {
                ret.append("; ");
            }
        }
        return ret.toString();
    }

    /**
     * Get the year of this song.
     *
     * @return the year of this song.
     */
    public String getYear() {
        return year;
    }

    /**
     * Get the copyright information of this song.
     *
     * @return the copyright information of this song.
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Get the key of this song.
     *
     * @return the key of this song.
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the general information about this song.
     *
     * @return the general information about this song.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Get the capo of this song.
     *
     * @return the capo of this song.
     */
    public String getCapo() {
        return capo;
    }

    /**
     * Set the capo of this song.
     *
     * @param capo the capo of this song.
     */
    public void setCapo(String capo) {
        this.capo = capo;
    }

    /**
     * Set whether to print the chords of this song - temporary field used when
     * printing chords.
     *
     * @param printChords true if chords should be printed, false otherwise.
     */
    public void setPrintChords(boolean printChords) {
        this.printChords = printChords;
    }

    /**
     * Set the info of this song.
     *
     * @param info the info of this song.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Set the key of this song.
     *
     * @param key the key of this song.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Set the ccli number of this song.
     *
     * @param ccli the ccli number of this song.
     */
    public void setCcli(String ccli) {
        this.ccli = ccli;
    }

    /**
     * Set the publisher of this song.
     *
     * @param publisher the publisher of this song.
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Set the tags of this song.
     *
     * @param tags the tags of this song.
     */
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    /**
     * Set the tags of this song as a list separated by semi-colons.
     *
     * @param tags the tags of this song.
     */
    public void setTags(String tags) {
        this.tags = tags.split(";");
    }

    /**
     * Set the year of this song.
     *
     * @param year the year of this song.
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Set the copyright field of this song.
     *
     * @param copyright the copyright field of this song.
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /**
     * Get all the lyrics to this song as a string. This can be parsed using the
     * setLyrics() method.
     *
     * @param chords true if any chords should be included, false otherwise.
     * @param comments true if any comments should be included, false otherwise.
     * @return the lyrics to this song.
     */
    public String getLyrics(boolean chords, boolean comments) {
        StringBuilder ret = new StringBuilder();
        for(TextSection section : sections) {
            if(section.getTitle() != null && !section.getTitle().equals("")) {
                ret.append(section.getTitle()).append("\n");
            }
            for(String line : section.getText(chords, comments)) {
                ret.append(line).append("\n");
            }
            ret.append("\n");
        }
        return ret.toString().trim();
    }

    /**
     * Set the lyrics to this song as a string. This will erase any sections
     * currently in the song and parse the given lyrics into a number of song
     * sections.
     *
     * @param lyrics the lyrics to set as this song's lyrics.
     */
    public void setLyrics(String lyrics) {
        sections.clear();
        lyrics = lyrics.replaceAll("\n\n+", "\n\n");
        for(String section : lyrics.split("\n\n")) {
            String[] sectionLines = section.split("\n");
            String[] newLyrics = section.split("\n");
            String sectionTitle = "";
            if(sectionLines.length == 0) {
                continue;
            }
            if(new LineTypeChecker(sectionLines[0]).getLineType() == LineTypeChecker.Type.TITLE) {
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
    }

    /**
     * Add a section to this song.
     *
     * @param section the section to add.
     */
    public void addSection(TextSection section) {
        if(section.getTheme() == null) {
            section.setTheme(theme);
        }
        sections.add(section);
    }

    /**
     * Add a section to this song at the specified index.
     *
     * @param index the index to add the song at.
     * @param section the section to add.
     */
    public void addSection(int index, TextSection section) {
        if(section.getTheme() == null) {
            section.setTheme(theme);
        }
        sections.add(index, section);
    }

    /**
     * Add a number of sections to this song.
     *
     * @param sections the sections to add.
     */
    public void addSections(TextSection[] sections) {
        for(TextSection section : sections) {
            addSection(section);
        }
    }

    /**
     * Replace the text section at the given index with the new section.
     *
     * @param newSection the new section to use to replace the existing one.
     * @param index the index of the section to replace.
     */
    public void replaceSection(TextSection newSection, int index) {
        sections.set(index, newSection);
    }
    
    /**
     * Remove the given text section.
     * 
     * @param index the index of the text section to remove.
     */
    public void removeSection(int index) {
        sections.remove(index);
    }

    /**
     * Get an array of all the sections in this song.
     *
     * @return the song sections.
     */
    @Override
    public TextSection[] getSections() {
        return sections.toArray(new TextSection[sections.size()]);
    }

    /**
     * Set the last search text (for highlighting.)
     *
     * @param lastSearch
     */
    public void setLastSearch(String lastSearch) {
        this.lastSearch = lastSearch;
    }

    /**
     * Get the HTML that should be displayed in the library song list. This
     * depends on what was searched for last, it bolds the search term in the
     * title (if it appears as such.)
     *
     * @return the appropriate HTML to display the song in the list.
     */
    public String getListHTML() {
        if(lastSearch == null) {
            return getTitle();
        }
        int startIndex = getTitle().toLowerCase().indexOf(lastSearch.toLowerCase());
        if(startIndex == -1) {
            return getTitle();
        }
        StringBuilder ret = new StringBuilder();
        ret.append("<html>");
        ret.append(getTitle().substring(0, startIndex));
        ret.append("<b>");
        ret.append(getTitle().substring(startIndex, startIndex + lastSearch.length()));
        ret.append("</b>");
        ret.append(getTitle().substring(startIndex + lastSearch.length()));
        ret.append("</html>");
        return ret.toString();
    }

    /**
     * Get a representation of this song in XML format.
     *
     * @return the song in XML format.
     */
    @Override
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
        for(TextSection section : sections) {
            xml.append(section.getXML());
        }
        xml.append("</lyrics>");
        xml.append("</song>");
        return xml.toString();
    }

    /**
     * Parse a song in XML format and return the song object.
     *
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
     *
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
     *
     * @param song the song node to parse.
     * @return the song, or null if an error occurs.
     */
    public static Song parseXML(Node song) {
        NodeList list = song.getChildNodes();
        String title = "";
        String author = "";
        List<TextSection> songSections = new ArrayList<>();
        for(int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if(node.getNodeName().equals("title")) {
                title = node.getTextContent();
            }
            if(node.getNodeName().equals("author")) {
                author = node.getTextContent();
            }
            if(node.getNodeName().equals("lyrics")) {
                NodeList sections = node.getChildNodes();
                for(int j = 0; j < sections.getLength(); j++) {
                    Node sectionNode = sections.item(j);
                    if(sectionNode.getNodeName().equals("section")) {
                        songSections.add(TextSection.parseXML(sectionNode));
                    }
                }
            }
        }
        Song ret = new Song(title, author, new Theme(Theme.DEFAULT_FONT, Theme.DEFAULT_FONT_COLOR, Theme.DEFAULT_BACKGROUND));
        for(TextSection section : songSections) {
            ret.addSection(section);
        }
        return ret;
    }

    /**
     * Generate a hashcode for this song.
     *
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
     *
     * @param obj the other object.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!(obj instanceof Song)) {
            return false;
        }
        final Song other = (Song) obj;
        if((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
            return false;
        }
        if((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
            return false;
        }
        if(this.sections != other.sections && (this.sections == null || !this.sections.equals(other.sections))) {
            return false;
        }
        if(this.theme != other.theme && (this.theme == null || !this.theme.equals(other.theme))) {
            return false;
        }
        return true;
    }

    /**
     * Compare this song to another song, first by title and then by author.
     *
     * @param other the other song.
     * @return 1 if this song is greater than the other song, 0 if they're the
     * same, and -1 if this is less than the other song.
     */
    @Override
    public int compareTo(Song other) {
        int result = getTitle().compareToIgnoreCase(other.getTitle());
        if(result == 0) {
            if(getAuthor() != null && other.getAuthor() != null) {
                result = getAuthor().compareToIgnoreCase(other.getAuthor());
            }
            if(result == 0 && getLyrics(false, false) != null && other.getLyrics(false, false) != null) {
                result = getLyrics(false, false).compareTo(other.getLyrics(false, false));
            }
        }
        return result;
    }

    /**
     * Get a string representation of this song.
     *
     * @return a string representation of the song.
     */
    @Override
    public String toString() {
        return getXML();
    }

    /**
     * Get the preview icon of this song.
     *
     * @return the song's preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        if(hasChords()) {
            return new ImageView(new Image("file:icons/lyricsandchords.png"));
        }
        else {
            return new ImageView(new Image("file:icons/lyrics.png"));
        }
    }

    /**
     * Get the preview text of this song.
     *
     * @return the song's preview text.
     */
    @Override
    public String getPreviewText() {
        return getTitle() + "\n" + getAuthor();
    }

    /**
     * Remove any duplicate sections in this song.
     */
    public void removeDuplicateSections() {
        Utils.removeDuplicateWithOrder(sections);
    }

    /**
     * Get all the files used by this song.
     *
     * @return all the files used by this song.
     */
    @Override
    public Collection<File> getResources() {
        Set<File> ret = new HashSet<>();
        for(TextSection section : getSections()) {
            Theme sectionTheme = section.getTheme();
            if(sectionTheme != null) {
                Background background = sectionTheme.getBackground();
                if(background.getImageLocation() != null) {
                    ret.add(background.getImageFile());
                }
            }
        }
        return ret;
    }

    /**
     * Get the summary text to print in the order of service.
     *
     * @return the summary text to print in the order of service.
     */
    @Override
    public String getPrintText() {
        return "Song: " + getTitle() + " (" + getAuthor() + ")";
    }
    //Field just used for the calculation of how to print the song
    private List<Integer> nextSection = new ArrayList<>();

    /**
     * Print out the song.
     *
     * @param graphics the graphics to print onto.
     * @param pageFormat the page format to print.
     * @param pageIndex the page index to be printed.
     * @return PAGE_EXISTS or NO_SUCH_PAGE
     * @throws PrinterException if something went wrong.
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if(pageIndex == 0) {
            nextSection.clear();
            nextSection.add(0);
        }
        else if(nextSection.get(pageIndex) >= getSections().length) {
            return NO_SUCH_PAGE;
        }

        final int minx = (int) pageFormat.getImageableX();
        final int miny = (int) pageFormat.getImageableY();
        final int maxx = minx + (int) pageFormat.getImageableWidth();
        final int maxy = miny + (int) pageFormat.getImageableHeight();

        int pos = miny;

        if(pageIndex == 0) {
            int fontSize = 38;
            int width;
            do {
                fontSize -= 2;
                graphics.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                width = graphics.getFontMetrics().stringWidth(getTitle().toUpperCase());
            } while(width > maxx - minx);

            graphics.drawString(getTitle().toUpperCase(), minx, miny + graphics.getFontMetrics().getHeight());
            pos += graphics.getFontMetrics().getHeight() + graphics.getFontMetrics().getDescent();

            if(!getAuthor().isEmpty() || (printChords && !getCapo().isEmpty())) {
                graphics.setFont(new Font("SansSerif", Font.ITALIC, 20));
                pos += graphics.getFontMetrics().getHeight();
                graphics.drawString(getAuthor(), minx, pos);
                if(printChords && !getCapo().isEmpty()) {
                    String capoStr = "Capo " + getCapo();
                    int capoStrWidth = graphics.getFontMetrics().stringWidth(capoStr);
                    graphics.drawString(capoStr, maxx - capoStrWidth, pos);
                }
                pos += 10;
            }
            graphics.fillRect(minx, pos, maxx - minx, 3);

        }

        pos += 30;

        for(int i = nextSection.get(pageIndex); i < getSections().length; i++) {
            TextSection section = getSections()[i];
            int height = graphics.getFontMetrics().getHeight() * section.getText(printChords, false).length;
            if(pos + height > maxy - miny) {
                if(nextSection.size() <= pageIndex + 1) {
                    nextSection.add(0);
                }
                nextSection.set(pageIndex + 1, i);
                return PAGE_EXISTS;
            }
            for(String str : section.getText(true, false)) {
                switch(new LineTypeChecker(str).getLineType()) {
                    case CHORDS:
                        if(!printChords) {
                            continue;
                        }
                        graphics.setFont(new Font("SansSerif", Font.BOLD, 16));
                        graphics.setColor(Color.BLACK);
                        break;
                    default:
                        graphics.setFont(new Font("SansSerif", 0, 16));
                        graphics.setColor(Color.BLACK);
                        break;
                }
                graphics.drawString(str, minx, pos);
                pos += graphics.getFontMetrics().getHeight();
            }
            pos += 30;
        }
        if(nextSection.size() <= pageIndex + 1) {
            nextSection.add(0);
        }
        nextSection.set(pageIndex + 1, getSections().length);
        return PAGE_EXISTS;
    }
    
    @Override
    public void dispose() {
        //Nothing needed here.
    }
}
