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
import javafx.stage.FileChooser;

import java.io.File;

/**
 * A text field with a button next to it for choosing a file, for a
 * {@link StringField} holding a file path. Like
 * {@link DirectorySelectorPreference}, but for files.
 *
 * @author Arvid
 */
public class FileSelectorPreference extends SimpleControl<StringField, StackPane> {

    private TextField editableField;
    private final Button fileChooserButton = new Button();
    private final HBox hBox = new HBox();
    private final String buttonText;
    private final File initialDirectory;
    private final FileChooser.ExtensionFilter filter;

    /**
     * @param buttonText       the text on the browse button.
     * @param initialDirectory the directory the file chooser starts in when
     *                         the field doesn't hold an existing file, may be
     *                         null.
     * @param filter           the files that may be chosen, may be null for
     *                         any file.
     */
    public FileSelectorPreference(String buttonText, File initialDirectory, FileChooser.ExtensionFilter filter) {
        this.buttonText = buttonText;
        this.initialDirectory = initialDirectory;
        this.filter = filter;
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

        fileChooserButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            if (filter != null) {
                fileChooser.getExtensionFilters().add(filter);
            }
            File current = new File(editableField.getText() == null ? "" : editableField.getText()).getAbsoluteFile();
            if (current.getParentFile() != null && current.getParentFile().isDirectory()) {
                fileChooser.setInitialDirectory(current.getParentFile());
            } else if (initialDirectory != null && initialDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
            File file = fileChooser.showOpenDialog(getNode().getScene().getWindow());
            if (file != null) {
                editableField.setText(file.getAbsolutePath());
            }
        });

        fileChooserButton.setText(buttonText);

        editableField.setPromptText(field.placeholderProperty().getValue());

        hBox.setSpacing(5);
        hBox.getChildren().addAll(editableField, fileChooserButton);
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
