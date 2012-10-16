/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.notice;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.LyricCanvas;

/**
 * The dialog used to manage the notices.
 * @author Michael
 */
public class NoticeDialog extends Stage implements NoticesChangedListener {

    private Button newNoticeButton;
    private Button removeNoticeButton;
    private Button editNoticeButton;
    private Button doneButton;
    private ListView<Notice> noticeList;
    private List<NoticeDrawer> noticeDrawers;

    /**
     * Create a new notice dialog.
     * @param owner the owner of this dialog.
     */
    public NoticeDialog() {
        initStyle(StageStyle.UTILITY);
        BorderPane mainPane = new BorderPane();
        getIcons().add(new Image("file:icons/info.png"));
        noticeDrawers = new ArrayList<>();
        setTitle("Notices");
        newNoticeButton = new Button(LabelGrabber.INSTANCE.getLabel("new.notice.text"));
        newNoticeButton.setAlignment(Pos.CENTER);
        newNoticeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                Notice notice = NoticeEntryDialog.getNotice(null);
                if(notice != null) {
                    noticeList.getItems().add(notice);
                    for(NoticeDrawer drawer : noticeDrawers) {
                        drawer.addNotice(notice);
                    }
                }
            }
        });
        editNoticeButton = new Button(LabelGrabber.INSTANCE.getLabel("edit.notice.text"));
        editNoticeButton.setAlignment(Pos.CENTER);
        editNoticeButton.setDisable(true);
        editNoticeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                NoticeEntryDialog.getNotice(noticeList.getSelectionModel().getSelectedItem());
            }
        });
        removeNoticeButton = new Button(LabelGrabber.INSTANCE.getLabel("remove.notice.text"));
        removeNoticeButton.setAlignment(Pos.CENTER);
        removeNoticeButton.setDisable(true);
        removeNoticeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                Notice notice = noticeList.getSelectionModel().getSelectedItem();
                noticeList.getItems().remove(noticeList.getSelectionModel().getSelectedIndex());
                for(NoticeDrawer drawer : noticeDrawers) {
                    drawer.removeNotice(notice);
                }
            }
        });
        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(newNoticeButton);
        leftPanel.getChildren().add(editNoticeButton);
        leftPanel.getChildren().add(removeNoticeButton);
        BorderPane leftPanelBorder = new BorderPane();
        leftPanelBorder.setTop(leftPanel);
        mainPane.setLeft(leftPanelBorder);

        noticeList = new ListView<>();
        noticeList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Notice>() {

            @Override
            public void changed(ObservableValue<? extends Notice> ov, Notice t, Notice t1) {
                editNoticeButton.setDisable(noticeList.getSelectionModel().getSelectedItem() == null);
                removeNoticeButton.setDisable(noticeList.getSelectionModel().getSelectedItem() == null);
            }
        });
        mainPane.setCenter(noticeList);

        doneButton = new Button(LabelGrabber.INSTANCE.getLabel("done.text"), new ImageView(new Image("file:icons/tick.png")));
        doneButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                hide();
            }
        });
        BorderPane.setAlignment(doneButton, Pos.CENTER);
        BorderPane.setMargin(doneButton, new Insets(5));
        mainPane.setBottom(doneButton);
        setScene(new Scene(mainPane));
    }

    /**
     * Called when the notice status has updated, i.e. it's removed or the 
     * counter is decremented.
     * @param notices the list of notices currently in possession by the calling 
     * canvas.
     */
    @Override
    public void noticesUpdated(List<Notice> notices) {
//        ((DefaultListModel<Notice>) noticeList.getModel()).removeAllElements();
//        Set<Notice> noticesSet = new HashSet<>();
//        for (NoticeDrawer drawer : noticeDrawers) {
//            noticesSet.addAll(drawer.getNotices());
//        }
//        for (Notice notice : noticesSet) {
//            ((DefaultListModel<Notice>) noticeList.getModel()).addElement(notice);
//        }
    }


    /**
     * Register a canvas to be updated using this notice dialog.
     * @param canvas the canvas to register.
     */
    public void registerCanvas(LyricCanvas canvas) {
        noticeDrawers.add(canvas.getNoticeDrawer());
        canvas.getNoticeDrawer().addNoticeChangedListener(this);
    }

}
