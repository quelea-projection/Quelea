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
package org.quelea.services.notice;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.NumberSpinner;

/**
 * The entry dialog for creating a notice.
 * @author Michael
 */
public class NoticeEntryDialog extends Stage {

    private static NoticeEntryDialog dialog;
    private TextField text;
    private NumberSpinner times;
    private CheckBox infinite;
    private Button addButton;
    private Button cancelButton;
    private Notice notice;

    /**
     * Create a new notice entry dialog.
     * @param owner the owner of this dialog.
     */
    public NoticeEntryDialog() {
        setTitle(LabelGrabber.INSTANCE.getLabel("new.notice.heading"));
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        getIcons().add(new Image("file:icons/info.png"));
        text = new TextField();
        text.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                addButton.setDisable(t1.trim().isEmpty());
            }
        });
        times = new NumberSpinner(1,1);
        infinite = new CheckBox();
        infinite.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if(infinite.isSelected()) {
                    times.setDisable(true);
                }
                else {
                    times.setDisable(false);
                }
            }
        });

        GridPane mainPanel = new GridPane();
        
        Label noticeText = new Label(LabelGrabber.INSTANCE.getLabel("notice.text"));
        GridPane.setConstraints(noticeText, 0, 0);
        mainPanel.getChildren().add(noticeText);
        GridPane.setConstraints(text, 1, 0);
        mainPanel.getChildren().add(text);
        
        Label noticeTimesText = new Label(LabelGrabber.INSTANCE.getLabel("notice.times.text"));
        GridPane.setConstraints(noticeTimesText, 0, 1);
        mainPanel.getChildren().add(noticeTimesText);
        GridPane.setConstraints(times, 1, 1);
        mainPanel.getChildren().add(times);
        
        Label noticeInfiniteText = new Label(LabelGrabber.INSTANCE.getLabel("notice.infinite.question"));
        GridPane.setConstraints(noticeInfiniteText, 0, 2);
        mainPanel.getChildren().add(noticeInfiniteText);
        GridPane.setConstraints(infinite, 1, 2);
        mainPanel.getChildren().add(infinite);
        
        HBox southPanel = new HBox();
        addButton = new Button(LabelGrabber.INSTANCE.getLabel("add.notice.button"), new ImageView(new Image("file:icons/tick.png")));
        addButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                int numberTimes;
                if(infinite.isSelected()) {
                    numberTimes = Integer.MAX_VALUE;
                }
                else {
                    numberTimes = times.getNumber();
                }
                if(notice == null) {
                    notice = new Notice(text.getText(), numberTimes);
                }
                else {
                    notice.setText(text.getText());
                    notice.setTimes(numberTimes);
                }
                hide();
            }
        });
        southPanel.getChildren().add(addButton);
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.text"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                notice = null;
                hide();
            }
        });
        southPanel.getChildren().add(cancelButton);
        southPanel.setSpacing(5);
        southPanel.setAlignment(Pos.CENTER);
        BorderPane.setMargin(southPanel, new Insets(5));

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(mainPanel);
        mainPane.setBottom(southPanel);
        setScene(new Scene(mainPane));
    }

    /**
     * Get the notice text.
     * @return the notice text.
     */
    public String getNoticeText() {
        return text.getText();
    }

    /**
     * Get the number of times remaining (Integer.MAX_VALUE) if infinite.
     * @return the number of times remaining (Integer.MAX_VALUE) if infinite.
     */
    public int getTimes() {
        if (infinite.isSelected()) {
            return Integer.MAX_VALUE;
        }
        return (int) times.getNumber();
    }

    /**
     * Set the dialog to show the given notice.
     * @param notice the notice to show.
     */
    private void setNotice(Notice notice) {
        this.notice = notice;
        if (notice == null) {
            infinite.setSelected(false);
            times.setNumber(1);
            text.setText("");
            addButton.setText(LabelGrabber.INSTANCE.getLabel("add.notice.button"));
            addButton.setDisable(true);
        }
        else {
            infinite.setSelected(notice.getTimes() == Integer.MAX_VALUE);
            if (!infinite.isSelected()) {
                times.setNumber(notice.getTimes());
            }
            text.setText(notice.getText());
            addButton.setText(LabelGrabber.INSTANCE.getLabel("edit.notice.button"));
        }
    }

    /**
     * Get a notice that the user enters.
     * @param existing any existing notice to fill the dialog with.
     * @return the user-entered notice.
     */
    public static Notice getNotice(Notice existing) {
        if (dialog == null) {
            dialog = new NoticeEntryDialog();
        }
        dialog.setNotice(existing);
        dialog.showAndWait();
        return dialog.notice;
    }
}
