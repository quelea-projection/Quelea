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
package org.quelea.windows.lyrics;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * Dialog where users can select the translations to be displayed.
 *
 * @author Michael
 */
public class TranslationChoiceDialog extends Stage {

    private SongDisplayable currentSong;
    private final VBox content;

    /**
     * Create the translation choice dialog.
     */
    public TranslationChoiceDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("translation.choice.title"));
        Utils.addIconsToStage(this);
        BorderPane root = new BorderPane();
        content = new VBox(5);
        BorderPane.setMargin(content, new Insets(10));
        root.setCenter(content);

        Label selectTranslationLabel = new Label(LabelGrabber.INSTANCE.getLabel("select.translation.label"));
        selectTranslationLabel.setWrapText(true);
        content.getChildren().add(selectTranslationLabel);

        Button okButton = new Button(LabelGrabber.INSTANCE.getLabel("close.button"));
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        okButton.setDefaultButton(true);
        StackPane.setMargin(okButton, new Insets(5));
        StackPane buttonPane = new StackPane();
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.getChildren().add(okButton);
        root.setBottom(buttonPane);

        setScene(new Scene(root));
    }

    /**
     * Select a song for this dialog to show.
     *
     * @param song a song for this dialog to show.
     */
    public void selectSong(final SongDisplayable song) {
        this.currentSong = song;
        List<Node> removes = new ArrayList<>();
        for (Node node : content.getChildren()) {
            if (node instanceof RadioButton) {
                removes.add(node);
            }
        }
        for (Node node : removes) {
            content.getChildren().remove(node);
        }
        ToggleGroup group = new ToggleGroup();
        RadioButton noneBut = new RadioButton(LabelGrabber.INSTANCE.getLabel("none.text"));
        noneBut.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                song.setCurrentTranslationLyrics(null);
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().refreshSong(song);
            }
        });
        noneBut.setToggleGroup(group);
        content.getChildren().add(noneBut);
        if (song.getCurrentTranslationLyrics() == null) {
            noneBut.setSelected(true);
        }
        if (song.getTranslations() != null) {
            for (String translationName : song.getTranslations().keySet()) {
                final RadioButton radBut = new RadioButton(translationName);
                radBut.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {
                        if (!radBut.getText().equals(LabelGrabber.INSTANCE.getLabel("none.text"))) {
                            song.setCurrentTranslationLyrics(radBut.getText());
                            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().refreshSong(song);
                        }
                    }
                });
                if (song.getCurrentTranslationName() != null && song.getCurrentTranslationName().equals(translationName)) {
                    radBut.setSelected(true);
                }
                radBut.setToggleGroup(group);
                content.getChildren().add(radBut);
            }
        }
    }

}
