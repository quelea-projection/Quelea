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

import java.util.Collections;
import java.util.Set;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.NumberSpinner;
import org.quelea.windows.newsong.FontSelectionDialog;

/**
 * The entry dialog for creating a notice.
 * <p/>
 * @author Michael
 */
public class NoticeEntryDialog extends Stage {

    private static FontSelectionDialog fontSelectionDialog;
    private final ComboBox<String> fontSelection;
    private final Button fontExpandButton;
    private static NoticeEntryDialog dialog;
    private TextField text;
    private NumberSpinner times;
    private ColorPicker colourPicker;
    private CheckBox infinite;
    private Button addButton;
    private Button cancelButton;
    private Notice notice;
    private boolean noticeRemoved;

    /**
     * Create a new notice entry dialog.
     */
    public NoticeEntryDialog() {
        setTitle(LabelGrabber.INSTANCE.getLabel("new.notice.heading"));
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        getIcons().add(new Image("file:icons/info.png"));
        colourPicker = new ColorPicker(Color.WHITE);
        colourPicker.setStyle("-fx-color-label-visible: false ;");
        if (fontSelectionDialog == null) {
            fontSelectionDialog = new FontSelectionDialog();
        }
        fontSelection = new ComboBox<>();
        fontSelection.setMaxWidth(Integer.MAX_VALUE);
        fontSelection.getItems().addAll(fontSelectionDialog.getChosenFonts());
        Collections.sort(fontSelection.getItems());
        HBox.setHgrow(fontSelection, Priority.ALWAYS);
        fontExpandButton = new Button("...");
        fontExpandButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("more.fonts.label") + "..."));
        Utils.setToolbarButtonStyle(fontExpandButton);
        fontExpandButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                fontSelectionDialog.showAndWait();
                String selected = fontSelection.getSelectionModel().getSelectedItem();
                fontSelection.getItems().clear();
                fontSelection.getItems().addAll(fontSelectionDialog.getChosenFonts());
                Collections.sort(fontSelection.getItems());
                fontSelection.getSelectionModel().select(selected);
            }
        });
        HBox fontBox = new HBox(5);
        fontBox.getChildren().add(fontSelection);
        fontBox.getChildren().add(fontExpandButton);

        text = new TextField();
        text.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                addButton.setDisable(t1.trim().isEmpty());
            }
        });
        times = new NumberSpinner(1, 1);
        infinite = new CheckBox();
        infinite.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (infinite.isSelected()) {
                    times.setDisable(true);
                } else {
                    times.setDisable(false);
                }
            }
        });

        GridPane mainPanel = new GridPane();
        mainPanel.setHgap(5);
        mainPanel.setVgap(5);

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

        Label noticeColorText = new Label(LabelGrabber.INSTANCE.getLabel("notice.colour.text"));
        GridPane.setConstraints(noticeColorText, 0, 3);
        mainPanel.getChildren().add(noticeColorText);
        GridPane.setConstraints(colourPicker, 1, 3);
        mainPanel.getChildren().add(colourPicker);

        Label noticeFontText = new Label(LabelGrabber.INSTANCE.getLabel("notice.font.text"));
        GridPane.setConstraints(noticeFontText, 0, 4);
        mainPanel.getChildren().add(noticeFontText);
        GridPane.setConstraints(fontBox, 1, 4);
        mainPanel.getChildren().add(fontBox);

        HBox southPanel = new HBox();
        addButton = new Button(LabelGrabber.INSTANCE.getLabel("add.notice.button"), new ImageView(new Image("file:icons/tick.png")));
        addButton.setDefaultButton(true);
        addButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                int numberTimes;
                if (infinite.isSelected()) {
                    numberTimes = Integer.MAX_VALUE;
                } else {
                    numberTimes = times.getNumber();
                }
                boolean edit = true;
                if (notice == null) {
                    edit = false;
                    notice = new Notice(text.getText(), numberTimes, new SerializableColor(colourPicker.getValue()), new SerializableFont(Font.font(fontSelection.getValue(), FontWeight.NORMAL, FontPosture.REGULAR, QueleaProperties.get().getNoticeFontSize())));
                } else {
                    notice.setText(text.getText());
                    notice.setTimes(numberTimes);
                    notice.setColor(new SerializableColor(colourPicker.getValue()));
                    notice.setFont(new SerializableFont(Font.font(fontSelection.getValue(), FontWeight.NORMAL, FontPosture.REGULAR, QueleaProperties.get().getNoticeFontSize())));
                }
                if (edit && noticeRemoved) {
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("notice.expired.title"), LabelGrabber.INSTANCE.getLabel("notice.expired.text")).addYesButton(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                        }
                    }).addNoButton(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            notice = null;
                        }
                    }).build().showAndWait();
                } else if (edit) {
                    notice = null;
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
        BorderPane.setMargin(mainPanel, new Insets(5));
        mainPane.setCenter(mainPanel);
        mainPane.setBottom(southPanel);
        setScene(new Scene(mainPane));
    }

    private void setNoticeRemoved() {
        noticeRemoved = true;
    }

    /**
     * Get the notice text.
     * <p/>
     * @return the notice text.
     */
    public String getNoticeText() {
        return text.getText();
    }

    /**
     * Get the number of times remaining (Integer.MAX_VALUE) if infinite.
     * <p/>
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
     * <p/>
     * @param notice the notice to show.
     */
    private void setNotice(Notice notice) {
        this.notice = notice;
        if (notice == null) {
            infinite.setSelected(false);
            times.setNumber(1);
            text.setText("");
            colourPicker.setValue(Color.WHITE);
            colourPicker.fireEvent(new ActionEvent());
            fontSelection.setValue("Arial");
            addButton.setText(LabelGrabber.INSTANCE.getLabel("add.notice.button"));
            addButton.setDisable(true);
        } else {
            infinite.setSelected(notice.getTimes() == Integer.MAX_VALUE);
            if (!infinite.isSelected()) {
                times.setNumber(notice.getTimes());
            }
            text.setText(notice.getText());
            colourPicker.setValue(notice.getColor().getColor());
            colourPicker.fireEvent(new ActionEvent());
            fontSelection.setValue(notice.getFont().getFont().getFamily());
            addButton.setText(LabelGrabber.INSTANCE.getLabel("edit.notice.button"));
        }
    }

    /**
     * Get a notice that the user enters.
     * <p/>
     * @param existing any existing notice to fill the dialog with.
     * @return the user-entered notice.
     */
    public static Notice getNotice(Notice existing) {
        if (dialog == null) {
            dialog = new NoticeEntryDialog();
        }
        dialog.noticeRemoved = false;
        dialog.setNotice(existing);
        dialog.text.requestFocus();
        dialog.showAndWait();
        return dialog.notice;
    }

    public static void noticesUpdated(Set<Notice> noticeSet) {
        if (dialog.notice != null && !noticeSet.contains(dialog.notice)) {
            dialog.setNoticeRemoved();
        }
    }
}
