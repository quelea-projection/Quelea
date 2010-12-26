package org.quelea.displayable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import org.quelea.bible.Bible;
import org.quelea.bible.BibleVerse;
import org.quelea.utils.Utils;

/**
 * A displayable passage from the bible.
 * @author Michael
 */
public class BiblePassage implements TextDisplayable {

    private final String summary;
    private final List<TextSection> textSections;
    private final BibleVerse[] verses;

    /**
     * Create a new bible passage.
     * @param bible the bible that the passage comes from.
     * @param location the location of the passage in the bible.
     * @param verses the verses, in order, that make up the passage.
     */
    public BiblePassage(Bible bible, String location, BibleVerse[] verses) {
        this.summary = "<html>" + location + "<br/><i>" + bible.getName() + "</i></html>";
        this.verses = Arrays.copyOf(verses, verses.length);
        textSections = new ArrayList<TextSection>();
        fillTextSections();
    }

    /**
     * Fill the text sections with the verses.
     */
    private void fillTextSections() {
        final int LINES_PER_SLIDE = 8;
        final int WORDS_PER_LINE = 7;
        List<String> words = new ArrayList<String>();
        for (int i = 0; i < verses.length; i++) {
            words.addAll(Arrays.asList(verses[i].getText().split(" ")));
        }

        List<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for(int i=0 ; i<words.size() ; i++) {
            line.append(words.get(i)).append(" ");
            if ((i != 0 && i % WORDS_PER_LINE == 0) || i == words.size() - 1) {
                lines.add(line.toString());
                line.setLength(0);
            }
        }
        List<String> sections = new ArrayList<String>();
        for(int i=0 ; i<lines.size(); i++) {
            sections.add(lines.get(i));
            if ((i != 0 && i % LINES_PER_SLIDE == 0) || i == lines.size() - 1) {
                textSections.add(new TextSection("", sections.toArray(new String[sections.size()]), false));
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
        ret.append(summary);
        ret.append("\">");
        for (BibleVerse verse : verses) {
            ret.append(verse.toXML());
        }
        ret.append("</passage>");
        return ret.toString();
    }

    /**
     * Get the bible preview icon.
     * @return the bible preview icon.
     */
    public Icon getPreviewIcon() {
        return Utils.getImageIcon("icons/bible.png");
    }

    /**
     * Get the preview text.
     * @return the preview text.
     */
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
}
