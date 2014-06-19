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
package org.quelea.windows.multimedia.advancedPlayer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Window;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

/**
 *
 * @author Greg
 */
public class OOPPlayer {

    public static String inputLine = "";
    private static Window window;
    private static Canvas canvas;
    private static MediaPlayerFactory mediaPlayerFactory;
    private static EmbeddedMediaPlayer mediaPlayer;

    private static void processInMessage() {
        System.err.println("Processing message...");

        Scanner scan = new Scanner(System.in);
        while (scan.hasNext()) {
            inputLine = scan.nextLine();

            if (inputLine != null) {

                if (inputLine.startsWith("open ")) {
                    inputLine = inputLine.substring("open ".length());
                    mediaPlayer.prepareMedia(inputLine);
                } else if (inputLine.equalsIgnoreCase("play")) {
                    mediaPlayer.play();
                } else if (inputLine.equalsIgnoreCase("pause")) {
                    mediaPlayer.pause();
                } else if (inputLine.equalsIgnoreCase("stop")) {
                    mediaPlayer.stop();
                } else if (inputLine.equalsIgnoreCase("toBack")) {
                    window.toBack();

                } else if (inputLine.equalsIgnoreCase("playable?")) {
                    System.out.println(mediaPlayer.isPlayable());
                } else if (inputLine.startsWith("setTime ")) {
                    inputLine = inputLine.substring("setTime ".length());
                    mediaPlayer.setTime(Long.parseLong(inputLine));
                } else if (inputLine.startsWith("setHue ")) {
                    inputLine = inputLine.substring("setHue ".length());
                    mediaPlayer.setAdjustVideo(true);
                    mediaPlayer.setHue(Integer.parseInt(inputLine));
                } else if (inputLine.startsWith("setRepeat ")) {
                    inputLine = inputLine.substring("setRepeat ".length());
                    mediaPlayer.setRepeat(Boolean.parseBoolean(inputLine));
                } else if (inputLine.startsWith("setPosition ")) {
                    inputLine = inputLine.substring("setPosition ".length());
                    mediaPlayer.setPosition(Float.parseFloat(inputLine));

                } else if (inputLine.startsWith("setMute ")) {
                    inputLine = inputLine.substring("setMute ".length());
                    mediaPlayer.mute(Boolean.parseBoolean(inputLine));
                } else if (inputLine.startsWith("setOpacity ")) {
                    inputLine = inputLine.substring("setOpacity ".length());
                    window.setOpacity(Float.parseFloat(inputLine));
                } else if (inputLine.startsWith("setVolume ")) {
                    inputLine = inputLine.substring("setVolume ".length());
                   mediaPlayer.setVolume(Integer.parseInt(inputLine));
                } else if (inputLine.startsWith("setXLocation ")) {
                    inputLine = inputLine.substring("setXLocation ".length());
                    window.setLocation(Integer.parseInt(inputLine), window.getY());
                } else if (inputLine.startsWith("setYLocation ")) {
                    inputLine = inputLine.substring("setYLocation ".length());
                    window.setLocation(window.getX(), Integer.parseInt(inputLine));
                } else if (inputLine.startsWith("setWidthSize ")) {
                    inputLine = inputLine.substring("setWidthSize ".length());
                    window.setSize(Integer.parseInt(inputLine), window.getHeight());
                } else if (inputLine.startsWith("setHeightSize ")) {
                    inputLine = inputLine.substring("setHeightSize ".length());
                    window.setSize(window.getWidth(), Integer.parseInt(inputLine));
                } else if (inputLine.equalsIgnoreCase("mute?")) {
                    boolean mute = mediaPlayer.isMute();
                    System.out.println(mute);
                } else if (inputLine.equalsIgnoreCase("length?")) {
                    long length = mediaPlayer.getLength();
                    System.out.println(length);
                } else if (inputLine.equalsIgnoreCase("time?")) {
                    long time = mediaPlayer.getTime();
                    System.out.println(time);
                } else if (inputLine.equalsIgnoreCase("close")) {
                    System.exit(0);
                } else {
                    System.err.println("unknown command: ." + inputLine + ".");
                }
            }
        }
        scan.close();

    }

    public static void main(String[] args) {
        final boolean VLC_OK = new NativeDiscovery().discover();
//        if(!VLC_OK){
//            System.exit(0);
//        }

        window = new Window(null);
        window.setBackground(Color.BLACK);
        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);
        mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show");
        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
        mediaPlayer.setVideoSurface(videoSurface);

        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void finished(MediaPlayer mp) {
                if (mediaPlayer.subItemCount() > 0) {
                    String mrl = mediaPlayer.subItems().remove(0);
                    mediaPlayer.playMedia(mrl);
                }
            }
        });
        window.add(canvas);
        window.setVisible(true);
        window.toBack();
        System.err.println("Started main method");

        processInMessage();

    }
}
