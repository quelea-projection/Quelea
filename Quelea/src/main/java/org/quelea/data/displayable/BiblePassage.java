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

import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.javafx.dialog.Dialog;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleBookNameUtil;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.bible.BibleVersionText;
import org.quelea.data.bible.versification.Versification;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.quelea.windows.style.QueleaIcon;
/**
 * A displayable passage from the bible.
 * <p>
 * @author Michael
 */
public class BiblePassage implements TextDisplayable, Serializable {

    private static final String VERSION_SEPARATOR = " + ";

    private String summary;
    private String[] smallText;
    private List<TextSection> textSections;
    private BibleVerse[] verses;
    private ThemeDTO theme;
    private final boolean multi;
    private Map<Dimension,Double> fontSizeCache;
    private final List<BibleVersionText> versions;
    private final List<String> missingVerses;

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
        this(summary, verses, theme, multi, Collections.emptyList(), Collections.emptyList());
    }

    private BiblePassage(String summary, BibleVerse[] verses, ThemeDTO theme, boolean multi,
                         List<BibleVersionText> versions, List<String> missingVerses) {
        this.versions = new ArrayList<>(versions);
        this.missingVerses = new ArrayList<>(missingVerses);
        fontSizeCache = new HashMap<>();
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
    
    @Override
    public Double getCachedUniformFontSize(Dimension dimension) {
        return fontSizeCache.get(dimension);
    }

    @Override
    public void setCachedUniformFontSize(Dimension dimension, double size) {
        fontSizeCache.put(dimension, size);
    }

    /**
     * Fill the text sections with the verses.
     */
    private void fillTextSections() {
        final int MAX_CHARS = QueleaProperties.get().getMaxChars();
        final int MAX_VERSES = QueleaProperties.get().getMaxBibleVerses();
        final boolean SPLIT_VERSES = QueleaProperties.get().getBibleSplitVerses();
        final boolean USE_CHARS = !QueleaProperties.get().getBibleUsingMaxChars();

        if (isParallel()) {
            fillParallelTextSections(MAX_CHARS, MAX_VERSES, SPLIT_VERSES, USE_CHARS);
            return;
        }

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
                        section.append(line);
                        lines++;
                        line.setLength(0);
                    }
                    line.append(verseWord).append(" ");
                }
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
//            line.append("\n");
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
     * Fill the text sections of a passage shown in several versions. Each
     * section holds one line per version, so the versions can be drawn apart.
     * The slide limits are shared between the versions, which keeps a slide
     * about as full as it would be with a single version.
     *
     * @param maxChars   the maximum characters in a line.
     * @param maxVerses  the maximum verses on a slide.
     * @param keepWhole  true if a verse may not be split across slides.
     * @param useChars   true to limit slides by size rather than by verses.
     */
    private void fillParallelTextSections(int maxChars, int maxVerses, boolean keepWhole, boolean useChars) {
        final int charsPerSlide = useChars ? Math.max(maxChars, (maxChars / 4) * maxChars) : Integer.MAX_VALUE;
        final int versesPerSlide = useChars ? Integer.MAX_VALUE : Math.max(1, maxVerses / versions.size());
        String[] slide = new String[versions.size()];
        Arrays.fill(slide, "");
        int slideVerses = 0;
        int slideChars = 0;
        for (int i = 0; i < verses.length; i++) {
            String[] verseTexts = new String[versions.size()];
            int verseChars = 0;
            for (int v = 0; v < verseTexts.length; v++) {
                String text = versions.get(v).getVerseText(i);
                verseTexts[v] = text == null || text.isEmpty() ? "" : getVerseLabel(verses[i], versions.get(v).getVerseRef(i)) + text;
                verseChars += verseTexts[v].length();
            }
            boolean full = slideVerses >= versesPerSlide || slideChars + verseChars > charsPerSlide;
            if (slideVerses > 0 && full) {
                addParallelSection(slide);
                slide = new String[versions.size()];
                Arrays.fill(slide, "");
                slideVerses = 0;
                slideChars = 0;
            }
            if (!keepWhole && verseChars > charsPerSlide) {
                int parts = (int) Math.ceil(verseChars / (double) charsPerSlide);
                String[][] split = new String[verseTexts.length][];
                for (int v = 0; v < verseTexts.length; v++) {
                    split[v] = splitWords(verseTexts[v], parts);
                }
                for (int part = 0; part < parts; part++) {
                    String[] partSlide = new String[verseTexts.length];
                    for (int v = 0; v < verseTexts.length; v++) {
                        partSlide[v] = split[v][part];
                    }
                    addParallelSection(partSlide);
                }
                continue;
            }
            for (int v = 0; v < slide.length; v++) {
                slide[v] = (slide[v] + " " + verseTexts[v]).trim();
            }
            slideVerses++;
            slideChars += verseChars;
        }
        if (slideVerses > 0) {
            addParallelSection(slide);
        }
    }

    private void addParallelSection(String[] slide) {
        List<String> lines = new ArrayList<>();
        for (String text : slide) {
            if (!text.isEmpty()) {
                lines.add(text);
            }
        }
        if (!lines.isEmpty()) {
            textSections.add(new TextSection("", lines.toArray(new String[0]), smallText, false));
        }
    }

    private String getVerseLabel(BibleVerse verse) {
        return getVerseLabel(verse, null);
    }

    /**
     * Get the verse number shown before a verse.
     *
     * @param verse the verse of the passage.
     * @param ref   the version's own numbers for the verse, such as "31:2",
     *              or null if they are the passage's.
     * @return the label, or an empty string if verse numbers aren't shown.
     */
    private String getVerseLabel(BibleVerse verse, String ref) {
        if (!QueleaProperties.get().getShowVerseNumbers()) {
            return "";
        }
        if (ref == null) {
            return "<sup>" + (multi ? verse.getChapterNum() + ":" : "") + verse.getNum() + "</sup>";
        }
        boolean sameChapter = ref.startsWith(verse.getChapterNum() + ":") && !ref.contains(";")
                && !ref.substring(ref.indexOf(':') + 1).contains(":");
        return "<sup>" + (multi || !sameChapter ? ref : ref.substring(ref.indexOf(':') + 1)) + "</sup>";
    }

    /**
     * Split a text at word boundaries into a number of roughly equal parts.
     *
     * @param text  the text to split.
     * @param parts the number of parts.
     * @return exactly that many parts, the last ones empty if the text runs out.
     */
    private static String[] splitWords(String text, int parts) {
        String[] words = text.split(" ");
        int wordsPerPart = (int) Math.ceil(words.length / (double) parts);
        String[] ret = new String[parts];
        for (int part = 0; part < parts; part++) {
            int from = Math.min(words.length, part * wordsPerPart);
            int to = Math.min(words.length, from + wordsPerPart);
            ret[part] = String.join(" ", Arrays.copyOfRange(words, from, to));
        }
        return ret;
    }

    /**
     * Determine whether this passage is shown in several bible versions.
     *
     * @return true if several versions are shown.
     */
    public boolean isParallel() {
        return !versions.isEmpty();
    }

    /**
     * Get the installed bibles this passage is shown in, in display order.
     *
     * @return the bibles. Versions that aren't installed here are left out.
     */
    public List<Bible> getBibleVersions() {
        List<Bible> ret = new ArrayList<>();
        for (BibleVersionText version : versions) {
            Bible bible = BibleManager.get().getBibleFromName(version.getBibleName());
            if (bible != null) {
                ret.add(bible);
            }
        }
        if (!isParallel() && verses.length > 0 && verses[0] != null && verses[0].getChapter().getBook().getBible() != null) {
            ret.add(verses[0].getChapter().getBook().getBible());
        }
        return ret;
    }

    /**
     * Get the name of the bible shown first in this passage.
     *
     * @return the bible name.
     */
    public String getPrimaryBibleName() {
        if (isParallel()) {
            return versions.get(0).getBibleName();
        }
        return summary.substring(summary.indexOf("\n") + 1);
    }

    /**
     * Get the verses that couldn't be found when this passage was created by
     * {@link #withVersions(List)}.
     *
     * @return a description of each missing verse, empty if none are missing.
     */
    public List<String> getMissingVerses() {
        return new ArrayList<>(missingVerses);
    }

    /**
     * Create a copy of this passage shown in the given bibles.
     *
     * @param bibles the bibles to use, in display order.
     * @return the new passage, or null if none of the bibles contains any of
     * the verses.
     */
    public BiblePassage withVersions(List<Bible> bibles) {
        if (bibles == null || bibles.isEmpty()) {
            return null;
        }
        // The rows follow the first version's verses
        List<BibleVerse> newVerses = new ArrayList<>();
        for (BibleVerse verse : verses) {
            if (verse == null) {
                continue;
            }
            List<BibleVerse> matched = Versification.match(verse, bibles.get(0)).verses();
            for (BibleVerse found : matched.isEmpty() ? List.of(verse) : matched) {
                if (newVerses.stream().noneMatch(added -> added == found)) {
                    newVerses.add(found);
                }
            }
        }
        List<String[]> texts = new ArrayList<>();
        List<String[]> refs = new ArrayList<>();
        boolean[] certain = new boolean[bibles.size()];
        Arrays.fill(certain, true);
        List<String> missing = new ArrayList<>();
        List<Set<BibleVerse>> used = new ArrayList<>();
        for (int i = 0; i < bibles.size(); i++) {
            used.add(Collections.newSetFromMap(new IdentityHashMap<>()));
        }
        boolean anyFound = false;
        for (BibleVerse verse : newVerses) {
            String[] rowTexts = new String[bibles.size()];
            String[] rowRefs = new String[bibles.size()];
            for (int i = 0; i < bibles.size(); i++) {
                Versification.Match match = Versification.match(verse, bibles.get(i));
                certain[i] &= match.certain();
                if (match.verses().isEmpty()) {
                    missing.add(bibles.get(i).getBibleName() + ": " + verse.getChapterNum() + ":" + verse.getNum());
                    continue;
                }
                anyFound = true;
                List<BibleVerse> unused = new ArrayList<>();
                for (BibleVerse found : match.verses()) {
                    if (used.get(i).add(found)) {
                        unused.add(found);
                    }
                }
                List<String> parts = new ArrayList<>();
                for (BibleVerse found : unused) {
                    parts.add(found.getText().replaceAll("\\s+", " ").trim());
                }
                rowTexts[i] = String.join(" ", parts);
                if (!unused.isEmpty() && !(unused.size() == 1 && sameNumber(unused.get(0), verse))) {
                    rowRefs[i] = describe(unused);
                }
            }
            texts.add(rowTexts);
            refs.add(rowRefs);
        }
        if (!anyFound) {
            return null;
        }
        // Rows with no text in any version
        for (int row = newVerses.size() - 1; row >= 0; row--) {
            boolean empty = true;
            for (String text : texts.get(row)) {
                empty &= text == null;
            }
            if (empty) {
                newVerses.remove(row);
                texts.remove(row);
                refs.remove(row);
            }
        }
        List<BibleVersionText> newVersions = new ArrayList<>();
        if (bibles.size() > 1) {
            for (int i = 0; i < bibles.size(); i++) {
                String[] versionTexts = new String[texts.size()];
                String[] versionRefs = new String[texts.size()];
                for (int row = 0; row < versionTexts.length; row++) {
                    versionTexts[row] = texts.get(row)[i];
                    versionRefs[row] = refs.get(row)[i];
                }
                newVersions.add(new BibleVersionText(bibles.get(i).getBibleName(), versionTexts, versionRefs, certain[i]));
            }
        }
        BibleBook book = newVerses.get(0).getChapter().getBook();
        BibleBook primaryBook = bibles.get(0).getMatchingBook(book);
        List<String> names = new ArrayList<>();
        for (Bible bible : bibles) {
            names.add(bible.getBibleName());
        }
        String numbers = sameNumbers(newVerses) ? getPassageNumbers() : describe(newVerses);
        String newSummary = (primaryBook == null ? book : primaryBook) + " " + numbers + "\n" + String.join(VERSION_SEPARATOR, names);
        return new BiblePassage(newSummary, newVerses.toArray(new BibleVerse[0]), theme, multi, newVersions, missing);
    }

    /**
     * Find the same verse in another bible.
     *
     * @param bible the bible to look in.
     * @param verse the verse to find.
     * @return the verse, or null if that bible doesn't have it or it's empty.
     * Where the bibles number the verse differently, the first of the verses
     * it corresponds to.
     */
    public static BibleVerse findVerse(Bible bible, BibleVerse verse) {
        List<BibleVerse> found = Versification.match(verse, bible).verses();
        return found.isEmpty() ? null : found.get(0);
    }

    private static boolean sameNumber(BibleVerse a, BibleVerse b) {
        return a.getChapterNum() == b.getChapterNum() && a.getNum() == b.getNum();
    }

    /**
     * Determine whether some verses are numbered the same as this passage's.
     */
    private boolean sameNumbers(List<BibleVerse> other) {
        List<BibleVerse> own = new ArrayList<>();
        for (BibleVerse verse : verses) {
            if (verse != null) {
                own.add(verse);
            }
        }
        if (own.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < own.size(); i++) {
            if (!sameNumber(own.get(i), other.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Describe the chapter and verse numbers of some verses, such as "31:1-2",
     * "1:17-2:1" or "3:16,18".
     *
     * @param verses the verses, in order.
     * @return the description.
     */
    public static String describe(List<BibleVerse> verses) {
        StringBuilder ret = new StringBuilder();
        int start = 0;
        int lastChapter = -1;
        while (start < verses.size()) {
            int end = start;
            while (end + 1 < verses.size() && follows(verses.get(end), verses.get(end + 1))) {
                end++;
            }
            BibleVerse first = verses.get(start);
            BibleVerse last = verses.get(end);
            if (ret.length() > 0) {
                ret.append(first.getChapterNum() == lastChapter ? "," : ";");
            }
            if (first.getChapterNum() != lastChapter) {
                ret.append(first.getChapterNum()).append(':');
            }
            ret.append(first.getNum());
            if (end > start) {
                ret.append('-');
                if (last.getChapterNum() != first.getChapterNum()) {
                    ret.append(last.getChapterNum()).append(':');
                }
                ret.append(last.getNum());
            }
            lastChapter = last.getChapterNum();
            start = end + 1;
        }
        return ret.toString();
    }

    /**
     * Determine whether a verse comes straight after another.
     */
    private static boolean follows(BibleVerse verse, BibleVerse next) {
        if (next.getChapterNum() == verse.getChapterNum()) {
            return next.getNum() == verse.getNum() + 1;
        }
        return next.getChapterNum() == verse.getChapterNum() + 1 && next.getNum() == 1;
    }

    /**
     * Get the location of this passage without the name of the book.
     *
     * @return the chapter and verse part of the location.
     */
    private String getPassageNumbers() {
        return getLocation().replaceFirst("^.*? (?=\\d)", "");
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
        //Older versions of Quelea read a single bible from here and skip the version elements
        ret.append(Utils.escapeXML(isParallel() ? getPrimaryBibleName() : bible));
        if (isParallel()) {
            ret.append("\" booknumber=\"").append(verses[0].getChapter().getBook().getBookNumber());
        }
        ret.append("\">");
        for (BibleVerse verse : verses) {
            ret.append(verse.toXML());
        }
        for (BibleVersionText version : versions) {
            ret.append("<version name=\"").append(Utils.escapeXML(version.getBibleName()));
            if (!version.isCertain()) {
                ret.append("\" certain=\"false");
            }
            ret.append("\">");
            for (int i = 0; i < verses.length; i++) {
                if (version.getVerseText(i) != null) {
                    ret.append("<vers row=\"").append(i);
                    ret.append("\" cnumber=\"").append(verses[i].getChapterNum());
                    ret.append("\" vnumber=\"").append(verses[i].getNum());
                    if (version.getVerseRef(i) != null) {
                        ret.append("\" ref=\"").append(Utils.escapeXML(version.getVerseRef(i)));
                    }
                    ret.append("\">");
                    ret.append(Utils.escapeXML(version.getVerseText(i)));
                    ret.append("</vers>");
                }
            }
            ret.append("</version>");
        }
        ret.append("<theme>");
        ret.append(theme.asXMLString());
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
        fontSizeCache.clear();
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
        int bookNumber = 0;
        if (passage.getAttributes().getNamedItem("booknumber") != null) {
            bookNumber = Integer.parseInt(passage.getAttributes().getNamedItem("booknumber").getTextContent());
        }
        Map<String, ParsedVersion> versionTexts = parseVersions(list);
        List<String> bibleNames = new ArrayList<>(versionTexts.keySet());
        if (bibleNames.isEmpty()) {
            bibleNames.add(bibleSummary);
        }
        String summary = passageSummary + "\n" + String.join(VERSION_SEPARATOR, bibleNames);
        ThemeDTO tempTheme = ThemeDTO.DEFAULT_THEME;
        List<BibleVerse> verses = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equals("vers")) {
                BibleVerse bv = BibleVerse.parseXML(node);
                if (bv != null) {
                    for (String bibleName : bibleNames) {
                        if (addToBible(bv, node, bibleName, passageSummary.split(" \\d")[0], bookNumber)) {
                            verses.add(bv);
                            break;
                        }
                    }
                }
            } else if (node.getNodeName().equals("theme")) {
                tempTheme = ThemeDTO.fromString(node.getTextContent(), Collections.emptyMap());
            }
        }
        BibleVerse[] verseArray = verses.toArray(new BibleVerse[verses.size()]);
        if (versionTexts.isEmpty() || verseArray.length == 0) {
            return new BiblePassage(summary, verseArray, tempTheme, multi);
        }
        List<BibleVersionText> versions = new ArrayList<>();
        for (Map.Entry<String, ParsedVersion> version : versionTexts.entrySet()) {
            ParsedVersion parsed = version.getValue();
            String[] texts = new String[verseArray.length];
            String[] refs = new String[verseArray.length];
            for (int i = 0; i < texts.length; i++) {
                if (parsed.byRow().isEmpty()) {
                    texts[i] = parsed.byNumber().get(verseArray[i].getChapterNum() + ":" + verseArray[i].getNum());
                } else {
                    texts[i] = parsed.byRow().get(i);
                    refs[i] = parsed.refs().get(i);
                }
            }
            versions.add(new BibleVersionText(version.getKey(), texts, refs, parsed.certain()));
        }
        return new BiblePassage(summary, verseArray, tempTheme, multi, versions, Collections.emptyList());
    }

    /**
     * The text of a passage in one version, as read from a schedule.
     *
     * @param byNumber the text of each verse by the passage's "chapter:verse",
     *                 for schedules saved before rows were numbered.
     * @param byRow    the text of each verse by its position in the passage.
     * @param refs     the version's own numbers by position, where they differ.
     * @param certain  false if the verses were paired by number somewhere
     *                 because the numbering wasn't known.
     */
    private record ParsedVersion(Map<String, String> byNumber, Map<Integer, String> byRow,
                                 Map<Integer, String> refs, boolean certain) {
    }

    /**
     * Read the version elements of a passage.
     *
     * @param list the child nodes of the passage.
     * @return the text of each version, by bible name in display order.
     */
    private static Map<String, ParsedVersion> parseVersions(NodeList list) {
        Map<String, ParsedVersion> ret = new LinkedHashMap<>();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals("version")) {
                ParsedVersion version = new ParsedVersion(new HashMap<>(), new HashMap<>(), new HashMap<>(),
                        !"false".equals(attribute(list.item(i), "certain")));
                NodeList verseNodes = list.item(i).getChildNodes();
                for (int j = 0; j < verseNodes.getLength(); j++) {
                    Node verse = verseNodes.item(j);
                    if (verse.getNodeName().equals("vers")) {
                        String row = attribute(verse, "row");
                        if (row != null) {
                            int index = Integer.parseInt(row.trim());
                            version.byRow().put(index, verse.getTextContent());
                            if (attribute(verse, "ref") != null) {
                                version.refs().put(index, attribute(verse, "ref"));
                            }
                        }
                        version.byNumber().put(attribute(verse, "cnumber") + ":" + attribute(verse, "vnumber"), verse.getTextContent());
                    }
                }
                ret.put(attribute(list.item(i), "name"), version);
            }
        }
        return ret;
    }

    private static String attribute(Node node, String name) {
        Node attribute = node.getAttributes().getNamedItem(name);
        return attribute == null ? null : attribute.getTextContent();
    }

    /**
     * Connect a verse read from a schedule to its book in an installed bible.
     *
     * @param verse      the verse.
     * @param node       the node the verse was read from.
     * @param bibleName  the name of the bible.
     * @param bookName   the name of the book in that bible.
     * @param bookNumber the number of the book, or 0 to find it by name.
     * @return true if the bible is installed and contains the book.
     */
    private static boolean addToBible(BibleVerse verse, Node node, String bibleName, String bookName, int bookNumber) {
        for (Bible b : BibleManager.get().getBibles()) {
            if (b.getBibleName().equals(bibleName)) {
                int ii = 1;
                for (BibleBook bb : b.getBooks()) {
                    if (bookNumber > 0 ? bb.getBookNumber() == bookNumber : bb.getBookName().equals(bookName)) {
                        verse.getChapter().setBook(BibleBook.parseXML(node, bb.getBookNumber(), BibleBookNameUtil.getBookNameForIndex(ii-1, b.getBooks().length)));
                        verse.getChapter().getBook().setBible(b);
                        // The copy holds only this verse, too little to tell which book it is
                        verse.getChapter().getBook().setBookCode(bb.getBookCode());
                        return true;
                    }
                    ii++;
                }
            }
        }
        return false;
    }

    /**
     * Get the bible preview icon.
     * <p>
     * @return the bible preview icon.
     */
    @Override
    public javafx.scene.Node getPreviewIcon() {
        return QueleaIcon.BIBLE.createTile();
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
        fontSizeCache.clear();
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
