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
package org.quelea.services.livetext;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * An input dialog for Live Text.
 *
 * @author Arvid
 */
public class LiveTextDialog extends Stage {

    private static LiveTextDialog dialog;
    private final TextArea textField;
    private final Label messageLabel;
    private final Button clear;
    private int caretPosition;

    /**
     * Create our live text dialog.
     */
    public LiveTextDialog() {

        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        BorderPane mainPane = new BorderPane();
        mainPane.setMaxSize(500, 300);
        messageLabel = new Label();
        BorderPane.setMargin(messageLabel, new Insets(5));
        mainPane.setTop(messageLabel);
        textField = new TextArea();
        textField.setPrefColumnCount(5);
        textField.setMinSize(250, 150);
        textField.setWrapText(true);

        // Update the text for each punctuation or space character
        textField.setOnKeyTyped((KeyEvent event) -> {
            if (event.getCharacter().matches("\\p{Blank}") || event.getCharacter().matches("\\p{Punct}")) {
                event.consume();
                textField.insertText(textField.getCaretPosition(), event.getCharacter());
                setLiveText(textField.getText());
            }
        });

        // Use ctrl + enter to clear the text
        textField.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            if (event.getCode().equals(KeyCode.ENTER) && event.isControlDown()) {
                setLiveText("");
                textField.setText("");
            }
        });

        // Select section to display by clicking
        textField.setOnMouseClicked(e -> {
            setLiveText(textField.getText());
        });

        BorderPane.setMargin(textField, new Insets(5));
        mainPane.setCenter(textField);
        clear = new Button(LabelGrabber.INSTANCE.getLabel("clear.live.text"));
        clear.setDefaultButton(true);
        clear.setOnAction((ActionEvent t) -> {
            setLiveText("");
            textField.setText("");
        });

        Button exit = new Button(LabelGrabber.INSTANCE.getLabel("exit.live.text"));
        exit.setOnAction((ActionEvent t) -> {
            setLiveText("");
            dialog.close();
        });

        HBox okPane = new HBox(10);
        VBox.setMargin(okPane, new Insets(10));
        okPane.setAlignment(Pos.CENTER);
        okPane.getChildren().add(clear);
        okPane.getChildren().add(exit);
        BorderPane.setMargin(okPane, new Insets(5));
        BorderPane.setAlignment(okPane, Pos.CENTER);
        mainPane.setBottom(okPane);
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
        Utils.fxRunAndWait(() -> {
            dialog = new LiveTextDialog();
            dialog.setTitle(title);
            dialog.textField.clear();
            dialog.messageLabel.setText(message);
            dialog.messageLabel.setWrapText(true);
            dialog.getIcons().add(new Image("file:icons/live_text.png"));
            dialog.showAndWait();
        });
        while (dialog.isShowing()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
        }
        if (!dialog.isShowing()) {
            QueleaApp.get().getMobileLyricsServer().setText("");
        }
        return dialog.textField.getText();
    }

    /**
     * Send text to Mobile Lyrics.
     * @param text String to send to Mobile Lyrics.
     */
    private void setLiveText(String text) {
        if (text.contains("\n\n")) {
            caretPosition = textField.getCaretPosition();
            String[] slides = text.split("\n\n");
            int length = 0;
            for (String slide : slides) {
                length += slide.length();
                if (caretPosition <= length) {
                    text = slide;
                    break;
                }
                length += 2;
            }
        }
        if (text.length() > 200) {
            text = text.substring(text.indexOf(" ", text.length() - 200));
        }
        QueleaApp.get().getMobileLyricsServer().setText(text);
    }

}
