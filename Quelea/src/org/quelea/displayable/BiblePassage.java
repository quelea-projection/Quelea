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
package org.quelea.displayable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.bible.BibleVerse;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * A displayable passage from the bible.
 * @author Michael
 */
public class BiblePassage implements TextDisplayable {

    private String summary;
    private String[] smallText;
    private List<TextSection> textSections;
    private BibleVerse[] verses;

    /**
     * Create a new bible passage.
     * @param biblename the bible that the passage comes from.
     * @param location the location of the passage in the bible.
     * @param verses the verses, in order, that make up the passage.
     */
    public BiblePassage(String biblename, String location, BibleVerse[] verses) {
        this(location + "\n" + biblename, verses);
    }

    /**
     * Create a new bible passage from a summary and an array of verses.
     * @param summary the summary to display in the schedule.
     * @param verses  the verses in the passage.
     */
    private BiblePassage(String summary, BibleVerse[] verses) {
        this.summary = summary;
        this.smallText = summary.split("<br/>");
        for (int i = 0; i < smallText.length; i++) {
            smallText[i] = Utils.removeTags(smallText[i]);
        }
        this.verses = Arrays.copyOf(verses, verses.length);
        textSections = new ArrayList<>();
        fillTextSections();
    }

    /**
     * Fill the text sections with the verses.
     */
    private void fillTextSections() {
        final int LINES_PER_SLIDE = 8;
        List<String> words = new ArrayList<>();
        for (int i = 0; i < verses.length; i++) {
            words.addAll(Arrays.asList(verses[i].getText().split(" ")));
        }

        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            line.append(words.get(i)).append(" ");
            int length = line.length();
            if (i < words.size() - 1) {
                length += words.get(i + 1).length();
            }
            if ((i != 0 && length >= QueleaProperties.get().getMaxChars()) || i == words.size() - 1) {
                lines.add(line.toString());
                line.setLength(0);
            }
        }
        List<String> sections = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            sections.add(lines.get(i));
            if ((i != 0 && i % LINES_PER_SLIDE == 0) || i == lines.size() - 1) {
                textSections.add(new TextSection("", sections.toArray(new String[sections.size()]), smallText, false));
                sections.clear();
            }
        }
    }

    /**
     * Get the XML behind this bible passage.
     * @return the XML.
     */
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<passage summary=\"");
        ret.append(Utils.escapeXML(summary));
        ret.append("\">");
        for (BibleVerse verse : verses) {
            ret.append(verse.toXML());
        }
        ret.append("</passage>");
        return ret.toString();
    }

    /**
     * Parse the xml from a bible passage and return the passage.
     * @param passage the passage to parse.
     * @return the passage object.
     */
    public static BiblePassage parseXML(Node passage) {
        NodeList list = passage.getChildNodes();
        String summary = passage.getAttributes().getNamedItem("summary").getNodeValue();
        List<BibleVerse> verses = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equals("vers")) {
                verses.add(BibleVerse.parseXML(node));
            }
        }
        return new BiblePassage(summary, verses.toArray(new BibleVerse[verses.size()]));
    }

    /**
     * Get the bible preview icon.
     * @return the bible preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/bible.png"));
    }

    /**
     * Get the preview text.
     * @return the preview text.
     */
    @Override
    public String getPreviewText() {
        return summary;
    }

    /**
     * Get the text sections in this passage.
     * @return the text sections in this passage.
     */
    public TextSection[] getSections() {
        return textSections.toArray(new TextSection[textSections.size()]);
    }

    /**
     * Bible passages don't need any resources, return an empty collection.
     * @return an empty list, always.
     */
    public Collection<File> getResources() {
        return new ArrayList<>();
    }

    /**
     * Return a summary to display when printed as part of the order of service.
     * @return the summary as a string.
     */
    @Override
    public String getPrintText() {
        return "Bible passage: " + summary;
    }

    /**
     * We support clear, so return true.
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
