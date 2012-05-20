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

import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * A blank adapter to provide empty implementations of AudioListener.
 *
 * @author Michael
 */
public class AudioAdapter implements AudioListener {

    private JButton play;
    private JToggleButton mute;
    
    public AudioAdapter(JButton play, JToggleButton mute) {
        this.play = play;
        this.mute = mute;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void played() {
        play.setIcon(Utils.getImageIcon("icons/pause.png", 24, 24));
        play.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void paused(boolean isPaused) {
        if(isPaused) {
            play.setIcon(Utils.getImageIcon("icons/play.png", 24, 24));
            play.setToolTipText(LabelGrabber.INSTANCE.getLabel("play.audio.control.tooltip"));
        }
        else {
            play.setIcon(Utils.getImageIcon("icons/pause.png", 24, 24));
            play.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stopped() {
        play.setIcon(Utils.getImageIcon("icons/play.png", 24, 24));
        play.setToolTipText(LabelGrabber.INSTANCE.getLabel("play.audio.control.tooltip"));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void volumeChanged(int newVolume) {
        if(newVolume == 0) {
            if(!(mute.isSelected())) {
                mute.doClick();
            }
        }
    }
}
