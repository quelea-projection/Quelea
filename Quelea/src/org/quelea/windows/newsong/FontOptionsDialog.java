/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2014 Michael Berry
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

import java.util.Collections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;

/**
 * A dialog for configuring further font options that aren't directly shown on
 * the theme toolbar.
 *
 * @author Michael
 */
public class FontOptionsDialog extends Stage {

    private static FontSelectionDialog fontSelectionDialog;
    private final ComboBox<String> fontSelection;
    private final Button fontExpandButton;
    private final ColorPicker fontColor;
    private final ColorPicker shadowColor;
    private final ToggleButton boldButton;
    private final ToggleButton italicButton;
    private final Slider shadowOffsetSlider;
    private final Button okButton;
    private final boolean bible;

    /**
     * Create the font options dialog.
     */
    public FontOptionsDialog(boolean bible) {
        this.bible = bible;
        initStyle(StageStyle.UTILITY);
        initModality(Modality.APPLICATION_MODAL);
        Utils.addIconsToStage(this);

        if (fontSelectionDialog == null) {
            fontSelectionDialog = new FontSelectionDialog();
        }
        fontSelection = new ComboBox<>();
        fontSelection.setMaxWidth(Integer.MAX_VALUE);
        fontSelection.getItems().addAll(fontSelectionDialog.getChosenFonts());
        Collections.sort(fontSelection.getItems());
        HBox.setHgrow(fontSelection, Priority.ALWAYS);

        fontExpandButton = new Button("...");
        fontExpandButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("more.fonts.label") + "..."));
        Utils.setToolbarButtonStyle(fontExpandButton);
        fontExpandButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                fontSelectionDialog.showAndWait();
                String selected = fontSelection.getSelectionModel().getSelectedItem();
                fontSelection.getItems().clear();
                fontSelection.getItems().addAll(fontSelectionDialog.getChosenFonts());
                Collections.sort(fontSelection.getItems());
                fontSelection.getSelectionModel().select(selected);
            }
        });

        fontColor = new ColorPicker(Color.WHITE);
        fontColor.setStyle("-fx-color-label-visible: false ;");

        shadowColor = new ColorPicker(Color.GRAY);
        shadowColor.setStyle("-fx-color-label-visible: false ;");

        shadowOffsetSlider = new Slider(0, 20, 2);
        shadowOffsetSlider.setShowTickMarks(false);
        shadowOffsetSlider.setBlockIncrement(1);
        shadowOffsetSlider.setMajorTickUnit(1);

        boldButton = new ToggleButton("", new ImageView(new Image("file:icons/bold.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(boldButton);
        italicButton = new ToggleButton("", new ImageView(new Image("file:icons/italic.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(italicButton);

        BorderPane root = new BorderPane();
        VBox controlRoot = new VBox();
        if (!bible) {
            controlRoot.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("translation.font.text") + ":"));
            HBox fontBox = new HBox(5);
            fontBox.getChildren().add(fontSelection);
            fontBox.getChildren().add(fontExpandButton);
            controlRoot.getChildren().add(fontBox);
            HBox fontButtonBox = new HBox(5);
            fontButtonBox.getChildren().add(boldButton);
            fontButtonBox.getChildren().add(italicButton);
            fontButtonBox.getChildren().add(fontColor);
            controlRoot.getChildren().add(fontButtonBox);
        }
        controlRoot.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("shadow.text") + ":"));
        HBox shadowBox = new HBox(5);
        shadowBox.getChildren().add(shadowColor);
        shadowBox.getChildren().add(shadowOffsetSlider);
        controlRoot.getChildren().add(shadowBox);

        BorderPane.setMargin(controlRoot, new Insets(10));
        StackPane buttonPane = new StackPane();
        buttonPane.setAlignment(Pos.CENTER);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        okButton.setAlignment(Pos.CENTER);
        StackPane.setMargin(okButton, new Insets(10));
        buttonPane.getChildren().add(okButton);
        root.setCenter(controlRoot);
        root.setBottom(buttonPane);
        setScene(new Scene(root, 230, 180));
    }

    /**
     * Get the theme to display on this font options dialog.
     *
     * @param theme the theme to display.
     */
    public void setTheme(ThemeDTO theme, boolean bible) {
        if (bible) {
            shadowColor.setValue(theme.getBibleShadow().getColor());
            shadowColor.fireEvent(new ActionEvent());
            shadowOffsetSlider.setValue(theme.getBibleShadow().getOffsetX());
        } else {
            fontSelection.getSelectionModel().select(theme.getTranslateFont().getFamily());
            fontColor.setValue(theme.getTranslateFontPaint());
            fontColor.fireEvent(new ActionEvent());
            boldButton.setSelected(theme.isTranslateBold());
            italicButton.setSelected(theme.isTranslateItalic());
            shadowColor.setValue(theme.getShadow().getColor());
            shadowColor.fireEvent(new ActionEvent());
            shadowOffsetSlider.setValue(theme.getShadow().getOffsetX());
        }
    }

    /**
     * Get the shadow options represented on this dialog.
     *
     * @return the shadow options.
     */
    public SerializableDropShadow getShadow() {
        return new SerializableDropShadow(shadowColor.getValue(), shadowOffsetSlider.getValue(), shadowOffsetSlider.getValue());
    }

    /**
     * Get the font represented on this dialog.
     *
     * @return the font.
     */
    public SerializableFont getTranslateFont() {
        return new SerializableFont(Font.font(fontSelection.getSelectionModel().getSelectedItem(),
                boldButton.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                italicButton.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize()));
    }

    /**
     * Get the font colour represented on this dialog.
     *
     * @return the font colour.
     */
    public Color getTranslateColour() {
        return fontColor.getValue();
    }

    /**
     * Determine whether the font is bold.
     *
     * @return true if bold, false otherwise.
     */
    public boolean isTranslateBold() {
        return boldButton.isSelected();
    }

    /**
     * Determine whether the font is italic.
     *
     * @return true if italic, false otherwise.
     */
    public boolean isTranslateItalic() {
        return italicButton.isSelected();
    }

}
