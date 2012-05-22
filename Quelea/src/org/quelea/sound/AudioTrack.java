/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.sound;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.quelea.utils.LoggerUtils;

/**
 * An audio track that can be played by the audio player.
 *
 * @author Michael
 */
public class AudioTrack {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String path;

    /**
     * Create a new audio track.
     *
     * @param path the path of the audio file this track represents.
     */
    public AudioTrack(String path) {
        this.path = path;
    }

    public boolean checkOK() {
        AudioInputStream stream = getAudioInputStream();
        if(stream != null) {
            try {
                stream.close();
            }
            catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't close audio input stream", ex);
            }
        }
        return stream != null;
    }

    /**
     * Get the path to this audio track.
     *
     * @return the path to this audio track.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the audio input stream that can be used to read data from this track.
     *
     * @return the track's audio input stream.
     */
    public AudioInputStream getAudioInputStream() {
        File file = new File(path);
        try {
            AudioInputStream in;
            String parsedPath = path.toLowerCase().trim();
            //Workaround, AAC doesn't have SPI - and if we give it SPI capability, it hogs everything then fails if it can't play it.
            if(parsedPath.endsWith("aac")
                    || parsedPath.endsWith("m4a")
                    || parsedPath.endsWith("m4b")
                    || parsedPath.endsWith("m4v")
                    || parsedPath.endsWith("m4p")
                    || parsedPath.endsWith("m4r")
                    || parsedPath.endsWith("3gp")
                    || parsedPath.endsWith("mp4")) {
                in = new AACAudioFileReader().getAudioInputStream(file);
            }
            else {
                in = AudioSystem.getAudioInputStream(file);
            }
            return in;
        }
        catch (UnsupportedAudioFileException | IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get audio input stream: " + file.getAbsolutePath(), ex);
            return null;
        }
    }
}
