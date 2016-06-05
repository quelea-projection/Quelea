/* 
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.main.actionhandlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Class to handle record button changes.
 *
 * @author Arvid
 */
public class RecordButtonHandler {

    private RecordingsHandler recorder;
    private String in;
    private ProgressBar pb;
    private TextField textField;
    private ToggleButton tb;

    /**
     * Method to pass varibles to the class.
     *
     * @param in Text for switch: "rec", "stop", "pause" or "resume"
     * @param pb ProgressBar to update sound level
     * @param textField TextField for getting name of file
     * @param tb ToggleButton to set recording time
     */
    public void passVariables(String in, ProgressBar pb, TextField textField, ToggleButton tb) {
        this.in = in;
        this.pb = pb;
        this.textField = textField;
        this.tb = tb;
        handler();
    }

    public void handler() {

        switch (in) {
            case "rec": {
                recorder = new RecordingsHandler();
                Runnable r = () -> {
                    recorder.start(pb, textField, tb);
                };
                new Thread(r).start();
                break;
            }
            case "stop": {
                Runnable r = () -> {
                    if (recorder != null) {
                        try {
                            recorder.finish(textField, tb);
                        } catch (UnsupportedAudioFileException ex) {
                            Logger.getLogger(RecordButtonHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                new Thread(r).start();
                break;
            }
        }
    }

    public RecordingsHandler getRecordingsHandler() {
        return recorder;
    }
}
