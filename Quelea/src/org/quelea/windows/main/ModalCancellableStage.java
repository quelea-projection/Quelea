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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Cancellable;

/**
 * A stage displayed when a lengthy, cancellable operation is taking place.
 * @author Michael
 */
public class ModalCancellableStage extends Stage {

    private boolean cancel = false;
    private Cancellable cancellable;

    public ModalCancellableStage(String displayText) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setOnShowing((event) -> {
            centerOnScreen();
            cancel = false;
        });
        StackPane root = new StackPane();
        VBox items = new VBox(10);
        Label label = new Label(displayText);
        label.setAlignment(Pos.CENTER);
        items.getChildren().add(label);
        StackPane barPane = new StackPane();
        ProgressBar bar = new ProgressBar();
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.prefWidthProperty().bind(widthProperty().subtract(50));
        StackPane.setAlignment(bar, Pos.CENTER);
        barPane.getChildren().add(bar);
        barPane.setAlignment(Pos.CENTER);
        items.getChildren().add(barPane);
        StackPane buttonPane = new StackPane();
        Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.text"));
        StackPane.setAlignment(buttonPane, Pos.CENTER);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.getChildren().add(cancelButton);
        cancelButton.setAlignment(Pos.CENTER);
        cancelButton.setOnAction((event) -> {
            cancel = true;
            hide();
            cancellable.cancelOp();
        });
        items.getChildren().add(buttonPane);
        StackPane.setMargin(items, new Insets(10));
        root.getChildren().add(items);
        setScene(new Scene(root));
    }

    public void showAndAssociate(Cancellable cancellable) {
        this.cancellable = cancellable;
        show();
    }

    public boolean isCancel() {
        return cancel;
    }
}
