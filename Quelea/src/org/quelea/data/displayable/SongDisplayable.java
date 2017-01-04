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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.data.db.SongManager;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A song that contains a number of sections (verses, choruses, etc.)
 * <p/>
 * @author Michael
 */
public class SongDisplayable implements TextDisplayable, Comparable<SongDisplayable>, Serializable {
    public int count = 0;

    /**
     * The builder responsible for building this song.
     */
    public static class Builder {

        private final SongDisplayable song;

        /**
         * Create a new builder with the required fields.
         * <p/>
         * @param title the title of the song.
         * @param author the author of the song.
         */
        public Builder(String title, String author) {
            song = new SongDisplayable(title, author);
        }

        /**
         * Set the id of the song.
         * <p/>
         * @param id the song's id.
         * @return this builder.
         */
        public Builder id(long id) {
            song.id = id;
            return this;
        }

        /**
         * Set the ccli number of the song.
         * <p/>
         * @param ccli the song's ccli number.
         * @return this builder.
         */
        public Builder ccli(String ccli) {
            if (ccli == null) {
                ccli = "";
            }
            song.ccli = ccli;
            return this;
        }

        /**
         * Set the year of the song.
         * <p/>
         * @param year the song's year.
         * @return this builder.
         */
        public Builder year(String year) {
            if (year == null) {
                year = "";
            }
            song.year = year;
            return this;
        }

        /**
         * Set the publisher of the song.
         * <p/>
         * @param publisher the song's publisher.
         * @return this builder.
         */
        public Builder publisher(String publisher) {
            if (publisher == null) {
                publisher = "";
            }
            song.publisher = publisher;
            return this;
        }

        /**
         * Set the tags of this song..
         * <p/>
         * @param tags the song's tags.
         * @return this builder.
         */
        public Builder tags(String tags) {
            if (tags == null) {
                song.tags = new String[0];
            } else {
                song.tags = tags.split(";");
            }
            return this;
        }

        /**
         * Set the tags of this song..
         * <p/>
         * @param tags the song's tags.
         * @return this builder.
         */
        public Builder tags(String[] tags) {
            if (tags == null) {
                tags = new String[0];
            }
            song.tags = tags;
            return this;
        }

        /**
         * Set the theme of this song..
         * <p/>
         * @param theme the song's theme.
         * @return this builder.
         */
        public Builder theme(ThemeDTO theme) {
            song.theme = theme;
            return this;
        }

        /**
         * Set the lyrics of this song..
         * <p/>
         * @param lyrics the song's tags.
         * @return this builder.
         */
        public Builder lyrics(String lyrics) {
            song.setLyrics(lyrics);
            return this;
        }

        /**
         * Set the copyright info of this song..
         * <p/>
         * @param copyright the song's copyright info.
         * @return this builder.
         */
        public Builder copyright(String copyright) {
            if (copyright == null) {
                copyright = "";
            }
            song.copyright = copyright;
            return this;
        }

        /**
         * Set the key of this song..
         * <p/>
         * @param key the song's key.
         * @return this builder.
         */
        public Builder key(String key) {
            if (key == null) {
                key = "";
            }
            song.key = key;
            return this;
        }

        public Builder translations(HashMap<String, String> translations) {
            song.translations = translations;
            return this;
        }

        /**
         * Set the capo of this song..
         * <p/>
         * @param capo the song's capo.
         * @return this builder.
         */
        public Builder capo(String capo) {
            if (capo == null) {
                capo = "";
            }
            song.capo = capo;
            return this;
        }

        /**
         * Set the info string of this song..
         * <p/>
         * @param info the song's information field.
         * @return this builder.
         */
        public Builder info(String info) {
            if (info == null) {
                info = "";
            }
            song.info = info;
            return this;
        }

        /**
         * Get the song from this builder with all the fields set appropriately.
         * <p/>
         * @return the song.
         */
        public SongDisplayable get() {
            return song;
        }
    }
    public static final DataFormat SONG_DISPLAYABLE_FORMAT = new DataFormat("songdisplayable");
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean updateInDB = true;
    private String title = "";
    private String author = "";
    private String ccli = "";
    private String year = "";
    private String publisher = "";
    private String copyright = "";
    private String key = "";
    private String capo = "";
    private String info = "";
    private boolean quickInsert;
    private String[] tags;
    private List<TextSection> sections = new ArrayList<>();
    private HashMap<String, String> translations = new HashMap<>();
    private String currentTranslation;
    private ThemeDTO theme;
    private long id = 0;
    private boolean printChords;
    private String lastSearch = "";

