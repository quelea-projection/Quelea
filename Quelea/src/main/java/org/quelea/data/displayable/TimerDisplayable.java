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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.timer.CountdownTimes;
import org.quelea.windows.timer.TimerDrawer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.quelea.windows.style.QueleaIcon;

/**
 * A displayable that's a countdown timer. It counts down either a fixed
 * duration or to a time of day, such as the start of a service.
 * <p>
 *
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
    private final LocalTime timeToFinish;

    /**
     * Create a new timer displayable.
     * <p>
     *
     * @param name
     * @param background The background of the timer
     * @param seconds    the duration of the countdown timer
     * @param pretext    any text to go before the timer
     * @param posttext   and text to go after the timer
     * @param theme      the theme to use for this displayable when displayed
     */
    public TimerDisplayable(String name, Background background, int seconds, String pretext, String posttext, ThemeDTO theme) {
        this.name = name;
        this.background = background;
        this.seconds = seconds;
        this.pretext = pretext;
        this.posttext = posttext;
        this.theme = theme;
        this.timeToFinish = null;
    }

    /**
     * Create a new timer displayable.
     * <p/>
     *
     * @param seconds  the duration of the countdown timer
     * @param pretext  any text to go before the timer
     * @param posttext and text to go after the timer
     * @param theme    the theme to use for this displayable when displayed
     */
    public TimerDisplayable(String name, int seconds, String pretext, String posttext, ThemeDTO theme) {
        this(name, theme.getBackground(), seconds, pretext, posttext, theme);
    }

    /**
     * Create a new timer displayable.
     * <p>
     *
     * @param name
     * @param background   The background of the timer
     * @param timeToFinish The time of day to countdown to
     * @param pretext      any text to go before the timer
     * @param posttext     and text to go after the timer
     * @param theme        the theme to use for this displayable when displayed
     */
    public TimerDisplayable(String name, Background background, LocalTime timeToFinish, String pretext, String posttext, ThemeDTO theme) {
        this.name = name;
        this.background = background;
        this.seconds = -1;
        this.pretext = pretext;
        this.posttext = posttext;
        this.theme = theme;
        this.timeToFinish = timeToFinish;
    }

    /**
     * Create a new timer displayable.
     * <p/>
     * @param timeToFinish the time of day at which the timer will be 0
     * @param pretext any text to go before the timer
     * @param posttext and text to go after the timer
     * @param theme    the theme to use for this displayable when displayed
     */
    public TimerDisplayable(String name, LocalTime timeToFinish, String pretext, String posttext, ThemeDTO theme) {
        this(name, theme.getBackground(), timeToFinish, pretext, posttext, theme);
    }

    /**
     * Get the displayable location.
     * <p>
     *
     * @return the displayable location.
     */
    @Override
    public String getLocation() {
        return location;
    }

    /**
     * Get the displayable name.
     * <p>
     *
     * @return the displayable name.
     */
    @Override
    public String getName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return getTimeText() + " " + LabelGrabber.INSTANCE.getLabel("countdown.label");
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p>
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static TimerDisplayable parseXML(Node node) {
        NodeList list = node.getChildNodes();

        int i = 0;
        String name = "";
        int duration = -1;
        LocalTime timeToFinish = null;
        String pretext, posttext, theme;
        if (list.getLength() == 5) {
            i = 1;
            name = list.item(0).getTextContent();
        }

        String time = list.item(i).getTextContent().trim();
        try {
            duration = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            timeToFinish = CountdownTimes.parseTimeOfDay(time);
            if (timeToFinish == null) {
                LoggerUtils.getLogger().log(Level.WARNING, "Could not parse time for timer: {0}", time);
                duration = 0;
            }
        }
        pretext = list.item(i + 1).getTextContent();
        posttext = list.item(i + 2).getTextContent();
        theme = list.item(i + 3).getTextContent();
        ThemeDTO tempTheme = ThemeDTO.fromString(theme, Collections.emptyMap());
        if (duration != -1) {
            return new TimerDisplayable(name, duration, pretext, posttext, tempTheme);
        } else {
            return new TimerDisplayable(name, timeToFinish, pretext, posttext, tempTheme);
        }
    }

    /**
     * Get the XML that forms this image displayable.
     * <p>
     *
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<timer>");
        ret.append("<name>");
        ret.append(Utils.escapeXML(getName()));
        ret.append("</name>");
        if (seconds != -1) {
            ret.append("<duration>");
            ret.append(seconds);
            ret.append("</duration>");
        } else {
            ret.append("<time>");
            ret.append(CountdownTimes.formatTimeOfDay(timeToFinish));
            ret.append("</time>");
        }
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
     *
     * @return the timer's preview icon.
     */
    @Override
    public javafx.scene.Node getPreviewIcon() {
        return QueleaIcon.TIMER.createTile();
    }

    /**
     * Get the preview text for the image.
     * <p>
     *
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return getName();
    }

    /**
     * Get any resources this displayable needs.
     * <p>
     *
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
     *
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
        if (seconds < 0) {
            return "00:00";
        }
        return seconds / 60 + ":" + ((seconds % 60) > 9 ? "" : "0") + seconds % 60;
    }

    /**
     * Get the timer's setting as text: the duration ("5:00") or the time of
     * day it counts down to ("09:30").
     *
     * @return the duration or time of day.
     */
    public String getTimeText() {
        if (isCountdownToTime()) {
            return CountdownTimes.formatTimeOfDay(timeToFinish);
        }
        return secondsToTime(seconds);
    }

    /**
     * Determine whether this timer counts down to a time of day rather than
     * for a fixed duration.
     *
     * @return true if it counts down to a time of day.
     */
    public boolean isCountdownToTime() {
        return seconds < 0 && timeToFinish != null;
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

    public void setDrawer(TimerDrawer drawer) {
        this.drawer = drawer;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the time of day this timer counts down to.
     *
     * @return the time of day, or null if the timer counts down a duration.
     */
    public LocalTime getTimeToFinish() {
        return this.timeToFinish;
    }
}
