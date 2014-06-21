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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.BibleVerse;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A displayable passage from the bible.
 * <p>
 * @author Michael
 */
public class BiblePassage implements TextDisplayable, Serializable {

    private String summary;
    private String[] smallText;
    private List<TextSection> textSections;
    private BibleVerse[] verses;
    private ThemeDTO theme;

    /**
     * Create a new bible passage.
     * <p>
     * @param biblename the bible that the passage comes from.
     * @param location the location of the passage in the bible.
     * @param verses the verses, in order, that make up the passage.
     */
    public BiblePassage(String biblename, String location, BibleVerse[] verses) {
        this(location + "\n" + biblename, verses, new ThemeDTO(ThemeDTO.DEFAULT_FONT,
                ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, 3, -1));
    }

    /**
     * Create a new bible passage from a summary and an array of verses.
     * <p>
     * @param summary the summary to display in the schedule.
     * @param verses the verses in the passage.
     */
    private BiblePassage(String summary, BibleVerse[] verses) {
        this(summary, verses, new ThemeDTO(ThemeDTO.DEFAULT_FONT,
                ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, 3, -1));
    }

    /**
     * Create a new bible passage from a summary and an array of verses.
     * <p>
     * @param summary the summary to display in the schedule.
     * @param verses the verses in the passage.
     * @param theme the theme of the passage.
     */
    private BiblePassage(String summary, BibleVerse[] verses, ThemeDTO theme) {
        this.summary = summary;
        this.smallText = summary.split("\n");
        for (int i = 0; i < smallText.length; i++) {
            smallText[i] = Utils.removeTags(smallText[i]);
        }
        this.verses = Arrays.copyOf(verses, verses.length);
        this.theme = theme;
        textSections = new ArrayList<>();
        fillTextSections();
    }

    /**
     * Fill the text sections with the verses.
     */
    private void fillTextSections() {
        final int MAX_ITEMS_PER_SLIDE = QueleaProperties.get().getMaxBibleItems();
        final boolean ITEM_VERSES = QueleaProperties.get().getBibleSectionVerses();

        StringBuilder section = new StringBuilder();
        int count = 0;
        for (BibleVerse verse : verses) {
            if (ITEM_VERSES) {
                count++;
            } else {
                count += verse.getVerseText().split(" ").length + 1;
            }

            if (count < MAX_ITEMS_PER_SLIDE) {
                if (QueleaProperties.get().getShowVerseNumbers()) {
                    section.append("<sup>");
                    section.append(verse.getNum());
                    section.append("</sup>");
                }
                section.append(verse.getVerseText());
            } else {
                TextSection ts = new TextSection("", new String[]{section.toString()}, smallText, false);
                if (ts.getTheme() == null) {
                    ts.setTheme(theme);
                }
                textSections.add(ts);
                section = new StringBuilder();
                if (QueleaProperties.get().getShowVerseNumbers()) {
                    section.append("<sup>");
                    section.append(verse.getNum());
                    section.append("</sup>");
                }
                section.append(verse.getVerseText());
                count = 0;
            }
        }
        if (!section.toString().isEmpty()) {
            textSections.add(new TextSection("", new String[]{section.toString()}, smallText, false));
        }
    }

    /**
     * Get the XML behind this bible passage.
     * <p>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<passage summary=\"");
        ret.append(Utils.escapeXML(summary));
        ret.append("\">");
        for (BibleVerse verse : verses) {
            ret.append(verse.toXML());
        }
        ret.append("<theme>");
        ret.append(theme.asString());
        ret.append("</theme>");
        ret.append("</passage>");
        return ret.toString();
    }

    /**
     * Return the first verse in this passage as a "preview".
     * <p>
     * @return the first verse in this passage as a "preview".
     */
    @Override
    public String toString() {
        if (textSections.isEmpty()) {
            return "";
        } else {
            return textSections.get(0).getText(false, false)[0];
        }
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
     * Set assigned theme
     * <p/>
     * @param theme new theme
     */
    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
    }

    /**
     * Parse the xml from a bible passage and return the passage.
     * <p>
     * @param passage the passage to parse.
     * @return the passage object.
     */
    public static BiblePassage parseXML(Node passage) {
        NodeList list = passage.getChildNodes();
        String summary = passage.getAttributes().getNamedItem("summary").getNodeValue();
        ThemeDTO tempTheme = ThemeDTO.DEFAULT_THEME;
        List<BibleVerse> verses = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equals("vers")) {
                verses.add(BibleVerse.parseXML(node));
            }
            else if(node.getNodeName().equals("theme")) {
                tempTheme = ThemeDTO.fromString(node.getTextContent());
            }
        }
        return new BiblePassage(summary, verses.toArray(new BibleVerse[verses.size()]), tempTheme);
    }

    /**
     * Get the bible preview icon.
     * <p>
     * @return the bible preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/bible.png"));
    }

    /**
     * Get the preview text.
     * <p>
     * @return the preview text.
     */
    @Override
    public String getPreviewText() {
        return summary;
    }

    /**
     * Get the text sections in this passage.
     * <p>
     * @return the text sections in this passage.
     */
    @Override
    public TextSection[] getSections() {
        return textSections.toArray(new TextSection[textSections.size()]);
    }

    /**
     * Bible passages don't need any resources, return an empty collection.
     * <p>
     * @return an empty list, always.
     */
    @Override
    public Collection<File> getResources() {
        ArrayList<File> ret = new ArrayList<>();
        for (TextSection section : getSections()) {
            ThemeDTO sectionTheme = section.getTheme();
            if (sectionTheme != null) {
                Background background = sectionTheme.getBackground();
                ret.addAll(background.getResources());
            }
        }
        return ret;
    }

    /**
     * Return a summary to display when printed as part of the order of service.
     * <p>
     * @return the summary as a string.
     */
    @Override
    public String getPrintText() {
        return "Bible passage: " + summary;
    }

    /**
     * We support clear, so return true.
     * <p>
     * @return true, always.
     */
    @Override
    public boolean supportClear() {
        return true;
    }

    @Override
    public void dispose() {
        //Nothing needed here.
    }
}
