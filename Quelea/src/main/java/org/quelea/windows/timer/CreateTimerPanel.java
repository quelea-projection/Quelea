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

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.newsong.ThemePanel;

import org.quelea.windows.style.StyleManager;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.services.utils.Utils;

/**
 * Creates the creation panel for a timer displayable
 * <p/>
 *
 * @author Ben
 */
public class CreateTimerPanel extends Stage {

    private ThemeDTO timerTheme = ThemeDTO.DEFAULT_THEME;
    private final CheckBox saveBox;
    private final Button tpConfirm;
    private final RadioButton durationModeButton;
    private final RadioButton timeModeButton;
    private final Label durationLabel;
    private final TextField durationTextField;
    private final TextArea textTextArea;
    private final TextField nameTextField;
    private final Button confirmButton;
    private final GridPane grid;
    private final ThemePanel tp;

    public CreateTimerPanel(TimerDisplayable td) {
        confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), QueleaIcon.CHECK.create());
        StyleManager.markPrimaryButton(confirmButton);

        setTitle(LabelGrabber.INSTANCE.getLabel("add.timer.title"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(QueleaApp.get().getMainWindow());
        resizableProperty().setValue(false);

        grid = new GridPane();
        grid.setPadding(new Insets(5));
        int rows = 0;

        Label nameLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.name.label"));
        GridPane.setConstraints(nameLabel, 0, rows);
        grid.getChildren().add(nameLabel);
        nameTextField = new TextField();
        nameTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> validate());
        nameLabel.setLabelFor(nameTextField);
        GridPane.setConstraints(nameTextField, 1, rows);
        grid.getChildren().add(nameTextField);
        rows++;

        Label modeLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.mode.label"));
        GridPane.setConstraints(modeLabel, 0, rows);
        grid.getChildren().add(modeLabel);
        ToggleGroup modeGroup = new ToggleGroup();
        durationModeButton = new RadioButton(LabelGrabber.INSTANCE.getLabel("timer.mode.duration"));
        durationModeButton.setToggleGroup(modeGroup);
        timeModeButton = new RadioButton(LabelGrabber.INSTANCE.getLabel("timer.mode.time"));
        timeModeButton.setToggleGroup(modeGroup);
        durationModeButton.setSelected(true);
        HBox modeBox = new HBox(10, durationModeButton, timeModeButton);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        modeLabel.setLabelFor(durationModeButton);
        GridPane.setConstraints(modeBox, 1, rows);
        grid.getChildren().add(modeBox);
        rows++;

        durationLabel = new Label();
        GridPane.setConstraints(durationLabel, 0, rows);
        grid.getChildren().add(durationLabel);
        durationTextField = new TextField();
        durationTextField.setTooltip(new Tooltip());
        durationTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            // "9:30am" was the old way of asking for a time of day
            if (durationModeButton.isSelected() && CountdownTimes.parseDuration(newValue) == -1
                    && newValue.toLowerCase(Locale.ROOT).matches(".*[ap]\\.?m.*")
                    && CountdownTimes.parseTimeOfDay(newValue) != null) {
                timeModeButton.setSelected(true);
            }
            validate();
        });
        modeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> updateMode());
        durationLabel.setLabelFor(durationTextField);
        GridPane.setConstraints(durationTextField, 1, rows);
        grid.getChildren().add(durationTextField);
        rows++;

        Label textLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.text.label"));
        GridPane.setConstraints(textLabel, 0, rows);
        grid.getChildren().add(textLabel);
        textTextArea = new TextArea();
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
        Button themeButton = new Button(LabelGrabber.INSTANCE.getLabel("timer.theme.button"), QueleaIcon.THEME.create());
        tpConfirm = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), QueleaIcon.CHECK.create());
        StyleManager.markPrimaryButton(tpConfirm);
        tpConfirm.setAlignment(Pos.CENTER);
        InlineCssTextArea wordsArea = new InlineCssTextArea();
        wordsArea.replaceText(durationTextField.getText());
        tp = new ThemePanel(wordsArea, tpConfirm, true);
        if (td == null) {
            tp.setTheme(timerTheme);
        } else {
            tp.setTheme(td.getTheme());
        }
        Stage tpStage = new Stage();
        tp.sizeDialog(tpStage);
        tpStage.setTitle(LabelGrabber.INSTANCE.getLabel("edit.theme.text"));
        Utils.addIconsToStage(tpStage);
        BorderPane bp = new BorderPane();
        bp.setCenter(tp);
        bp.setBottom(tpConfirm);
        BorderPane.setAlignment(tpConfirm, Pos.CENTER);
        Scene sc = new Scene(bp);
        StyleManager.apply(sc);
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

        Label saveLabel = new Label(LabelGrabber.INSTANCE.getLabel("timer.save.label"));
        GridPane.setConstraints(saveLabel, 0, rows);
        grid.getChildren().add(saveLabel);
        saveBox = new CheckBox();
        saveLabel.setLabelFor(saveBox);
        GridPane.setConstraints(saveBox, 1, rows);
        grid.getChildren().add(saveBox);
        rows++;

        // OK button lives in the dialog footer (see below)

        confirmButton.setOnAction((ActionEvent event) -> {
            if (isValid()) {

                String pretext = "", posttext = "";
                if (!textTextArea.getText().isEmpty() && textTextArea.getText().contains("#")) {
                    String[] s = textTextArea.getText().split("#");
                    pretext = (s.length > 0) ? s[0] : "";
                    posttext = (s.length > 1) ? s[1] : "";
                }

                TimerDisplayable displayable;
                if (timeModeButton.isSelected()) {
                    displayable = new TimerDisplayable(nameTextField.getText(), timerTheme.getBackground(), CountdownTimes.parseTimeOfDay(durationTextField.getText()), pretext, posttext, tp.getTheme());
                } else {
                    displayable = new TimerDisplayable(nameTextField.getText(), timerTheme.getBackground(), CountdownTimes.parseDuration(durationTextField.getText()), pretext, posttext, tp.getTheme());
                }

                if (saveBox.isSelected()) {
                    String fileName = QueleaProperties.get().getTimerDir().getAbsolutePath() + "/" + nameTextField.getText().replace(" ", "_") + ".cdt";
                    File f = new File(fileName);
                    if (f.exists()) {
                        FileChooser fc = new FileChooser();
                        fc.setInitialDirectory(QueleaProperties.get().getTimerDir());
                        fc.getExtensionFilters().add(FileFilters.TIMERS);
                        f = fc.showSaveDialog(null);
                        if (f != null) {
                            if (!f.getName().endsWith(".cdt")) {
                                f = new File(f.getAbsolutePath() + ".cdt");
                            }
                        }
                    }
                    try {
                        TimerIO.timerToFile(displayable, f);
                        QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().forceTimer();
                    } catch (IOException ex) {
                        LoggerUtils.getLogger().log(Level.WARNING, "Could not save timer to file");
                    }
                }
                ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
                int idx = sl.indexOf(td);
                if (td != null && idx > -1) {
                    IndexedDisplayable indexedDisplayable = new IndexedDisplayable(displayable, idx);
                    sl.getItems().set(idx, indexedDisplayable);
                    sl.getSelectionModel().select(indexedDisplayable);
                } else {
                    sl.add(displayable);
                }
                hide();
            }
        });
        if (td != null) {
            createEdit(td);
        }
        updateMode();
        BorderPane root = new BorderPane(grid);
        root.setBottom(new HBox(confirmButton));
        Scene scene = new Scene(root);
        StyleManager.apply(scene);
        setScene(scene);
    }

    /**
     * Show the label, hint and validation for the chosen kind of countdown.
     */
    private void updateMode() {
        boolean time = timeModeButton.isSelected();
        durationLabel.setText(LabelGrabber.INSTANCE.getLabel(time ? "timer.time.label" : "timer.duration.label"));
        durationTextField.setPromptText(time ? "09:30" : "5:00");
        durationTextField.getTooltip().setText(LabelGrabber.INSTANCE.getLabel(time ? "time.tooltip.label" : "duration.tooltip.label"));
        validate();
        if (getScene() != null) {
            sizeToScene(); // the labels differ in length
        }
    }

    private boolean isValid() {
        String text = durationTextField.getText();
        if (timeModeButton.isSelected()) {
            return CountdownTimes.parseTimeOfDay(text) != null;
        }
        return CountdownTimes.parseDuration(text) != -1;
    }

    private void validate() {
        boolean valid = isValid();
        if (valid || durationTextField.getText().isEmpty()) {
            durationTextField.setStyle("");
        } else {
            durationTextField.setStyle("-fx-text-fill: -q-lyrics-error;");
        }
        confirmButton.setDisable(!valid || nameTextField.getText().isEmpty());
    }

    private void setTimerTheme(ThemeDTO theme) {
        timerTheme = theme;
    }

    private void createEdit(TimerDisplayable td) {
        if (td.isCountdownToTime()) {
            timeModeButton.setSelected(true);
        } else {
            durationModeButton.setSelected(true);
        }
        durationTextField.setText(td.getTimeText());
        textTextArea.setText(td.getPretext() + "#" + td.getPosttext());
        nameTextField.setText(td.getName());
    }

}
