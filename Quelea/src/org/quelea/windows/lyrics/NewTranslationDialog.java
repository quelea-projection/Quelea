/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.lyrics;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 * Simple dialog to get the name of a new translation from the user.
 *
 * @author Michael
 */
public class NewTranslationDialog extends Stage {

    private static final NewTranslationDialog dialog = new NewTranslationDialog();
    private final TextField nameField;
    private final Button okButton;
    private final Button cancelButton;
    private List<String> existingNames;
    /**
     * Field used to set if the ok button was pressed.
     */
    private boolean ok;

    /**
     * Create a new NewTranslationDialog.
     */
    private NewTranslationDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("new.translation.title"));
        Utils.addIconsToStage(this);
        StackPane root = new StackPane();
        VBox rootVBox = new VBox();
        StackPane.setMargin(rootVBox, new Insets(10));
        root.getChildren().add(rootVBox);
        rootVBox.setSpacing(10);
        Label label = new Label(LabelGrabber.INSTANCE.getLabel("enter.translation.name.label"));
        rootVBox.getChildren().add(label);
        nameField = new TextField();
        nameField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String newStr) {
                if (newStr.isEmpty()) {
                    okButton.setDisable(true);
                    return;
                }
                for (String str : existingNames) {
                    if (newStr.trim().equalsIgnoreCase(str.trim())) {
                        okButton.setDisable(true);
                        return;
                    }
                }
                okButton.setDisable(false);
            }
        });
        rootVBox.getChildren().add(nameField);
        StackPane buttonWrapperPane = new StackPane();
        HBox buttonPane = new HBox(5);
        buttonPane.setAlignment(Pos.CENTER);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png", 16, 16, true, true)));
        okButton.setDisable(true);
        okButton.setDefaultButton(true);
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                ok = true;
                hide();
            }
        });
        buttonPane.getChildren().add(okButton);
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png", 16, 16, true, true)));
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                ok = false;
                hide();
            }
        });
        buttonPane.getChildren().add(cancelButton);
        buttonWrapperPane.getChildren().add(buttonPane);
        Region spacer = new Region();
        VBox.setVgrow(buttonWrapperPane, Priority.ALWAYS);
        rootVBox.getChildren().add(spacer);
        rootVBox.getChildren().add(buttonWrapperPane);
        setScene(new Scene(root));
        setResizable(false);
    }

    /**
     * Pop up the dialog and get the name of the translation.
     *
     * @param existingNames names of translations that already exist.
     * @return the name of the new translation.
     */
    public static String getTranslationName(List<String> existingNames) {
        dialog.existingNames = existingNames;
        dialog.nameField.clear();
        dialog.okButton.setDisable(true);
        dialog.ok = false;
        dialog.showAndWait();
        if (dialog.ok) {
            return dialog.nameField.getText();
        } else {
            return null;
        }
    }

}
