package org.quelea.windows.options.customprefs;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ColorPickerPreference extends SimpleControl<StringField, StackPane> {

    /**
     * - The colorPicker is the container that displays the node to select a color value.
     */
    private ColorPicker colorPicker;
    private Color initialValue;

    public ColorPickerPreference(Color initialValue) {
        this.initialValue = initialValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();

        node = new StackPane();
        node.getStyleClass().add("simple-text-control");

        colorPicker = new ColorPicker(initialValue);
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setOnAction(event -> {
            if (!field.valueProperty().getValue().equals(getColorString(colorPicker.getValue())))
                field.valueProperty().setValue(getColorString(colorPicker.getValue()));
        });

        field.valueProperty().setValue(getColorString(colorPicker.getValue()));
    }

    private String getColorString(Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {
        node.getChildren().addAll(colorPicker);
        node.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupBindings() {
        super.setupBindings();
        field.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                String[] rgb = newValue.split(",");
                Color newColor = Color.rgb((int) (Double.parseDouble(rgb[0]) * 255), (int) (Double.parseDouble(rgb[1]) * 255), (int) (Double.parseDouble(rgb[2]) * 255));
                if (!colorPicker.getValue().equals(newColor)) {
                    colorPicker.setValue(newColor);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupValueChangedListeners() {
        super.setupValueChangedListeners();

        colorPicker.focusedProperty().addListener(
                (observable, oldValue, newValue) -> toggleTooltip(colorPicker)
        );
    }
}
