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
package org.quelea.services.notice;

import java.util.Objects;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A notice to be displayed on the bottom of the main projection screen.
 *
 * @author Michael
 */
public class Notice {

    private SerializableColor color;
    private SerializableFont font;
    private String text;
    private int times;
    private int originalDuration;
    private long creationTime = 0;

    /**
     * Create a new notice.
     *
     * @param str the notice text.
     * @param times the number of times to display the notice.
     * @param color the font color.
     * @param font the notice font.
     */
    public Notice(String str, int times, SerializableColor color, SerializableFont font) {
        this.text = str;
        this.times = times;
        this.color = color;
        this.font = font;
        originalDuration = times;
        if (creationTime == 0) {
            creationTime = System.currentTimeMillis();
        }
    }

    /**
     * Copy attributes from one notice to this notice.
     *
     * @param other the other notice.
     */
    public void copyAttributes(Notice other) {
        this.text = other.text;
        this.times = other.times;
        this.color = other.color;
        this.font = other.font;
    }

    /**
     * Get the notice text.
     *
     * @return the notice text.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the number of times this notice should be displayed.
     *
     * @return the number of times this notice should be displayed.
     */
    public int getTimes() {
        return times;
    }

    /**
     * Decrement the times - call after the notice has been displayed once.
     */
    public void decrementTimes() {
        times--;
    }

    /**
     * Set the notice text.
     *
     * @param text the notice text.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the time of creation.
     *
     * @return creation time of the notice-
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Set the time of creation.
     *
     * @param time creation time as a long value.
     */
    public void setCreationTime(long time) {
        creationTime = time;
    }

    /**
     * Set the number of times this notice should display.
     *
     * @param times the number of times this notice should display.
     */
    public void setTimes(int times) {
        this.times = times;
        originalDuration = times;
    }

    public int getOriginalTimes() {
        return originalDuration;
    }

    public SerializableColor getColor() {
        return color;
    }

    public void setColor(SerializableColor color) {
        this.color = color;
    }

    public SerializableFont getFont() {
        return font;
    }

    public void setFont(SerializableFont font) {
        this.font = font;
    }

    /**
     * Convert to a string.
     *
     * @return the notice text.
     */
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.color);
        hash = 11 * hash + Objects.hashCode(this.font);
        hash = 11 * hash + Objects.hashCode(this.text);
        hash = 11 * hash + this.times;
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
        final Notice other = (Notice) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.font, other.font)) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this.times != other.times) {
            return false;
        }
        return true;
    }

    /**
     * Get the XML that forms this notice.
     * <p>
     * @return the XML.
     */
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<notice>");
        ret.append("<text>");
        ret.append(Utils.escapeXML(text));
        ret.append("</text>");
        ret.append("<duration>");
        int times = originalDuration;
        if (times == Integer.MAX_VALUE) {
            times = 0;
        }
        ret.append(times);
        ret.append("</duration>");
        ret.append("<color>");
        ret.append(getColor().getColor());
        ret.append("</color>");
        ret.append("<font>");
        ret.append(getFont().getFont().getName());
        ret.append(",");
        ret.append(getFont().getFont().getSize());
        ret.append("</font>");
        ret.append("</notice>");
        return ret.toString();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p>
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static Notice parseXML(Node node) {
        NodeList list = node.getChildNodes();
        String text = "";
        int duration = 0;
        String colorString = "0xffffffff";
        String fontString = "System Regular,50.0";
        for (int i = 0; i < list.getLength(); i++) {
            switch (list.item(i).getNodeName()) {
                case "text":
                    text = list.item(i).getTextContent();
                    break;
                case "duration":
                    duration = Integer.parseInt(list.item(i).getTextContent());
                    if (duration == 0) {
                        duration = Integer.MAX_VALUE;
                    }
                    break;
                case "color":
                    colorString = list.item(i).getTextContent();
                    break;
                case "font":
                    fontString = list.item(i).getTextContent();
                    break;
            }
        }

        SerializableColor color = new SerializableColor(Color.web(colorString));
        String[] fontTemp = fontString.split(",");
        SerializableFont font = new SerializableFont(new Font(fontTemp[0], Double.parseDouble(fontTemp[1])));
        return new Notice(text, duration, color, font);
    }

}
