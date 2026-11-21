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
package org.quelea.windows.options.customprefs;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
import org.quelea.windows.style.QueleaIcon;

/**
 * The bibles that are installed, with the ones new passages are shown in ticked.
 * They are shown in the order they are ticked, as in the dialog for choosing
 * the versions of a passage, and the first ticked bible is the default bible. The field
 * holds the names of the ticked bibles, one per line, as a bible name can
 * contain anything else.
 *
 * @author Arvid
 */
public class BibleVersionsList extends SimpleControl<StringField, StackPane> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final double ROW_HEIGHT = 34;
    private static final int VISIBLE_ROWS = 5;

    private final ObservableList<String> names = FXCollections.observableArrayList();
    private final Set<String> ticked = new LinkedHashSet<>();
    private ListView<String> listView;
    private Button addButton;
    private Button deleteButton;
    private boolean updating;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();
        node = new StackPane();
        node.getStyleClass().add("bible-versions-control");
        listView = new ListView<>(names);
        listView.getStyleClass().add("bible-versions-list");
        listView.setCellFactory(list -> new VersionCell());
        listView.setFixedCellSize(ROW_HEIGHT);
        listView.setPrefHeight(ROW_HEIGHT * VISIBLE_ROWS + 2);
        addButton = new Button(LabelGrabber.INSTANCE.getLabel("add.bible.label"), QueleaIcon.ADD.create());
        deleteButton = new Button(LabelGrabber.INSTANCE.getLabel("bible.delete.button"), QueleaIcon.CLOSE.create());
        for (Button button : List.of(addButton, deleteButton)) {
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
        }
        loadFromField();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {
        VBox buttons = new VBox(6, addButton, deleteButton);
        buttons.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(listView, Priority.ALWAYS);
        HBox row = new HBox(10, listView, buttons);
        Label hint = new Label(LabelGrabber.INSTANCE.getLabel("bible.versions.hint"));
        hint.setWrapText(true);
        hint.getStyleClass().add("muted-label");
        hint.setMinHeight(Region.USE_PREF_SIZE);
        VBox box = new VBox(8, row, hint);
        node.setAlignment(Pos.CENTER_LEFT);
        node.getChildren().add(box);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupValueChangedListeners() {
        super.setupValueChangedListeners();
        field.userInputProperty().addListener((obs, oldValue, newValue) -> {
            if (!updating) {
                loadFromField();
            }
        });
        listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> updateButtons());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupEventHandlers() {
        addButton.setOnAction(event -> addBible());
        deleteButton.setOnAction(event -> deleteBible());
        listView.setOnKeyPressed(event -> {
            String name = listView.getSelectionModel().getSelectedItem();
            if (event.getCode() == KeyCode.SPACE && name != null) {
                toggle(name);
                event.consume();
            }
        });
        updateButtons();
    }

    /**
     * Show the installed bibles, ticked as in the field.
     */
    private void loadFromField() {
        List<String> installed = new ArrayList<>();
        for (Bible bible : BibleManager.get().getBibles()) {
            installed.add(bible.getName());
        }
        ticked.clear();
        String input = field.getUserInput() == null ? "" : field.getUserInput();
        input.lines().filter(installed::contains).forEach(ticked::add);
        if (ticked.isEmpty() && !installed.isEmpty()) {
            ticked.add(installed.getFirst());
        }
        String selected = listView.getSelectionModel().getSelectedItem();
        names.setAll(installed);
        listView.getSelectionModel().select(selected);
        writeField();
    }

    private void toggle(String name) {
        if (!ticked.remove(name)) {
            ticked.add(name);
        } else if (ticked.isEmpty()) {
            // There always has to be a default bible
            ticked.add(name);
        }
        writeField();
    }

    /**
     * Put the ticked bibles in the field, in the order they were ticked.
     */
    private void writeField() {
        List<String> versions = new ArrayList<>(ticked);
        updating = true;
        field.userInputProperty().set(String.join("\n", versions));
        updating = false;
        listView.refresh();
        updateButtons();
    }

    private void updateButtons() {
        deleteButton.setDisable(listView.getSelectionModel().getSelectedIndex() < 0);
    }

    private void addBible() {
        FileChooser chooser = new FileChooser();
        if (QueleaProperties.get().getLastDirectory() != null) {
            chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
        }
        chooser.getExtensionFilters().add(FileFilters.XML_BIBLE);
        File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
        if (file == null) {
            return;
        }
        QueleaProperties.get().setLastDirectory(file.getParentFile());
        if (Bible.parseBible(file) == null) {
            LOGGER.log(Level.WARNING, "Tried to add corrupt bible: {0}", file.getAbsolutePath());
            Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.load.error.title"),
                    LabelGrabber.INSTANCE.getLabel("bible.load.error.question"));
            return;
        }
        try {
            Utils.copyFile(file, new File(QueleaProperties.get().getBibleDir(), file.getName()));
            BibleManager.get().refreshAndLoad();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error copying bible file", ex);
            Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.copy.error.heading"),
                    LabelGrabber.INSTANCE.getLabel("bible.copy.error.text"));
        }
        loadFromField();
    }

    private void deleteBible() {
        Bible bible = BibleManager.get().getBibleFromName(listView.getSelectionModel().getSelectedItem());
        if (bible == null || bible.getFilePath() == null) {
            return;
        }
        AtomicBoolean yes = new AtomicBoolean();
        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.bible.label"),
                LabelGrabber.INSTANCE.getLabel("delete.bible.confirmation").replace("$1", bible.getBibleName()))
                .addYesButton(event -> yes.set(true))
                .addNoButton(event -> {
                }).build().showAndWait();
        if (!yes.get()) {
            return;
        }
        try {
            Files.delete(Paths.get(bible.getFilePath()));
            BibleManager.get().refreshAndLoad();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error deleting bible file", ex);
            Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.delete.error.heading"),
                    LabelGrabber.INSTANCE.getLabel("bible.delete.error.text"));
        }
        loadFromField();
    }

    /**
     * A bible with a tick box, its place in the order and a mark on the
     * default bible.
     */
    private final class VersionCell extends ListCell<String> {

        private final CheckBox checkBox = new CheckBox();
        private final Label position = new Label();
        private final Label name = new Label();
        private final Label badge = new Label(LabelGrabber.INSTANCE.getLabel("default.bible.badge"));
        private final HBox root;

        private VersionCell() {
            checkBox.setFocusTraversable(false);
            checkBox.setOnAction(event -> {
                if (getItem() != null) {
                    toggle(getItem());
                }
            });
            position.getStyleClass().add("bible-versions-position");
            position.setMinWidth(18);
            name.setMaxWidth(Double.MAX_VALUE);
            name.setMinWidth(0);
            HBox.setHgrow(name, Priority.ALWAYS);
            badge.getStyleClass().add("bible-versions-default");
            badge.setMinWidth(Region.USE_PREF_SIZE);
            root = new HBox(10, checkBox, position, name, badge);
            root.setAlignment(Pos.CENTER_LEFT);
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            List<String> order = new ArrayList<>(ticked);
            int index = order.indexOf(item);
            checkBox.setSelected(index >= 0);
            checkBox.setAccessibleText(item);
            position.setText(index >= 0 ? (index + 1) + "." : "");
            name.setText(item);
            badge.setVisible(index == 0);
            badge.setManaged(index == 0);
            setGraphic(root);
        }
    }
}
