/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.help;

import java.awt.Desktop;
import java.io.File;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.DesktopApi;

/**
 * Quelea's about Dialog, displaying general features about the program and the
 * debug log location (so we can point any users here who may be looking for
 * it.)
 * <p/>
 * @author Michael
 */
public class AboutDialog extends Stage {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new about dialog.
     */
    public AboutDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        setTitle(LabelGrabber.INSTANCE.getLabel("help.about.title"));

        BorderPane newLayout = new BorderPane();
        ImageView logo = new ImageView(new Image("file:icons/full logo.png"));
        BorderPane.setAlignment(logo, Pos.CENTER);
        newLayout.setTop(logo);

        VBox subLayout = new VBox();
        Text headingText = new Text(LabelGrabber.INSTANCE.getLabel("help.about.version") + " " + QueleaProperties.VERSION.getVersionString());
        headingText.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 20));
        headingText.getStyleClass().add("text");
        subLayout.getChildren().add(headingText);
        subLayout.getChildren().add(new Text(" "));
        Text text1 = new Text(LabelGrabber.INSTANCE.getLabel("help.about.line1"));
        text1.getStyleClass().add("text");
        subLayout.getChildren().add(text1);
        Text text2 = new Text(LabelGrabber.INSTANCE.getLabel("help.about.line2"));
        text2.getStyleClass().add("text");
        subLayout.getChildren().add(text2);
        subLayout.getChildren().add(new Text(" "));
        subLayout.getChildren().add(new Label("Java: " + System.getProperty("java.version")));
        HBox debugBox = new HBox(5);
        debugBox.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("debug.location") + ":"));
        Text debugLogText = new Text(LoggerUtils.getHandlerFileLocation());
        debugLogText.getStyleClass().add("text");
        if(Desktop.isDesktopSupported()) {
            debugLogText.setCursor(Cursor.HAND);
            debugLogText.setFill(Color.BLUE);
            debugLogText.setStyle("-fx-underline: true;");
            debugLogText.setOnMouseClicked(t -> {
                DesktopApi.open(new File(LoggerUtils.getHandlerFileLocation()));
            });
        }
        debugBox.getChildren().add(debugLogText);
        subLayout.getChildren().add(debugBox);
        Button closeButton = new Button(LabelGrabber.INSTANCE.getLabel("help.about.close"));
        closeButton.setOnAction(t -> {
            hide();
        });
        newLayout.setCenter(subLayout);
        BorderPane.setMargin(subLayout, new Insets(10));
        BorderPane.setAlignment(closeButton, Pos.CENTER);
        BorderPane.setMargin(closeButton, new Insets(10));
        newLayout.setBottom(closeButton);

        Scene scene = new Scene(newLayout);
        if (QueleaProperties.get().getUseDarkTheme()) {
            scene.getStylesheets().add("org/modena_dark.css");
        }
        setScene(scene);
    }
}
