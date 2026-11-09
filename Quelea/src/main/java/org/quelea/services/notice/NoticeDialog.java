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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayCanvas;

import org.quelea.windows.style.StyleManager;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.services.utils.Utils;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * The dialog used to manage the notices.
 * <p/>
 * @author Michael
 */
public class NoticeDialog extends Stage {

    private Button newNoticeButton;
    private Button removeNoticeButton;
    private Button editNoticeButton;
    private Button doneButton;
    private ListView<Notice> noticeList;
    private ListView<Notice> noticeTemplates;
    private List<NoticeDrawer> noticeDrawers;
    private final Text savedNoticesLabel;
    private Thread noticeTemplatesUpdateThread;
    private ObservableList<Notice> readOnlyNotices;

    /**
     * Create a new notice dialog.
     */
    public NoticeDialog() {
        BorderPane mainPane = new BorderPane();
        Utils.addIconsToStage(this);
        noticeDrawers = new ArrayList<>();
        setTitle(LabelGrabber.INSTANCE.getLabel("notices.heading"));
        newNoticeButton = new Button(LabelGrabber.INSTANCE.getLabel("new.notice.text"), QueleaIcon.ADD.create());
        newNoticeButton.setAlignment(Pos.CENTER);
        newNoticeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                Notice notice = NoticeEntryDialog.getNotice(null, false);
                if (notice != null) {
                    addNotice(notice);
                }
            }
        });
        editNoticeButton = new Button(LabelGrabber.INSTANCE.getLabel("edit.notice.text"), QueleaIcon.EDIT.create());
        editNoticeButton.setAlignment(Pos.CENTER);
        editNoticeButton.setDisable(true);
        editNoticeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                Notice notice = NoticeEntryDialog.getNotice(noticeList.getSelectionModel().getSelectedItem(), false);
                if (notice != null) {
                    noticeList.getItems().add(notice);
                    for (NoticeDrawer drawer : noticeDrawers) {
                        drawer.addNotice(notice);
                    }
                }
            }
        });
        removeNoticeButton = new Button(LabelGrabber.INSTANCE.getLabel("remove.notice.text"), QueleaIcon.DELETE.create());
        removeNoticeButton.setAlignment(Pos.CENTER);
        removeNoticeButton.setDisable(true);
        removeNoticeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                removeNotice(noticeList.getSelectionModel().getSelectedItem());
            }
        });
        getNoticesFromFile();
        // Toolbar with the actions for the current notices
        HBox toolbar = new HBox(newNoticeButton, editNoticeButton, removeNoticeButton);
        toolbar.getStyleClass().add("dialog-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        mainPane.setTop(toolbar);

        // Saved notices (templates) in a column on the left
        savedNoticesLabel = new Text(LabelGrabber.INSTANCE.getLabel("saved.notices"));
        savedNoticesLabel.getStyleClass().addAll("text", "strip-title");
        VBox leftPanel = new VBox(6, savedNoticesLabel, noticeTemplates);
        VBox.setVgrow(noticeTemplates, Priority.ALWAYS);
        leftPanel.setPrefWidth(220);
        BorderPane.setMargin(leftPanel, new Insets(12, 0, 12, 12));
        mainPane.setLeft(leftPanel);

        noticeList = new ListView<>();
        readOnlyNotices = FXCollections.unmodifiableObservableList(noticeList.getItems());
        noticeList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Notice>() {
            @Override
            public void changed(ObservableValue<? extends Notice> ov, Notice t, Notice t1) {
                boolean disable = noticeList.getSelectionModel().getSelectedItem() == null;
                if (!noticeList.getItems().contains(noticeList.getSelectionModel().getSelectedItem())) {
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
                if (disable) {
                    noticeList.getSelectionModel().clearSelection();
                }
            }
        });

        Text currentNoticesLabel = new Text(LabelGrabber.INSTANCE.getLabel("notices.heading"));
        currentNoticesLabel.getStyleClass().addAll("text", "strip-title");
        VBox centerPanel = new VBox(6, currentNoticesLabel, noticeList);
        VBox.setVgrow(noticeList, Priority.ALWAYS);
        BorderPane.setMargin(centerPanel, new Insets(12));
        mainPane.setCenter(centerPanel);

        noticeTemplates.setOnMouseClicked(e -> {
            if (noticeTemplates.getSelectionModel().getSelectedItem() != null) {
                useTemplate(noticeTemplates.getSelectionModel().getSelectedItem());
                noticeTemplates.getSelectionModel().clearSelection();
            }
        });

        doneButton = new Button(LabelGrabber.INSTANCE.getLabel("done.text"), QueleaIcon.CHECK.create());
        StyleManager.markPrimaryButton(doneButton);
        doneButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                hide();
            }
        });
        BorderPane.setAlignment(doneButton, Pos.CENTER);
        BorderPane.setMargin(doneButton, new Insets(5));
        mainPane.setBottom(doneButton);
        Scene scene = new Scene(mainPane);
        StyleManager.apply(scene);
        setScene(scene);
    }

    /**
     * Offer a saved notice to be shown. It only goes on screen if the user
     * confirms it in the dialog that this opens.
     * <p/>
     * @param template the saved notice.
     */
    public void useTemplate(Notice template) {
        show();
        toFront();
        template.setTimes(template.getOriginalTimes());
        Notice notice = NoticeEntryDialog.getNotice(template, true);
        if (notice != null && NoticeEntryDialog.closed) {
            addNotice(notice);
        }
    }

    /**
     * Show a notice on every registered canvas and list it here.
     * <p/>
     * @param notice the notice to show.
     */
    public void addNotice(Notice notice) {
        noticeList.getItems().add(notice);
        for (NoticeDrawer drawer : noticeDrawers) {
            drawer.addNotice(notice);
        }
    }

    /**
     * Stop showing a notice. One that is scrolling past right now finishes
     * its pass.
     * <p/>
     * @param notice the notice to remove.
     */
    public void removeNotice(Notice notice) {
        noticeList.getItems().remove(notice);
        for (NoticeDrawer drawer : noticeDrawers) {
            drawer.removeNotice(notice);
        }
    }

    /**
     * The notices being shown, kept up to date as they are added, removed
     * or counted down. Read-only; use {@link #addNotice} and
     * {@link #removeNotice} to change it.
     * <p/>
     * @return the current notices.
     */
    public ObservableList<Notice> getCurrentNotices() {
        return readOnlyNotices;
    }

    /**
     * Called when the notice status has updated, i.e. it's removed or the
     * counter is decremented.
     */
    public void noticesUpdated() {
        Notice selected = noticeList.getSelectionModel().getSelectedItem();
        noticeList.getItems().clear();
        Set<Notice> noticesSet = new HashSet<>();
        for (NoticeDrawer drawer : noticeDrawers) {
            noticesSet.addAll(drawer.getNotices());
        }
        for (Notice notice : noticesSet) {
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
    }

    /**
     * Get the list of stored notices.
     * @return list of notice templates
     */
    public ListView<Notice> getTemplates() {
        return noticeTemplates;
    }

    /**
     * Read the stored notice files and add them to the list.
     */
    private void getNoticesFromFile() {
        noticeTemplates = new ListView<>();
        final File[] files = new File(QueleaProperties.get().getNoticeDir().getAbsolutePath()).listFiles();
        if (noticeTemplatesUpdateThread != null && noticeTemplatesUpdateThread.isAlive()) {
            return;
        }
        noticeTemplatesUpdateThread = new Thread() {
            @Override
            public void run() {
                if (files != null) {
                    for (final File file : files) {
                        Platform.runLater(() -> {
                            Notice n = NoticeFileHandler.noticeFromFile(file);
                            if (n != null) {
                                noticeTemplates.getItems().add(n);
                            }
                        });
                    }
                }
            }
        };
        noticeTemplatesUpdateThread.start();
    }

}