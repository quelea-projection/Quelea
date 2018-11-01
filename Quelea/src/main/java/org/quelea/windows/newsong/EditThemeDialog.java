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
package org.quelea.windows.newsong;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.utils.ThemeUtils;

/**
 * A modal dialog where a theme can be edited.
 *
 * @author Michael
 */
public class EditThemeDialog extends Stage {

    private ThemePanel panel;
    private ThemeDTO theme;
    private File themeFile;
    private Button confirmButton;
    private Button cancelButton;
    private TextField nameField;
    private ComboBox<ThemeDTO> themeCombo;

    /**
     * Create a new edit theme dialog.
     */
    public EditThemeDialog() {
        initModality(Modality.WINDOW_MODAL);
        Utils.addIconsToStage(this);
        setTitle(LabelGrabber.INSTANCE.getLabel("edit.theme.heading"));
        setResizable(false);

        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color:#dddddd;");

        VBox northBox = new VBox();
        HBox themeSelectPanel = new HBox();
        themeSelectPanel.setPadding(new Insets(5));
        Label themeSelectLabel = new Label(LabelGrabber.INSTANCE.getLabel("theme.select.label") + ": ");
        themeCombo = new ComboBox<>();
        themeCombo.setOnAction(event -> {
            setTheme(themeCombo.getSelectionModel().getSelectedItem());
        });
        themeCombo.setItems(ThemeUtils.getThemes());
        ThemeDTO newTheme = ThemeDTO.DEFAULT_THEME;
        newTheme.setThemeName("New...");
        themeCombo.getItems().add(newTheme);
        themeSelectPanel.getChildren().addAll(themeSelectLabel, themeCombo);

        HBox themeNamePanel = new HBox(5);
        northBox.getChildren().addAll(themeSelectPanel, themeNamePanel);

        themeNamePanel.setPadding(new Insets(5));
        mainPane.setTop(northBox);
        nameField = new TextField();
        nameField.setPromptText(LabelGrabber.INSTANCE.getLabel("theme.name.label"));
        themeNamePanel.getChildren().add(nameField);
        panel = new ThemePanel();
        panel.setPrefSize(500, 500);
        mainPane.setCenter(panel);
        confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        confirmButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                String themeName;
                if (nameField.getText().trim().isEmpty()) {
                    themeName = LabelGrabber.INSTANCE.getLabel("untitled.theme.text");
                } else {
                    themeName = nameField.getText();
                }
                theme = panel.getTheme();
                theme.setFile(themeFile);
                theme.setThemeName(themeName);
                hide();
            }
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                theme = null;
                hide();
            }
        });

        HBox southPanel = new HBox(10);
        southPanel.setPadding(new Insets(10));
        southPanel.setAlignment(Pos.CENTER);
        southPanel.getChildren().add(confirmButton);
        southPanel.getChildren().add(cancelButton);
        mainPane.setBottom(southPanel);

        setScene(new Scene(mainPane));
    }

    /**
     * Get the theme from this dialog.
     *
     * @return the theme.
     */
    public ThemeDTO getTheme() {
        return theme;
    }

    /**
     * Set the theme on this dialog.
     *
     * @param theme the theme.
     */
    public void setTheme(ThemeDTO theme) {
        if (theme == null) {
            theme = new ThemeDTO(ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                    ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, -1, 0);
            theme.setThemeName("");
        }
        if (theme.getFile() == null) {
            File file;
            int filenum = 1;
            do {
                file = new File(new File(QueleaProperties.get().getQueleaUserHome(), "themes"), "theme" + filenum + ".th");
                filenum++;
            } while (file.exists());
            theme.setFile(file);
        }
        if (!theme.equals(themeCombo.getValue())) {
            themeCombo.setValue(theme);
        }
        themeFile = theme.getFile();
        nameField.setText(theme.getThemeName());
        panel.setTheme(theme);
    }
}
