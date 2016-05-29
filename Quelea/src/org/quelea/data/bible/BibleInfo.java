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
package org.quelea.data.bible;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * General information about a specified bible.
 *
 * @author Michael
 */
public class BibleInfo implements Serializable {

    private final Map<String, String> attributes;

    /**
     * Create a new bible info object.
     *
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
        attributes = new HashMap<>();
    }

    /**
     * Put an attribute into the bible info object.
     *
     * @param attribute
     * @param value
     */
    public void putAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    /**
     * Get all the attributes as name and value pairs.
     *
     * @return all the attributes as name and value pairs.
     */
    public Set<Map.Entry<String, String>> getAtributes() {
        return attributes.entrySet();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.attributes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BibleInfo other = (BibleInfo) obj;
        if (!Objects.equals(this.attributes, other.attributes)) {
            return false;
        }
        return true;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param info the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static BibleInfo parseXML(Node info) {
        NodeList list = info.getChildNodes();
        BibleInfo ret = new BibleInfo();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (!node.getNodeName().contains("#")) {
                ret.putAttribute(node.getNodeName(), node.getTextContent());
            }
        }
        return ret;
    }

    /**
     * Generate an XML representation of this bible info object.
     *
     * @return an XML representation of this bible info object.
     */
    public String toXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<information>");
        for (Map.Entry<String, String> attrib : getAtributes()) {
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
