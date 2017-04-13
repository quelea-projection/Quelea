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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 * Dialog used for switching between bible translations.
 *
 * @author Michael
 */
public class SwitchBibleVersionDialog extends Stage {

    private ComboBox<Bible> comboBox = new ComboBox<>();
    private final Button okButton;
    private final Button cancelButton;
    private Bible selectedVersion;

    /**
     * Create the dialog to switch bible versions.
     */
    public SwitchBibleVersionDialog() {
        Utils.addIconsToStage(this);
        setOnShowing(event -> {
            selectedVersion = null;
        });
        VBox root = new VBox(5);
        Label switchToLabel = new Label(LabelGrabber.INSTANCE.getLabel("switch.to.text") + "...");
        root.getChildren().add(switchToLabel);
        root.getChildren().add(comboBox);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setDefaultButton(true);
        okButton.setOnAction(t -> {
            selectedVersion = comboBox.getValue();
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
        setResizable(false);
    }

    /**
     * Show the dialog and get the bible version to switch to (null if
     * cancelled.)
     *
     * @param exclude a bible to exclude (useful to exclude the current
     * translation sometimes.)
     * @return the bible version to switch to (null if cancelled.)
     */
    public Bible getSwitchVersion(Bible exclude) {
        Bible[] bibles = BibleManager.get().getBibles();
        for (Bible bible : bibles) {
            if (bible != exclude) {
                comboBox.getItems().add(bible);
            }
        }
        if (!comboBox.getItems().isEmpty()) {
            comboBox.getSelectionModel().select(0);
            showAndWait();
            return selectedVersion;
        } else {
            return null;
        }
    }

}
