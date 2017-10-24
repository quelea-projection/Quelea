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
import org.javafx.dialog.Dialog;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
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
    private final boolean multi;

    /**
     * Create a new bible passage.
     * <p>
     * @param biblename the bible that the passage comes from.
     * @param location the location of the passage in the bible.
     * @param verses the verses, in order, that make up the passage.
     */
    public BiblePassage(String biblename, String location, BibleVerse[] verses, boolean multi) {
        this(location + "\n" + biblename, verses, new ThemeDTO(ThemeDTO.DEFAULT_FONT,
                ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, 3, -1), multi);
    }

    /**
     * Create a new bible passage from a summary and an array of verses.
     * <p>
     * @param summary the summary to display in the schedule.
     * @param verses the verses in the passage.
     * @param theme the theme of the passage.
     */
    public BiblePassage(String summary, BibleVerse[] verses, ThemeDTO theme, boolean multi) {
        this.summary = summary;
        this.multi = multi;
        this.smallText = summary.split("\n");
        for (int i = 0; i < smallText.length; i++) {
            smallText[i] = Utils.removeTags(smallText[i]);
        }
        this.verses = Arrays.copyOf(verses, verses.length);
        this.theme = theme;
        textSections = new ArrayList<>();
        fillTextSections();
        for (TextSection ts : getSections()) {
            ts.setTheme(theme);
        }
    }

    /**
     * Fill the text sections with the verses.
     */
    private void fillTextSections() {
        final int MAX_CHARS = QueleaProperties.get().getMaxChars();
        final int MAX_VERSES = QueleaProperties.get().getMaxBibleVerses();
        final boolean SPLIT_VERSES = QueleaProperties.get().getBibleSplitVerses();
        final boolean USE_CHARS = QueleaProperties.get().getBibleUsingMaxChars();

        StringBuilder section = new StringBuilder();
        StringBuilder line = new StringBuilder();
        int lines = 0;
        int count = 0;
        boolean verseError = false;
        for (BibleVerse verse : verses) {
            if (verse == null) {
                verseError = true;
            } else {
                if (QueleaProperties.get().getShowVerseNumbers()) {
                    line.append("<sup>");
                    if (multi) {
                        line.append(verse.getChapterNum()).append(":");
                    }
                    line.append(verse.getNum());
                    line.append("</sup>");
                }
                String verseText = verse.getText();
                String[] verseWords = verseText.split(" ");
                for (String verseWord : verseWords) {
                    if (line.toString().replaceAll("\\<sup\\>[0-9]+\\<\\/sup\\>", "").length() + verseWord.length() > MAX_CHARS) {
                        line.append("\n");
                        section.append(line);
                        lines++;
                        line.setLength(0);
                    }
                    line.append(verseWord).append(" ");
                }
//            section.append(line);
//            line.setLength(0); //Empty
                count++;
                if (USE_CHARS) {
                    if (!SPLIT_VERSES) {
                        if (lines >= MAX_CHARS / 4) {
                            lines = 0;
                            textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
                            section.setLength(0);
                        }
                    }
                    if (SPLIT_VERSES) {
                        if (!line.toString().trim().isEmpty()) {
                            lines++;
                        }
                        if (lines >= MAX_CHARS / 4) {
                            if (!line.toString().isEmpty()) {
                                line.append("\n");
                                section.append(line);
                                line.setLength(0);
                                lines++;
                            }
                            lines = 0;
                            textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
                            section.setLength(0);
                        }
                    }

                } else { // using verses
                    if (count >= MAX_VERSES && count > 0) {
                        if (!line.toString().isEmpty()) {
                            line.append("\n");
                            section.append(line);
                            line.setLength(0);
                        }
                        textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
                        section = new StringBuilder();
                        count = 0;
                    }
                }
            }
        }

        // Clean up anything left by the for loop (possible extra verses)
        if (!line.toString().isEmpty()) {
            line.append("\n");
            section.append(line);
        }

        if (!section.toString().isEmpty()) {
            textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
        }

        if (verseError) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("no.verse.title"), LabelGrabber.INSTANCE.getLabel("no.verse.message"), QueleaApp.get().getMainWindow());
        }
    }

    /**
     * Get (a copy of) the verses shown in this passage.
     *
     * @return A copy of the verses shown in this passage.
     */
    public BibleVerse[] getVerses() {
        return Arrays.copyOf(verses, verses.length);
    }

    /**
     * Get the XML behind this bible passage.
     * <p/>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<passage summary=\"");
        String summaryText = "";
        String bible = "";
        //Handle old schedules...
        if (summary.split("\n").length == 1) {
            summaryText = summary.split("(?<=\\d)\\s")[0];
            bible = summary.split("(?<=\\d)\\s")[1];
        } else {
            summaryText = summary.split("\n")[0];
            bible = summary.split("\n")[1];
        }
        ret.append(Utils.escapeXML(summaryText));
        ret.append("\" multi=\"").append(multi);
        ret.append("\" bible=\"");
        ret.append(Utils.escapeXML(bible));
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
    @Override
    public ThemeDTO getTheme() {
        return this.theme;
    }

    /**
     * Set assigned theme
     * <p/>
     * @param theme new theme
     */
    @Override
    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
        for (TextSection ts : getSections()) {
            ts.setTheme(theme);
        }
    }

    /**
     * Parse the xml from a bible passage and return the passage.
     * <p>
     * @param passage the passage to parse.
     * @return the passage object.
     */
    public static BiblePassage parseXML(Node passage) {
        NodeList list = passage.getChildNodes();
        String passageSummary = passage.getAttributes().getNamedItem("summary").getTextContent();
        String bibleSummary = "";
        if (passage.getAttributes().getNamedItem("bible") != null) {
            bibleSummary = passage.getAttributes().getNamedItem("bible").getTextContent();
        }
        boolean multi = false;
        if (passage.getAttributes().getNamedItem("multi") != null) {
            multi = Boolean.parseBoolean(passage.getAttributes().getNamedItem("multi").getTextContent());
        }
        String summary = passageSummary + "\n" + bibleSummary;
        ThemeDTO tempTheme = ThemeDTO.DEFAULT_THEME;
        List<BibleVerse> verses = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equals("vers")) {
                BibleVerse bv = BibleVerse.parseXML(node);
                if (bv != null) {
                    for (Bible b : BibleManager.get().getBibles()) {
                        if (b.getBibleName().equals(bibleSummary)) {
                            int ii = 1;
                            for (BibleBook bb : b.getBooks()) {
                                String p = passageSummary.split(" \\d")[0];
                                if (bb.getBookName().equals(p)) {
                                    bv.getChapter().setBook(BibleBook.parseXML(node, ii));
                                    bv.getChapter().getBook().setBible(b);
                                    verses.add(bv);
                                }
                                ii++;
                            }
                        }
                    }
                }
            } else if (node.getNodeName().equals("theme")) {
                tempTheme = ThemeDTO.fromString(node.getTextContent());
            }
        }
        return new BiblePassage(summary, verses.toArray(new BibleVerse[verses.size()]), tempTheme, multi);
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

    public void updateBibleLines() {
        textSections.clear();
        fillTextSections();
        for (TextSection ts : getSections()) {
            ts.setTheme(theme);
        }
    }

    public boolean getMulti() {
        return multi;
    }

    public String getLocation() {
        return summary.split("\n")[0];
    }
}
