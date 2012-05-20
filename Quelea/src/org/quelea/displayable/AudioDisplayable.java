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
import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A single AudioDisplayable file
 *
 * @author Ben Goodwin
 * @version 19-May-2012
 */
public class AudioDisplayable implements Displayable {

    private String audioPath;

    public AudioDisplayable() {
        this(null);
    }

    public AudioDisplayable(String path) {
        this.audioPath = path;
    }

    public static Displayable parseXML(Node node) {
        return new AudioDisplayable(node.getTextContent());
    }
    
    @Override
    public boolean supportClear() {
        return false;
    }

    @Override
    public void setAudio(String path) {
        this.audioPath = path;
    }

    @Override
    public String getAudio() {
        return audioPath;
    }

    @Override
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<audio>");
        xml.append("<path>");
        xml.append(Utils.escapeXML(audioPath));
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
        return "<html>" + audioPath + "<br/></html>";
    }

    @Override
    public String getPrintText() {
        return "Audio: " + audioPath;
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
