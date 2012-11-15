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
package org.quelea.windows.newsong;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.javafx.dialog.Dialog;
import org.quelea.data.Background;
import org.quelea.data.ColourBackground;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.LyricCanvas;
import org.quelea.windows.main.widgets.CardPane;

/**
 * The panel where the user chooses what visual theme a song should have.
 * <p/>
 * @author Michael
 */
public class ThemePanel extends BorderPane {

    private static final double THRESHOLD = 0.1;
    public static final String[] SAMPLE_LYRICS = {"Amazing Grace how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private HBox fontToolbar;
    private HBox backgroundPanel;

    private ComboBox<String> fontSelection;
    private ColorPicker fontColorPicker;
    private ColorPicker backgroundColorPicker;
    private ComboBox<String> backgroundTypeSelect;
    
    //Text shadow options
    private VBox shadowPanel;
    private ColorPicker shadowColorPicker;
    private TextField shadowOffsetX;
    private TextField shadowOffsetY;
    private TextField shadowRadius;
    private TextField shadowWidth;
    
    private TextField backgroundImgLocation;
    private TextField backgroundVidLocation;
    private ToggleButton boldButton;
    private ToggleButton italicButton;
    private final LyricCanvas canvas;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        canvas = new LyricCanvas(false, false);
        canvas.setText(SAMPLE_LYRICS, null, false);
        setCenter(canvas);
        VBox toolbarPanel = new VBox();
        setupFontToolbar();
        toolbarPanel.getChildren().add(fontToolbar);
        setupBackgroundToolbar();
        toolbarPanel.getChildren().add(backgroundPanel);
        setupShadowPanel();
        toolbarPanel.getChildren().add(shadowPanel);
        setTop(toolbarPanel);
        updateTheme(false);
    }

