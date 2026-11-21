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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVersionText;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.library.ChosenVersesEditor;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.windows.style.StyleManager;

/**
 * Dialog used for choosing which bible versions to show, and in what order.
 * Versions are ticked rather than selected, because the order they are chosen
 * in matters and needs to work the same way on every platform. For a single
 * passage, each version shows where the passage is in it, and the verses of
 * the other versions can be chosen by hand.
 *
 * @author Arvid
 */
public class SelectBibleVersionDialog extends Stage {

    /**
     * The versions chosen in the dialog.
     *
     * @param versions the versions, in display order.
     * @param chosen   the passage chosen by hand for some of the versions, by
     *                 bible name.
     */
    public record Choice(List<Bible> versions, Map<String, String> chosen) {
    }

    private final ListView<Bible> listView = new ListView<>();
    private final ObservableList<Bible> ticked = FXCollections.observableArrayList();
    private final Map<String, String> chosen = new LinkedHashMap<>();
    private final VBox editorPane = new VBox();
    private final Button okButton;
    private final Button cancelButton;
    private BiblePassage passage;
    private BiblePassage preview;
    private Bible editing;
    private Choice result;

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
        hintLabel.setMinHeight(Region.USE_PREF_SIZE);
        root.getChildren().addAll(hintLabel, listView, editorPane);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), QueleaIcon.CHECK.create());
        StyleManager.markPrimaryButton(okButton);
        okButton.setDefaultButton(true);
        okButton.setDisable(true);
        okButton.setOnAction(t -> {
            List<Bible> versions = new ArrayList<>(ticked);
            Map<String, String> kept = new LinkedHashMap<>();
            chosen.forEach((name, reference) -> {
                if (versions.stream().anyMatch(bible -> bible.getBibleName().equals(name))) {
                    kept.put(name, reference);
                }
            });
            result = new Choice(versions, kept);
            hide();
        });
        ticked.addListener((ListChangeListener<Bible>) change -> {
            okButton.setDisable(ticked.isEmpty());
            if (editing != null && ticked.indexOf(editing) < 1) {
                editing = null;
            }
            updatePreview();
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
        Scene scene = new Scene(containerPane, 500, 420);
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
        Choice choice = choose(selected, null);
        return choice == null ? null : choice.versions();
    }

    /**
     * Show the dialog and get the bible versions chosen, and the verses chosen
     * by hand for them.
     *
     * @param selected the versions to tick when the dialog opens.
     * @param passage  the passage the versions are for, or null if they are
     *                 for several passages.
     * @return what was chosen, or null if cancelled.
     */
    public Choice choose(List<Bible> selected, BiblePassage passage) {
        result = null;
        this.passage = passage;
        editing = null;
        chosen.clear();
        if (passage != null) {
            chosen.putAll(passage.getChosenReferences());
        }
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

    private void updatePreview() {
        preview = passage == null || ticked.isEmpty() ? null : passage.withVersions(new ArrayList<>(ticked), chosen);
        editorPane.getChildren().clear();
        if (editing != null && preview != null && preview.isParallel()) {
            ChosenVersesEditor editor = new ChosenVersesEditor(preview.getLocation(), ticked.getFirst(),
                    preview.getVerses()[0].getChapter().getBook(), editing, getVersionText(editing), preview.getVerses().length,
                    reference -> {
                        if (reference == null) {
                            chosen.remove(editing.getBibleName());
                        } else {
                            chosen.put(editing.getBibleName(), reference);
                        }
                        editing = null;
                        updatePreview();
                    }, () -> {
                        editing = null;
                        updatePreview();
                    });
            editorPane.getChildren().add(editor);
            editor.focus();
        }
        listView.refresh();
    }

    /**
     * Get how the passage is shown in a version.
     *
     * @return the text of the passage in that version, or null if it isn't
     * known.
     */
    private BibleVersionText getVersionText(Bible bible) {
        int index = ticked.indexOf(bible);
        if (preview == null || index < 0 || index >= preview.getVersionTexts().size()) {
            return null;
        }
        return preview.getVersionTexts().get(index);
    }

    /**
     * A row with a tick box, showing where in the order the version comes and,
     * for a single passage, where the passage is in the version.
     */
    private final class VersionCell extends ListCell<Bible> {

        private final CheckBox checkBox = new CheckBox();
        private final Label name = new Label();
        private final Label location = new Label();
        private final Button change = new Button(LabelGrabber.INSTANCE.getLabel("bible.match.change.button"));
        private final HBox root;

        private VersionCell() {
            checkBox.setMouseTransparent(true);
            checkBox.setFocusTraversable(false);
            name.setMinWidth(Region.USE_PREF_SIZE);
            location.getStyleClass().add("bible-version-location");
            location.setTextOverrun(OverrunStyle.ELLIPSIS);
            location.setMinWidth(0);
            location.setMaxWidth(Double.MAX_VALUE);
            location.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(location, Priority.ALWAYS);
            change.setMinWidth(Region.USE_PREF_SIZE);
            change.setFocusTraversable(false);
            change.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
            change.setOnAction(event -> {
                editing = getItem();
                updatePreview();
            });
            root = new HBox(8, checkBox, name, location, change);
            root.setAlignment(Pos.CENTER_LEFT);
            setPrefWidth(0);
            setOnMouseClicked(event -> {
                if (getItem() != null) {
                    toggle(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Bible bible, boolean empty) {
            super.updateItem(bible, empty);
            setText(null);
            if (empty || bible == null) {
                setGraphic(null);
                return;
            }
            int position = ticked.indexOf(bible);
            checkBox.setSelected(position >= 0);
            name.setText(position >= 0 ? (position + 1) + ". " + bible.getBibleName() : bible.getBibleName());
            BibleVersionText text = getVersionText(bible);
            String where = text != null ? text.getLocation()
                    : position == 0 && preview != null ? preview.getLocation() : null;
            boolean unknown = text != null && (text.getMatch() == BibleVersionText.Match.BY_NUMBER || where == null);
            boolean hand = text != null && text.getMatch() == BibleVersionText.Match.CHOSEN;
            location.setText(position < 0 || preview == null ? "" : where == null
                    ? LabelGrabber.INSTANCE.getLabel("bible.not.in.version") : where);
            location.setGraphic(unknown && position > 0 ? QueleaIcon.WARNING.create(12) : null);
            location.setTooltip(unknown && position > 0 ? new Tooltip(LabelGrabber.INSTANCE.getLabel(where == null
                    ? "bible.match.missing.warning" : "bible.match.unknown.warning").replace("$1", bible.getBibleName()))
                    : hand ? new Tooltip(LabelGrabber.INSTANCE.getLabel("bible.match.chosen")) : null);
            location.getStyleClass().removeAll("bible-match-chosen", "bible-match-unknown");
            if (hand) {
                location.getStyleClass().add("bible-match-chosen");
            } else if (unknown && position > 0) {
                location.getStyleClass().add("bible-match-unknown");
            }
            boolean canChange = position > 0 && preview != null && preview.isParallel();
            change.setVisible(canChange);
            change.setManaged(canChange);
            setGraphic(root);
        }
    }
}
