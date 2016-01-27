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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.quelea.data.Background;
import org.quelea.data.ColourBackground;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.CardPane;

/**
 * The toolbar that sits atop of the theme dialog, and gives the user control
 * over the theme currently in use.
 * <p>
 * @author Michael
 */
public class ThemeToolbar extends HBox {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ComboBox<String> fontSelection;
    private final Button fontExpandButton;
    private final Button moreOptionsButton;
    private final FontOptionsDialog moreFontOptionsDialog;
    private final ToggleButton boldButton;
    private final ToggleButton italicButton;
    private final ToggleButton leftAlignButton;
    private final ToggleButton centreAlignButton;
    private final ToggleButton rightAlignButton;
    private final ColorPicker fontColor;
    private final ComboBox<String> backTypeSelection;
    private final TextField backgroundImageLocation;
    private final TextField backgroundVidLocation;
    private final ColorPicker backgroundColorPicker;
    private final Slider vidHueSlider;
    private final CheckBox vidStretchCheckbox;
    private final ThemePanel themePanel;
    private static FontSelectionDialog fontSelectionDialog;

    /**
     * Create a new theme toolbar.
     * <p>
     * @param themePanel the theme panel that this toolbar sits on.
     */
    public ThemeToolbar(final ThemePanel themePanel) {
        Utils.checkFXThread();
        this.themePanel = themePanel;
        moreFontOptionsDialog = new FontOptionsDialog(themePanel);
        setPadding(new Insets(5));
        setStyle("-fx-background-color:#dddddd;");
        VBox topLevelFontBox = new VBox(10);
        topLevelFontBox.setStyle("-fx-border-color: bbbbbb;");
        topLevelFontBox.setPadding(new Insets(10));
        getChildren().add(topLevelFontBox);

        HBox fontTop = new HBox(3);
        if (fontSelectionDialog == null) {
            fontSelectionDialog = new FontSelectionDialog();
        }
        fontSelection = new ComboBox<>();
        fontSelection.setMaxWidth(Integer.MAX_VALUE);
        fontSelection.getItems().addAll(fontSelectionDialog.getChosenFonts());
        Collections.sort(fontSelection.getItems());
        HBox.setHgrow(fontSelection, Priority.ALWAYS);
        fontSelection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(false);
            }
        });
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
        fontTop.getChildren().add(fontSelection);
        fontTop.getChildren().add(fontExpandButton);
        topLevelFontBox.getChildren().add(fontTop);

        HBox fontMid = new HBox();
        boldButton = new ToggleButton("", new ImageView(new Image("file:icons/bold.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(boldButton);
        boldButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false);
            }
        });
        italicButton = new ToggleButton("", new ImageView(new Image("file:icons/italic.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(italicButton);
        italicButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false);
            }
        });
        ToggleGroup alignGroup = new ToggleGroup();
        leftAlignButton = new ToggleButton("", new ImageView(new Image("file:icons/leftalign.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(leftAlignButton);
        leftAlignButton.setToggleGroup(alignGroup);
        leftAlignButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false);
            }
        });
        centreAlignButton = new ToggleButton("", new ImageView(new Image("file:icons/centrealign.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(centreAlignButton);
        centreAlignButton.setToggleGroup(alignGroup);
        centreAlignButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false);
            }
        });
        rightAlignButton = new ToggleButton("", new ImageView(new Image("file:icons/rightalign.png", 15, 15, false, true)));
        Utils.setToolbarButtonStyle(rightAlignButton);
        rightAlignButton.setToggleGroup(alignGroup);
        rightAlignButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false);
            }
        });
        centreAlignButton.setSelected(true);
        fontColor = new ColorPicker(Color.WHITE);
        fontColor.setStyle("-fx-color-label-visible: false ;");
        fontColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                themePanel.updateTheme(false);
            }
        });
        fontMid.getChildren().add(boldButton);
        fontMid.getChildren().add(italicButton);
        fontMid.getChildren().add(leftAlignButton);
        fontMid.getChildren().add(centreAlignButton);
        fontMid.getChildren().add(rightAlignButton);
        fontMid.getChildren().add(fontColor);
        topLevelFontBox.getChildren().add(fontMid);

        StackPane fontBottom = new StackPane();
        Text fontText = new Text(LabelGrabber.INSTANCE.getLabel("font.theme.label"));
        fontText.setFill(Color.GRAY);
        fontBottom.getChildren().add(fontText);
        moreOptionsButton = new Button("...");
        Utils.setToolbarButtonStyle(moreOptionsButton);
        moreOptionsButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("more.font.options.label") + "..."));
        moreOptionsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                moreFontOptionsDialog.showAndWait();
                themePanel.updateTheme(false);
            }
        });
        StackPane.setAlignment(moreOptionsButton, Pos.BOTTOM_RIGHT);
        fontBottom.getChildren().add(moreOptionsButton);
        topLevelFontBox.getChildren().add(fontBottom);

        //-------
        Region spacer = new Region();
        spacer.setMaxWidth(20);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        //-------

        VBox topLevelBackBox = new VBox(10);
        topLevelBackBox.setStyle("-fx-border-color: bbbbbb;");
        topLevelBackBox.setPadding(new Insets(10));
        getChildren().add(topLevelBackBox);

        HBox backTop = new HBox(15);
        backTypeSelection = new ComboBox<>();
        backTypeSelection.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(false);
                checkConfirmButton();
            }
        });
        backTypeSelection.getItems().add(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
        backTypeSelection.getItems().add(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backTypeSelection.getItems().add(LabelGrabber.INSTANCE.getLabel("video.theme.label"));
        backTop.getChildren().add(backTypeSelection);
        
        vidStretchCheckbox = new CheckBox(LabelGrabber.INSTANCE.getLabel("stretch.video.label"));
        vidStretchCheckbox.setStyle("-fx-text-fill:#666666");
        HBox.setMargin(vidStretchCheckbox, new Insets(2,0,0,0));
        vidStretchCheckbox.setAlignment(Pos.CENTER_RIGHT);
        vidStretchCheckbox.setVisible(false);
        Region stretchSpacer = new Region();
        stretchSpacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(stretchSpacer, Priority.ALWAYS);
        backTop.getChildren().add(stretchSpacer);
        backTop.getChildren().add(vidStretchCheckbox);
        topLevelBackBox.getChildren().add(backTop);

        final CardPane<Node> backgroundCentre = new CardPane<>();
        final HBox colourPanel = new HBox();
        backgroundColorPicker = new ColorPicker(Color.BLACK);
        backgroundColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                themePanel.updateTheme(true);
            }
        });
        colourPanel.getChildren().add(backgroundColorPicker);
        backgroundCentre.add(colourPanel, "colour");
        final HBox imagePanel = new HBox();
        backgroundImageLocation = new TextField();
        backgroundImageLocation.setEditable(false);
        backgroundImageLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(false);
                checkConfirmButton();
            }
        });
        Button backgroundImageSelectButton = new ImageButton(backgroundImageLocation, themePanel.getCanvas());
        imagePanel.getChildren().add(backgroundImageLocation);
        imagePanel.getChildren().add(backgroundImageSelectButton);
        backgroundCentre.add(imagePanel, "image");
        final VBox vidPanel = new VBox(5);
        final HBox vidLocationPanel = new HBox(5);
        backgroundVidLocation = new TextField();
        backgroundVidLocation.setMaxWidth(130);
        backgroundVidLocation.setEditable(false);
        backgroundVidLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(false);
                checkConfirmButton();
            }
        });
        Button backgroundVidSelectButton = new VideoButton(backgroundVidLocation, themePanel.getCanvas());
        vidLocationPanel.getChildren().add(backgroundVidLocation);
        vidLocationPanel.getChildren().add(backgroundVidSelectButton);
        vidPanel.getChildren().add(vidLocationPanel);
        Region spacer2 = new Region();
        spacer2.setPrefWidth(5);
        vidLocationPanel.getChildren().add(spacer2);
        Label vidHueLabel = new Label(LabelGrabber.INSTANCE.getLabel("video.hue.label"));
        vidHueLabel.setStyle("-fx-text-fill:#666666");
        vidHueLabel.setMaxHeight(Double.MAX_VALUE);
        vidHueLabel.setAlignment(Pos.CENTER);
        vidLocationPanel.getChildren().add(vidHueLabel);
        vidHueSlider = new Slider(0, 1, 0);
        vidHueSlider.setMajorTickUnit(0.001);
        vidHueSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                themePanel.updateTheme(false);
            }
        });
        vidHueSlider.setPrefWidth(70);
        StackPane sliderStack = new StackPane();
        sliderStack.setAlignment(Pos.CENTER);
        sliderStack.getChildren().add(vidHueSlider);
        vidHueLabel.setLabelFor(vidHueSlider);
        vidLocationPanel.getChildren().add(sliderStack);
        backgroundCentre.add(vidPanel, "video");

        backTypeSelection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
                    backgroundCentre.show("colour");
                    vidStretchCheckbox.setVisible(false);
                } else if (backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
                    backgroundCentre.show("image");
                    vidStretchCheckbox.setVisible(false);
                } else if (backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
                    backgroundCentre.show("video");
                    vidStretchCheckbox.setVisible(true);
                } else {
                    throw new AssertionError("Bug - " + backTypeSelection.getSelectionModel().getSelectedItem() + " is an unknown selection value");
                }
            }
        });
        topLevelBackBox.getChildren().add(backgroundCentre);

        StackPane backBottom = new StackPane();
        Text backText = new Text(LabelGrabber.INSTANCE.getLabel("background.text"));
        backText.setFill(Color.GRAY);
        backBottom.getChildren().add(backText);
        topLevelBackBox.getChildren().add(backBottom);
    }

    /**
     * Enable / disable the confirm button on the dialog based on the state of
     * the background field.
     */
    private void checkConfirmButton() {
        if (themePanel.getConfirmButton() != null) {
            if (backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
                themePanel.getConfirmButton().setDisable(false);
            } else if (backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
                themePanel.getConfirmButton().setDisable(backgroundImageLocation.getText().trim().isEmpty());
            } else if (backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
                themePanel.getConfirmButton().setDisable(backgroundVidLocation.getText().trim().isEmpty());
            }
        }
    }

    /**
     * Set the theme represented by this toolbar.
     * <p>
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {
        Utils.checkFXThread();
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        Font font = theme.getFont();
        fontSelection.getSelectionModel().select(font.getFamily());
        fontColor.setValue(theme.getFontPaint());
        fontColor.fireEvent(new ActionEvent());
        boldButton.setSelected(theme.isBold());
        italicButton.setSelected(theme.isItalic());
        int align = theme.getTextAlignment();
        if (align == -1) {
            leftAlignButton.setSelected(true);
        } else if (align == 1) {
            rightAlignButton.setSelected(true);
        } else {
            centreAlignButton.setSelected(true);
        }
        moreFontOptionsDialog.setTheme(theme);
        Background background = theme.getBackground();
        background.setThemeForm(backgroundColorPicker, backTypeSelection, backgroundImageLocation, backgroundVidLocation, vidHueSlider, vidStretchCheckbox);
    }

    private int getAlignmentVal() {
        int alignment = 0;
        if (leftAlignButton.isSelected()) {
            alignment = -1;
        } else if (rightAlignButton.isSelected()) {
            alignment = 1;
        }
        return alignment;
    }

    /**
     * Get the theme represented by this toolbar.
     * <p>
     * @return the theme.
     */
    public ThemeDTO getTheme() {
        Utils.checkFXThread();
        Font font = Font.font(fontSelection.getSelectionModel().getSelectedItem(),
                boldButton.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                italicButton.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        
        LOGGER.log(Level.INFO, "Selected font theme \"{0}\", font family is \"{1}\"", new Object[]{fontSelection.getSelectionModel().getSelectedItem(), font.getFamily()});

        Background background = new ColourBackground(Color.BLACK);
        if (backTypeSelection.getSelectionModel().getSelectedItem() == null) {
            return ThemeDTO.DEFAULT_THEME;
        }
        if (backTypeSelection.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
            background = new ColourBackground(backgroundColorPicker.getValue());
        } else if (backTypeSelection.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            String text = backgroundImageLocation.getText();
            if (!text.isEmpty()) {
                background = new ImageBackground(text);
            }
        } else if (backTypeSelection.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
            String text = backgroundVidLocation.getText();
            if (!text.isEmpty()) {
                background = new VideoBackground(text, vidHueSlider.getValue(), vidStretchCheckbox.isSelected());
            }
        } else {
            throw new AssertionError("Bug - " + backTypeSelection.getSelectionModel().getSelectedItem() + " is an unknown selection value");
        }
        final SerializableDropShadow shadow = moreFontOptionsDialog.getShadow();
        ThemeDTO resultTheme = new ThemeDTO(new SerializableFont(font), fontColor.getValue(), moreFontOptionsDialog.getTranslateFont(), moreFontOptionsDialog.getTranslateColour(),
                background, shadow, boldButton.isSelected(), italicButton.isSelected(), moreFontOptionsDialog.isTranslateBold(), moreFontOptionsDialog.isTranslateItalic(), -1, getAlignmentVal());
        return resultTheme;
    }

}
