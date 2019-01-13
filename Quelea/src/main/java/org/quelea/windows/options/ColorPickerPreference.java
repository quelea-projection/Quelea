package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ColorPickerPreference extends SimpleControl<StringField, StackPane> {

    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The editableField allows users to modify the field's value.
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

        colorPicker.setOnAction(event -> {
            field.valueProperty().setValue(colorPicker.getValue().toString());
        });

        field.valueProperty().setValue(colorPicker.getValue().toString());
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

        colorPicker.visibleProperty().bind(Bindings.and(field.editableProperty(),
                field.multilineProperty().not()));

        colorPicker.managedProperty().bind(colorPicker.visibleProperty());
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
