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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.event.EventListenerList;
import org.quelea.services.languages.LabelGrabber;

/**
 *
 * @author Bronson
 */


public class PlanningCenterOnlineLoginDialog extends Stage {
    
    private Button okButton;
    private TextField userField;
    private TextField passwordField;
    private boolean isLoggedIn = false;
    private final PlanningCenterOnlineImportDialog importDialog;
    private final PlanningCenterOnlineParser parser;

    public PlanningCenterOnlineLoginDialog(PlanningCenterOnlineImportDialog importDlg, PlanningCenterOnlineParser parse) {
        importDialog = importDlg;
        parser = parse;
        
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        setTitle(LabelGrabber.INSTANCE.getLabel("pco.login.import.heading"));

        setupUI();
        
        setOnCloseRequest(new EventHandler<WindowEvent>() {
          public void handle(WindowEvent we) {
              System.out.println("Stage is closing");
          }
      });  
    }
    
    public void setupUI() {
        BorderPane mainPane = new BorderPane();
        final VBox centrePanel = new VBox();

        GridPane topPanel = new GridPane();

        userField = new TextField();
        GridPane.setHgrow(userField, Priority.ALWAYS);
        Label titleLabel = new Label(LabelGrabber.INSTANCE.getLabel("email.label"));
        GridPane.setConstraints(titleLabel, 1, 1);
        topPanel.getChildren().add(titleLabel);
        titleLabel.setLabelFor(userField);
        GridPane.setConstraints(userField, 2, 1);
        topPanel.getChildren().add(userField);

        passwordField = new TextField();
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        Label authorLabel = new Label(LabelGrabber.INSTANCE.getLabel("password.label"));
        GridPane.setConstraints(authorLabel, 1, 2);
        topPanel.getChildren().add(authorLabel);
        authorLabel.setLabelFor(passwordField);
        GridPane.setConstraints(passwordField, 2, 2);
        topPanel.getChildren().add(passwordField);

        centrePanel.getChildren().add(topPanel);

        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setDefaultButton(true);
        okButton.setOnAction((ActionEvent t) -> {
            if (login()) {
                isLoggedIn = true;
                hide();
                importDialog.onLogin();
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
        
        mainPane.setCenter(centrePanel);
        setScene(new Scene(mainPane));
    }    
    
    private boolean login() {
        return parser.login(userField.getText(), passwordField.getText());
    }
    
    public void start() {
        if (!isLoggedIn)
        {
            show();
        }
        else
        {
            // already logged in from previously
            importDialog.onLogin();
        }
    }
}
