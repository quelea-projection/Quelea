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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * A headless player that sits out of process in a separate VM, feeding one or
 * more players that sit on the other end of the stream.
 * @author Michael
 */
public class OutOfProcessHeadlessPlayer extends OutOfProcessPlayer {

    private final int port;
    private MediaPlayer mediaPlayer;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new headless player that sits out of process.
     * @param port the port to run this headless player on.
     * @throws IOException if something went wrong.
     */
    public OutOfProcessHeadlessPlayer(int port) throws IOException {
        MediaPlayerFactory factory = new MediaPlayerFactory(new String[]{"--no-video-title"});
        mediaPlayer = factory.newMediaPlayer();
        this.port = port;
    }

    /**
     * Get the port this headless player is running on.
     * @return the port we're using.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the player string used to play from this headless player.
     * @return the player string.
     */
    public String getPlayMediaString() {
        return "http://127.0.0.1:" + port;
    }

    /**
     * Get the special options to pass to vlcj for playing over a headless player.
     * In this case it tells vlcj we're using localhost and playing over HTTP.
     * @return the prepare options as a string array.
     */
    @Override
    public String[] getPrepareOptions() {
        String ret = ":sout=#duplicate{dst=std{access=http,mux=ts,dst=127.0.0.1:" + port + "}}";
        return new String[]{ret};
    }
    
    /**
     * On for testing, off for normal...
     */
    private static final boolean TEST_MODE = false;

    /**
     * Testing stuff.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        if (TEST_MODE) {
            args = new String[]{"5555"};
        }
        File nativeDir;
        if(Utils.is64Bit()) {
            nativeDir = new File("lib/native64");
        }
        else {
            nativeDir = new File("lib/native");
        }
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
            LOGGER.log(Level.WARNING, "Error occured", ex);
        }
    }
}
