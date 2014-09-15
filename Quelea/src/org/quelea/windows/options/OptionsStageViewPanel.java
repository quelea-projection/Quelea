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

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel that shows the stage view options.
 *
 * @author mjrb5
 */
public class OptionsStageViewPanel extends GridPane implements PropertyPanel {

    private CheckBox showChordsCheckBox;
    private ComboBox<String> lineAlignment;
    private ComboBox<String> fontSelection;
    private ColorPicker backgroundColorPicker;
    private ColorPicker chordColorPicker;
    private ColorPicker lyricsColorPicker;
    private CheckBox usePreview;
    private CheckBox useUnuniformText;
    private CheckBox drawImages;

    /**
     * Create the stage view options panel.
     */
    public OptionsStageViewPanel() {
        setVgap(5);

        Label chordsLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.show.chords"));
        GridPane.setConstraints(chordsLabel, 1, 1);
        getChildren().add(chordsLabel);
        showChordsCheckBox = new CheckBox();
        GridPane.setConstraints(showChordsCheckBox, 2, 1);
        getChildren().add(showChordsCheckBox);

        Label alignmentLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.line.alignment"));
        GridPane.setConstraints(alignmentLabel, 1, 2);
        getChildren().add(alignmentLabel);
        lineAlignment = new ComboBox<>();
        lineAlignment.setEditable(false);
        for (TextAlignment alignment : TextAlignment.values()) {
            lineAlignment.itemsProperty().get().add(alignment.toFriendlyString());
        }
        GridPane.setConstraints(lineAlignment, 2, 2);
        getChildren().add(lineAlignment);

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

        Label backgroundLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.background.colour"));
        GridPane.setConstraints(backgroundLabel, 1, 4);
        getChildren().add(backgroundLabel);
        backgroundColorPicker = new ColorPicker(Color.BLACK);
        GridPane.setConstraints(backgroundColorPicker, 2, 4);
        getChildren().add(backgroundColorPicker);

        Label colourLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.lyrics.colour"));
        GridPane.setConstraints(colourLabel, 1, 5);
        getChildren().add(colourLabel);
        lyricsColorPicker = new ColorPicker(Color.BLACK);
        GridPane.setConstraints(lyricsColorPicker, 2, 5);
        getChildren().add(lyricsColorPicker);

        Label chordColourLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.chord.colour"));
        GridPane.setConstraints(chordColourLabel, 1, 6);
        getChildren().add(chordColourLabel);
        chordColorPicker = new ColorPicker(Color.BLACK);
        GridPane.setConstraints(chordColorPicker, 2, 6);
        getChildren().add(chordColorPicker);

        Label usePreviewLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.use.preview"));
        GridPane.setConstraints(usePreviewLabel, 1, 7);
        getChildren().add(usePreviewLabel);
        usePreview = new CheckBox();
        GridPane.setConstraints(usePreview, 2, 7);
        getChildren().add(usePreview);

        Label useUnuniformTextLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.use.ununiform.text"));
        GridPane.setConstraints(useUnuniformTextLabel, 1, 8);
        getChildren().add(useUnuniformTextLabel);
        useUnuniformText = new CheckBox();
        GridPane.setConstraints(useUnuniformText, 2, 8);
        getChildren().add(useUnuniformText);

        Label drawImagesLabel = new Label(LabelGrabber.INSTANCE.getLabel("stage.draw.images"));
        GridPane.setConstraints(drawImagesLabel, 1, 9);
        getChildren().add(drawImagesLabel);
        drawImages = new CheckBox();
        GridPane.setConstraints(drawImages, 2, 9);
        getChildren().add(drawImages);
        readProperties();
    }

    /**
     * Set the properties based on the values in this frame.
     */
    @Override
    public void setProperties() {
        QueleaProperties.get().setShowChords(showChordsCheckBox.isSelected());
        QueleaProperties.get().setStageTextAlignment(TextAlignment.parse(lineAlignment.itemsProperty().get().get(lineAlignment.getSelectionModel().getSelectedIndex())));
        QueleaProperties.get().setStageTextFont(fontSelection.itemsProperty().get().get(fontSelection.getSelectionModel().getSelectedIndex()));
        QueleaProperties.get().setStageBackgroundColor(backgroundColorPicker.getValue());
        QueleaProperties.get().setStageChordColor(chordColorPicker.getValue());
        QueleaProperties.get().setStageLyricsColor(lyricsColorPicker.getValue());
        QueleaProperties.get().setStageUsePreview(usePreview.isSelected());
        QueleaProperties.get().setStageUseUnuniformText(useUnuniformText.isSelected());
        QueleaProperties.get().setStageDrawImages(drawImages.isSelected());
        QueleaApp.get().getStageWindow().updateStage();
    }

    /**
     * Read the properties into this frame.
     */
    @Override
    public final void readProperties() {
        showChordsCheckBox.setSelected(QueleaProperties.get().getShowChords());
        lyricsColorPicker.setValue(QueleaProperties.get().getStageLyricsColor());
        lyricsColorPicker.fireEvent(new ActionEvent());
        backgroundColorPicker.setValue(QueleaProperties.get().getStageBackgroundColor());
        backgroundColorPicker.fireEvent(new ActionEvent());
        chordColorPicker.setValue(QueleaProperties.get().getStageChordColor());
        chordColorPicker.fireEvent(new ActionEvent());
        fontSelection.getSelectionModel().select(QueleaProperties.get().getStageTextFont());
        lineAlignment.getSelectionModel().select(QueleaProperties.get().getStageTextAlignment());
        usePreview.setSelected(QueleaProperties.get().getStageUsePreview());
        useUnuniformText.setSelected(QueleaProperties.get().getStageUseUnuniformText());
        drawImages.setSelected(QueleaProperties.get().getStageDrawImages());
    }
}
