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
package org.quelea.windows.options;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.data.displayable.VerticalAlignment;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * The panel that shows the Text Only view options.
 *
 * @author mjrb5
 */
public class OptionsTextOnlyViewPanel extends GridPane implements PropertyPanel {

 
    private ComboBox<String> lineAlignment;
    private ComboBox<String> vertAlignment;
    private ComboBox<String> fontSelection;
    private ColorPicker backgroundColorPicker;
    private ColorPicker lyricsColorPicker;
    private CheckBox isItalic;
    private CheckBox isBold;
    private CheckBox useThemeAlignment;
    private CheckBox useThemeVertAlignment;
    private CheckBox useThemeFont;
    private CheckBox useThemeBackground;
    private CheckBox useThemeLyricsColor;
    private CheckBox useThemeStyling;
    private final Slider maxCharsSlider;

    /**
     * Create the Text Only view options panel.
     */
    public OptionsTextOnlyViewPanel() {
        setVgap(5);

        Label alignmentLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.line.alignment"));
        GridPane.setConstraints(alignmentLabel, 1, 1);
        getChildren().add(alignmentLabel);
        lineAlignment = new ComboBox<>();
        lineAlignment.setEditable(false);
        for (TextAlignment alignment : TextAlignment.values()) {
            lineAlignment.itemsProperty().get().add(alignment.toFriendlyString());
        }
        GridPane.setConstraints(lineAlignment, 2, 1);
        getChildren().add(lineAlignment);
        useThemeAlignment = new CheckBox(LabelGrabber.INSTANCE.getLabel("use.theme.align.textOnly"));
        GridPane.setConstraints(useThemeAlignment, 4, 1);
        getChildren().add(useThemeAlignment);

        Label vertAlignmentLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.line.vertical.alignment"));
        GridPane.setConstraints(vertAlignmentLabel, 1, 2);
        getChildren().add(vertAlignmentLabel);
        vertAlignment = new ComboBox<>();
       vertAlignment.setEditable(false);
        for (VerticalAlignment alignment : VerticalAlignment.values()) {
            vertAlignment.itemsProperty().get().add(alignment.toFriendlyString());
        }
        GridPane.setConstraints(vertAlignment, 2, 2);
        getChildren().add(vertAlignment);
         useThemeVertAlignment = new CheckBox(LabelGrabber.INSTANCE.getLabel("use.theme.vertAlign.textOnly"));
        GridPane.setConstraints(useThemeVertAlignment, 4, 2);
        getChildren().add(useThemeVertAlignment);

        Label fontLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.font.selection"));
        GridPane.setConstraints(fontLabel, 1, 3);
        getChildren().add(fontLabel);
        fontSelection = new ComboBox<>();
        fontSelection.setEditable(false);
        for (String font : Utils.getAllFonts()) {
            fontSelection.itemsProperty().get().add(font);
        }
        GridPane.setConstraints(fontSelection, 2, 3);
        getChildren().add(fontSelection);
          useThemeFont = new CheckBox(LabelGrabber.INSTANCE.getLabel("use.theme.font.textOnly"));
        GridPane.setConstraints(useThemeFont, 4, 3);
        getChildren().add(useThemeFont);

       GridPane optionPane = new GridPane();
        isBold = new CheckBox(LabelGrabber.INSTANCE.getLabel("textOnly.font.bold") + "     ");
        GridPane.setConstraints(isBold, 1, 1);
        optionPane.getChildren().add(isBold);
        isItalic = new CheckBox(LabelGrabber.INSTANCE.getLabel("textOnly.font.italic"));
        GridPane.setConstraints(isItalic, 2, 1);
        optionPane.getChildren().add(isItalic);
        GridPane.setConstraints(optionPane, 2,4);
        getChildren().add(optionPane);
          useThemeStyling = new CheckBox(LabelGrabber.INSTANCE.getLabel("use.theme.styling.textOnly"));
        GridPane.setConstraints(useThemeStyling, 4, 4);
        getChildren().add(useThemeStyling);
        
        Label backgroundLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.background.colour"));
        GridPane.setConstraints(backgroundLabel, 1, 5);
        getChildren().add(backgroundLabel);
        backgroundColorPicker = new ColorPicker(Color.BLACK);
        GridPane.setConstraints(backgroundColorPicker, 2, 5);
        getChildren().add(backgroundColorPicker);
          useThemeBackground = new CheckBox(LabelGrabber.INSTANCE.getLabel("use.theme.background.textOnly"));
        GridPane.setConstraints(useThemeBackground, 4, 5);
        getChildren().add(useThemeBackground);

        Label colourLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.lyrics.colour"));
        GridPane.setConstraints(colourLabel, 1, 6);
        getChildren().add(colourLabel);
        lyricsColorPicker = new ColorPicker(Color.BLACK);
        GridPane.setConstraints(lyricsColorPicker, 2, 6);
        getChildren().add(lyricsColorPicker);
           useThemeLyricsColor = new CheckBox(LabelGrabber.INSTANCE.getLabel("use.theme.lyricColor.textOnly"));
        GridPane.setConstraints(useThemeLyricsColor, 4, 6);
        getChildren().add(useThemeLyricsColor);

        Label maxCharsLabel = new Label(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"));
        GridPane.setConstraints(maxCharsLabel, 1, 7);
        getChildren().add(maxCharsLabel);
        maxCharsSlider = new Slider(10, 80, 0);
        GridPane.setConstraints(maxCharsSlider, 2,7);
        getChildren().add(maxCharsSlider);
        maxCharsLabel.setLabelFor(maxCharsSlider);
        final Label maxCharsValue = new Label(Integer.toString((int) maxCharsSlider.getValue()));
        GridPane.setConstraints(maxCharsValue, 3, 7);
        getChildren().add(maxCharsValue);
        maxCharsValue.setLabelFor(maxCharsSlider);
        maxCharsSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                maxCharsValue.setText(Integer.toString((int) maxCharsSlider.getValue()));
            }
        });
        
        readProperties();
    }

    /**
     * Set the properties based on the values in this frame.
     */
    @Override
    public void setProperties() {
         QueleaProperties.get().setTextOnlyVerticalAlignment(VerticalAlignment.parse(vertAlignment.itemsProperty().get().get(vertAlignment.getSelectionModel().getSelectedIndex())));
        QueleaProperties.get().setTextOnlyTextAlignment(TextAlignment.parse(lineAlignment.itemsProperty().get().get(lineAlignment.getSelectionModel().getSelectedIndex())));
        QueleaProperties.get().setTextOnlyTextFont(fontSelection.itemsProperty().get().get(fontSelection.getSelectionModel().getSelectedIndex()));
        QueleaProperties.get().setTextOnlyBackgroundColor(backgroundColorPicker.getValue());
        QueleaProperties.get().setTextOnlyLyricsColor(lyricsColorPicker.getValue());
        QueleaProperties.get().setTextOnlyUseThemeAlignment(useThemeAlignment.isSelected());
        QueleaProperties.get().setTextOnlyUseThemeVertAlignment(useThemeVertAlignment.isSelected());
        QueleaProperties.get().setTextOnlyUseThemeFont(useThemeFont.isSelected());
        QueleaProperties.get().setTextOnlyUseThemeBackground(useThemeBackground.isSelected());
        QueleaProperties.get().setTextOnlyUseThemeLyricColor(useThemeLyricsColor.isSelected());
        QueleaProperties.get().setTextOnlyUseThemeStyling(useThemeStyling.isSelected());
        QueleaProperties.get().setTextOnlyIsBold(isBold.isSelected());
        QueleaProperties.get().setTextOnlyIsItalic(isItalic.isSelected());
        QueleaProperties.get().setTextOnlyMaxCharNumber((int) maxCharsSlider.getValue());
    }

    /**
     * Read the properties into this frame.
     */
    @Override
    public final void readProperties() {
  
        lyricsColorPicker.setValue(QueleaProperties.get().getTextOnlyLyricsColor());
        lyricsColorPicker.fireEvent(new ActionEvent());
        backgroundColorPicker.setValue(QueleaProperties.get().getTextOnlyBackgroundColor());
        backgroundColorPicker.fireEvent(new ActionEvent());
        fontSelection.getSelectionModel().select(QueleaProperties.get().getTextOnlyTextFont());
        lineAlignment.getSelectionModel().select(QueleaProperties.get().getTextOnlyTextAlignment());
        vertAlignment.getSelectionModel().select(QueleaProperties.get().getTextOnlyVerticalAlignment());
        useThemeAlignment.setSelected(QueleaProperties.get().getTextOnlyUseThemeAlignment());
        useThemeVertAlignment.setSelected(QueleaProperties.get().getTextOnlyUseThemeVertAlignment());
        useThemeFont.setSelected(QueleaProperties.get().getTextOnlyUseThemeFont());
        useThemeBackground.setSelected(QueleaProperties.get().getTextOnlyUseThemeBackground());
        useThemeLyricsColor.setSelected(QueleaProperties.get().getTextOnlyUseThemeLyricColor());
        useThemeStyling.setSelected(QueleaProperties.get().getTextOnlyUseThemeStyling());
        isBold.setSelected(QueleaProperties.get().getTextOnlyIsBold());
        isItalic.setSelected(QueleaProperties.get().getTextOnlyIsItalic());
        maxCharsSlider.setValue(QueleaProperties.get().getTextOnlyMaxCharNumber());
    }
}
