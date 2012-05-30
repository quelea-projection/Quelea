package org.quelea.windows.main.toolbars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.sound.AudioControlListener;
import org.quelea.utils.Utils;
import org.quelea.windows.main.actionlisteners.MuteActionListener;
import org.quelea.windows.main.actionlisteners.PlayActionListener;
import org.quelea.windows.main.actionlisteners.SkipActionListener;

/**
 *
 *
 * @author Ben Goodwin
 * @version 20-May-2012
 */
public class AudioToolbar extends JToolBar {

    private JButton playpauseButton;
    private JButton skipButton;
    private JToggleButton muteButton;
    private JButton playlist;
    
    public AudioToolbar() {
        setFloatable(false);
        
        playpauseButton = new JButton(Utils.getImageIcon("icons/pause.png", 24, 24));
        playpauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
        playpauseButton.addActionListener(new PlayActionListener(playpauseButton));
        this.add(playpauseButton);
        
        skipButton = new JButton(Utils.getImageIcon("icons/skipaudio.png", 24, 24));
        skipButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("skip.audio.control.tooltip"));
        skipButton.addActionListener(new SkipActionListener());
        this.add(skipButton);
        
        muteButton = new JToggleButton(Utils.getImageIcon("icons/mute.png", 24, 24));
        muteButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("mute.audio.control.tooltip"));
        muteButton.addActionListener(new MuteActionListener(muteButton));
        this.add(muteButton);
        
        playlist = new JButton(Utils.getImageIcon("icons/playlist.png", 24, 24));
        playlist.setToolTipText(LabelGrabber.INSTANCE.getLabel("playlist.select.tooltip"));
        playlist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create new JList thingy
            }
        });
        this.add(playlist);
        
        Application.get().getAudioPlayer().addAudioListener(new AudioControlListener(playpauseButton, muteButton));
    }
        
}
