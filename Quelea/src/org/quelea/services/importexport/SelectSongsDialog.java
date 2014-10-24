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
package org.quelea.services.importexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;

/**
 * A dialog where given songs can be selected.
 * <p/>
 * @author Michael
 */
public class SelectSongsDialog extends Stage {

    private final Button addButton;
    private final CheckBox selectAllCheckBox;
    private final GridPane gridPane;
    private List<SongDisplayable> songs;
    private final List<CheckBox> checkBoxes;
    private final ScrollPane gridScroll;

    /**
     * Create a new imported songs dialog.
     * <p/>
     * @param text a list of lines to be shown in the dialog.
     * @param acceptText text to place on the accpet button.
     * @param checkboxText text to place in the column header for the
     * checkboxes.
     */
    public SelectSongsDialog(String[] text, String acceptText, String checkboxText) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("select.songs.title"));

        checkBoxes = new ArrayList<>();
        selectAllCheckBox = new CheckBox();
        selectAllCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                for(CheckBox checkBox : checkBoxes) {
                    checkBox.setSelected(t1);
                }
            }
        });

        VBox mainPanel = new VBox(5);
        VBox textBox = new VBox();
        for(String str : text) {
            textBox.getChildren().add(new Label(str));
        }
        VBox.setMargin(textBox, new Insets(10));
        mainPanel.getChildren().add(textBox);
        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridScroll = new ScrollPane();
        VBox.setVgrow(gridScroll, Priority.ALWAYS);
        VBox scrollContent = new VBox(10);
        HBox topBox = new HBox(5);
        Label checkAllLabel = new Label(LabelGrabber.INSTANCE.getLabel("check.uncheck.all.text"));
        checkAllLabel.setStyle("-fx-font-weight: bold;");
        topBox.getChildren().add(selectAllCheckBox);
        topBox.getChildren().add(checkAllLabel);
        scrollContent.getChildren().add(topBox);
        scrollContent.getChildren().add(gridPane);
        StackPane intermediatePane = new StackPane();
        StackPane.setMargin(scrollContent, new Insets(10));
        intermediatePane.getChildren().add(scrollContent);
        gridScroll.setContent(intermediatePane);
        gridScroll.setFitToWidth(true);
        gridScroll.setFitToHeight(true);
        mainPanel.getChildren().add(gridScroll);
        addButton = new Button(acceptText, new ImageView(new Image("file:icons/tick.png")));
        StackPane stackAdd = new StackPane();
        stackAdd.getChildren().add(addButton);
        VBox.setMargin(stackAdd, new Insets(10));
        mainPanel.getChildren().add(stackAdd);

        setScene(new Scene(mainPanel, 800, 600));
    }

    /**
     * Set the songs to be shown in the dialog.
     * <p/>
     * @param songs the list of songs to be shown.
     * @param checkList a list corresponding to the song list - each position is
     * true if the checkbox should be selected, false otherwise.
     * @param defaultVal the default value to use for the checkbox if checkList
     * is null or smaller than the songs list.
     */
    public void setSongs(final List<SongDisplayable> songs, final boolean[] checkList, final boolean defaultVal) {
        Collections.sort(songs);
        this.songs = songs;
        gridPane.getChildren().clear();
        checkBoxes.clear();
        gridPane.getColumnConstraints().add(new ColumnConstraints(20));
        ColumnConstraints titleConstraints = new ColumnConstraints();
        titleConstraints.setHgrow(Priority.ALWAYS);
        titleConstraints.setPercentWidth(50);
        gridPane.getColumnConstraints().add(titleConstraints);
        ColumnConstraints authorConstraints = new ColumnConstraints();
        authorConstraints.setHgrow(Priority.ALWAYS);
        authorConstraints.setPercentWidth(45);
        gridPane.getColumnConstraints().add(authorConstraints);

        Label titleHeader = new Label(LabelGrabber.INSTANCE.getLabel("title.label"));
        titleHeader.setAlignment(Pos.CENTER);
        Label authorHeader = new Label(LabelGrabber.INSTANCE.getLabel("author.label"));
        authorHeader.setAlignment(Pos.CENTER);
        gridPane.add(titleHeader, 1, 0);
        gridPane.add(authorHeader, 2, 0);

        for(int i = 0; i < songs.size(); i++) {
            SongDisplayable song = songs.get(i);
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    checkEnableButton();
                }
            });
            if(checkList != null && i < checkList.length) {
                checkBox.setSelected(checkList[i]);
            }
            checkBoxes.add(checkBox);
            gridPane.add(checkBox, 0, i + 1);
            gridPane.add(new Label(song.getTitle()), 1, i + 1);
            gridPane.add(new Label(song.getAuthor()), 2, i + 1);
        }

        for(int i = 0; i < 2; i++) {
            Node n = gridPane.getChildren().get(i);
            if(n instanceof Control) {
                Control control = (Control) n;
                control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                control.setStyle("-fx-alignment: center;-fx-font-weight: bold;");
            }
            if(n instanceof Pane) {
                Pane pane = (Pane) n;
                pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                pane.setStyle("-fx-alignment: center;-fx-font-weight: bold;");
            }
        }
        gridScroll.setVvalue(0);
        checkEnableButton();
    }

    /**
     * Disable / enable the add button depending on if anything is selected.
     */
    private void checkEnableButton() {
        for(CheckBox checkBox : checkBoxes) {
            if(checkBox.isSelected()) {
                addButton.setDisable(false);
                return;
            }
        }
        addButton.setDisable(true);
    }

    /**
     * Get the list of selected songs.
     * <p/>
     * @return the list of selected songs.
     */
    public List<SongDisplayable> getSelectedSongs() {
        List<SongDisplayable> ret = new ArrayList<>();
        for(int i = 0; i < songs.size(); i++) {
            if(checkBoxes.get(i).isSelected()) {
                ret.add(songs.get(i));
            }
        }
        return ret;
    }

    /**
     * Get the add button.
     * <p/>
     * @return the add button.
     */
    public Button getAddButton() {
        return addButton;
    }
}
