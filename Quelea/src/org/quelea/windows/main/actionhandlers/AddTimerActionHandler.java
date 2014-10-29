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
package org.quelea.windows.main.actionhandlers;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.widgets.DisplayPositionSelector;

/**
 * The action handler for adding a video.
 *
 * @author Michael
 */
public class AddTimerActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {

        Button confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        InlineCssTextArea wordsArea = new InlineCssTextArea();
        wordsArea.replaceText("5:00");

        final Stage s = new Stage();
        s.setTitle(LabelGrabber.INSTANCE.getLabel("add.timer.title"));
        s.initModality(Modality.APPLICATION_MODAL);
        s.initOwner(QueleaApp.get().getMainWindow());
        s.resizableProperty().setValue(true);

        final BorderPane bp = new BorderPane();

        GridPane grid = new GridPane();
        int rows = 0;
        Label durationLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.duration.label"));
        GridPane.setConstraints(durationLabel, 1, rows);
        grid.getChildren().add(durationLabel);
        TextField durationTextField = new TextField();
        durationTextField.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("duration.tooltip.label")));
        durationTextField.setPromptText("5:00");
        durationTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (parsable(newValue)) {
                    durationTextField.setStyle("-fx-text-fill: black;");
                } else {
                    durationTextField.setStyle("-fx-text-fill: red;");
                }
            }
        });
        durationLabel.setLabelFor(durationTextField);
        GridPane.setHgrow(durationTextField, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setConstraints(durationTextField, 2, rows);
        grid.getChildren().add(durationTextField);
        rows++;

        Label fileLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.file.label"));
        GridPane.setConstraints(fileLabel, 1, rows);
        grid.getChildren().add(fileLabel);
        HBox hb = new HBox();
        TextField fileTextField = new TextField();
        Button browse = new Button("...");
        browse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(FileFilters.IMAGE_VIDEOS);
                File file = fileChooser.showOpenDialog(QueleaApp.get().getMainWindow());
                if (file != null) {
                    fileTextField.setText(file.getAbsolutePath());
                }
            }
        });
        hb.getChildren().addAll(fileTextField, browse);
        fileLabel.setLabelFor(hb);
        GridPane.setHgrow(hb, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(fileTextField, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setConstraints(hb, 2, rows);
        grid.getChildren().add(hb);
        rows++;

        DisplayPositionSelector positionSelector = new DisplayPositionSelector(null);
        positionSelector.prefWidthProperty().bind(s.widthProperty());
        positionSelector.prefHeightProperty().bind(s.widthProperty());
        positionSelector.setTheme(ThemeDTO.DEFAULT_THEME);
        StackPane themePreviewPane = new StackPane();
        Rectangle preview = new Rectangle();
        preview.setFill(Color.BLACK);
        preview.widthProperty().bind(s.widthProperty());
        preview.heightProperty().bind(s.widthProperty());
        
        themePreviewPane.getChildren().add(preview);
        themePreviewPane.getChildren().add(positionSelector);
        GridPane.setConstraints(themePreviewPane, 1, rows, 3, 1);
        grid.getChildren().add(themePreviewPane);
        rows++;

        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!fileTextField.getText().isEmpty() && !durationTextField.getText().isEmpty() && parsable(durationTextField.getText())) {
                    TimerDisplayable displayable = new TimerDisplayable(fileTextField.getText(), parse(durationTextField.getText()));
                    displayable.setTextPosition(DisplayPositionSelector.getPosFromIndex(positionSelector.getSelectedButtonIndex()));
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
                    s.hide();
                }
            }
        });
        GridPane.setConstraints(confirmButton, 1, rows, 3, 1);
        GridPane.setHalignment(confirmButton, HPos.CENTER);
        grid.getChildren().add(confirmButton);

        s.setScene(new Scene(grid));
        s.setMinHeight(432);
        s.setMinWidth(320);
        s.showAndWait();
    }

    private boolean parsable(String newValue) {
        if (parse(newValue) == -1) {
            return false;
        }
        return true;
    }

    private int parse(String newValue) {
        newValue = newValue.replace(" ", "");
        String[] ss;
        if(newValue.contains(":")) {
            ss = newValue.split(":");
        }
        else {
            ss = newValue.split("m");
            try { 
                ss[1] = ss[1].replace("s", "");
            }
            catch(Exception e) {
                return -1;
            }
        }
        try {
            int minutes = Integer.parseInt(ss[0]);
            if (minutes > 60 || minutes < 0) {
                return -1;
            }
            int seconds = Integer.parseInt(ss[1]);
            if (seconds >= 60 || seconds < 0) {
                return -1;
            }
            if (minutes * 60 + seconds > 3600) {
                return -1;
            } else {
                return minutes * 60 + seconds;
            }

        } catch (Exception e) {
            return -1;
        }
    }
}
