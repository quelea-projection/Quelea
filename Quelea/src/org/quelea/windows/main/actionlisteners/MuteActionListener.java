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
package org.quelea.windows.main.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToggleButton;
import org.quelea.Application;
import org.quelea.sound.AudioPlayer;

/**
 *
 *
 * @author Ben Goodwin
 * @version 20-May-2012
 */
public class MuteActionListener implements ActionListener {

    private JToggleButton button;
    private int volume;
    
    public MuteActionListener(JToggleButton b) {
        this.button = b;
    }   
    
    @Override
    public void actionPerformed(ActionEvent e) {
        AudioPlayer ap = Application.get().getAudioPlayer();
        if(button.isSelected()) {
            volume = ap.getVolume();
            ap.setVolume(0);
        }
        else ap.setVolume(volume);
    }

}
