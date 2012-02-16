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
package org.quelea.windows.video;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.quelea.utils.Utils;

/**
 * A volume slider. Consists of a slider and up / down icons.
 * @author Michael
 */
public class VolumeSlider extends JPanel {
    
    private JSlider volumeSlider;
    private List<Runnable> runners;
    
    /**
     * Create the volume slider.
     */
    public VolumeSlider() {
        volumeSlider = new JSlider(0,100,100);
        volumeSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                for(Runnable runner : runners) {
                    runner.run();
                }
            }
        });
        runners = new ArrayList<>();
        
        setLayout(new BorderLayout());
        add(new JLabel(Utils.getImageIcon("icons/volumedown.png", 16, 16)), BorderLayout.WEST);
        add(volumeSlider, BorderLayout.CENTER);
        add(new JLabel(Utils.getImageIcon("icons/volumeup.png", 16, 16)), BorderLayout.EAST);
    }
    
    /**
     * Get the current value of the slider.
     * @return the slider's value, between 0-100.
     */
    public int getValue() {
        return volumeSlider.getValue();
    }
    
    /**
     * Add a runner to be executed when the value of the slider changes.
     * @param runner the runnable to run when the slider's value changes.
     */
    public void addRunner(Runnable runner) {
        runners.add(runner);
    }
    
}
