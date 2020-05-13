package org.quelea.windows.options.customprefs;

import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultBibleSelector extends SimpleControl<SingleSelectionField<String>, StackPane> {

    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The comboBox is the container that displays the values in the
     * ComboBox.
     * - The readOnlyLabel is used to show the current selection in read only.
     * - The node is a StackPane to hold the field and read only label.
     */
    private ComboBox<String> comboBox;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();


        node = new StackPane();
        node.setMaxWidth(Double.MAX_VALUE);
        node.getStyleClass().add("simple-select-control");

        comboBox = new ComboBox<String>(field.getItems());

        comboBox.getSelectionModel().select(field.getItems().indexOf(field.getSelection()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {

        comboBox.setVisibleRowCount(4);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setMinWidth(100);

        node.setAlignment(Pos.CENTER_LEFT);
        HBox hBox = new HBox();
        hBox.getChildren().addAll(comboBox, createAddBibleButton(), createDeleteBibleButton());
        HBox.setHgrow(comboBox, Priority.ALWAYS);
        node.getChildren().addAll(hBox);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupBindings() {
        super.setupBindings();

        comboBox.visibleProperty().bind(field.editableProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupValueChangedListeners() {
        super.setupValueChangedListeners();

        field.itemsProperty().addListener(
                (observable, oldValue, newValue) -> comboBox.setItems(field.getItems())
        );

        field.selectionProperty().addListener((observable, oldValue, newValue) -> {
            if (field.getSelection() != null) {
                comboBox.getSelectionModel().select(field.getItems().indexOf(field.getSelection()));
            } else {
                comboBox.getSelectionModel().clearSelection();
            }
        });

        field.errorMessagesProperty().addListener(
                (observable, oldValue, newValue) -> toggleTooltip(comboBox)
        );
        field.tooltipProperty().addListener(
                (observable, oldValue, newValue) -> toggleTooltip(comboBox)
        );
        comboBox.focusedProperty().addListener(
                (observable, oldValue, newValue) -> toggleTooltip(comboBox)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupEventHandlers() {
        comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                field.select(comboBox.getSelectionModel().getSelectedIndex())
        );
    }

    private Button createAddBibleButton() {
        final Button addBibleButton = new Button(LabelGrabber.INSTANCE.getLabel("add.bible.label"),
                new ImageView(new Image("file:icons/ic-add.png",16,16,false,true)));
        addBibleButton.setOnAction(t -> {

            FileChooser chooser = new FileChooser();
            if (QueleaProperties.get().getLastDirectory() != null) {
                chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
            }
            chooser.getExtensionFilters().add(FileFilters.XML_BIBLE);
            File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
            if (file != null) {
                QueleaProperties.get().setLastDirectory(file.getParentFile());
                try {
                    Utils.copyFile(file, new File(QueleaProperties.get().getBibleDir(), file.getName()));
                    BibleManager.get().refreshAndLoad();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Error copying bible file", ex);
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.copy.error.heading"),
                            LabelGrabber.INSTANCE.getLabel("bible.copy.error.text"));
                }
            }

        });
        return addBibleButton;
    }

    private Button createDeleteBibleButton() {
        final Button deleteBibleButton = new Button(LabelGrabber.INSTANCE.getLabel("delete.bible.label"),
                new ImageView(new Image("file:icons/ic-cancel.png",16,16,false,true)));
        deleteBibleButton.setOnAction(t -> {

            Bible bible = BibleManager.get().getBibleFromName(comboBox.getSelectionModel().getSelectedItem());
            if (bible != null && bible.getFilePath() != null) {

                final AtomicBoolean yes = new AtomicBoolean();
                Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.bible.label"),
                        LabelGrabber.INSTANCE.getLabel("delete.bible.confirmation").replace("$1", bible.getBibleName())).
                        addYesButton(ae -> {
                            yes.set(true);
                        }).addNoButton(ae -> {
                }).build().showAndWait();

                if (yes.get()) {
                    try {
                        Files.delete(Paths.get(bible.getFilePath()));
                        BibleManager.get().refreshAndLoad();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error deleting bible file", ex);
                        Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.delete.error.heading"),
                                LabelGrabber.INSTANCE.getLabel("bible.delete.error.text"));
                    }
                }
            }
        });
        return deleteBibleButton;
    }
}