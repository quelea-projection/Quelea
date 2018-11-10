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

import com.sun.istack.Nullable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dialog used for selecting multiple bible translations.
 *
 * @author Arvid
 */
public class SelectBibleVersionDialog extends Stage {

    private ListView<Bible> listView = new ListView<>();
    private final Button okButton;
    private final Button cancelButton;
    private ArrayList<Bible> selectedVersion = new ArrayList<>();
    private ArrayList<String> preSelect = new ArrayList<>();

    /**
     * Create the dialog to switch bible versions.
     */
    public SelectBibleVersionDialog() {
        Utils.addIconsToStage(this);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setOnShowing(event -> {
            listView.refresh();
        });
        VBox root = new VBox(5);
        Label switchToLabel = new Label(LabelGrabber.INSTANCE.getLabel("change.bible.version.text"));
        root.getChildren().add(switchToLabel);
        root.getChildren().add(listView);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setDefaultButton(true);
        okButton.setOnAction(t -> {
            selectedVersion.clear();
            for (String s : preSelect) {
                for (Bible b : listView.getItems()) {
                    if (b.getBibleName().equals(s))
                        selectedVersion.add(b);
                }
            }
            hide();
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(t -> {
            hide();
        });
        HBox buttonPanel = new HBox(5);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(okButton, cancelButton);
        root.getChildren().add(buttonPanel);
        StackPane containerPane = new StackPane();
        StackPane.setMargin(root, new Insets(10));
        containerPane.getChildren().add(root);
        setScene(new Scene(containerPane));
    }

    /**
     * Show the dialog and get the bible version to switch to (null if cancelled).
     *
     * @param exclude a bible to exclude (e.g. the current translation), can be null
     * @return the bible version to switch to (null if cancelled.)
     */
    public ArrayList<Bible> getAddVersion(@Nullable Bible exclude) {
        return getAddVersion(exclude, "");
    }

    /**
     * Show the dialog and get the bible version to switch to (null if cancelled).
     *
     * @param exclude        a bible to exclude (e.g. the current translation), can be null
     * @param selectedBibles list of bibles to select on open
     * @return the bible version to switch to (null if cancelled.)
     */
    public ArrayList<Bible> getAddVersion(@Nullable Bible exclude, List<Bible> selectedBibles) {
        StringBuilder sb = new StringBuilder();
        for (Bible b : selectedBibles) {
            sb.append(b.getBibleName()).append(",");
        }
        return getAddVersion(exclude, sb.toString());
    }

    /**
     * Show the dialog and get the bible version to switch to (null if cancelled).
     *
     * @param exclude a bible to exclude (e.g. the current translation), can be null
     * @param select  list bibles to select on open as comma-separated values
     * @return the bible version to switch to (null if cancelled.)
     */
    public ArrayList<Bible> getAddVersion(@Nullable Bible exclude, String select) {
        Bible[] bibles = BibleManager.get().getBibles();
        preSelect = new ArrayList<>(Arrays.asList(select.split(",")));
        for (Bible bible : bibles) {
            if (bible != exclude) {
                listView.getItems().add(bible);
                if (preSelect.contains(bible.getBibleName())) {
                    selectedVersion.add(bible);
                    listView.getSelectionModel().select(listView.getItems().size() - 1);
                }
            }
        }
        listView.setCellFactory(lv -> {
            ListCell<Bible> cell = new ListCell<>() {
                @Override
                protected void updateItem(Bible t, boolean empty) {
                    super.updateItem(t, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        if (isSelected()) {
                            int pos = preSelect.indexOf(t.getBibleName());
                            setText(pos > -1 ? (pos + 1) + ". " + t.getBibleName() : t.getBibleName());
                        } else {
                            setText(t.getBibleName());
                        }
                    }
                }
            };

            cell.setOnMouseClicked(e -> {
                if (cell.getItem() != null) {
                    if (!e.isControlDown())
                        preSelect.clear();
                    if (!cell.isSelected()) {
                        preSelect.remove(cell.getItem().getBibleName());
                    } else if (!preSelect.contains(cell.getItem().getBibleName())) {
                        if (!e.isControlDown())
                            preSelect.clear();
                        preSelect.add(cell.getItem().getBibleName());
                    }

                    listView.refresh();
                }
            });

            return cell;
        });
        if (!listView.getItems().isEmpty()) {
            showAndWait();
            return selectedVersion;
        } else {
            return null;
        }
    }

}
