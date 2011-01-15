package org.quelea.bible;

import org.quelea.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * General information about a specified bible.
 * @author Michael
 */
public class BibleInfo {

    private final Map<String, String> attributes;

    /**
     * Create a new bible info object.
     * @param title the title of the bible, eg. "King James Version"
     */
    public BibleInfo(String title) {
        this();
        attributes.put("title", title);
        attributes.put("format", "Zefania XML Bible Markup Language");
    }

    /**
     * For internal use only.
     */
    private BibleInfo() {
        attributes = new HashMap<String, String>();
    }

    /**
     * Put an attribute into the bible info object.
     * @param attribute
     * @param value
     */
    public void putAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    /**
     * Get all the attributes as name and value pairs.
     * @return all the attributes as name and value pairs.
     */
    public Set<Map.Entry<String, String>> getAtributes() {
        return attributes.entrySet();
    }

    /**
     * Parse some XML representing this object and return the object it represents.
     * @param info the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleInfo parseXML(Node info) {
        NodeList list = info.getChildNodes();
        BibleInfo ret = new BibleInfo();
        for(int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if(!node.getNodeName().contains("#")) {
                ret.putAttribute(node.getNodeName(), node.getTextContent());
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this bible info object.
     * @return an XML representation of this bible info object.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<information>");
        for(Map.Entry<String, String> attrib : getAtributes()) {
            ret.append('<');
            ret.append(Utils.escapeXML(attrib.getKey()));
            ret.append('>');
            ret.append(Utils.escapeXML(attrib.getValue()));
            ret.append("</");
            ret.append(Utils.escapeXML(attrib.getKey()));
            ret.append('>');
        }
        ret.append("</information>");
        return ret.toString();
    }

}
