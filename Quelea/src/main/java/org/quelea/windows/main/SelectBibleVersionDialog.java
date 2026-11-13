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
package org.quelea.windows.main;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.windows.style.StyleManager;

/**
 * Dialog used for choosing which bible versions to show, and in what order.
 * Versions are ticked rather than selected, because the order they are chosen
 * in matters and needs to work the same way on every platform.
 *
 * @author Arvid
 */
public class SelectBibleVersionDialog extends Stage {

    private final ListView<Bible> listView = new ListView<>();
    private final ObservableList<Bible> ticked = FXCollections.observableArrayList();
    private final Button okButton;
    private final Button cancelButton;
    private List<Bible> result;

    /**
     * Create the dialog to choose bible versions.
     */
    public SelectBibleVersionDialog() {
        Utils.addIconsToStage(this);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("select.multi.bible.label"));
        listView.setCellFactory(list -> new VersionCell());
        listView.setPrefHeight(200);
        listView.setOnKeyPressed(event -> {
            Bible focused = listView.getFocusModel().getFocusedItem();
            if (event.getCode() == KeyCode.SPACE && focused != null) {
                toggle(focused);
                event.consume();
            }
        });
        VBox root = new VBox(5);
        Label hintLabel = new Label(LabelGrabber.INSTANCE.getLabel("select.bible.versions.hint"));
        hintLabel.setWrapText(true);
        root.getChildren().addAll(hintLabel, listView);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), QueleaIcon.CHECK.create());
        StyleManager.markPrimaryButton(okButton);
        okButton.setDefaultButton(true);
        okButton.setDisable(true);
        okButton.setOnAction(t -> {
            result = new ArrayList<>(ticked);
            hide();
        });
        ticked.addListener((ListChangeListener<Bible>) change -> {
            okButton.setDisable(ticked.isEmpty());
            listView.refresh();
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), QueleaIcon.CLOSE.create());
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(t -> hide());
        HBox buttonPanel = new HBox(8);
        buttonPanel.getChildren().addAll(okButton, cancelButton);
        root.setSpacing(8);
        root.setPadding(new Insets(16, 18, 16, 18));
        VBox.setVgrow(listView, Priority.ALWAYS);
        BorderPane containerPane = new BorderPane(root);
        containerPane.setBottom(buttonPanel);
        Scene scene = new Scene(containerPane, 420, 340);
        StyleManager.apply(scene);
        setScene(scene);
    }

    /**
     * Show the dialog and get the bible versions chosen.
     *
     * @param selected the versions to tick when the dialog opens.
     * @return the versions chosen, in display order, or null if cancelled.
     */
    public List<Bible> getVersions(List<Bible> selected) {
        result = null;
        listView.getItems().setAll(BibleManager.get().getBibles());
        ticked.setAll(selected);
        if (listView.getItems().isEmpty()) {
            return null;
        }
        showAndWait();
        return result;
    }

    private void toggle(Bible bible) {
        if (!ticked.remove(bible)) {
            ticked.add(bible);
        }
    }

    /**
     * A row with a tick box, showing where in the order the version comes.
     */
    private final class VersionCell extends ListCell<Bible> {

        private final CheckBox checkBox = new CheckBox();

        private VersionCell() {
            checkBox.setMouseTransparent(true);
            checkBox.setFocusTraversable(false);
            setOnMouseClicked(event -> {
                if (getItem() != null) {
                    toggle(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Bible bible, boolean empty) {
            super.updateItem(bible, empty);
            if (empty || bible == null) {
                setText(null);
                setGraphic(null);
            } else {
                int position = ticked.indexOf(bible);
                checkBox.setSelected(position >= 0);
                setText(position >= 0 ? (position + 1) + ". " + bible.getBibleName() : bible.getBibleName());
                setGraphic(checkBox);
            }
        }
    }
}
