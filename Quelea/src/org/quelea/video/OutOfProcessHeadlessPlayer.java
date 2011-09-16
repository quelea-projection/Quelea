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

import com.sun.jna.NativeLibrary;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.quelea.utils.QueleaProperties;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 *
 * @author Michael
 */
public class OutOfProcessHeadlessPlayer extends OutOfProcessPlayer {

    private final int port;
    private MediaPlayer mediaPlayer;

    public OutOfProcessHeadlessPlayer(int port) throws IOException {
        MediaPlayerFactory factory = new MediaPlayerFactory(new String[]{"--no-video-title"});
        mediaPlayer = factory.newMediaPlayer();
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getPlayMediaString() {
        return "http://127.0.0.1:" + port;
    }

    @Override
    public String[] getPrepareOptions() {
        String ret = ":sout=#duplicate{dst=std{access=http,mux=ts,dst=127.0.0.1:" + port + "}}";
        return new String[]{ret};
    }
    private static final boolean TEST_MODE = false;

    public static void main(String[] args) {
        if (TEST_MODE) {
            args = new String[]{"5555"};
        }
        File nativeDir = new File("lib/native");
        NativeLibrary.addSearchPath("libvlc", nativeDir.getAbsolutePath());
        NativeLibrary.addSearchPath("vlc", nativeDir.getAbsolutePath());
        try (PrintStream stream = new PrintStream(new File(QueleaProperties.getQueleaUserHome(), "ooplog.txt"))) {
            System.setErr(stream); //Important, MUST redirect err stream
            OutOfProcessHeadlessPlayer player = new OutOfProcessHeadlessPlayer(Integer.parseInt(args[0]));
            if (TEST_MODE) {
                player.mediaPlayer.prepareMedia("dvdsimple://E:");
                player.mediaPlayer.play();
            }
            else {
                player.read(player.mediaPlayer);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
