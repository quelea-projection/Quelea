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
package org.quelea.services.importexport;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.NumberTextField;

/**
 * Shows a dialog to get a range of numbers to search for in the Kingsway Importer
 * <p/>
 * @author Ben Goodwin
 */
public class KingswayRangeInputDialog extends Stage {
    private static KingswayRangeInputDialog dialog;

    private final Button okButton;
    private final NumberTextField startNTF;
    private final NumberTextField endNTF;

    public KingswayRangeInputDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        setTitle(LabelGrabber.INSTANCE.getLabel("kingsway.range.import.heading"));

        BorderPane mainPane = new BorderPane();
        startNTF = new NumberTextField();
        endNTF = new NumberTextField();

        Label textPane = new Label(LabelGrabber.INSTANCE.getLabel("kingsway.range.import.text"));
        mainPane.setTop(textPane);
        BorderPane.setMargin(textPane, new Insets(5));

        HBox hb = new HBox();
        hb.getChildren().addAll(startNTF, new Label("-"), endNTF);
        BorderPane.setMargin(hb, new Insets(5));
        hb.setAlignment(Pos.CENTER);
        mainPane.setCenter(hb);

        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setDefaultButton(true);
        okButton.setOnAction((ActionEvent t) -> {
            if (endNTF.getNumber() > startNTF.getNumber()) {
                hide();
            }
        });
        BorderPane.setMargin(okButton, new Insets(5));
        BorderPane.setAlignment(okButton, Pos.CENTER);
        mainPane.setBottom(okButton);

        ChangeListener<Integer> cl = (ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
            if (newValue < 0 || newValue == null) {
                okButton.setDisable(true);
            } else {
                okButton.setDisable(false);
            }
        };

        startNTF.numberProperty().addListener(cl);
        endNTF.numberProperty().addListener(cl);

        setResizable(false);
        setScene(new Scene(mainPane));

    }

    /**
     * Display a dialog grabbing the user's input.
     *
     * @return the user entered text.
     */
    public static String getUserInput() {
        Utils.fxRunAndWait(() -> {
            dialog = new KingswayRangeInputDialog();
            dialog.showAndWait();
        });
        while (dialog.isShowing()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
        }
        return "" + dialog.startNTF.getNumber() + "," + dialog.endNTF.getNumber();
    }
}