/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2012 Ben Goodwin and Michael Berry
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
package org.quelea.displayable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.quelea.Application;
import org.quelea.sound.AudioTrack;
import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A single AudioDisplayable file that can be saved and read from a schedule 
 *
 * @author Ben Goodwin
 * @version 19-May-2012
 */
public class AudioDisplayable implements Displayable {

    private AudioTrack track;

    public AudioDisplayable() {
        this(null);
    }

    public AudioDisplayable(AudioTrack track) {
        this.track = track;
    }

    public static Displayable parseXML(Node node) {
        return new AudioDisplayable(new AudioTrack(node.getTextContent()));
    }
    
    @Override
    public boolean supportClear() {
        return false;
    }

    @Override
    public void setAudio(AudioTrack track) {
        this.track = track;
    }

    @Override
    public AudioTrack getAudio() {
        return track;
    }

    @Override
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<audio>");
        xml.append("<path>");
        xml.append(Utils.escapeXML(track.getPath()));
        xml.append("</path>");
        xml.append("</audio>");
        return xml.toString();
    }

    @Override
    public Icon getPreviewIcon() {
        return Utils.getImageIcon("icons/audio30.png");
    }

    @Override
    public String getPreviewText() {
        return "<html>" + track + "<br/></html>";
    }

    @Override
    public String getPrintText() {
        return "Audio: " + track;
    }

    @Override
    public Collection<File> getResources() {
        return new ArrayList<>();
    }

    @Override
    public void dispose() {
        Application.get().getAudioPlayer().stop();
        // Nothing needed here? 
    }
}