    /**
     * Copy constructor - creates a shallow copy.
     * <p/>
     * @param song the song to copy to create the new song.
     */
    public SongDisplayable(SongDisplayable song) {
        this.title = song.title;
        this.author = song.author;
        this.sections = new ArrayList<>();
        for(TextSection section : song.getSections()) {
            this.sections.add(new TextSection(section));
        }
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
        this.translations = song.translations;
        this.count = song.count++;
    }

    /**
     * Create a new, empty song.
     * <p/>
     * @param title the title of the song.
     * @param author the author of the song.
     */
    public SongDisplayable(String title, String author) {
        this(title, author, new ThemeDTO(ThemeDTO.DEFAULT_FONT,
                ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, -1, 0));
    }

    /**
     * Create a new, empty song.
     * <p/>
     * @param title the title of the song.
     * @param author the author of the song.
     * @param theme the theme of the song.
     */
    public SongDisplayable(String title, String author, ThemeDTO theme) {
        id = -1;
        this.title = title;
        this.author = author;
        this.theme = theme;
        sections = new ArrayList<>();
    }
    
    /**
     * Ensure changes to this song are not updated in the database.
     */
    public void setNoDBUpdate() {
        updateInDB = false;
    }
    
    /**
     * Check whether changes to this song should be persisted to the database.
     * @return true if changes should be persisted, false otherwise.
     */
    public boolean checkDBUpdate() {
        return updateInDB;
    }

    /**
     * Set that this song is a quick insert song, and should not be updated in
     * the database.
     */
    public void setQuickInsert() {
        quickInsert = true;
    }

    /**
     * Determine if this song was entered via quick insert, and thus should not
     * be updated in the database.
     * <p>
     * @return true if this is a "quick insert" song, false otherwise.
     */
    public boolean isQuickInSert() {
        return quickInsert;
    }

    /**
     * Get the current translation lyrics that should be displayed for this song
     * and section, or null if none should be displayed.
     *
     * @param index the index of the section.
     * @return the current translation that should be displayed for this song
     * and section.
     */
    public String getCurrentTranslationSection(int index) {
        String val = getCurrentTranslationLyrics();
        if (val == null) {
            return null;
        }
        String[] parts = val.split("\n\n");
        if (parts.length > index) {
            return parts[index].trim();
        }
        return null;
    }

    /**
     * Set the translation that should be displayed alongside the default
     * lyrics. It should match a key in the translations map.
     *
     * @param currentTranslation the translation that should be displayed
     * alongside the default lyrics.
     */
    public void setCurrentTranslationLyrics(String currentTranslation) {
        this.currentTranslation = currentTranslation;
    }

    /**
     * Get the full translation lyrics for this song, or null if no translation
     * is selected.
     *
     * @return the full translation lyrics for this song, or null if no
     * translation is selected.
     */
    public String getCurrentTranslationLyrics() {
        if (translations == null) {
            return null;
        }
        String val = translations.get(currentTranslation);
        if (val == null) {
            return null;
        }
        return val;
    }

    /**
     * Get the name of the current translation in use, or null if none exists.
     *
     * @return the name of the current translation in use, or null if none
     * exists.
     */
    public String getCurrentTranslationName() {
        if (getCurrentTranslationLyrics() != null) {
            return currentTranslation;
        }
        return null;
    }

    /**
     * Try and give this song an ID based on the ID in the database. If this
     * can't be done, leave it as -1.
     */
    public void matchID() {
        if (id == -1 && updateInDB) {
            for (SongDisplayable song : SongManager.get().getSongs()) {
                if (this.title.equals(song.title) && getLyrics(true, true).equals(song.getLyrics(true, true)) && this.author.equals(song.author)) {
                    id = song.getID();
                }
            }
        }
    }

