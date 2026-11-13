/*
 * This file is part of Quelea, free projection software for churches.
 *
 * Copyright (C) 2012 Michael Berry
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
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.converter.NumberStringConverter;
import org.quelea.data.Background;
import org.quelea.data.ColourBackground;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.CardPane;
import org.quelea.windows.style.QueleaIcon;

/**
 * The theme editor above the theme preview: the background, the text (main
 * and translation, alignment) and the text shadow, each under its own
 * heading, all visible at once.
 * <p>
 * The background type is chosen with segmented buttons. They drive a combo
 * box that isn't shown, because the background classes fill in the form
 * through it (see {@link Background#setThemeForm}).
 *
 * @author Michael
 */
public class ThemeToolbar extends VBox {

    private static FontSelectionDialog fontSelectionDialog;

    private final ThemePanel themePanel;
    private final TextStyleRow mainText;
    private final TextStyleRow translationText;
    private final ToggleButton leftAlignButton;
    private final ToggleButton centreAlignButton;
    private final ToggleButton rightAlignButton;
    private final ComboBox<String> backTypeSelection = new ComboBox<>();
    private final ToggleGroup backTypeGroup = new ToggleGroup();
    private final TextField backgroundImageLocation;
    private final TextField backgroundVidLocation;
    private final ColorPicker backgroundColorPicker;
    private final Slider vidHueSlider;
    private final CheckBox vidStretchCheckbox;
    private final ToggleButton useShadowSwitch;
    private final ColorPicker shadowColor;
    private final Slider shadowOffsetSlider;
    private final Slider shadowRadiusSlider;
    private final Slider shadowSpreadSlider;

