package org.quelea.bible;

import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A verse in the bible.
 * @author Michael
 */
public final class BibleVerse {

    private String verse;
    private int num;

    /**
     * For internal use only.
     */
    private BibleVerse() {
        //For internal use
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * @param info the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleVerse parseXML(Node node) {
        BibleVerse ret = new BibleVerse();
        if(node.getAttributes().getNamedItem("vnumber")==null) {
            ret.num = Integer.parseInt(node.getAttributes().getNamedItem("n").getNodeValue());
        }
        else {
            ret.num = Integer.parseInt(node.getAttributes().getNamedItem("vnumber").getNodeValue());
        }
        ret.verse = node.getTextContent();
        return ret;
    }

    /**
     * Generate an XML representation of this verse.
     * @return an XML representation of this verse.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<vers vnumber=\"");
        ret.append(num);
        ret.append("\">");
        ret.append(Utils.escapeXML(verse));
        ret.append("</vers>");
        return ret.toString();
    }

    /**
     * Get the number of this verse.
     * @return the verse number.
     */
    public int getNum() {
        return num;
    }

    /**
     * Get the textual content of the verse.
     * @return the textual content of the verse.
     */
    public String getText() {
        return verse;
    }

}
