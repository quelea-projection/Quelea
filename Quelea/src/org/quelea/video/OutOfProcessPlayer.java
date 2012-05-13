/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.video;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.list.MediaList;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerMode;

/**
 * Sits out of process so as not to crash the primary VM.
 * @author Michael
 */
public abstract class OutOfProcessPlayer {

    /**
     * Start the main loop reading from the standard input stream and writing
     * to sout.
     * @param mediaPlayer the media player to control via the commands 
     * received.
     * @throws IOException if something goes wrong.
     */
    public void read(MediaPlayer mediaPlayer) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String inputLine;

        //Process the input - I know this isn't very OO but it works for now...
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.startsWith("openloop ")) {
                mediaPlayer.stop();
                MediaPlayerFactory factory = new MediaPlayerFactory();
                MediaListPlayer player = factory.newMediaListPlayer();
                player.setMediaPlayer(mediaPlayer);
                MediaList mediaList = factory.newMediaList();
                inputLine = inputLine.substring("openloop ".length());
                mediaList.addMedia(inputLine);
                player.setMediaList(mediaList);
                player.setMode(MediaListPlayerMode.LOOP);
                mediaPlayer.prepareMedia(inputLine, getPrepareOptions());
            }
            else if (inputLine.startsWith("open ")) {
                mediaPlayer.stop();
                inputLine = inputLine.substring("open ".length());
                mediaPlayer.prepareMedia(inputLine, getPrepareOptions());
            }
            else if (inputLine.equalsIgnoreCase("play")) {
                mediaPlayer.play();
            }
            else if (inputLine.equalsIgnoreCase("loop")) {
                mediaPlayer.play();
            }
            else if (inputLine.equalsIgnoreCase("pause")) {
                mediaPlayer.pause();
            }
            else if (inputLine.equalsIgnoreCase("stop")) {
                mediaPlayer.stop();
            }
            else if (inputLine.equalsIgnoreCase("playable?")) {
                System.out.println(mediaPlayer.isPlayable());
            }
            else if (inputLine.startsWith("setTime ")) {
                inputLine = inputLine.substring("setTime ".length());
                mediaPlayer.setTime(Long.parseLong(inputLine));
            }
            else if (inputLine.startsWith("setVolume ")) {
                inputLine = inputLine.substring("setVolume ".length());
                mediaPlayer.setVolume(Integer.parseInt(inputLine));
            }
            else if (inputLine.startsWith("setMute ")) {
                inputLine = inputLine.substring("setMute ".length());
                mediaPlayer.mute(Boolean.parseBoolean(inputLine));
            }
            else if (inputLine.equalsIgnoreCase("mute?")) {
                boolean mute = mediaPlayer.isMute();
                System.out.println(mute);
            }
            else if (inputLine.equalsIgnoreCase("length?")) {
                long length = mediaPlayer.getLength();
                System.out.println(length);
            }
            else if (inputLine.equalsIgnoreCase("time?")) {
                long time = mediaPlayer.getTime();
                System.out.println(time);
            }
            else if (inputLine.equalsIgnoreCase("close")) {
                System.exit(0);
            }
            else {
                System.out.println("Unknown command: '" + inputLine + "'");
            }
        }
    }
    
    /**
     * This method should return an array of any options that need to be passed 
     * onto VLCJ and in turn libvlc. If no options are required, an empty array
     * should be returned rather than null.
     * @return the options required by libvlc.
     */
    public abstract String[] getPrepareOptions();

}