    /**
     * Create the theme editor.
     *
     * @param themePanel the theme panel that this editor sits on.
     */
    public ThemeToolbar(final ThemePanel themePanel) {
        Utils.checkFXThread();
        this.themePanel = themePanel;
        if (fontSelectionDialog == null) {
            fontSelectionDialog = new FontSelectionDialog();
        }
        getStyleClass().add("theme-editor");

        // ---- Background ---------------------------------------------------
        String colourLabel = LabelGrabber.INSTANCE.getLabel("color.theme.label");
        String imageLabel = LabelGrabber.INSTANCE.getLabel("image.theme.label");
        String videoLabel = LabelGrabber.INSTANCE.getLabel("video.theme.label");
        backTypeSelection.getItems().addAll(colourLabel, imageLabel, videoLabel);
        HBox backTypeButtons = segmented(
                typeButton(colourLabel, QueleaIcon.THEME),
                typeButton(imageLabel, QueleaIcon.IMAGES),
                typeButton(videoLabel, QueleaIcon.VIDEO));
        keepOneSelected(backTypeGroup);
        backTypeGroup.selectedToggleProperty().addListener((ov, oldToggle, toggle) -> {
            if (toggle != null) {
                backTypeSelection.getSelectionModel().select((String) toggle.getUserData());
            }
        });

        CardPane<Node> backgroundCentre = new CardPane<>();
        backgroundColorPicker = new ColorPicker(Color.BLACK);
        backgroundColorPicker.getStyleClass().add("theme-colour");
        backgroundColorPicker.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("background.colour.label")));
        backgroundColorPicker.valueProperty().addListener((ov, t, t1) -> themePanel.updateTheme(true));
        backgroundCentre.add(row(backgroundColorPicker), "colour");

        backgroundImageLocation = new TextField();
        backgroundImageLocation.setEditable(false);
        backgroundImageLocation.setPrefColumnCount(16);
        backgroundImageLocation.textProperty().addListener((ov, t, t1) -> {
            themePanel.updateTheme(false);
            checkConfirmButton();
        });
        backgroundCentre.add(row(backgroundImageLocation, new ImageButton(backgroundImageLocation)), "image");

        backgroundVidLocation = new TextField();
        backgroundVidLocation.setEditable(false);
        backgroundVidLocation.setPrefColumnCount(12);
        backgroundVidLocation.textProperty().addListener((ov, t, t1) -> {
            themePanel.updateTheme(false);
            checkConfirmButton();
        });
        Label vidHueLabel = fixedLabel("video.hue.label");
        vidHueSlider = new Slider(0, 1, 0);
        vidHueSlider.setMajorTickUnit(0.001);
        vidHueSlider.setPrefWidth(90);
        vidHueSlider.valueProperty().addListener((ov, t, t1) -> themePanel.updateTheme(false));
        vidHueLabel.setLabelFor(vidHueSlider);
        vidStretchCheckbox = new CheckBox(LabelGrabber.INSTANCE.getLabel("stretch.video.label"));
        vidStretchCheckbox.selectedProperty().addListener((ov, t, t1) -> themePanel.updateTheme(false));
        backgroundCentre.add(row(backgroundVidLocation, new VideoButton(backgroundVidLocation), gap(), vidHueLabel, vidHueSlider, gap(), vidStretchCheckbox), "video");

        backTypeSelection.getSelectionModel().selectedItemProperty().addListener((ov, oldType, type) -> {
            if (type == null) {
                return;
            }
            backgroundCentre.show(type.equals(imageLabel) ? "image" : type.equals(videoLabel) ? "video" : "colour");
            for (var toggle : backTypeGroup.getToggles()) {
                if (type.equals(toggle.getUserData())) {
                    backTypeGroup.selectToggle(toggle);
                }
            }
            themePanel.updateTheme(false);
            checkConfirmButton();
        });

        HBox backgroundRow = new HBox(12, backTypeButtons, backgroundCentre);
        backgroundRow.setAlignment(Pos.CENTER_LEFT);
        VBox backgroundSection = new VBox(6, heading("background.text"), backgroundRow);

        // ---- Text: alignment, main text, translation ----------------------
        ToggleGroup alignGroup = new ToggleGroup();
        leftAlignButton = iconToggle(QueleaIcon.ALIGN_LEFT, "theme.align.left.tooltip");
        centreAlignButton = iconToggle(QueleaIcon.ALIGN_CENTER, "theme.align.centre.tooltip");
        rightAlignButton = iconToggle(QueleaIcon.ALIGN_RIGHT, "theme.align.right.tooltip");
        for (ToggleButton button : List.of(leftAlignButton, centreAlignButton, rightAlignButton)) {
            button.setToggleGroup(alignGroup);
            button.setOnAction(e -> themePanel.updateTheme(false));
        }
        keepOneSelected(alignGroup);
        centreAlignButton.setSelected(true);

        mainText = new TextStyleRow("font.colour.label");
        translationText = new TextStyleRow("translation.font.colour.label");

        GridPane textGrid = new GridPane();
        textGrid.getStyleClass().add("theme-grid");
        textGrid.getColumnConstraints().addAll(growColumn(), new ColumnConstraints(), new ColumnConstraints());
        Label alignLabel = heading("stage.line.alignment");
        textGrid.add(alignLabel, 0, 0);
        HBox alignButtons = segmented(leftAlignButton, centreAlignButton, rightAlignButton);
        textGrid.add(alignButtons, 1, 0, 2, 1);
        GridPane.setHalignment(alignButtons, HPos.RIGHT);
        textGrid.add(heading("theme.main.text.label"), 0, 1);
        textGrid.add(caption("color.theme.label"), 1, 1);
        mainText.addTo(textGrid, 2);
        textGrid.add(heading("translation.font.text"), 0, 3);
        textGrid.add(caption("color.theme.label"), 1, 3);
        translationText.addTo(textGrid, 4);

        // ---- Text shadow ----------------------------------------------------
        useShadowSwitch = new ToggleButton();
        useShadowSwitch.getStyleClass().add("theme-switch");
        Region thumb = new Region();
        thumb.getStyleClass().add("theme-switch-thumb");
        useShadowSwitch.setGraphic(thumb);
        useShadowSwitch.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        useShadowSwitch.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("use.shadow.label")));
        useShadowSwitch.setSelected(true);
        useShadowSwitch.selectedProperty().addListener((ov, t, t1) -> themePanel.updateTheme(false));

        shadowColor = new ColorPicker(Color.GRAY);
        shadowColor.getStyleClass().add("theme-colour");
        shadowColor.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("shadow.color.label")));
        shadowColor.valueProperty().addListener((ov, t, t1) -> themePanel.updateTheme(false));
        shadowOffsetSlider = new Slider(0, 60, 0);
        shadowRadiusSlider = new Slider(0, 1000, 0);
        shadowSpreadSlider = new Slider(0, 1, 0);

        GridPane shadowGrid = new GridPane();
        shadowGrid.getStyleClass().add("theme-grid");
        shadowGrid.getColumnConstraints().addAll(new ColumnConstraints(), growColumn(), new ColumnConstraints());
        shadowGrid.add(heading("shadow.text"), 0, 0, 2, 1);
        shadowGrid.add(useShadowSwitch, 2, 0);
        GridPane.setHalignment(useShadowSwitch, HPos.RIGHT);
        Label shadowColourLabel = fixedLabel("shadow.color.label");
        shadowColourLabel.setLabelFor(shadowColor);
        shadowGrid.add(shadowColourLabel, 0, 1);
        shadowGrid.add(shadowColor, 1, 1);
        addSliderRow(shadowGrid, 2, "shadow.offset.label", shadowOffsetSlider);
        addSliderRow(shadowGrid, 3, "shadow.radius.label", shadowRadiusSlider);
        addSliderRow(shadowGrid, 4, "shadow.spread.label", shadowSpreadSlider);
        for (Node node : shadowGrid.getChildren()) {
            if (node != useShadowSwitch && GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0) {
                node.disableProperty().bind(useShadowSwitch.selectedProperty().not());
            }
        }

        Separator columnSeparator = new Separator(Orientation.VERTICAL);
        HBox textAndShadow = new HBox(16, textGrid, columnSeparator, shadowGrid);
        HBox.setHgrow(textGrid, Priority.ALWAYS);
        HBox.setHgrow(shadowGrid, Priority.ALWAYS);
        textGrid.setMaxWidth(Double.MAX_VALUE);
        shadowGrid.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(backgroundSection, new Separator(), textAndShadow);
    }

    /** One line of text settings: font (+ more fonts), colour, bold, italic. */
    private final class TextStyleRow {

        private final ComboBox<String> font = new ComboBox<>();
        private final ColorPicker colour = new ColorPicker(Color.WHITE);
        private final ToggleButton bold = iconToggle(QueleaIcon.BOLD, "theme.bold.tooltip");
        private final ToggleButton italic = iconToggle(QueleaIcon.ITALIC, "theme.italic.tooltip");
        private final HBox fontBox;

        TextStyleRow(String colourTooltipKey) {
            font.setMaxWidth(Double.MAX_VALUE);
            font.setPrefWidth(150);
            font.setMinWidth(80);
            font.getItems().setAll(fontSelectionDialog.getChosenFonts());
            Collections.sort(font.getItems());
            font.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> themePanel.updateTheme(false));
            Button moreFonts = new Button("", QueleaIcon.ADD.create());
            Utils.setToolbarButtonStyle(moreFonts);
            moreFonts.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("more.fonts.label") + "..."));
            moreFonts.setOnAction(e -> {
                fontSelectionDialog.showAndWait();
                mainText.refreshFonts();
                translationText.refreshFonts();
            });
            fontBox = new HBox(2, font, moreFonts);
            fontBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(font, Priority.ALWAYS);
            colour.getStyleClass().add("theme-colour");
            colour.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel(colourTooltipKey)));
            colour.setOnAction(e -> themePanel.updateTheme(false));
            bold.setOnAction(e -> themePanel.updateTheme(false));
            italic.setOnAction(e -> themePanel.updateTheme(false));
        }

        void addTo(GridPane grid, int row) {
            grid.add(fontBox, 0, row);
            grid.add(colour, 1, row);
            grid.add(segmented(bold, italic), 2, row);
        }

        void refreshFonts() {
            String selected = font.getSelectionModel().getSelectedItem();
            font.getItems().setAll(fontSelectionDialog.getChosenFonts());
            Collections.sort(font.getItems());
            font.getSelectionModel().select(selected);
        }

        void set(String family, Color paint, boolean isBold, boolean isItalic) {
            font.getSelectionModel().select(family);
            colour.setValue(paint);
            colour.fireEvent(new ActionEvent());
            bold.setSelected(isBold);
            italic.setSelected(isItalic);
        }

        Font toFont() {
            return Font.font(font.getSelectionModel().getSelectedItem(),
                    bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                    italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    QueleaProperties.get().getMaxFontSize());
        }
    }

    // ---- Small building blocks ------------------------------------------------

    private static Label heading(String labelKey) {
        Label label = fixedLabel(labelKey);
        label.getStyleClass().add("theme-heading");
        return label;
    }

    private static Label caption(String labelKey) {
        Label label = fixedLabel(labelKey);
        label.getStyleClass().add("muted-label");
        return label;
    }

    /** A label that is never cut short; the font lists and sliders shrink instead. */
    private static Label fixedLabel(String labelKey) {
        Label label = new Label(LabelGrabber.INSTANCE.getLabel(labelKey));
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    private static HBox row(Node... nodes) {
        HBox box = new HBox(6, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Region gap() {
        Region gap = new Region();
        gap.setMinWidth(6);
        return gap;
    }

    private static ColumnConstraints growColumn() {
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    /** Buttons joined into one control, e.g. [Colour | Image | Video]. */
    private static HBox segmented(ToggleButton... buttons) {
        HBox box = new HBox();
        box.getStyleClass().add("segmented");
        box.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setMinWidth(Region.USE_PREF_SIZE);
            buttons[i].getStyleClass().add(buttons.length == 1 ? "only-pill"
                    : i == 0 ? "left-pill" : i == buttons.length - 1 ? "right-pill" : "center-pill");
            box.getChildren().add(buttons[i]);
        }
        return box;
    }

    private ToggleButton typeButton(String label, QueleaIcon icon) {
        ToggleButton button = new ToggleButton(label, icon.create());
        button.setUserData(label);
        button.setToggleGroup(backTypeGroup);
        return button;
    }

    private static ToggleButton iconToggle(QueleaIcon icon, String tooltipKey) {
        ToggleButton button = new ToggleButton("", icon.create());
        button.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel(tooltipKey)));
        return button;
    }

    /** Clicking the selected button of a group must not leave none selected. */
    private static void keepOneSelected(ToggleGroup group) {
        group.selectedToggleProperty().addListener((ov, oldToggle, toggle) -> {
            if (toggle == null && oldToggle != null) {
                group.selectToggle(oldToggle);
            }
        });
    }

    private void addSliderRow(GridPane grid, int row, String labelKey, Slider slider) {
        Label label = fixedLabel(labelKey);
        label.setLabelFor(slider);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setMinWidth(60);
        slider.valueProperty().addListener((ov, t, t1) -> themePanel.updateTheme(false));
        NumberTextField value = new NumberTextField();
        value.getStyleClass().add("theme-number");
        Bindings.bindBidirectional(value.textProperty(), slider.valueProperty(), new NumberStringConverter());
        grid.add(label, 0, row);
        grid.add(slider, 1, row);
        grid.add(value, 2, row);
    }

    // ---- State ----------------------------------------------------------------

    /**
     * Enable / disable the confirm button on the dialog based on the state of
     * the background field.
     */
    private void checkConfirmButton() {
        if (themePanel.getConfirmButton() == null || backTypeSelection.getValue() == null) {
            return;
        }
        String type = backTypeSelection.getValue();
        if (type.equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            themePanel.getConfirmButton().setDisable(backgroundImageLocation.getText().trim().isEmpty());
        } else if (type.equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
            themePanel.getConfirmButton().setDisable(backgroundVidLocation.getText().trim().isEmpty());
        } else {
            themePanel.getConfirmButton().setDisable(false);
        }
    }

    /**
     * Set the theme represented by this editor.
     *
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {
        Utils.checkFXThread();
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        mainText.set(theme.getFont().getFamily(), theme.getFontPaint(), theme.isBold(), theme.isItalic());
        translationText.set(theme.getTranslateFont().getFamily(), theme.getTranslateFontPaint(), theme.isTranslateBold(), theme.isTranslateItalic());
        int align = theme.getTextAlignment();
        if (align == -1) {
            leftAlignButton.setSelected(true);
        } else if (align == 1) {
            rightAlignButton.setSelected(true);
        } else {
            centreAlignButton.setSelected(true);
        }
        SerializableDropShadow shadow = theme.getShadow();
        shadowColor.setValue(shadow.getColor());
        shadowOffsetSlider.setValue(shadow.getOffsetX());
        shadowRadiusSlider.setValue(shadow.getRadius());
        shadowSpreadSlider.setValue(shadow.getSpread());
        useShadowSwitch.setSelected(shadow.getUse());
        theme.getBackground().setThemeForm(backgroundColorPicker, backTypeSelection, backgroundImageLocation, backgroundVidLocation, vidHueSlider, vidStretchCheckbox);
    }

    private int getAlignmentVal() {
        if (leftAlignButton.isSelected()) {
            return -1;
        }
        return rightAlignButton.isSelected() ? 1 : 0;
    }

    /**
     * Get the theme represented by this editor.
     *
     * @return the theme.
     */
    public ThemeDTO getTheme() {
        Utils.checkFXThread();
        String type = backTypeSelection.getValue();
        if (type == null) {
            return ThemeDTO.DEFAULT_THEME;
        }
        Background background = new ColourBackground(Color.BLACK);
        if (type.equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
            background = new ColourBackground(backgroundColorPicker.getValue());
        } else if (type.equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            String text = backgroundImageLocation.getText();
            if (!text.isEmpty()) {
                background = new ImageBackground(text);
            }
        } else if (type.equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
            String text = backgroundVidLocation.getText();
            if (!text.isEmpty()) {
                background = new VideoBackground(text, vidHueSlider.getValue(), vidStretchCheckbox.isSelected());
            }
        }
        SerializableDropShadow shadow = new SerializableDropShadow(shadowColor.getValue(), shadowOffsetSlider.getValue(),
                shadowOffsetSlider.getValue(), shadowRadiusSlider.getValue(), shadowSpreadSlider.getValue(), useShadowSwitch.isSelected());
        return new ThemeDTO(new SerializableFont(mainText.toFont()), mainText.colour.getValue(),
                new SerializableFont(translationText.toFont()), translationText.colour.getValue(),
                background, shadow, mainText.bold.isSelected(), mainText.italic.isSelected(),
                translationText.bold.isSelected(), translationText.italic.isSelected(), -1, getAlignmentVal());
    }
}
