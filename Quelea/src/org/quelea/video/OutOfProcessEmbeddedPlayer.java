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
import com.sun.jna.Pointer;
import java.awt.Canvas;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import uk.co.caprica.vlcj.binding.LibVlcFactory;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_player_t;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.linux.LinuxEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.mac.MacEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.WindowsEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * An embedded player that sits out of process in a separate VM. In process
 * players are unstable using VLC due to concurrency bugs in libvlc and its
 * dependencies, so we need to fall back on a mechanism that's a little more
 * complicated than the norm!
 * @author Michael
 */
public class OutOfProcessEmbeddedPlayer extends OutOfProcessPlayer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private EmbeddedMediaPlayer mediaPlayer;

    public OutOfProcessEmbeddedPlayer(final long canvasId) throws IOException {

        //Lifted pretty much out of the VLCJ code
        if (RuntimeUtil.isNix()) {
            mediaPlayer = new LinuxEmbeddedMediaPlayer(LibVlcFactory.factory().synchronise().log().create().libvlc_new(1, new String[]{"--no-video-title"}), null) {

                @Override
                protected void nativeSetVideoSurface(libvlc_media_player_t mediaPlayerInstance, Canvas videoSurface) {
                    libvlc.libvlc_media_player_set_xwindow(mediaPlayerInstance, (int) canvasId);
                }
            };
        }
        else if (RuntimeUtil.isWindows()) {
            mediaPlayer = new WindowsEmbeddedMediaPlayer(LibVlcFactory.factory().synchronise().log().create().libvlc_new(1, new String[]{"--no-video-title"}), null) {

                @Override
                protected void nativeSetVideoSurface(libvlc_media_player_t mediaPlayerInstance, Canvas videoSurface) {
                    Pointer ptr = Pointer.createConstant(canvasId);
                    libvlc.libvlc_media_player_set_hwnd(mediaPlayerInstance, ptr);
                }
            };
        }
        else if (RuntimeUtil.isMac()) {
            mediaPlayer = new MacEmbeddedMediaPlayer(LibVlcFactory.factory().synchronise().log().create().libvlc_new(2, new String[]{"--no-video-title", "--vout=macosx"}), null) {

                @Override
                protected void nativeSetVideoSurface(libvlc_media_player_t mediaPlayerInstance, Canvas videoSurface) {
                    Pointer ptr = Pointer.createConstant(canvasId);
                    libvlc.libvlc_media_player_set_nsobject(mediaPlayerInstance, ptr);
                }
            };
        }
        else {
            mediaPlayer = null;
            System.exit(1);
        }
        mediaPlayer.setVideoSurface(new Canvas()); //Required with a dummy canvas to active the above nativeSetVideoSurface method

    }

    /**
     * No special options needed for this player.
     * @return an empty string array.
     */
    @Override
    public String[] getPrepareOptions() {
        return new String[0];
    }
    
    /**
     * Set this to true if we want to test a file straight off.
     */
    private static final boolean TEST_MODE = false;

    /**
     * Testing stuff.
     * @param args 
     */
    public static void main(String[] args) {
        if (TEST_MODE) {
            args = new String[]{"0"};
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
            OutOfProcessEmbeddedPlayer player = new OutOfProcessEmbeddedPlayer(Integer.parseInt(args[0]));
            if (TEST_MODE) {
                player.mediaPlayer.prepareMedia("dvd:///D:\\");
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
