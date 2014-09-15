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
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

/**
 * Contains the class which is run out of process to enable the creation of
 * multiple media players.
 *
 * @author Greg
 */
public class OOPPlayer {

    public static String inputLine = "";
    private static Window window;
    private static Canvas canvas;
    private static MediaPlayerFactory mediaPlayerFactory;
    private static EmbeddedMediaPlayer mediaPlayer;
    private static boolean init = false;

    /**
     * Strictly a convenience method, called in the main method. This method is
     * responsible for opening a scanner of the system.in stream, and processing
     * the command, passing it along to the proper methods.
     */
    private static void processInMessage() {
        System.err.println("Processing message...");

        Scanner scan = new Scanner(System.in);
        while (scan.hasNext()) {
            inputLine = scan.nextLine();
            try {
                if (inputLine != null) {
                    /**
                     * this is a little ugly, but I'm not sure how to do it
                     * differently.
                     */
                    if (inputLine.startsWith("open ")) {
                        inputLine = inputLine.substring("open ".length());
                        mediaPlayer.prepareMedia(inputLine);
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("play")) {
                        mediaPlayer.play();
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("pause")) {
                        mediaPlayer.pause();
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("stop")) {
                        mediaPlayer.stop();
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("toBack")) {
                        window.toBack();
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("toFront")) {
                        window.toFront();
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("hue?")) {
                        System.out.println(mediaPlayer.getHue());
                    } else if (inputLine.equalsIgnoreCase("playable?")) {
                        System.out.println(mediaPlayer.isPlayable());
                    } else if (inputLine.startsWith("setTime ")) {
                        inputLine = inputLine.substring("setTime ".length());
                        long l;
                        try {
                            l = Long.parseLong(inputLine);
                        } catch (NumberFormatException ex) {
                            l = 0;
                        }
                        mediaPlayer.setTime(l);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setHue ")) {
                        inputLine = inputLine.substring("setHue ".length());
                        mediaPlayer.setAdjustVideo(true);
                        int i;
                        try {
                            i = Integer.parseInt(inputLine);
                        } catch (NumberFormatException ex) {
                            i = 0;
                        }
                        mediaPlayer.setHue(i);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setRepeat ")) {
                        inputLine = inputLine.substring("setRepeat ".length());
                        boolean b;
                        try {
                            b = Boolean.parseBoolean(inputLine);
                        } catch (Exception ex) {
                            b = true;
                        }
                        mediaPlayer.setRepeat(b);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setPosition ")) {
                        inputLine = inputLine.substring("setPosition ".length());
                        float f;
                        try {
                            f = Float.parseFloat(inputLine);
                        } catch (NumberFormatException ex) {
                            f = 0;
                        }
                        if (f > 1) {
                            f = 1;
                        } else if (f < 0) {
                            f = 0;
                        }
                        mediaPlayer.setPosition(f);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setMute ")) {
                        inputLine = inputLine.substring("setMute ".length());
                        boolean b;
                        try {
                            b = Boolean.parseBoolean(inputLine);
                        } catch (Exception ex) {
                            b = false;
                        }
                        mediaPlayer.mute(b);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setOpacity ")) {
                        inputLine = inputLine.substring("setOpacity ".length());
                        float f;
                        try {
                            f = Float.parseFloat(inputLine);
                        } catch (NumberFormatException ex) {
                            f = 0;
                        }
                        if (f > 1) {
                            f = 1;
                        } else if (f < 0) {
                            f = 0;
                        }
                        window.setOpacity(f);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setVolume ")) {
                        inputLine = inputLine.substring("setVolume ".length());
                        int i;
                        try {
                            i = Integer.parseInt(inputLine);
                        } catch (NumberFormatException ex) {
                            i = 100;
                        }
                        mediaPlayer.setVolume(i);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setXLocation ")) {
                        inputLine = inputLine.substring("setXLocation ".length());
                        int i;
                        try {
                            i = Integer.parseInt(inputLine);
                        } catch (NumberFormatException ex) {
                            i = 0;
                        }
                        window.setLocation(i, window.getY());
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setYLocation ")) {
                        inputLine = inputLine.substring("setYLocation ".length());
                        int i;
                        try {
                            i = Integer.parseInt(inputLine);
                        } catch (NumberFormatException ex) {
                            i = 0;
                        }
                        window.setLocation(window.getX(), i);
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setWidthSize ")) {
                        inputLine = inputLine.substring("setWidthSize ".length());
                        int i;
                        try {
                            i = Integer.parseInt(inputLine);
                        } catch (NumberFormatException ex) {
                            i = 0;
                        }
                        window.setSize(i, window.getHeight());
//                    System.out.println("void");
                    } else if (inputLine.startsWith("setHeightSize ")) {
                        inputLine = inputLine.substring("setHeightSize ".length());
                        int i;
                        try {
                            i = Integer.parseInt(inputLine);
                        } catch (NumberFormatException ex) {
                            i = 0;
                        }
                        window.setSize(window.getWidth(), i);
//                    System.out.println("void");
                    } else if (inputLine.equalsIgnoreCase("mute?")) {
                        boolean mute = mediaPlayer.isMute();
                        System.out.println(mute);
                    } else if (inputLine.equalsIgnoreCase("length?")) {
                        long length = mediaPlayer.getLength();
                        System.out.println(length);
                    } else if (inputLine.equalsIgnoreCase("init?")) {
                        System.out.println(init);
                    } else if (inputLine.equalsIgnoreCase("time?")) {
                        long time = mediaPlayer.getTime();
                        System.out.println(time);
                    } else if (inputLine.equalsIgnoreCase("opacity?")) {
                        float opacity = window.getOpacity();
                        System.out.println(opacity);
                    } else if (inputLine.equalsIgnoreCase("close")) {
                        System.exit(0);
                    } else {
                        System.err.println("unknown command: ." + inputLine + ".");
                    }

                }
            } catch (Exception ex) {
                System.err.printf("Error Processing Message");
                ex.printStackTrace(System.err);
            }
        }
        scan.close();
        System.exit(0);

    }

    /**
     * main method
     *
     * @param args takes arguments that are not used in this case
     */
    public static void main(String[] args) {
        final boolean VLC_OK = new NativeDiscovery().discover();
        //exit process if VLC is not on the system
        if (!VLC_OK) {
            System.exit(0);
        }
        try {
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
            init = true;
        } catch (Exception ex) {
            init = false;
        } finally {
            processInMessage();
        }

    }
}
