/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/**
 * A volume slider. Consists of a slider and up / down icons.
 * @author Michael
 */
public class VolumeSlider extends BorderPane {
    
    private Slider volumeSlider;
    private List<Runnable> runners;
    
    /**
     * Create the volume slider.
     */
    public VolumeSlider() {
        volumeSlider = new Slider(0,100,100);
        runners = new ArrayList<>();
        volumeSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                for(Runnable runner : runners) {
                    runner.run();
                }
            }
        });
        
        setLeft(new Label("",new ImageView(new Image("file:icons/volumedown.png", 16, 16, false, true))));
        setCenter(volumeSlider);
        setRight(new Label("",new ImageView(new Image("file:icons/volumeup.png", 16, 16, false, true))));
    }
    
    /**
     * Get the current value of the slider.
     * @return the slider's value, between 0-100.
     */
    public double getValue() {
        return (int)volumeSlider.valueProperty().get();
    }
    
    /**
     * Add a runner to be executed when the value of the slider changes.
     * @param runner the runnable to run when the slider's value changes.
     */
    public void addRunner(Runnable runner) {
        runners.add(runner);
    }
    
}
