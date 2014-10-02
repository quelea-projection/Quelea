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
package org.quelea.windows.main.widgets;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.data.YoutubeInfo;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 * The dialog responsible for handling Youtube addition.
 * <p>
 * @author Michael
 */
public class YoutubeDialog extends Stage {

    private TextField urlField;
    private ImageView previewImg;
    private Label title;
    private ExecutorService previewExecutor = Executors.newSingleThreadExecutor();
    private Future<YoutubeInfo> previewFuture;
    private volatile YoutubeInfo curInfo;

    /**
     * Create a new youtube dialog for the user to select a youtube video url.
     */
    public YoutubeDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("add.youtube.button"));
        Utils.addIconsToStage(this);
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        Label label = new Label(LabelGrabber.INSTANCE.getLabel("youtube.url.label") + ":");
        urlField = new TextField();
        VBox.setMargin(urlField, new Insets(5));
        urlField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, final String t1) {
                previewImg.setImage(null);
                title.setText("");
                if (previewFuture != null) {
                    previewFuture.cancel(true);
                }
                String t2 = t1;
                if (t1.toLowerCase().startsWith("https")) {
                    t2 = t1.replaceFirst("https", "http");
                }
                curInfo = new YoutubeInfo(t2);
                previewFuture = previewExecutor.submit(new Callable<YoutubeInfo>() {

                    @Override
                    public YoutubeInfo call() throws Exception {
                        curInfo.initParams();
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                previewImg.setImage(curInfo.getPreviewImage());
                                title.setText(curInfo.getTitle());
                            }
                        });
                        return curInfo;
                    }
                });
            }
        });
        root.getChildren().add(label);
        root.getChildren().add(urlField);
        final Button okButton = new Button(LabelGrabber.INSTANCE.getLabel("add.video.button"), new ImageView(new Image("file:icons/tick.png")));
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        okButton.setDefaultButton(true);
        final Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                urlField.clear();
                curInfo = null;
                hide();
            }
        });
        HBox previewPane = new HBox(10);
        previewImg = new ImageView();
        previewImg.setFitWidth(240);
        previewImg.setFitHeight(180);
        previewImg.setPreserveRatio(false);
        title = new Label();
        title.setWrapText(true);
        title.setMinWidth(200);
        previewPane.getChildren().add(previewImg);
        previewPane.getChildren().add(title);
        root.getChildren().add(previewPane);
        HBox okPane = new HBox(10);
        VBox.setMargin(okPane, new Insets(10));
        okPane.setAlignment(Pos.CENTER);
        okPane.getChildren().add(okButton);
        okPane.getChildren().add(cancelButton);
        okButton.setDisable(true);
        urlField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                okButton.setDisable(t1.trim().isEmpty());
            }
        });
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);
        root.getChildren().add(okPane);
        setScene(new Scene(root));
        setResizable(false);
    }

    /**
     * Show the dialog, wait for it to close and get the corresponding location.
     * <p>
     * @return the location the user entered in the dialog.
     */
    public YoutubeInfo getLocation() {
        urlField.clear();
        showAndWait();
        try {
            previewFuture.get(3, TimeUnit.SECONDS);
        } catch (Exception ex) {
            //Never mind, timeout
        }
        if(urlField.getText().trim().isEmpty()) {
            return null;
        }
        if (curInfo == null) {
            return new YoutubeInfo(urlField.getText());
        } else {
            return curInfo;
        }
    }

}
