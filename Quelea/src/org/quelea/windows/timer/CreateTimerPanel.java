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
package org.quelea.windows.timer;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.newsong.ThemePanel;

/**
 * Creates the creation panel for a timer displayable
 * <p/>
 * @author Ben
 */
public class CreateTimerPanel extends Stage {
    private ThemeDTO timerTheme = ThemeDTO.DEFAULT_THEME;

    public CreateTimerPanel(Button confirmButton) {

        setTitle(LabelGrabber.INSTANCE.getLabel("add.timer.title"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(QueleaApp.get().getMainWindow());
        resizableProperty().setValue(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5));
        int rows = 0;

        Label durationLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.duration.label"));
        GridPane.setConstraints(durationLabel, 0, rows);
        grid.getChildren().add(durationLabel);
        TextField durationTextField = new TextField();
        durationTextField.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("duration.tooltip.label")));
        durationTextField.setPromptText("5:00");
        durationTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (parsable(newValue)) {
                durationTextField.setStyle("-fx-text-fill: black;");
            } else {
                durationTextField.setStyle("-fx-text-fill: red;");
            }
        });
        durationLabel.setLabelFor(durationTextField);
        GridPane.setConstraints(durationTextField, 1, rows);
        grid.getChildren().add(durationTextField);
        rows++;

        Label textLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.text.label"));
        GridPane.setConstraints(textLabel, 0, rows);
        grid.getChildren().add(textLabel);
        TextArea textTextArea = new TextArea();
        textTextArea.setPrefRowCount(4);
        textTextArea.setPrefColumnCount(30);
        textTextArea.setPromptText(LabelGrabber.INSTANCE.getLabel("timer.text.prompt"));
        textLabel.setLabelFor(textTextArea);
        GridPane.setConstraints(textTextArea, 1, rows);
        grid.getChildren().add(textTextArea);
        rows++;

        Label themeLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.theme.label"));
        GridPane.setConstraints(themeLabel, 0, rows);
        grid.getChildren().add(themeLabel);
        Button themeButton = new Button(LabelGrabber.INSTANCE.getLabel("timer.theme.button"), new ImageView(new Image("file:icons/theme.png", 16, 16, false, true)));
        Button tpConfirm = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        tpConfirm.setAlignment(Pos.CENTER);
        InlineCssTextArea wordsArea = new InlineCssTextArea();
        wordsArea.replaceText(durationTextField.getText());
        ThemePanel tp = new ThemePanel(wordsArea, tpConfirm);
        tp.setPrefSize(500, 500);
        tp.setTheme(timerTheme);
        Stage tpStage = new Stage();
        BorderPane bp = new BorderPane();
        bp.setCenter(tp);
        bp.setBottom(tpConfirm);
        BorderPane.setAlignment(tpConfirm, Pos.CENTER);
        Scene sc = new Scene(bp);
        tpStage.setScene(sc);
        themeButton.setOnAction((ActionEvent Event) -> {
            tpStage.showAndWait();
        });
        tpConfirm.setOnAction((ActionEvent Event) -> { 
            setTimerTheme(tp.getTheme());
            tpStage.hide();
        });
        themeLabel.setLabelFor(themeButton);
        GridPane.setConstraints(themeButton, 1, rows);
        grid.getChildren().add(themeButton);
        rows++;

        GridPane.setConstraints(confirmButton, 0, rows, 2, 1);
        GridPane.setHalignment(confirmButton, HPos.CENTER);
        grid.getChildren().add(confirmButton);

        confirmButton.setOnAction((ActionEvent event) -> {
            if (!durationTextField.getText().isEmpty() && parsable(durationTextField.getText())) {

                String pretext = "", posttext = "";
                if (!textTextArea.getText().isEmpty() && textTextArea.getText().contains("#")) {
                    String[] s = textTextArea.getText().split("#");
                    pretext = (s.length > 0) ? s[0] : "";
                    posttext = (s.length > 1) ? s[1] : "";
                }

                TimerDisplayable displayable = new TimerDisplayable(timerTheme.getBackground(), parse(durationTextField.getText()), pretext, posttext, timerTheme);

                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
                hide();
            }
        });

        setScene(new Scene(grid));
    }

    private boolean parsable(String newValue) {
        return parse(newValue) != -1;
    }

    private int parse(String newValue) {
        newValue = newValue.replace(" ", "");
        String[] ss;
        if (newValue.contains(":")) {
            ss = newValue.split(":");
        } else {
            ss = newValue.split("m");
            try {
                ss[1] = ss[1].replace("s", "");
            } catch (Exception e) {
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

    private void setTimerTheme(ThemeDTO theme) {
        timerTheme = theme;
    }
}
