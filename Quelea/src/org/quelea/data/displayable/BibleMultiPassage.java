///* 
// * This file is part of Quelea, free projection software for churches.
// * 
// * 
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package org.quelea.data.displayable;
//
//import java.io.File;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import org.quelea.data.Background;
//import org.quelea.data.ThemeDTO;
//import org.quelea.data.bible.BibleVerse;
//import org.quelea.services.utils.QueleaProperties;
//import org.quelea.services.utils.Utils;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
///**
// * A displayable passage from the bible.
// * <p>
// * @author Ben
// */
//public class BibleMultiPassage implements TextDisplayable, Serializable {
//
//    private String summary;
//    private String[] smallText;
//    private BibleVerse[] verses;
//    private ThemeDTO theme;
//    private List<BiblePassage> passages;
//    private ArrayList<TextSection> textSections;
//    private final String translation;
//    private final String locations;
//
//    public BibleMultiPassage(String translation, String locations, List<BiblePassage> passages) {
//        this(translation, locations, passages, new ThemeDTO(ThemeDTO.DEFAULT_FONT,
//                ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
//                ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, 3, -1));
//    }
//
//    /**
//     * Create a new multi bible passage.
//     * <p/>
//     * @param translation the bible translation
//     * @param locations The book and verse locations
//     * @param passages the list of bible passages.
//     * @param theme the theme of the passages
//     */
//    public BibleMultiPassage(String translation, String locations, List<BiblePassage> passages, ThemeDTO theme) {
//        this.passages = passages;
//        this.translation = translation;
//        this.locations = locations;
//        this.smallText = generateSmallText();
//        this.summary = generateSummary();
//        this.verses = generateVerses();
//        this.theme = theme;
//        textSections = new ArrayList<>();
//        fillTextSections();
//        for (TextSection ts : getSections()) {
//            ts.setTheme(theme);
//        }
//    }
//
//    /**
//     * Fill the text sections with the verses.
//     */
//    private void fillTextSections() {
//        final int MAX_CHARS = QueleaProperties.get().getMaxChars();
//        final boolean SPLIT_VERSES = QueleaProperties.get().getBibleSectionVerses();
//
//        StringBuilder section = new StringBuilder();
//        StringBuilder line = new StringBuilder();
//        int lines = 1;
//        for (BibleVerse verse : verses) {
//            if (QueleaProperties.get().getShowVerseNumbers()) {
//                line.append("<sup>");
//                line.append(verse.getNum());
//                line.append("</sup>");
//            }
//            String verseText = verse.getText();
//            String[] verseWords = verseText.split(" ");
//            for (String verseWord : verseWords) {
//                line.append(verseWord).append(" ");
//                if (line.toString().replaceAll("\\<sup\\>[0-9]+\\<\\/sup\\>", "").length() > MAX_CHARS) {
//                    line.append("\n");
//                    section.append(line);
//                    line.setLength(0); //Empty
//                    lines++;
//                    if (!SPLIT_VERSES) {
//                        if (lines >= MAX_CHARS / 4) {
//                            lines = 0;
//                            textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
//                            section.setLength(0);
//                        }
//                    }
//                }
//            }
//            if (SPLIT_VERSES) {
//                if (!line.toString().trim().isEmpty()) {
//                    lines++;
//                }
//                if (lines >= MAX_CHARS / 4) {
//                    if (!line.toString().isEmpty()) {
//                        line.append("\n");
//                        section.append(line);
//                        line.setLength(0);
//                        lines++;
//                    }
//                    lines = 0;
//                    textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
//                    section.setLength(0);
//                }
//            }
//        }
//        if (!line.toString().isEmpty()) {
//            line.append("\n");
//            section.append(line);
//        }
//        if (!section.toString().isEmpty()) {
//            textSections.add(new TextSection("", new String[]{section.toString().trim()}, smallText, false));
//        }
//    }
//
//    /**
//     * Get (a copy of) the verses shown in this passage.
//     *
//     * @return A copy of the verses shown in this passage.
//     */
//    public BibleVerse[] getVerses() {
//        return Arrays.copyOf(verses, verses.length);
//    }
//
////    /**
////     * Get the XML behind this bible passage.
////     * <p/>
////     * @return the XML.
////     */
////    @Override
////    public String getXML() {
////        StringBuilder ret = new StringBuilder();
////        ret.append("<passage summary=\"");
////        String summaryText = "";
////        String bible = "";
////        //Handle old schedules...
////        if (summary.split("\n").length == 1) {
////            summaryText = summary.split("(?<=\\d)\\s")[0];
////            bible = summary.split("(?<=\\d)\\s")[1];
////        } else {
////            summaryText = summary.split("\n")[0];
////            bible = summary.split("\n")[1];
////        }
////        ret.append(Utils.escapeXML(summaryText));
////        ret.append("\" bible=\"");
////        ret.append(Utils.escapeXML(bible));
////        ret.append("\">");
////        for (BibleVerse verse : verses) {
////            ret.append(verse.toXML());
////        }
////        ret.append("<theme>");
////        ret.append(theme.asString());
////        ret.append("</theme>");
////        ret.append("</passage>");
////        return ret.toString();
////    }
//    /**
//     * Return the first verse in this passage as a "preview".
//     * <p>
//     * @return the first verse in this passage as a "preview".
//     */
//    @Override
//    public String toString() {
//        if (textSections.isEmpty()) {
//            return "";
//        } else {
//            return textSections.get(0).getText(false, false)[0];
//        }
//    }
//
//    /**
//     * Retrieve assigned theme
//     * <p/>
//     * @return assigned theme
//     */
//    @Override
//    public ThemeDTO getTheme() {
//        return this.theme;
//    }
//
//    /**
//     * Set assigned theme
//     * <p/>
//     * @param theme new theme
//     */
//    @Override
//    public void setTheme(ThemeDTO theme) {
//        this.theme = theme;
//        for (TextSection ts : getSections()) {
//            ts.setTheme(theme);
//        }
//    }
//
////    /**
////     * Parse the xml from a bible passage and return the passage.
////     * <p>
////     * @param passage the passage to parse.
////     * @return the passage object.
////     */
////    public static BibleMultiPassage parseXML(Node passage) {
////        NodeList list = passage.getChildNodes();
////        String passageSummary = passage.getAttributes().getNamedItem("summary").getTextContent();
////        String bibleSummary = "";
////        if (passage.getAttributes().getNamedItem("bible") != null) {
////            bibleSummary = passage.getAttributes().getNamedItem("bible").getTextContent();
////        }
////        String summary = passageSummary + "\n" + bibleSummary;
////        ThemeDTO tempTheme = ThemeDTO.DEFAULT_THEME;
////        List<BibleVerse> verses = new ArrayList<>();
////        for (int i = 0; i < list.getLength(); i++) {
////            Node node = list.item(i);
////            if (node.getNodeName().equals("vers")) {
////                verses.add(BibleVerse.parseXML(node));
////            } else if (node.getNodeName().equals("theme")) {
////                tempTheme = ThemeDTO.fromString(node.getTextContent());
////            }
////        }
////        return new BibleMultiPassage(summary, verses.toArray(new BibleVerse[verses.size()]), tempTheme);
////    }
//    /**
//     * Get the bible preview icon.
//     * <p>
//     * @return the bible preview icon.
//     */
//    @Override
//    public ImageView getPreviewIcon() {
//        return new ImageView(new Image("file:icons/bible.png"));
//    }
//
//    /**
//     * Get the preview text.
//     * <p>
//     * @return the preview text.
//     */
//    @Override
//    public String getPreviewText() {
//        return summary;
//    }
//
//    /**
//     * Get the text sections in this passage.
//     * <p>
//     * @return the text sections in this passage.
//     */
//    @Override
//    public TextSection[] getSections() {
//        return textSections.toArray(new TextSection[textSections.size()]);
//    }
//
//    /**
//     * Bible passages don't need any resources, return an empty collection.
//     * <p>
//     * @return an empty list, always.
//     */
//    @Override
//    public Collection<File> getResources() {
//        ArrayList<File> ret = new ArrayList<>();
//        for (TextSection section : getSections()) {
//            ThemeDTO sectionTheme = section.getTheme();
//            if (sectionTheme != null) {
//                Background background = sectionTheme.getBackground();
//                ret.addAll(background.getResources());
//            }
//        }
//        return ret;
//    }
////
//
//    /**
//     * We support clear, so return true.
//     * <p>
//     * @return true, always.
//     */
//    @Override
//    public boolean supportClear() {
//        return true;
//    }
////
//
//    @Override
//    public void dispose() {
//        //Nothing needed here.
//    }
////
////    public void updateBibleLines() {
////        textSections.clear();
////        fillTextSections();
////        for (TextSection ts : getSections()) {
////            ts.setTheme(theme);
////        }
////    }
//
//    @Override
//    public String getXML() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    private String[] generateSmallText() {
//        String[] text = new String[2];
//        text[0] = translation;
//        text[1] = locations;
//        return text;
//    }
//
//    private String generateSummary() {
//        String text = translation + " " + locations;
//        return text;
//    }
//
//    private BibleVerse[] generateVerses() {
//        ArrayList<BibleVerse> v = new ArrayList<>();
//        for (BiblePassage bp : passages) {
//            for (BibleVerse bv : bp.getVerses()) {
//                v.add(bv);
//            }
//        }
//        return (BibleVerse[]) v.toArray();
//    }
//}