    /**
     * Determine whether this song contains any lines of chords.
     * <p/>
     * @return true if it contains chords, false otherwise.
     */
    public boolean hasChords() {
        String[] lyrics = getLyrics(true, true).split("\n");
        for (String line : lyrics) {
            if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the unique ID of the song.
     * <p/>
     * @return the ID of the song.
     */
    public long getID() {
        return id;
    }

    /**
     * Set the unique ID of this song.
     * <p/>
     * @param id the id of the song.
     */
    public void setID(long id) {
        this.id = id;
    }

    /**
     * Get the title of this song.
     * <p/>
     * @return the title of this song.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the song.
     * <p/>
     * @param title the new song title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public void setTranslations(HashMap<String, String> translations) {
        this.translations = translations;
    }

    /**
     * Get the author of this song.
     * <p/>
     * @return the author of the song.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author of the song.
     * <p/>
     * @param author the new song author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Return true because songs can be cleared.
     * <p/>
     * @return true, always.
     */
    @Override
    public boolean supportClear() {
        return true;
    }

    /**
     * Get the CCLI number of this song.
     * <p/>
     * @return the CCLI number of this song.
     */
    public String getCcli() {
        return ccli;
    }

    /**
     * Get the publisher of this song.
     * <p/>
     * @return the publisher of this song.
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Get the tags of this song.
     * <p/>
     * @return the tags of this song.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Get the tags of this song as a single string delimited by semicolons.
     * <p/>
     * @return the tags of this song.
     */
    public String getTagsAsString() {
        if (tags == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(tags.length * 5);
        for (int i = 0; i < tags.length; i++) {
            ret.append(tags[i]);
            if (i != tags.length - 1) {
                ret.append("; ");
            }
        }
        return ret.toString();
    }

    /**
     * Get the year of this song.
     * <p/>
     * @return the year of this song.
     */
    public String getYear() {
        return year;
    }

    /**
     * Retrieve assigned theme
     * <p/>
     * @return assigned theme
     */
    public ThemeDTO getTheme() {
        return this.theme;
    }

    /**
     * Get the copyright information of this song.
     * <p/>
     * @return the copyright information of this song.
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Get the key of this song.
     * <p/>
     * @return the key of this song.
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the general information about this song.
     * <p/>
     * @return the general information about this song.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Get the capo of this song.
     * <p/>
     * @return the capo of this song.
     */
    public String getCapo() {
        return capo;
    }

    /**
     * Set the capo of this song.
     * <p/>
     * @param capo the capo of this song.
     */
    public void setCapo(String capo) {
        this.capo = capo;
    }

    /**
     * Set whether to print the chords of this song - temporary field used when
     * printing chords.
     * <p/>
     * @param printChords true if chords should be printed, false otherwise.
     */
    public void setPrintChords(boolean printChords) {
        this.printChords = printChords;
    }

    /**
     * Set the info of this song.
     * <p/>
     * @param info the info of this song.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Set the key of this song.
     * <p/>
     * @param key the key of this song.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Set the ccli number of this song.
     * <p/>
     * @param ccli the ccli number of this song.
     */
    public void setCcli(String ccli) {
        this.ccli = ccli;
    }

    /**
     * Set the publisher of this song.
     * <p/>
     * @param publisher the publisher of this song.
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Set the tags of this song.
     * <p/>
     * @param tags the tags of this song.
     */
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    /**
     * Set the tags of this song as a list separated by semi-colons.
     * <p/>
     * @param tags the tags of this song.
     */
    public void setTags(String tags) {
        this.tags = tags.split(";");
    }

    /**
     * Set the year of this song.
     * <p/>
     * @param year the year of this song.
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Set the copyright field of this song.
     * <p/>
     * @param copyright the copyright field of this song.
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /**
     * Get all the lyrics to this song as a string. This can be parsed using the
     * setLyrics() method.
     * <p/>
     * @param chords true if any chords should be included, false otherwise.
     * @param comments true if any comments should be included, false otherwise.
     * @return the lyrics to this song.
     */
    public String getLyrics(boolean chords, boolean comments) {
        StringBuilder ret = new StringBuilder();
        for (TextSection section : sections) {
            if (section.getTitle() != null && !section.getTitle().equals("")) {
                ret.append(section.getTitle()).append("\n");
            }
            for (String line : section.getText(chords, comments)) {
                ret.append(line).append("\n");
            }
            ret.append("\n");
        }
        return ret.toString().trim().replace(" ", "<>");
    }

    public void addTranslation(String translationName, String translationText) {
        translations.put(translationName, translationText.trim());
    }

    public HashMap<String, String> getTranslations() {
        return translations;
    }

    /**
     * Set the lyrics to this song as a string. This will erase any sections
     * currently in the song and parse the given lyrics into a number of song
     * sections.
     * <p/>
     * @param lyrics the lyrics to set as this song's lyrics.
     */
    public void setLyrics(String lyrics) {
        sections.clear();
        boolean foundTitle = !(title == null || title.isEmpty());
        lyrics = lyrics.replaceAll("\n\n+", "\n\n");
        lyrics = lyrics.replace("<>", " ");
        for (String section : lyrics.split("(\n\n)|(\r\r)|(\r\n\r\n)")) {
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
            if (!foundTitle) {
                for (String line : sectionLines) {
                    if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.NORMAL) {
                        title = line;
                        foundTitle = true;
                        break;
                    }
                }
            }
            String[] smallLines = new String[]{
                title,
                author + ((ccli.equals("")) ? " " : (" (" + ccli + ")"))
            };
            sections.add(new TextSection(sectionTitle, newLyrics, smallLines, true));
        }
    }

    /**
     * Add a section to this song.
     * <p/>
     * @param section the section to add.
     */
    public void addSection(TextSection section) {
        if (section.getTheme() == null) {
            section.setTheme(theme);
        }
        sections.add(section);
    }

    /**
     * Add a section to this song at the specified index.
     * <p/>
     * @param index the index to add the song at.
     * @param section the section to add.
     */
    public void addSection(int index, TextSection section) {
        if (section.getTheme() == null) {
            section.setTheme(theme);
        }
        sections.add(index, section);
    }

    /**
     * Add a number of sections to this song.
     * <p/>
     * @param sections the sections to add.
     */
    public void addSections(TextSection[] sections) {
        for (TextSection section : sections) {
            addSection(section);
        }
    }

    /**
     * Replace the text section at the given index with the new section.
     * <p/>
     * @param newSection the new section to use to replace the existing one.
     * @param index the index of the section to replace.
     */
    public void replaceSection(TextSection newSection, int index) {
        sections.set(index, newSection);
    }

    /**
     * Remove the given text section.
     * <p/>
     * @param index the index of the text section to remove.
     */
    public void removeSection(int index) {
        sections.remove(index);
    }

    /**
     * Get an array of all the sections in this song.
     * <p/>
     * @return the song sections.
     */
    @Override
    public TextSection[] getSections() {
        return sections.toArray(new TextSection[sections.size()]);
    }

    /**
     * Set the last search text (for highlighting.)
     * <p/>
     * @param lastSearch
     */
    public void setLastSearch(String lastSearch) {
        this.lastSearch = lastSearch;
    }

    /**
     * Get the HTML that should be displayed in the library song list. This
     * depends on what was searched for last, it bolds the search term in the
     * title (if it appears as such.)
     * <p/>
     * @return the appropriate HTML to display the song in the list.
     */
    public String getListHTML() {//@todo wrong method name
        return getTitle();
//        if(lastSearch == null) {
//            return getTitle();
//        }
//        int startIndex = getTitle().toLowerCase().indexOf(lastSearch.toLowerCase());
//        if(startIndex == -1) {
//            return getTitle();
//        }
//        StringBuilder ret = new StringBuilder();
//        ret.append("<html>");
//        ret.append(getTitle().substring(0, startIndex));
//        ret.append("<b>");
//        ret.append(getTitle().substring(startIndex, startIndex + lastSearch.length()));
//        ret.append("</b>");
//        ret.append(getTitle().substring(startIndex + lastSearch.length()));
//        ret.append("</html>");
//        return ret.toString();
    }

    /**
     * Get a representation of this song in XML format.
     * <p/>
     * @return the song in XML format.
     */
    @Override
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<song>");
        xml.append("<updateInDB>");
        xml.append(updateInDB);
        xml.append("</updateInDB>");
        xml.append("<title>");
        xml.append(Utils.escapeXML(title));
        xml.append("</title>");
        xml.append("<author>");
        xml.append(Utils.escapeXML(author));
        xml.append("</author>");
        xml.append("<ccli>");
        xml.append(Utils.escapeXML(ccli));
        xml.append("</ccli>");
        xml.append("<copyright>");
        xml.append(Utils.escapeXML(copyright));
        xml.append("</copyright>");
        xml.append("<year>");
        xml.append(Utils.escapeXML(year));
        xml.append("</year>");
        xml.append("<publisher>");
        xml.append(Utils.escapeXML(publisher));
        xml.append("</publisher>");
        xml.append("<key>");
        xml.append(Utils.escapeXML(key));
        xml.append("</key>");
        xml.append("<capo>");
        xml.append(Utils.escapeXML(capo));
        xml.append("</capo>");
        xml.append("<notes>");
        xml.append(Utils.escapeXML(info));
        xml.append("</notes>");
        xml.append("<lyrics>");
        for (TextSection section : sections) {
            xml.append(section.getXML());
        }
        xml.append("</lyrics>");

        xml.append("<translation>");
        if (getCurrentTranslationLyrics() != null) {
            xml.append("<name>");
            xml.append(currentTranslation);
            xml.append("</name>");
            xml.append("<tlyrics>");
            xml.append(Utils.escapeXML(translations.get(currentTranslation)));
            xml.append("</tlyrics>");
        } else {
            xml.append("");
        }
        xml.append("</translation>");
        xml.append("</song>");
        return xml.toString();
    }

    /**
     * Get the XML used to print the song (will be transferred via XSLT.)
     *
     * @param includeTranslations true if translations should be included in the
     * export, false otherwise.
     * @return the XML used to print the song.
     */
    public String getPrintXML(boolean includeTranslations) {
        StringBuilder xml = new StringBuilder();
        Map<String, String> lyricsMap = new TreeMap<>((String o1, String o2) -> { //Ensure "Default" translation is first
            if (o1.equals("Default")) {
                return -1;
            }
            if (o2.equals("Default")) {
                return 1;
            }
            return o1.compareTo(o2);
        });
        StringBuilder mainLyrics = new StringBuilder();
        for (TextSection section : sections) {
            mainLyrics.append(section.getTitle()).append("\n");
            for (String line : section.getText(printChords, false)) {
                mainLyrics.append(Utils.escapeXML(line)).append("\n");
            }
            mainLyrics.append("\n");
        }
        lyricsMap.put("Default", mainLyrics.toString().trim());
        if (includeTranslations) {
            lyricsMap.putAll(getTranslations());
        }
        xml.append("<songs>");
        for (Entry<String, String> lyricsEntry : lyricsMap.entrySet()) {
            xml.append("<song>");
            xml.append("<title>");
            xml.append(Utils.escapeXML(title));
            if (!lyricsEntry.getKey().equals("Default")) {
                xml.append(" (").append(lyricsEntry.getKey()).append(")");
            }
            xml.append("</title>");
            xml.append("<author>");
            xml.append(Utils.escapeXML(author));
            xml.append("</author>");
            xml.append("<lyrics>");
            xml.append(lyricsEntry.getValue().replace(" ", Character.toString((char) 160)));
            xml.append("</lyrics>");
            xml.append("</song>");
        }
        xml.append("</songs>");
        return xml.toString();
    }

    /**
     * Parse a song in XML format and return the song object.
     * <p/>
     * @param xml the xml string to parse.
     * @return the song, or null if an error occurs.
     */
    public static SongDisplayable parseXML(String xml) {
        try {
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF8"));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return parseXML(doc.getFirstChild());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return null;
        }
    }

    /**
     * Parse a song in XML format and return the song object.
     * <p/>
     * @param inputStream the input stream containing the xml.
     * @return the song, or null if an error occurs.
     */
    public static SongDisplayable parseXML(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return parseXML(doc.getChildNodes().item(0));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.INFO, "Couldn't parse the schedule", ex);
            return null;
        }
    }

    /**
     * Parse a song in XML format and return the song object.
     * <p/>
     * @param song the song node to parse.
     * @return the song, or null if an error occurs.
     */
    public static SongDisplayable parseXML(Node song) {
        NodeList list = song.getChildNodes();
        String title = "";
        String author = "";
        String ccli = "";
        String copyright = "";
        String year = "";
        String publisher = "";
        String key = "";
        String capo = "";
        String notes = "";
        String currentTranslation = "";
        String translationLyrics = "";
        boolean updateInDB = true;
        List<TextSection> songSections = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equals("updateInDB")) {
                if(node.getTextContent().equals("false")) {
                    updateInDB = false;
                }
            }
            if (node.getNodeName().equals("title")) {
                title = node.getTextContent();
            }
            if (node.getNodeName().equals("ccli")) {
                ccli = node.getTextContent();
            }
            if (node.getNodeName().equals("copyright")) {
                copyright = node.getTextContent();
            }
            if (node.getNodeName().equals("year")) {
                year = node.getTextContent();
            }
            if (node.getNodeName().equals("publisher")) {
                publisher = node.getTextContent();
            }
            if (node.getNodeName().equals("key")) {
                key = node.getTextContent();
            }
            if (node.getNodeName().equals("capo")) {
                capo = node.getTextContent();
            }
            if (node.getNodeName().equals("notes")) {
                notes = node.getTextContent();
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
            if (node.getNodeName().equals("translation")) {
                if (node.hasChildNodes()) {
                    NodeList nl = node.getChildNodes();
                    currentTranslation = nl.item(0).getTextContent();
                    translationLyrics = nl.item(1).getTextContent();
                }
            }
        }
        SongDisplayable ret = new SongDisplayable(title, author,
                new ThemeDTO(ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                        ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, -1, 0));
        if(!updateInDB) {
            ret.setNoDBUpdate();
        }
        for (TextSection section : songSections) {
            ret.addSection(section);
        }
        ret.setCcli(ccli);
        ret.setCopyright(copyright);
        ret.setYear(year);
        ret.setPublisher(publisher);
        ret.setKey(key);
        ret.setCapo(capo);
        ret.setInfo(notes);
        if (!currentTranslation.equals("")) {
            ret.setCurrentTranslationLyrics(currentTranslation);
            ret.addTranslation(currentTranslation, translationLyrics);
        }
        return ret;
    }

    /**
     * Generate a hashcode for this song.
     * <p/>
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 29 * hash + (this.author != null ? this.author.hashCode() : 0);
        hash = 29 * hash + (this.sections != null ? this.sections.hashCode() : 0);
        hash = 29 * hash + (this.theme != null ? this.theme.hashCode() : 0);
        hash = hash + count;
        return hash;
    }

    /**
     * Determine whether this song equals another object.
     * <p/>
     * @param obj the other object.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SongDisplayable)) {
            return false;
        }
        final SongDisplayable other = (SongDisplayable) obj;
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
     * <p/>
     * @param other the other song.
     * @return 1 if this song is greater than the other song, 0 if they're the
     * same, and -1 if this is less than the other song.
     */
    @Override
    public int compareTo(SongDisplayable other) {
        Collator collator = Collator.getInstance();
        int result = collator.compare(getTitle(), other.getTitle());
        if (result == 0) {
            if (getAuthor() != null && other.getAuthor() != null) {
                result = collator.compare(getAuthor(), other.getAuthor());
            }
            if (result == 0 && getLyrics(false, false) != null && other.getLyrics(false, false) != null) {
                result = collator.compare(getLyrics(false, false), other.getLyrics(false, false));
            }
        }
        return result;
    }

    /**
     * Get a string representation of this song.
     * <p/>
     * @return a string representation of the song.
     */
    @Override
    public String toString() {
        return getXML();
    }

    /**
     * Get the preview icon of this song.
     * <p/>
     * @return the song's preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        if(getID()<0) {
            return new ImageView(new Image("file:icons/lyricscopy.png"));
        }
        else if (hasChords()) {
            return new ImageView(new Image("file:icons/lyricsandchords.png"));
        } else {
            return new ImageView(new Image("file:icons/lyrics.png"));
        }
    }

    /**
     * Get the preview text of this song.
     * <p/>
     * @return the song's preview text.
     */
    @Override
    public String getPreviewText() {
        String ret = getTitle();
        if (getCurrentTranslationName() != null) {
            ret += " (+ " + getCurrentTranslationName() + ")";
        }
        ret += "\n" + getAuthor();
        return ret;
    }

    /**
     * Remove any duplicate sections in this song.
     */
    public void removeDuplicateSections() {
        Utils.removeDuplicateWithOrder(sections);
    }

    /**
     * Get all the files used by this song.
     * <p/>
     * @return all the files used by this song.
     */
    @Override
    public Collection<File> getResources() {
        Set<File> ret = new HashSet<>();
        for (TextSection section : getSections()) {
            ThemeDTO sectionTheme = section.getTheme();
            if (sectionTheme != null) {
                Background background = sectionTheme.getBackground();
                ret.addAll(background.getResources());
            }
        }
        return ret;
    }

    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
    }

    @Override
    public void dispose() {
        //Nothing needed here.
    }
}
