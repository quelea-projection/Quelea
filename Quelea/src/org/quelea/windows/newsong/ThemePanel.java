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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.javafx.dialog.Dialog;
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
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.schedule.ScheduleThemeNode;
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
    private HBox themeActionsPanel;
    private ColorPicker shadowColorPicker;
    private TextField shadowOffsetX;
    private TextField shadowOffsetY;
    private TextField shadowWidth;
    private TextField backgroundImgLocation;
    private TextField backgroundVidLocation;
    private ToggleButton boldButton;
    private Boolean isFontBold = false;
    private ToggleButton italicButton;
    private Boolean isFontItalic = false;
    private TextField themeNameField;
    private Button saveThemeButton;
    private Button selectThemeButton;
    private final DisplayCanvas canvas;
    private ThemeDTO selectedTheme = null;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        canvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateTheme(true, null);
            }
        }, Priority.LOW);
        canvas.setMinWidth(getWidth());
        canvas.setMinHeight(getHeight());
        setCenter(canvas);
        LyricDrawer drawer = new LyricDrawer();
        drawer.setCanvas(canvas);
        drawer.setText(SAMPLE_LYRICS, null, false, -1);
        VBox toolbarPanel = new VBox();
        setupFontToolbar();
        toolbarPanel.getChildren().add(fontToolbar);
        setupBackgroundToolbar();
        toolbarPanel.getChildren().add(backgroundPanel);
        setupShadowPanel();
        toolbarPanel.getChildren().add(shadowPanel);
        //setupThemeActionsToolbars(); //@todo to be finished
        //toolbarPanel.getChildren().add(themeActionsPanel);
        setTop(toolbarPanel);
        updateTheme(false, null);

    }

    /**
     * Setup the background toolbar.
     */
    private void setupBackgroundToolbar() {
        backgroundPanel = new HBox();
        final CardPane<HBox> backgroundChooserPanel = new CardPane<>();

        backgroundTypeSelect = new ComboBox<>();
        backgroundTypeSelect.getItems().add(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
        backgroundTypeSelect.getItems().add(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backgroundTypeSelect.getItems().add(LabelGrabber.INSTANCE.getLabel("video.theme.label"));
        backgroundTypeSelect.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(false, null);
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
                updateTheme(true, null);
            }
        });

        final HBox imagePanel = new HBox();
        backgroundImgLocation = new TextField();
        backgroundImgLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true, null);
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
                updateTheme(true, null);
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
                if(backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
                    backgroundChooserPanel.show("colour");
                }
                else if(backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
                    backgroundChooserPanel.show("image");
                }
                else if(backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
                    backgroundChooserPanel.show("video");
                }
                else {
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

        shadowColorPicker = new ColorPicker(Color.BLACK);
        final HBox colourPanel = new HBox();
        colourPanel.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("shadow.color"))); //@todo add languages
        colourPanel.getChildren().add(shadowColorPicker);
        confFirstLine.getChildren().add(colourPanel);
        shadowColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                updateTheme(true, null);
            }
        });
        shadowPanel.getChildren().add(confFirstLine);
        final HBox confSecondLine = new HBox();
        confSecondLine.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("shadow.x")));
        shadowOffsetX = new TextField();
        confSecondLine.getChildren().add(shadowOffsetX);
        shadowOffsetX.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true, null);
            }
        });

        confSecondLine.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("shadow.y")));
        shadowOffsetY = new TextField();
        confSecondLine.getChildren().add(shadowOffsetY);
        shadowOffsetY.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(true, null);
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
        for(String font : Utils.getAllFonts()) {
            fontSelection.itemsProperty().get().add(font);
        }
        fontSelection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateTheme(false, null);
            }
        });
        fontToolbar.getChildren().add(fontSelection);
        boldButton = new ToggleButton("", new ImageView(new Image("file:icons/bold.png")));
        boldButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                isFontBold = !isFontBold;
                updateTheme(false, null);
            }
        });
        fontToolbar.getChildren().add(boldButton);
        italicButton = new ToggleButton("", new ImageView(new Image("file:icons/italic.png")));
        italicButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                isFontItalic = !isFontItalic;
                updateTheme(false, null);
            }
        });
        fontToolbar.getChildren().add(italicButton);
        fontColorPicker = new ColorPicker(Color.WHITE);
        fontColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                updateTheme(true, null);
            }
        });
        fontToolbar.getChildren().add(fontColorPicker);

    }

    /**
     * Setup the font toolbar.
     */
    private void setupThemeActionsToolbars() {
        themeActionsPanel = new HBox();
        themeActionsPanel.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("theme.name.label")));
        themeNameField = new TextField();
        themeActionsPanel.getChildren().add(themeNameField);
        themeNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                saveThemeButton.setDisable(themeNameField.getText().trim().isEmpty());
            }
        });
        saveThemeButton = new Button(LabelGrabber.INSTANCE.getLabel("save"));
        themeActionsPanel.getChildren().add(saveThemeButton);
        saveThemeButton.setDisable(true);
        saveThemeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                ThemeDTO theme = getTheme();
                File themeFile = new File(QueleaProperties.getQueleaUserHome()
                        + "/themes/" + themeNameField.getText() + ".th");
                theme.setFile(themeFile);
                theme.setThemeName(themeNameField.getText());
                try {
                    try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(themeFile), "UTF-8"))) {
                        out.write(theme.asString());
                    }
                }
                catch(Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        });
        selectThemeButton = new Button("", new ImageView(new Image("file:icons/settings.png", 16, 16, false, true)));
        themeActionsPanel.getChildren().add(selectThemeButton);
        final ThemePanel panel = this;
        selectThemeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                ContextMenu themeMenu = new ContextMenu();

                MenuItem themeItem = new MenuItem("", new ScheduleThemeNode(new ScheduleThemeNode.UpdateThemeCallback() {
                    @Override
                    public void updateTheme(ThemeDTO theme) {
                        panel.updateTheme(true, theme);
                        selectedTheme = theme;
                    }
                }, null, null));
                themeItem.setDisable(true);
                themeItem.setStyle("-fx-background-color: #000000;");
                themeMenu.getItems().add(themeItem);
                themeMenu.show(selectThemeButton, Side.BOTTOM, 0, 0);
            }
        });
        selectThemeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("adjust.theme.tooltip")));
    }

    /**
     * Update the canvas with the current theme.
     */
    private void updateTheme(boolean warning, ThemeDTO newTheme) {

        final ThemeDTO theme = (newTheme != null) ? newTheme : getTheme();
        if(warning && theme.getBackground() instanceof ColourBackground) {
            checkAccessibility(theme.getFontPaint(), ((ColourBackground) theme.getBackground()).getColour());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LyricDrawer drawer = new LyricDrawer();
                drawer.setCanvas(canvas);
                drawer.setTheme(theme);
                drawer.setText(SAMPLE_LYRICS, null, false, -1);
            }
        });
    }

    /**
     * Set the current theme to represent in this panel.
     * <p/>
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {
        if(theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        Font font = theme.getFont();
        fontSelection.getSelectionModel().select(font.getFamily());
        fontColorPicker.setValue(theme.getFontPaint());
        Background background = theme.getBackground();
        background.setThemeForm(backgroundColorPicker, backgroundTypeSelect, backgroundImgLocation, backgroundVidLocation);
        updateTheme(false, null);
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
        if(diff < THRESHOLD) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("warning.label"), LabelGrabber.INSTANCE.getLabel("similar.colors.text"));
        }
    }

    /**
     * Get the canvas on this theme panel.
     * <p/>
     * @return the canvas on this theme panel.
     */
    public DisplayCanvas getCanvas() {
        return canvas;
    }

    /**
     * Get the theme currently represented by the state of this panel.
     * <p/>
     * @return the current theme.
     */
    public ThemeDTO getTheme() {
        Font font = new Font(fontSelection.getSelectionModel().getSelectedItem(), 72);
        Font.font(font.getName(),
                isFontBold ? FontWeight.BOLD : FontWeight.NORMAL,
                isFontItalic ? FontPosture.ITALIC : FontPosture.REGULAR,
                72);

        Background background;
        if(backgroundTypeSelect.getSelectionModel().getSelectedItem() == null) {
            return ThemeDTO.DEFAULT_THEME;
        }
        if(backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
            background = new ColourBackground(backgroundColorPicker.getValue());
        }
        else if(backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            background = new ImageBackground(backgroundImgLocation.getText());
        }
        else if(backgroundTypeSelect.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
            background = new VideoBackground(backgroundVidLocation.getText());
        }
        else {
            throw new AssertionError("Bug - " + backgroundTypeSelect.getSelectionModel().getSelectedItem() + " is an unknown selection value");
        }
        final SerializableDropShadow shadow = new SerializableDropShadow(shadowColorPicker.getValue(),
                Double.valueOf(shadowOffsetX.getText().isEmpty() ? "3" : shadowOffsetX.getText()),
                Double.valueOf(shadowOffsetY.getText().isEmpty() ? "3" : shadowOffsetY.getText()));
        ThemeDTO resultTheme = new ThemeDTO(new SerializableFont(font), fontColorPicker.getValue(),
                background, shadow, isFontBold, isFontItalic);
        return resultTheme;
    }

    /**
     * @return the selectedTheme
     */
    public ThemeDTO getSelectedTheme() {
        return selectedTheme;
    }
}
