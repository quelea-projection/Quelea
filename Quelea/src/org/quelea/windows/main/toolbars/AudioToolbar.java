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
package org.quelea.windows.main.toolbars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.languages.LabelGrabber;
import org.quelea.sound.AudioListener;
import org.quelea.sound.AudioPlayer;
import org.quelea.sound.AudioTrack;
import org.quelea.sound.Playlist;
import org.quelea.sound.PlaylistListener;
import org.quelea.utils.Utils;

/**
 * Audio toolbar that resides in the main toolbar for control over the currently
 * playing audio track.
 * <p/>
 * @author Ben Goodwin
 * @version 20-May-2012
 */
public class AudioToolbar extends JToolBar {

    private JButton playpauseButton;
    private JButton skipButton;
    private JToggleButton muteButton;
    private JButton playlistButton;
    private AudioPlayer player;

    /**
     * Create the audio toolbar.
     */
    public AudioToolbar() {
        player = new AudioPlayer();

        setFloatable(false);

        playpauseButton = new JButton(Utils.getImageIcon("icons/pause.png", 24, 24));
        playpauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
        playpauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                player.togglePause();
            }
        });
        playpauseButton.setEnabled(false);
        this.add(playpauseButton);

        skipButton = new JButton(Utils.getImageIcon("icons/skipaudio.png", 24, 24));
        skipButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("skip.audio.control.tooltip"));
        skipButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Skip button
            }
        });
        this.add(skipButton);

        muteButton = new JToggleButton(Utils.getImageIcon("icons/mute.png", 24, 24));
        muteButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("mute.audio.control.tooltip"));
        muteButton.addActionListener(new ActionListener() {

            private int volume;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if(muteButton.isSelected()) {
                    volume = player.getVolume();
                    player.setVolume(0);
                }
                else {
                    player.setVolume(volume);
                }
            }
        });
        this.add(muteButton);

        playlistButton = new JButton(Utils.getImageIcon("icons/playlist.png", 24, 24));
        playlistButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("playlist.select.tooltip"));
        playlistButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Playlist management
            }
        });
        this.add(playlistButton);
        
        player.getPlaylist().addPlaylistListener(new PlaylistListener() {

            @Override
            public void trackChanged(AudioTrack newTrack) {
                //TODO: Update toolbar?
            }

            @Override
            public void trackAdded(AudioTrack newTrack) {
                playpauseButton.setEnabled(true);
            }

            @Override
            public void trackRemoved(AudioTrack newTrack) {
                if(player.getPlaylist().isEmpty()) {
                    playpauseButton.setEnabled(false);
                }
            }
        });

        player.addAudioListener(new AudioListener() {

            /**
             * @inheritDoc
             */
            @Override
            public void played(AudioTrack track) {
                playpauseButton.setIcon(Utils.getImageIcon("icons/pause.png", 24, 24));
                playpauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
            }

            /**
             * @inheritDoc
             */
            @Override
            public void paused(boolean isPaused) {
                if(isPaused) {
                    playpauseButton.setIcon(Utils.getImageIcon("icons/play.png", 24, 24));
                    playpauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("play.audio.control.tooltip"));
                }
                else {
                    playpauseButton.setIcon(Utils.getImageIcon("icons/pause.png", 24, 24));
                    playpauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
                }
            }

            /**
             * @inheritDoc
             */
            @Override
            public void stopped() {
                playpauseButton.setIcon(Utils.getImageIcon("icons/play.png", 24, 24));
                playpauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("play.audio.control.tooltip"));
            }

            /**
             * @inheritDoc
             */
            @Override
            public void volumeChanged(int newVolume) {
                if(newVolume == 0) {
                    if(!(muteButton.isSelected())) {
                        muteButton.doClick();
                    }
                }
            }
        });
    }

    public AudioPlayer getPlayer() {
        return player;
    }
}
