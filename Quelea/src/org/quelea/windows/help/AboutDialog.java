/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.QueleaProperties;

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
        layout.getChildren().add(new Label("Quelea " + LabelGrabber.INSTANCE.getLabel("help.about.version") + " " + QueleaProperties.VERSION.getVersionString()));
        layout.getChildren().add(new ImageView(new Image("file:icons/logo.png")));
        layout.getChildren().add(new Label(" "));
        layout.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("help.about.line1")));
        layout.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("help.about.line2")));
        layout.getChildren().add(new Label(" "));
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