    /**
     * Setup the background toolbar.
     */
    private void setupBackgroundToolbar() {
        backgroundPanel = new HBox();
        final CardPane backgroundChooserPanel = new CardPane();

        backgroundTypeSelect = new ComboBox<>();
        backgroundTypeSelect.getItems().add(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
        backgroundTypeSelect.getItems().add(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backgroundTypeSelect.getItems().add(LabelGrabber.INSTANCE.getLabel("video.theme.label"));
        backgroundTypeSelect.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(false);
            }
        });
        backgroundPanel.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("background.theme.label") + ":"));
        backgroundPanel.getChildren().add(backgroundTypeSelect);
        backgroundPanel.getChildren().add(backgroundChooserPanel);

        final HBox colourPanel = new HBox();

        backgroundColorPicker = new ColorPicker(Color.BLACK);
        colourPanel.getChildren().add(backgroundColorPicker);
        backgroundColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                updateTheme(true);
            }
        });

        final HBox imagePanel = new HBox();
        backgroundImgLocation = new TextField();
        backgroundImgLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true);
            }
        });
        backgroundImgLocation.setEditable(false);
        imagePanel.getChildren().add(backgroundImgLocation);
        imagePanel.getChildren().add(new ImageButton(backgroundImgLocation, canvas));

        final HBox videoPanel = new HBox();
        backgroundVidLocation = new TextField();
        backgroundVidLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true);
            }
        });
        backgroundVidLocation.setEditable(false);
        videoPanel.getChildren().add(backgroundVidLocation);
        videoPanel.getChildren().add(new VideoButton(backgroundVidLocation, canvas));

        backgroundChooserPanel.add(colourPanel, "colour");
        backgroundChooserPanel.add(imagePanel, "image");
        backgroundChooserPanel.add(videoPanel, "video");

        backgroundTypeSelect.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                if (backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
                    backgroundChooserPanel.show("colour");
                } else if (backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
                    backgroundChooserPanel.show("image");
                } else if (backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
                    backgroundChooserPanel.show("video");
                } else {
                    throw new AssertionError("Bug - " + backgroundTypeSelect.getSelectionModel().getSelectedItem() + " is an unknown selection value");
                }
            }
        });
    }

    /**
     *
     */
    private void setupShadowPanel() {
        shadowPanel = new VBox();
        final HBox confFirstLine = new HBox();
        confFirstLine.getChildren().add(new Label("shadow type"));

        shadowColorPicker = new ColorPicker(Color.BLACK);
        final HBox colourPanel = new HBox();
        colourPanel.getChildren().add(new Label("shadow color"));
        colourPanel.getChildren().add(shadowColorPicker);
        confFirstLine.getChildren().add(colourPanel);
        shadowColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                updateTheme(true);
            }
        });
        shadowPanel.getChildren().add(confFirstLine);
        final HBox confSecondLine = new HBox();
        confSecondLine.getChildren().add(new Label("shadow X"));
        shadowOffsetX = new TextField();
        confSecondLine.getChildren().add(shadowOffsetX);
        shadowOffsetX.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true);
            }
        });

        confSecondLine.getChildren().add(new Label("shadow Y"));
        shadowOffsetY = new TextField();
        confSecondLine.getChildren().add(shadowOffsetY);
        shadowOffsetY.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true);
            }
        });
        shadowPanel.getChildren().add(confSecondLine);
    }

    /**
     * Setup the font toolbar.
     */
    private void setupFontToolbar() {
        fontToolbar = new HBox();
        fontToolbar.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("font.theme.label") + ":"));
        fontSelection = new ComboBox<>();
        for (String font : Utils.getAllFonts()) {
            fontSelection.itemsProperty().get().add(font);
        }
        fontSelection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(false);
            }
        });
        fontToolbar.getChildren().add(fontSelection);
        boldButton = new ToggleButton("", new ImageView(new Image("file:icons/bold.png")));
        boldButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                updateTheme(false);
            }
        });
        fontToolbar.getChildren().add(boldButton);
        italicButton = new ToggleButton("", new ImageView(new Image("file:icons/italic.png")));
        italicButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                updateTheme(false);
            }
        });
        fontToolbar.getChildren().add(italicButton);
        fontColorPicker = new ColorPicker(Color.WHITE);
        fontColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                updateTheme(true);
            }
        });
        fontToolbar.getChildren().add(fontColorPicker);

    }

    /**
     * Update the canvas with the current theme.
     */
    private void updateTheme(boolean warning) {
        final ThemeDTO theme = getTheme();
        if (warning && theme.getBackground() instanceof ColourBackground) {
            checkAccessibility((Color) theme.getFontPaint(), ((ColourBackground) theme.getBackground()).getColour());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                canvas.setTheme(theme);
            }
        });
    }

    /**
     * Set the current theme to represent in this panel.
     * <p/>
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        Font font = theme.getFont();
        fontSelection.getSelectionModel().select(font.getFamily());
        fontColorPicker.setValue((Color) theme.getFontPaint());
        Background background = theme.getBackground();
        background.setThemeForm(backgroundColorPicker, backgroundTypeSelect, backgroundImgLocation, backgroundVidLocation);
        updateTheme(false);
    }

    /**
     * Check whether the two colours are too closely matched to read clearly. If
     * they are, display a warning message.
     * <p/>
     * @param col1 first colour.
     * @param col2 second colour.
     */
    private void checkAccessibility(Color col1, Color col2) {
        double diff = Utils.getColorDifference(col1, col2);
        if (diff < THRESHOLD) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("warning.label"), LabelGrabber.INSTANCE.getLabel("similar.colors.text"));
        }
    }

    /**
     * Get the canvas on this theme panel.
     * <p/>
     * @return the canvas on this theme panel.
     */
    public LyricCanvas getCanvas() {
        return canvas;
    }

    /**
     * Get the theme currently represented by the state of this panel.
     * <p/>
     * @return the current theme.
     */
    public ThemeDTO getTheme() {
        Font font = new Font(fontSelection.getSelectionModel().getSelectedItem(), 72);
        Background background;
        if (backgroundTypeSelect.getSelectionModel().getSelectedItem() == null) {
            return ThemeDTO.DEFAULT_THEME;
        }
        if (backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
            background = new ColourBackground(backgroundColorPicker.getValue());
        } else if (backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            background = new ImageBackground(backgroundImgLocation.getText());
        } else if (backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
            background = new VideoBackground(backgroundVidLocation.getText());
        } else {
            throw new AssertionError("Bug - " + backgroundTypeSelect.getSelectionModel().getSelectedItem() + " is an unknown selection value");
        }
        final DropShadow shadow = new DropShadow();
        shadow.setColor(shadowColorPicker.getValue());
        shadow.setOffsetX(Double.valueOf(shadowOffsetX.getText().isEmpty() ? "3" : shadowOffsetX.getText()));
        shadow.setOffsetY(Double.valueOf(shadowOffsetY.getText().isEmpty() ? "3" : shadowOffsetY.getText()));
        return new ThemeDTO(font, fontColorPicker.getValue(), background, shadow);
    }
}
