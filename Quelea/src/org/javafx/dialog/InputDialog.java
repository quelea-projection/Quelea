/*
 * This file is part of Quelea, free projection software for churches.
 * (C) 2012 Michael Berry
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
package org.javafx.dialog;

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
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 * A JavaFX simple input dialog - since one isn't provided for us.
 *
 * @author Michael
 */
public class InputDialog extends Stage {

    private static InputDialog dialog;
    private final TextField textField;
    private final Label messageLabel;
    private final Button okButton;

    /**
     * Create our input dialog.
     */
    private InputDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        BorderPane mainPane = new BorderPane();
        messageLabel = new Label();
        BorderPane.setMargin(messageLabel, new Insets(5));
        mainPane.setTop(messageLabel);
        textField = new TextField();
        BorderPane.setMargin(textField, new Insets(5));
        mainPane.setCenter(textField);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setDefaultButton(true);
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        BorderPane.setMargin(okButton, new Insets(5));
        BorderPane.setAlignment(okButton, Pos.CENTER);
        mainPane.setBottom(okButton);
        setScene(new Scene(mainPane));
    }

    /**
     * Display a dialog grabbing the user's input.
     *
     * @param message the message to display to the user on the dialog.
     * @param title the title of the dialog.
     * @return the user entered text.
     */
    public static String getUserInput(final String message, final String title) {
        Utils.fxRunAndWait(new Runnable() {

            @Override
            public void run() {
                dialog = new InputDialog();
                dialog.setTitle(title);
                dialog.textField.clear();
                dialog.messageLabel.setText(message);
                dialog.showAndWait();
            }
        });
        while(dialog.isShowing()) {
            try {
                Thread.sleep(10);
            }
            catch(InterruptedException ex) {
            }
        }
        return dialog.textField.getText();
    }

}
