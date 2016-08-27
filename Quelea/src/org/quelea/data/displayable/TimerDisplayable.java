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
import java.util.Collection;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.timer.TimerDrawer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A displayable that's a countdown timer.
 * <p>
 * @author Ben Goodwin
 */
public class TimerDisplayable implements MultimediaDisplayable, Serializable {

    private final Background background;
    private final int seconds;
    private Pos pos = Pos.CENTER;
    private final String pretext;
    private final String posttext;
    private ThemeDTO theme;
    private final String location = "";
    private TimerDrawer drawer;
    private String name = "";

    /**
     * Create a new timer displayable.
     * <p>
     * @param background The background of the timer
     * @param seconds the duration of the countdown timer
     * @param pretext any text to go before the timer
     * @param posttext and text to go after the timer
     * @param theme the theme to use for this displayable when displayed
     */
    public TimerDisplayable(String name, Background background, int seconds, String pretext, String posttext, ThemeDTO theme) {
        this.name = name;
        this.background = background;
        this.seconds = seconds;
        this.pretext = pretext;
        this.posttext = posttext;
        this.theme = theme;
    }

    /**
     * Create a new timer displayable.
     * <p/>
     * @param seconds the duration of the countdown timer
     * @param pretext any text to go before the timer
     * @param posttext and text to go after the timer
     * @param theme the theme to use for this displayable when displayed
     */
    public TimerDisplayable(String name, int seconds, String pretext, String posttext, ThemeDTO theme) {
        this(name, theme.getBackground(), seconds, pretext, posttext, theme);
    }

    /**
     * Get the displayable location.
     * <p>
     * @return the displayable location.
     */
    @Override
    public String getLocation() {
        return location;
    }

    /**
     * Get the displayable name.
     * <p>
     * @return the displayable name.
     */
    @Override
    public String getName() {
        if (name != null || !name.equals("")) {
            return name;
        } else {
            return secondsToTime(seconds) + " " + LabelGrabber.INSTANCE.getLabel("countdown.label");
        }
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p>
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static TimerDisplayable parseXML(Node node) {
        NodeList list = node.getChildNodes();

        int i = 0;
        String name = "";
        int duration = 0;
        String pretext,posttext,theme;
        if (list.getLength() == 5) {
            i = 1;
            name = list.item(0).getTextContent();
        }
        duration = Integer.parseInt(list.item(i).getTextContent());
        pretext = list.item(i+1).getTextContent();
        posttext = list.item(i+2).getTextContent();
        theme = list.item(i+3).getTextContent();
        ThemeDTO tempTheme = ThemeDTO.fromString(theme);
        return new TimerDisplayable(name, duration, pretext, posttext, tempTheme);
    }

    /**
     * Get the XML that forms this image displayable.
     * <p>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<timer>");
        ret.append("<name>");
        ret.append(Utils.escapeXML(name));
        ret.append("</name>");
        ret.append("<duration>");
        ret.append(seconds);
        ret.append("</duration>");
        ret.append("<pretext>");
        ret.append(Utils.escapeXML(pretext));
        ret.append("</pretext>");
        ret.append("<posttext>");
        ret.append(Utils.escapeXML(posttext));
        ret.append("</posttext>");
        ret.append("<theme>");
        ret.append(theme.asXMLString());
        ret.append("</theme>");
        ret.append("</timer>");
        return ret.toString();
    }

    /**
     * Get the preview icon of this video.
     * <p>
     * @return the timer's preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/timer-light.png", 30, 30, false, true));
    }

    /**
     * Get the preview text for the image.
     * <p>
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return getName();
    }

    /**
     * Get any resources this displayable needs.
     * <p>
     * @return the image backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<>();
        files.addAll(theme.getBackground().getResources());
        if (theme.getFile() != null) {
            files.add(theme.getFile());
        }
        return files;
    }

    /**
     * Determine whether videos support clearing, which they don't.
     * <p>
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return false;
    }

    @Override
    public void dispose() {
        //Nothing needed here.
    }

    public String secondsToTime(int seconds) {
        return (int) Math.floor(seconds / 60) + ":" + ((seconds % 60) > 9 ? "" : "0") + seconds % 60;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setTextPosition(Pos posFromIndex) {
        this.pos = posFromIndex;
    }

    public Pos getTextPosition() {
        return pos;
    }

    public String getPretext() {
        return pretext;
    }

    public String getPosttext() {
        return posttext;
    }

    public ThemeDTO getTheme() {
        return theme;
    }

    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
        if (drawer != null) {
            drawer.setTheme(theme);
        }
    }

    public void addDrawer(TimerDrawer drawer) {
        this.drawer = drawer;
    }

    public void setName(String name) {
        this.name = name;
    }
}
