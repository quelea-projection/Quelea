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
package org.quelea.data.chord;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;

/**
 * The dialog shown to the user when choosing how to transpose the chords of 
 * a song.
 * @author Michael
 */
public class TransposeDialog extends Stage {

    private ComboBox<String> keySelection;
    private int semitones = 0;

    /**
     * Create a new transpose dialog.
     */
    public TransposeDialog() {
        initModality(Modality.WINDOW_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("transpose.label"));
        
        VBox contentPane = new VBox();
        contentPane.setSpacing(5);
        
        keySelection = new ComboBox<>();
        Label label = new Label(LabelGrabber.INSTANCE.getLabel("select.key.label"));
        label.setAlignment(Pos.BASELINE_LEFT);
        contentPane.getChildren().add(label);
        contentPane.getChildren().add(keySelection);

        HBox buttonPanel = new HBox();
        Button okButton = new Button(LabelGrabber.INSTANCE.getLabel("transpose.label"));
        okButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                semitones = keySelection.getSelectionModel().getSelectedIndex() - 4;
                if(semitones <= 0) {
                    semitones--;
                }
                hide();
            }
        });
        buttonPanel.getChildren().add(okButton);
        Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.text"));
        cancelButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                hide();
            }
        });
        buttonPanel.getChildren().add(cancelButton);
        buttonPanel.setAlignment(Pos.BASELINE_LEFT);
        contentPane.getChildren().add(buttonPanel);
        
        setScene(new Scene(contentPane));
    }

    /**
     * Set the root key of the dialog, adjusting the options accordingly.
     * @param key the key the song is currently in.
     */
    public void setKey(String key) {
        keySelection.itemsProperty().get().clear();
        for (int i = -5; i < 7; i++) {
            if (i == 0) {
                continue;
            }
            String transKey = new ChordTransposer(key).transpose(i, null);
            String istr = Integer.toString(i);
            if (i > 0) {
                istr = "+" + istr;
            }
            keySelection.itemsProperty().get().add(transKey + " (" + istr + ")");
        }

        keySelection.getSelectionModel().select(5);
    }

    /**
     * Get the amount of semitones the user has selected to transpose by.
     * @return the amount of semitones to transpose by.
     */
    public int getSemitones() {
        return semitones;
    }
}
