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
package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import org.pushingpixels.substance.internal.ui.SubstanceSliderUI;
import org.quelea.utils.Utils;
import org.quelea.video.RemotePlayer;
import org.quelea.video.RemotePlayerFactory;

/**
 * The control panel for displaying the video.
 * @author Michael
 */
public class VideoControlPanel extends JPanel {

    private static final boolean TEST_DISABLE_SUBSTANCE = false;
    private JButton play;
    private JButton pause;
    private JButton stop;
    private JButton mute;
    private JSlider positionSlider;
    private Canvas videoArea;
    private List<RemotePlayer> mediaPlayers;
    private List<LyricCanvas> registeredCanvases;
    private ScheduledExecutorService executorService;
    private boolean pauseCheck;
    private String videoPath;

    /**
     * Create a new video control panel.
     */
    public VideoControlPanel() {

        executorService = Executors.newSingleThreadScheduledExecutor();
        play = new JButton(Utils.getImageIcon("icons/play.png"));
        play.setEnabled(false);
        play.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                playVideo();
            }
        });
        pause = new JButton(Utils.getImageIcon("icons/pause.png"));
        pause.setEnabled(false);
        pause.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pauseVideo();
            }
        });
        stop = new JButton(Utils.getImageIcon("icons/stop.png"));
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopVideo();
                positionSlider.setValue(0);
            }
        });
        mute = new JButton(Utils.getImageIcon("icons/mute.png"));
        mute.setEnabled(false);
        mute.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setMute(!getMute());
            }
        });
        positionSlider = new JSlider(0, 1000);
        positionSlider.setEnabled(false);
        positionSlider.setValue(0);
        positionSlider.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                for (RemotePlayer mediaPlayer : mediaPlayers) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        pauseCheck = false;
                    }
                    else {
                        pauseCheck = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (RemotePlayer mediaPlayer : mediaPlayers) {
                    mediaPlayer.setTime((long) ((positionSlider.getValue() / (double) 1000) * mediaPlayer.getLength()));
                    if (!pauseCheck) {
                        mediaPlayer.play();
                    }
                }
            }
        });
        if (!TEST_DISABLE_SUBSTANCE) {
            try {
                positionSlider.setUI(new SubstanceSliderUI(positionSlider) {

                    @Override
                    protected void scrollDueToClickInTrack(int direction) {
                        // this is the default behaviour, let's comment that out
                        //scrollByBlock(direction);

                        int value = positionSlider.getValue();

                        if (positionSlider.getOrientation() == JSlider.HORIZONTAL) {
                            value = this.valueForXPosition(positionSlider.getMousePosition().x);
                        }
                        else if (positionSlider.getOrientation() == JSlider.VERTICAL) {
                            value = this.valueForYPosition(positionSlider.getMousePosition().y);
                        }
                        positionSlider.setValue(value);
                    }
                });
            }
            catch (Exception ex) {
                //UI issue, cannot do a lot and don't want to break program...
            }
        }
        videoArea = new Canvas();
        videoArea.setBackground(Color.BLACK);
        videoArea.setMinimumSize(new Dimension(20, 20));
        videoArea.setPreferredSize(new Dimension(100, 100));
        setLayout(new BorderLayout());
        add(videoArea, BorderLayout.CENTER);
        registeredCanvases = new ArrayList<>();

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
        sliderPanel.add(positionSlider);
        controlPanel.add(sliderPanel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(play);
        buttonPanel.add(pause);
        buttonPanel.add(stop);
        buttonPanel.add(mute);
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.NORTH);
        mediaPlayers = new ArrayList<>();
        videoArea.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0 && videoArea.isShowing()) {
                    new Thread() {

                        @Override
                        public void run() {
                            RemotePlayer player = RemotePlayerFactory.getEmbeddedRemotePlayer(videoArea);
                            mediaPlayers.add(0, player);
                            if (videoPath != null) {
                                player.load(videoPath);
                            }
                            play.setEnabled(true);
                            pause.setEnabled(true);
                            stop.setEnabled(true);
                            mute.setEnabled(true);
                            positionSlider.setEnabled(true);
                        }
                    }.start();
                    videoArea.removeHierarchyListener(this);
                }
            }
        });
    }

    /**
     * Register a canvas to be controlled via this video control panel.
     * @param canvas the canvas to control.
     */
    public void registerCanvas(final LyricCanvas canvas) {
        registeredCanvases.add(canvas);
        if (!canvas.isShowing()) {
            canvas.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0 && canvas.isShowing()) {
                        RemotePlayer player = RemotePlayerFactory.getEmbeddedRemotePlayer(canvas);
                        player.setMute(true);
                        mediaPlayers.add(player);
                        if (videoPath != null) {
                            player.load(videoPath);
                        }
                        canvas.removeHierarchyListener(this);
                    }
                }
            });
        }
        else {
            RemotePlayer player = RemotePlayerFactory.getEmbeddedRemotePlayer(canvas);
            player.setMute(true);
            mediaPlayers.add(player);
        }
    }

    /**
     * Get a list of registered lyric canvases.
     * @return a list of registered lyric canvases.
     */
    public List<LyricCanvas> getRegisteredCanvases() {
        return registeredCanvases;
    }

    /**
     * Load the given video to be controlled via this panel.
     * @param videoPath the video path to load.
     */
    public void loadVideo(String videoPath) {
        this.videoPath = videoPath;
        for (RemotePlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.load(videoPath);
        }
    }

    /**
     * Play the loaded video.
     */
    public void playVideo() {
        for (int i = 0; i < mediaPlayers.size(); i++) {
            final RemotePlayer mediaPlayer = mediaPlayers.get(i);
            if (i > 0) {
                mediaPlayer.setMute(true);
            }
            mediaPlayer.play();
            executorService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                int timeVal = (int) ((mediaPlayer.getTime() / (double) mediaPlayer.getLength()) * 1000);
                                positionSlider.setValue(timeVal);
                            }
                        });
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Get the current time of the video.
     * @return the current time of the video.
     */
    public long getTime() {
        return mediaPlayers.get(0).getTime();
    }

    /**
     * Set the current time of the video.
     * @param time the current time of the video.
     */
    public void setTime(long time) {
        for (RemotePlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.setTime(time);
        }
    }

    /**
     * Pause the currently playing video.
     */
    public void pauseVideo() {
        for (RemotePlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.pause();
        }
    }

    /**
     * Stop the currently playing video.
     */
    public void stopVideo() {
        for (RemotePlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.stop();
        }
    }

    /**
     * Set whether the video is muted.
     * @param muteState true to mute, false to unmute.
     */
    public void setMute(boolean muteState) {
        mediaPlayers.get(0).setMute(muteState);
        if (getMute()) {
            mute.setIcon(Utils.getImageIcon("icons/unmute.png"));
        }
        else {
            mute.setIcon(Utils.getImageIcon("icons/mute.png"));
        }
    }

    /**
     * Determine if this video is muted.
     * @return true if muted, false if not.
     */
    public boolean getMute() {
        return mediaPlayers.get(0).getMute();
    }

    /**
     * Close down all the players controlled via this control panel and stop
     * the external VM's / remote players it controls.
     */
    public void close() {
        for (RemotePlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.close();
        }
        executorService.shutdownNow();
    }

    /**
     * Try and stop and clear up if we haven't already.
     * @throws Throwable if something goes wrong.
     */
    @Override
    protected void finalize() throws Throwable {
        stopVideo();
        super.finalize();
        close();
    }

    /**
     * Just for testing.
     * @param args 
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        VideoControlPanel panel = new VideoControlPanel();
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        panel.loadVideo("F:\\Videos\\Inception\\Inception.mkv");
//        panel.loadVideo("C:\\1.avi");
        panel.playVideo();
    }
}
