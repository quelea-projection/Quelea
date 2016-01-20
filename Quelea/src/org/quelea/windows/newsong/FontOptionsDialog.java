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
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
import javafx.util.converter.NumberStringConverter;
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
    private final CheckBox useShadowCheckbox;
    private final Slider shadowOffsetSlider;
    private final NumberTextField shadowOffsetText;
    private final Slider shadowRadiusSlider;
    private final NumberTextField shadowRadiusText;
    private final Slider shadowSpreadSlider;
    private final NumberTextField shadowSpreadText;
    private final Button okButton;

    /**
     * Create the font options dialog.
     *
     * @param themePanel the ThemePanel to update as the values in this dialog
     * change.
     */
    public FontOptionsDialog(final ThemePanel themePanel) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("font.options.title"));
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

        shadowOffsetSlider = new Slider(0, 60, 0);
        shadowOffsetSlider.setShowTickMarks(false);

        shadowRadiusSlider = new Slider(0, 1000, 0);
        shadowRadiusSlider.setShowTickMarks(false);

        shadowSpreadSlider = new Slider(0, 1, 0);
        shadowSpreadSlider.setShowTickMarks(false);

        boldButton = new ToggleButton("", new ImageView(new Image("file:icons/bold.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(boldButton);
        italicButton = new ToggleButton("", new ImageView(new Image("file:icons/italic.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(italicButton);

        BorderPane root = new BorderPane();
        VBox controlRoot = new VBox(10);
        controlRoot.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("translation.font.text") + ":"));
        HBox fontBox = new HBox(10);
        fontBox.getChildren().add(fontSelection);
        fontBox.getChildren().add(fontExpandButton);
        controlRoot.getChildren().add(fontBox);
        HBox fontButtonBox = new HBox(10);
        fontButtonBox.getChildren().add(boldButton);
        fontButtonBox.getChildren().add(italicButton);
        fontButtonBox.getChildren().add(fontColor);
        controlRoot.getChildren().add(fontButtonBox);
        controlRoot.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("shadow.text") + ":"));
        GridPane shadowPane = new GridPane();
        shadowPane.setHgap(10);
        shadowPane.setVgap(10);

        Label useShadowLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.shadow.label"));
        GridPane.setConstraints(useShadowLabel, 1, 1);
        shadowPane.getChildren().add(useShadowLabel);
        useShadowCheckbox = new CheckBox();
        useShadowCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                themePanel.updateTheme(false);
            }
        });
        GridPane.setConstraints(useShadowCheckbox, 2, 1);
        shadowPane.getChildren().add(useShadowCheckbox);
        useShadowLabel.setLabelFor(useShadowCheckbox);
        useShadowCheckbox.setSelected(true);

        Label shadowColorLabel = new Label(LabelGrabber.INSTANCE.getLabel("shadow.color.label"));
        GridPane.setConstraints(shadowColorLabel, 1, 2);
        shadowPane.getChildren().add(shadowColorLabel);
        shadowColor.valueProperty().addListener(new ChangeListener<Color>() {

            @Override
            public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                themePanel.updateTheme(false);
            }
        });
        shadowColorLabel.setLabelFor(shadowColor);
        GridPane.setConstraints(shadowColor, 2, 2);
        shadowPane.getChildren().add(shadowColor);

        Label shadowOffsetLabel = new Label(LabelGrabber.INSTANCE.getLabel("shadow.offset.label"));
        GridPane.setConstraints(shadowOffsetLabel, 1, 3);
        shadowPane.getChildren().add(shadowOffsetLabel);
        shadowOffsetSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                themePanel.updateTheme(false);
            }
        });
        shadowOffsetText = new NumberTextField();
        shadowOffsetText.setMaxWidth(50);
        Bindings.bindBidirectional(shadowOffsetText.textProperty(), shadowOffsetSlider.valueProperty(), new NumberStringConverter());
        HBox shadowOffsetBox = new HBox(5);
        shadowOffsetBox.getChildren().add(shadowOffsetSlider);
        shadowOffsetBox.getChildren().add(shadowOffsetText);

        shadowOffsetLabel.setLabelFor(shadowOffsetBox);
        GridPane.setConstraints(shadowOffsetBox, 2, 3);
        shadowPane.getChildren().add(shadowOffsetBox);

        Label shadowRadiusLabel = new Label(LabelGrabber.INSTANCE.getLabel("shadow.radius.label"));
        GridPane.setConstraints(shadowRadiusLabel, 1, 4);
        shadowPane.getChildren().add(shadowRadiusLabel);
        shadowRadiusSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                themePanel.updateTheme(false);
            }
        });
        shadowRadiusText = new NumberTextField();
        shadowRadiusText.setMaxWidth(50);
        Bindings.bindBidirectional(shadowRadiusText.textProperty(), shadowRadiusSlider.valueProperty(), new NumberStringConverter());
        HBox shadowRadiusBox = new HBox(5);
        shadowRadiusBox.getChildren().add(shadowRadiusSlider);
        shadowRadiusBox.getChildren().add(shadowRadiusText);
        
        shadowRadiusLabel.setLabelFor(shadowRadiusBox);
        GridPane.setConstraints(shadowRadiusBox, 2, 4);
        shadowPane.getChildren().add(shadowRadiusBox);

        Label shadowSpreadLabel = new Label(LabelGrabber.INSTANCE.getLabel("shadow.spread.label"));
        GridPane.setConstraints(shadowSpreadLabel, 1, 5);
        shadowPane.getChildren().add(shadowSpreadLabel);
        shadowSpreadSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                themePanel.updateTheme(false);
            }
        });
        shadowSpreadText = new NumberTextField();
        shadowSpreadText.setMaxWidth(50);
        Bindings.bindBidirectional(shadowSpreadText.textProperty(), shadowSpreadSlider.valueProperty(), new NumberStringConverter());
        HBox shadowSpreadBox = new HBox(5);
        shadowSpreadBox.getChildren().add(shadowSpreadSlider);
        shadowSpreadBox.getChildren().add(shadowSpreadText);
        
        shadowSpreadLabel.setLabelFor(shadowSpreadBox);
        GridPane.setConstraints(shadowSpreadBox, 2, 5);
        shadowPane.getChildren().add(shadowSpreadBox);

        controlRoot.getChildren().add(shadowPane);

        BorderPane.setMargin(controlRoot, new Insets(10));
        StackPane buttonPane = new StackPane();
        buttonPane.setAlignment(Pos.CENTER);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setOnAction((ActionEvent t) -> {
            hide();
        });
        okButton.setAlignment(Pos.CENTER);
        StackPane.setMargin(okButton, new Insets(10));
        buttonPane.getChildren().add(okButton);
        root.setCenter(controlRoot);
        root.setBottom(buttonPane);
        setScene(new Scene(root, 330, 350));
    }

    /**
     * Get the theme to display on this font options dialog.
     *
     * @param theme the theme to display.
     */
    public void setTheme(ThemeDTO theme) {
        fontSelection.getSelectionModel().select(theme.getTranslateFont().getFamily());
        fontColor.setValue(theme.getTranslateFontPaint());
        fontColor.fireEvent(new ActionEvent());
        boldButton.setSelected(theme.isTranslateBold());
        italicButton.setSelected(theme.isTranslateItalic());
        shadowColor.setValue(theme.getShadow().getColor());
        shadowColor.fireEvent(new ActionEvent());
        shadowOffsetSlider.setValue(theme.getShadow().getOffsetX());
        useShadowCheckbox.setSelected(theme.getShadow().getUse());
        shadowRadiusSlider.setValue(theme.getShadow().getRadius());
        shadowSpreadSlider.setValue(theme.getShadow().getSpread());
    }

    /**
     * Get the shadow options represented on this dialog.
     *
     * @return the shadow options.
     */
    public SerializableDropShadow getShadow() {
        return new SerializableDropShadow(shadowColor.getValue(), shadowOffsetSlider.getValue(), shadowOffsetSlider.getValue(), shadowRadiusSlider.getValue(), shadowSpreadSlider.getValue(), useShadowCheckbox.isSelected());
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

class NumberTextField extends TextField {

    @Override
    public void replaceText(int start, int end, String text) {
        if (validate(text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (validate(text)) {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text) {
        return ("".equals(text) || text.matches("[0-9]"));
    }
}
