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
package org.quelea.windows.main.widgets;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.ImageManager;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * A dialog showing the available test patterns that can be projected on the
 * displays.
 * <p>
 * @author Michael
 */
public class TestPaneDialog extends Stage {

    List<ImageView> ivs = new ArrayList<>();

    /**
     * Create a new test pane dialog.
     */
    public TestPaneDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("test.patterns.text"));
        Utils.addIconsToStage(this);
        setOnShowing(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                for(ImageView iv : ivs) {
                    iv.setEffect(null);
                }
            }
        });
        setOnHiding(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                clearTestImage();
            }
        });

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#dddddd;");
        Label explanation = new Label(LabelGrabber.INSTANCE.getLabel("test.patterns.explanation"));
        explanation.setWrapText(true);
        BorderPane.setMargin(explanation, new Insets(20));
        root.setTop(explanation);

        GridPane centrePane = new GridPane();
        centrePane.setPadding(new Insets(20));
        centrePane.setHgap(30);
        centrePane.setVgap(30);

        centrePane.add(getTestView("file:icons/SMPTE Bars.png", false), 0, 0);
        centrePane.add(getTestView("file:icons/position calibrate.png", false), 0, 1);
        centrePane.add(getTestView("file:icons/square wedges.png", true), 1, 0);
        centrePane.add(getTestView("file:icons/colorbands.png", false), 1, 1);
        root.setCenter(centrePane);

        StackPane bottomPane = new StackPane();
        Button closeButton = new Button(LabelGrabber.INSTANCE.getLabel("help.about.close"), new ImageView(new Image("file:icons/tick.png")));
        closeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        StackPane.setMargin(closeButton, new Insets(10));
        bottomPane.getChildren().add(closeButton);
        root.setBottom(bottomPane);

        setScene(new Scene(root));
    }

    private void clearTestImage() {
        setTestImage(null, false);
    }

    private void setTestImage(String uri, boolean preserveAspect) {
        Image img;
        if(uri==null) {
            img = null;
        }
        else {
            img = ImageManager.INSTANCE.getImage(uri);
        }
        QueleaApp.get().getProjectionWindow().setTestImage(img, preserveAspect);
        QueleaApp.get().getStageWindow().setTestImage(img, preserveAspect);
    }

    private ImageView getTestView(final String uri, final boolean preserveAspect) {
        final ImageView iv = new ImageView(ImageManager.INSTANCE.getImage(uri, 385, 216, false));
        iv.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                for(ImageView iv : ivs) {
                    iv.setEffect(null);
                }
                iv.setEffect(new DropShadow(20, Color.YELLOWGREEN));
                setTestImage(uri, preserveAspect);
            }
        });
        iv.setSmooth(true);
        iv.setPreserveRatio(false);
        iv.setFitWidth(385);
        iv.setFitHeight(216);
        ivs.add(iv);
        return iv;
    }

}
