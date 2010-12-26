package org.quelea.bible;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A chapter in the bible.
 * @author Michael
 */
public final class BibleChapter {

    private final int num;
    private final List<BibleVerse> verses;

    /**
     * Create a new bible chapter.
     * @param num the chapter number (or -1 if it's unknown.)
     */
    private BibleChapter(int num) {
        this.num = num;
        verses = new ArrayList<BibleVerse>();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * @param info the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleChapter parseXML(Node node) {
        int num = -1;
        if(node.getAttributes().getNamedItem("cnumber")!=null) {
            num = Integer.parseInt(node.getAttributes().getNamedItem("cnumber").getNodeValue());
        }
        BibleChapter ret = new BibleChapter(num);
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equalsIgnoreCase("vers")||
                    list.item(i).getNodeName().equalsIgnoreCase("v")) {
                ret.addVerse(BibleVerse.parseXML(list.item(i)));
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this chapter.
     * @return an XML representation of this chapter.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<chapter");
        if(num!=-1) {
            ret.append(" cnumber=\"");
            ret.append(num);
            ret.append('\"');
        }
        ret.append(">");
        for (BibleVerse verse : verses) {
            ret.append(verse.toXML());
        }
        ret.append("</chapter>");
        return ret.toString();
    }

    /**
     * Get all the text in this chapter as a string.
     * @return all the text in this chapter as a string.
     */
    public String getText() {
        StringBuilder ret = new StringBuilder();
        for(BibleVerse verse : verses) {
            ret.append(verse.getText());
        }
        return ret.toString();
    }

    /**
     * Add a verse to this chapter.
     * @param verse the verse to add.
     */
    public void addVerse(BibleVerse verse) {
        verses.add(verse);
    }

    /**
     * Get all the verses in this chapter .
     * @return all the verses in the chapter.
     */
    public BibleVerse[] getVerses() {
        return verses.toArray(new BibleVerse[verses.size()]);
    }
    
    /**
     * Get a specific verse from this chapter.
     * @param i the verse number to get.
     * @return the verse at the specified number, or null if it doesn't
     * exist.
     */
    public BibleVerse getVerse(int i) {
        if (i < verses.size() && i >= 0) {
            return verses.get(i);
        }
        else {
            return null;
        }
    }

    /**
     * Get the number of this chapter (or -1 if no number has been set.)
     * @return the chapter number.
     */
    public int getNum() {
        return num;
    }
}
