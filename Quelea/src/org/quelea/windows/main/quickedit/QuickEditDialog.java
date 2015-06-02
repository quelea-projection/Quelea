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
package org.quelea.windows.main.quickedit;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * A frame used for quickly editing a song.
 *
 * @author Michael
 */
public class QuickEditDialog extends Stage {

    private TextArea sectionArea;
    private Label statusLabel;
    private Button okButton;
    private Button cancelButton;
    private SongDisplayable currentSong;
    private int currentIndex;

    /**
     * Construct a quick edit dialog.
     */
    public QuickEditDialog() {
        setTitle(LabelGrabber.INSTANCE.getLabel("quick.edit.text"));
        currentIndex = -1;
        BorderPane mainPane = new BorderPane();
        
        statusLabel = new Label();
        HBox statusPanel = new HBox();
        statusPanel.getChildren().add(statusLabel);
        mainPane.setTop(statusPanel);
        sectionArea = new TextArea();
        
        sectionArea.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {

            @Override
            public void handle(javafx.scene.input.KeyEvent t ){
                if(t.isShiftDown() && t.getCode()==KeyCode.ENTER) {
                    okButton.fire();
                }
                else if(t.getCode()==KeyCode.ESCAPE) {
                    hide();
                }
            }
        });
        mainPane.setCenter(sectionArea);
        HBox buttonPanel = new HBox();
        buttonPanel.setAlignment(Pos.CENTER);
        BorderPane.setMargin(buttonPanel, new Insets(5,0,5,0));
        buttonPanel.setSpacing(5);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                TextSection oldSection = currentSong.getSections()[currentIndex];
                String[] sectionLyrics = sectionArea.getText().replace("<>", " ").split("\n\n");
                currentSong.replaceSection(new TextSection(oldSection.getTitle(), sectionLyrics[0].split("\n"), oldSection.getSmallText(), oldSection.shouldCapitaliseFirst(), oldSection.getTheme(), oldSection.getTempTheme()), currentIndex);
                for(int i = 1; i < sectionLyrics.length; i++) {
                    String[] lyrics = sectionLyrics[i].split("\n");
                    String newTitle = "";
                    if(oldSection.getTitle() != null && !oldSection.getTitle().trim().isEmpty()) {
                        newTitle = oldSection.getTitle() + " (" + LabelGrabber.INSTANCE.getLabel("part") + " " + (i + 1) + ")";
                    }
                    currentSong.addSection(currentIndex + i, new TextSection(newTitle, lyrics, oldSection.getSmallText(), oldSection.shouldCapitaliseFirst(), oldSection.getTheme(), oldSection.getTempTheme()));
                }
                if(sectionArea.getText().trim().isEmpty()) {
                    currentSong.removeSection(currentIndex);
                }
                hide();
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                Utils.updateSongInBackground(currentSong, false, true);
            }
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                hide();
            }
        });
        buttonPanel.getChildren().add(okButton);
        buttonPanel.getChildren().add(cancelButton);
        mainPane.setBottom(buttonPanel);
        
        setScene(new Scene(mainPane));
    }

    /**
     * Set the song and section of this dialog.
     *
     * @param song the song to set the dialog to.
     * @param sectionIndex the section to set the dialog to.
     */
    public void setSongSection(SongDisplayable song, int sectionIndex) {
        currentSong = song;
        setStatusLabel(song);
        if(song == null) {
            currentIndex = -1;
            sectionArea.setText("");
            return;
        }
        currentIndex = sectionIndex;
        StringBuilder text = new StringBuilder();
        String[] lines = song.getSections()[sectionIndex].getText(true, true);
        for(int i = 0; i < lines.length; i++) {
            String str = lines[i];
            text.append(str);
            if(i < lines.length - 1) {
                text.append('\n');
            }
        }
        sectionArea.setText(text.toString());
    }

    /**
     * Set the status label to a particular song.
     *
     * @param song the song to use.
     */
    private void setStatusLabel(SongDisplayable song) {
        if(song == null) {
            statusLabel.setText("");
        }
        else {
            statusLabel.setText(song.getTitle() + "  (" + LabelGrabber.INSTANCE.getLabel("quick.shortcut.description") + ")");
        }
    }
}
