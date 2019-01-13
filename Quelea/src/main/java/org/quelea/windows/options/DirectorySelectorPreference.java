package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class DirectorySelectorPreference extends SimpleControl<StringField, StackPane> {

    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The editableField allows users to modify the field's value.
     */
    private TextField editableField;
    private Button directoryChooserButton = new Button();
    private Stage stage;
    private HBox hBox = new HBox();
    private String buttonText;

    public DirectorySelectorPreference(Stage stage, String buttonText) {
        this.stage = stage;
        this.buttonText = buttonText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();

        node = new StackPane();
        node.getStyleClass().add("simple-text-control");

        editableField = new TextField(field.getValue());

        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooserButton.setOnAction(event -> {
            File dir = directoryChooser.showDialog(stage);
            if (dir != null) {
                editableField.setText(dir.getAbsolutePath());
            }
        });

        directoryChooserButton.setText(buttonText);

        editableField.setPromptText(field.placeholderProperty().getValue());
        hBox.getChildren().addAll(editableField, directoryChooserButton);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {
        node.getChildren().addAll(hBox);

        node.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupBindings() {
        super.setupBindings();

        editableField.visibleProperty().bind(Bindings.and(field.editableProperty(),
                field.multilineProperty().not()));

        editableField.textProperty().bindBidirectional(field.userInputProperty());
        editableField.promptTextProperty().bind(field.placeholderProperty());
        editableField.managedProperty().bind(editableField.visibleProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupValueChangedListeners() {
        super.setupValueChangedListeners();


        editableField.focusedProperty().addListener(
                (observable, oldValue, newValue) -> toggleTooltip(editableField)
        );
    }
}
