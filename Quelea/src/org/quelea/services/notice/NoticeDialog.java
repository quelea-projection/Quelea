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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.DisplayCanvas;

/**
 * The dialog used to manage the notices.
 * <p/>
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
     */
    public NoticeDialog() {
        BorderPane mainPane = new BorderPane();
        getIcons().add(new Image("file:icons/info.png"));
        noticeDrawers = new ArrayList<>();
        setTitle(LabelGrabber.INSTANCE.getLabel("notices.heading"));
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
                Notice notice = NoticeEntryDialog.getNotice(noticeList.getSelectionModel().getSelectedItem());
                if(notice != null) {
                    noticeList.getItems().add(notice);
                    for(NoticeDrawer drawer : noticeDrawers) {
                        drawer.addNotice(notice);
                    }
                }
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
        VBox leftPanel = new VBox(5);
        newNoticeButton.setMaxWidth(Double.MAX_VALUE);
        editNoticeButton.setMaxWidth(Double.MAX_VALUE);
        removeNoticeButton.setMaxWidth(Double.MAX_VALUE);
        leftPanel.getChildren().add(newNoticeButton);
        leftPanel.getChildren().add(editNoticeButton);
        leftPanel.getChildren().add(removeNoticeButton);
        BorderPane leftPanelBorder = new BorderPane();
        BorderPane.setMargin(leftPanelBorder, new Insets(5));
        leftPanelBorder.setTop(leftPanel);
        mainPane.setLeft(leftPanelBorder);

        noticeList = new ListView<>();
        noticeList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Notice>() {
            @Override
            public void changed(ObservableValue<? extends Notice> ov, Notice t, Notice t1) {
                boolean disable = noticeList.getSelectionModel().getSelectedItem() == null;
                if(!noticeList.getItems().contains(noticeList.getSelectionModel().getSelectedItem())) {
                    disable = true;
                }
                editNoticeButton.setDisable(disable);
                removeNoticeButton.setDisable(disable);
            }
        });
        noticeList.getItems().addListener(new ListChangeListener<Notice>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Notice> change) {
                boolean disable = noticeList.getSelectionModel().getSelectedItem() == null;
                editNoticeButton.setDisable(disable);
                removeNoticeButton.setDisable(disable);
                if(disable) {
                    noticeList.getSelectionModel().clearSelection();
                }
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
     */
    @Override
    public void noticesUpdated() {
        Notice selected = noticeList.getSelectionModel().getSelectedItem();
        noticeList.getItems().clear();
        Set<Notice> noticesSet = new HashSet<>();
        for(NoticeDrawer drawer : noticeDrawers) {
            noticesSet.addAll(drawer.getNotices());
        }
        for(Notice notice : noticesSet) {
            noticeList.getItems().add(notice);
        }
        NoticeEntryDialog.noticesUpdated(noticesSet);
        noticeList.getSelectionModel().select(selected);
    }

    /**
     * Register a canvas to be updated using this notice dialog.
     * <p/>
     * @param canvas the canvas to register.
     */
    public void registerCanvas(DisplayCanvas canvas) {
        noticeDrawers.add(canvas.getNoticeDrawer());
        canvas.getNoticeDrawer().addNoticeChangedListener(this);
    }
}
