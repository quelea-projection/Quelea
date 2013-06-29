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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * Quelea's about Dialog, displaying general features about the program.
 * <p/>
 * @author Michael
 */
public class AboutDialog extends Stage {

    /**
     * Create a new about dialog.
     * <p/>
     * @param owner the owner of the dialog (should be the main window.)
     */
    public AboutDialog() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        
        setTitle(LabelGrabber.INSTANCE.getLabel("help.about.title"));
        VBox layout = new VBox();
        Text headingText = new Text("Quelea: " + LabelGrabber.INSTANCE.getLabel("help.about.version") + " " + QueleaProperties.VERSION.getFullVersionString());
        headingText.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 20));
        layout.getChildren().add(headingText);
        layout.getChildren().add(new ImageView(new Image("file:icons/logo.png")));
        layout.getChildren().add(new Text(" "));
        layout.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("help.about.line1")));
        layout.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("help.about.line2")));
        layout.getChildren().add(new Text(" "));
        layout.getChildren().add(new Text("Java: " + System.getProperty("java.version")));
        layout.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("debug.location") + ": " + LoggerUtils.getHandlerFileLocation()));
        Button closeButton = new Button(LabelGrabber.INSTANCE.getLabel("help.about.close"));
        closeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        layout.getChildren().add(closeButton);
        
        setScene(new Scene(layout));
    }
}
