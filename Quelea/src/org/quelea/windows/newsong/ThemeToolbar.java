/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.newsong;

import java.util.Collections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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

    private ComboBox<String> fontSelection;
    private Button fontExpandButton;
    private ToggleButton boldButton;
    private ToggleButton italicButton;
    private ColorPicker fontColor;
    private ComboBox<String> backTypeSelection;
    private TextField backgroundImageLocation;
    private TextField backgroundVidLocation;
    private ColorPicker backgroundColorPicker;
    private ThemePanel themePanel;
    private static FontSelectionDialog fontSelectionDialog;

    /**
     * Create a new theme toolbar.
     * <p>
     * @param themePanel the theme panel that this toolbar sits on.
     */
    public ThemeToolbar(final ThemePanel themePanel) {
        this.themePanel = themePanel;
        setPadding(new Insets(5));
        setStyle("-fx-background-color:#dddddd;");
        VBox topLevelFontBox = new VBox(10);
        topLevelFontBox.setStyle("-fx-border-color: bbbbbb;");
        topLevelFontBox.setPadding(new Insets(10));
        getChildren().add(topLevelFontBox);

        HBox fontTop = new HBox(3);
        if(fontSelectionDialog == null) {
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
                themePanel.updateTheme(false, null);
            }
        });
        fontExpandButton = new Button("...");
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

        HBox fontMid = new HBox(10);
        boldButton = new ToggleButton("", new ImageView(new Image("file:icons/bold.png", 20, 20, false, true)));
        Utils.setToolbarButtonStyle(boldButton);
        boldButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false, null);
            }
        });
        italicButton = new ToggleButton("", new ImageView(new Image("file:icons/italic.png", 20, 20, false, true)));
        Utils.setToolbarButtonStyle(italicButton);
        italicButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themePanel.updateTheme(false, null);
            }
        });
        fontColor = new ColorPicker(Color.WHITE);
        fontColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                themePanel.updateTheme(true, null);
            }
        });
        fontMid.getChildren().add(boldButton);
        fontMid.getChildren().add(italicButton);
        fontMid.getChildren().add(fontColor);
        topLevelFontBox.getChildren().add(fontMid);

        StackPane fontBottom = new StackPane();
        Text fontText = new Text("Font");
        fontText.setFill(Color.GRAY);
        fontBottom.getChildren().add(fontText);
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

        HBox backTop = new HBox(5);
        backTypeSelection = new ComboBox<>();
        backTypeSelection.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(true, null);
            }
        });
        backTypeSelection.getItems().add(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
        backTypeSelection.getItems().add(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backTypeSelection.getItems().add(LabelGrabber.INSTANCE.getLabel("video.theme.label"));
        backTop.getChildren().add(backTypeSelection);
        topLevelBackBox.getChildren().add(backTop);

        final CardPane<HBox> backCentre = new CardPane<>();
        final HBox colourPanel = new HBox();
        backgroundColorPicker = new ColorPicker(Color.BLACK);
        backgroundColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                themePanel.updateTheme(true, null);
            }
        });
        colourPanel.getChildren().add(backgroundColorPicker);
        backCentre.add(colourPanel, "colour");
        final HBox imagePanel = new HBox();
        backgroundImageLocation = new TextField();
        backgroundImageLocation.setEditable(false);
        backgroundImageLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(true, null);
            }
        });
        Button backgroundImageSelectButton = new ImageButton(backgroundImageLocation, themePanel.getCanvas());
        imagePanel.getChildren().add(backgroundImageLocation);
        imagePanel.getChildren().add(backgroundImageSelectButton);
        backCentre.add(imagePanel, "image");
        final HBox vidPanel = new HBox();
        backgroundVidLocation = new TextField();
        backgroundVidLocation.setEditable(false);
        backgroundVidLocation.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                themePanel.updateTheme(true, null);
            }
        });
        Button backgroundVidSelectButton = new VideoButton(backgroundImageLocation, themePanel.getCanvas());
        vidPanel.getChildren().add(backgroundVidLocation);
        vidPanel.getChildren().add(backgroundVidSelectButton);
        backCentre.add(vidPanel, "video");

        backTypeSelection.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                if(backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
                    backCentre.show("colour");
                }
                else if(backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
                    backCentre.show("image");
                }
                else if(backTypeSelection.getSelectionModel().getSelectedItem().equalsIgnoreCase(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
                    backCentre.show("video");
                }
                else {
                    throw new AssertionError("Bug - " + backTypeSelection.getSelectionModel().getSelectedItem() + " is an unknown selection value");
                }
            }
        });
        topLevelBackBox.getChildren().add(backCentre);

        StackPane backBottom = new StackPane();
        Text backText = new Text(LabelGrabber.INSTANCE.getLabel("background.text"));
        backText.setFill(Color.GRAY);
        backBottom.getChildren().add(backText);
        topLevelBackBox.getChildren().add(backBottom);

    }

    /**
     * Set the theme represented by this toolbar.
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {
        if(theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        Font font = theme.getFont();
        fontSelection.getSelectionModel().select(font.getFamily());
        fontColor.setValue(theme.getFontPaint());
        boldButton.setSelected(theme.isBold());
        italicButton.setSelected(theme.isItalic());
        Background background = theme.getBackground();
        background.setThemeForm(backgroundColorPicker, backTypeSelection, backgroundImageLocation, backgroundVidLocation);
    }

    /**
     * Get the theme represented by this toolbar.
     * @return the theme.
     */
    public ThemeDTO getTheme() {
        Font font = Font.font(fontSelection.getSelectionModel().getSelectedItem(),
                boldButton.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                italicButton.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());

        Background background;
        if(backTypeSelection.getSelectionModel().getSelectedItem() == null) {
            return ThemeDTO.DEFAULT_THEME;
        }
        if(backTypeSelection.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
            background = new ColourBackground(backgroundColorPicker.getValue());
        }
        else if(backTypeSelection.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            background = new ImageBackground(backgroundImageLocation.getText());
        }
        else if(backTypeSelection.getSelectionModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("video.theme.label"))) {
            background = new VideoBackground(backgroundVidLocation.getText());
        }
        else {
            throw new AssertionError("Bug - " + backTypeSelection.getSelectionModel().getSelectedItem() + " is an unknown selection value");
        }
        final SerializableDropShadow shadow = new SerializableDropShadow(Color.BLACK, 3, 3);
        ThemeDTO resultTheme = new ThemeDTO(new SerializableFont(font), fontColor.getValue(),
                background, shadow, boldButton.isSelected(), italicButton.isSelected());
        return resultTheme;
    }

}
