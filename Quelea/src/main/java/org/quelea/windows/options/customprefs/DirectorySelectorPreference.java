package org.quelea.windows.options.customprefs;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class DirectorySelectorPreference extends SimpleControl<StringField, StackPane> {

    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The editableField allows users to modify the field's value.
     */
    private TextField editableField;
    private Button directoryChooserButton = new Button();
    private HBox hBox = new HBox();
    private String buttonText;
    private File initialDirectory;

    public DirectorySelectorPreference(String buttonText, File initialDirectory) {
        this.buttonText = buttonText;
        this.initialDirectory = initialDirectory;
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

        if (initialDirectory != null) {
            directoryChooser.setInitialDirectory(initialDirectory);
        }

        directoryChooserButton.setOnAction(event -> {
            File dir = directoryChooser.showDialog(getNode().getScene().getWindow());
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
        HBox.setHgrow(editableField, Priority.ALWAYS);
        node.setAlignment(Pos.CENTER_LEFT);
        node.getChildren().addAll(hBox);
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
