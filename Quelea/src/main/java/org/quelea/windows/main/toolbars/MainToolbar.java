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
package org.quelea.windows.main.toolbars;

import java.util.Date;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import javax.swing.Timer;

import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.AddDVDActionHandler;
import org.quelea.windows.main.actionhandlers.AddImageActionHandler;
import org.quelea.windows.main.actionhandlers.AddPdfActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.AddVideoActionHandler;
import org.quelea.windows.main.actionhandlers.AddTimerActionHandler;
import org.quelea.windows.main.actionhandlers.AddWebActionHandler;
import org.quelea.windows.main.actionhandlers.RecordButtonHandler;
import org.quelea.windows.main.actionhandlers.NewScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;
import org.quelea.windows.main.actionhandlers.OpenScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.PrintScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.QuickInsertActionHandler;
import org.quelea.windows.main.actionhandlers.SaveScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.ShowNoticesActionHandler;
import org.quelea.windows.style.QueleaIcon;

/**
 * Quelea's main toolbar.
 * <p/>
 *
 * @author Michael
 */
public class MainToolbar extends ToolBar {

    private final Button newScheduleButton;
    private final Button openScheduleButton;
    private final Button saveScheduleButton;
    private final Button printScheduleButton;
    private final Button newSongButton;
    private final Button quickInsertButton;
    private final Button manageNoticesButton;
    private final MenuButton add;
    private final ProgressIndicator loadingView;
    private final StackPane dvdImageStack;
    private final ToggleButton recordAudioButton;
    private final ProgressBar pb = new ProgressBar(0);
    private Dialog setRecordinPathWarning;
    private RecordButtonHandler recordingsHandler;
    private TextField recordingPathTextField;
    private boolean recording;
    private long openTime;
    private long recTime;
    private Timer recCount;

    /**
     * Create the toolbar and any associated shortcuts.
     */
    public MainToolbar() {
        newScheduleButton = getButton(QueleaIcon.NEW_SCHEDULE, "new.schedule.tooltip");
        newScheduleButton.setOnAction(new NewScheduleActionHandler());
        getItems().add(newScheduleButton);

        openScheduleButton = getButton(QueleaIcon.OPEN, "open.schedule.tooltip");
        openScheduleButton.setOnAction(new OpenScheduleActionHandler());
        getItems().add(openScheduleButton);

        saveScheduleButton = getButton(QueleaIcon.SAVE, "save.schedule.tooltip");
        saveScheduleButton.setOnAction(new SaveScheduleActionHandler(false));
        getItems().add(saveScheduleButton);

        printScheduleButton = getButton(QueleaIcon.PRINT, "print.schedule.tooltip");
        printScheduleButton.setOnAction(new PrintScheduleActionHandler());
        getItems().add(printScheduleButton);

        getItems().add(new Separator());

        newSongButton = getButton(QueleaIcon.NEW_SONG, "new.song.tooltip");
        newSongButton.setOnAction(new NewSongActionHandler());
        getItems().add(newSongButton);

        getItems().add(new Separator());

        quickInsertButton = getButton(QueleaIcon.QUICK_INSERT, "quick.insert.text");
        quickInsertButton.setOnAction(new QuickInsertActionHandler());
        getItems().add(quickInsertButton);

        add = new MenuButton("", QueleaIcon.ADD_ITEM.create(QueleaIcon.TOOLBAR_SIZE));
        Utils.setToolbarButtonStyle(add);
        getItems().add(add);
        add.setOnMouseEntered(evt -> {
            QueleaApp.get().getMainWindow().requestFocus();
            add.show();
            openTime = new Date().getTime();
        });

        // Avoid menu being closed if users click to open it
        add.setOnMouseClicked(e -> {
            if (new Date().getTime() - openTime < 1000) {
                add.show();
            }
        });

        MenuItem addPresentationButton = getMenuItem(QueleaIcon.PRESENTATION, "add.presentation.tooltip");
        addPresentationButton.setOnAction(new AddPowerpointActionHandler());
        add.getItems().add(addPresentationButton);

        MenuItem addMultimediaButton = getMenuItem(QueleaIcon.VIDEO, "add.multimedia.tooltip");
        addMultimediaButton.setOnAction(new AddVideoActionHandler());
        add.getItems().add(addMultimediaButton);

        MenuItem addTimerButton = getMenuItem(QueleaIcon.TIMER, "add.timer.tooltip");
        addTimerButton.setOnAction(new AddTimerActionHandler());
        add.getItems().add(addTimerButton);

        loadingView = new ProgressIndicator();
        loadingView.setMaxSize(20, 20);
        dvdImageStack = new StackPane();
        dvdImageStack.getChildren().add(QueleaIcon.DVD.create(QueleaIcon.TOOLBAR_SIZE));

        MenuItem addDVDButton = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.dvd.button"), dvdImageStack);
        addDVDButton.setOnAction(new AddDVDActionHandler());
        if (!Utils.isMac()) {
            add.getItems().add(addDVDButton);
        }

        MenuItem addPdfButton = getMenuItem(QueleaIcon.PDF, "add.pdf.tooltip");
        addPdfButton.setOnAction(new AddPdfActionHandler());
        add.getItems().add(addPdfButton);

        MenuItem addWebButton = getMenuItem(QueleaIcon.WEB, "add.website");
        addWebButton.setOnAction(new AddWebActionHandler());
        add.getItems().add(addWebButton);

        MenuItem addImageGroupButton = getMenuItem(QueleaIcon.IMAGES, "add.images.panel");
        addImageGroupButton.setOnAction(new AddImageActionHandler());
        add.getItems().add(addImageGroupButton);

        getItems().add(new Separator());

        manageNoticesButton = getButton(QueleaIcon.NOTICES, "manage.notices.tooltip");
        manageNoticesButton.setOnAction(new ShowNoticesActionHandler());
        getItems().add(manageNoticesButton);

        // Auto-hide add menu
        quickInsertButton.setOnMouseEntered(evt -> {
            add.hide();
        });
        manageNoticesButton.setOnMouseEntered(evt -> {
            add.hide();
        });
        QueleaApp.get().getMainWindow().getMainPanel().setOnMouseEntered(evt -> {
            add.hide();
        });
        QueleaApp.get().getMainWindow().getMainMenuBar().setOnMouseEntered(evt -> {
            add.hide();
        });

        recordingsHandler = new RecordButtonHandler();

        recordingPathTextField = new TextField();
        recordingPathTextField.setMinWidth(Region.USE_PREF_SIZE);
        recordingPathTextField.setMaxWidth(Region.USE_PREF_SIZE);

        // Set dynamic TextField width
        recordingPathTextField.textProperty().addListener((ov, prevText, currText) -> {
            Platform.runLater(() -> {
                Text text = new Text(currText);
                text.getStyleClass().add("text");
                text.setFont(recordingPathTextField.getFont());
                double width = text.getLayoutBounds().getWidth()
                        + recordingPathTextField.getPadding().getLeft() + recordingPathTextField.getPadding().getRight()
                        + 2d;
                recordingPathTextField.setPrefWidth(width);
                recordingPathTextField.positionCaret(recordingPathTextField.getCaretPosition());
            });
        });

        recordAudioButton = new ToggleButton("", QueleaIcon.RECORD.create(QueleaIcon.TOOLBAR_SIZE));
        recordAudioButton.getStyleClass().add("record-button");
        Utils.setToolbarButtonStyle(recordAudioButton);
        recording = false;
        recordAudioButton.setOnMouseClicked(e -> {
            if (!QueleaProperties.get().getRecordingsPath().equals("")) {
                if (recording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            } else {
                recordAudioButton.setSelected(false);
                Dialog.Builder setRecordingWarningBuilder = new Dialog.Builder()
                        .create()
                        .setTitle(LabelGrabber.INSTANCE.getLabel("recording.warning.title"))
                        .setMessage(LabelGrabber.INSTANCE.getLabel("recording.warning.message"))
                        .addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), (ActionEvent t) -> {
                            setRecordinPathWarning.hide();
                            setRecordinPathWarning = null;
                        });
                setRecordinPathWarning = setRecordingWarningBuilder.setWarningIcon().build();
                setRecordinPathWarning.showAndWait();
            }
        });
        recordAudioButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("record.audio.tooltip")));
        getItems().add(recordAudioButton);

    }

    public void setToggleButtonText(String text) {
        recordAudioButton.setText(text);
    }

    /**
     * Create a flat toolbar button showing an icon and a tooltip.
     */
    private Button getButton(QueleaIcon icon, String tooltipLabel) {
        Button button = new Button("", icon.create(QueleaIcon.TOOLBAR_SIZE));
        Utils.setToolbarButtonStyle(button);
        button.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel(tooltipLabel)));
        return button;
    }

    private MenuItem getMenuItem(QueleaIcon icon, String label) {
        return new MenuItem(LabelGrabber.INSTANCE.getLabel(label), icon.create(QueleaIcon.TOOLBAR_SIZE));
    }

    /**
     * Set if the DVD is loading.
     * <p>
     *
     * @param loading true if it's loading, false otherwise.
     */
    public void setDVDLoading(boolean loading) {
        if (loading && !dvdImageStack.getChildren().contains(loadingView)) {
            dvdImageStack.getChildren().add(loadingView);
        } else if (!loading) {
            dvdImageStack.getChildren().remove(loadingView);
        }
    }

    public void startRecording() {
        recordAudioButton.setSelected(true);
        recording = true;
//        getItems().add(pb);
        getItems().add(recordingPathTextField);
        recordAudioButton.setText("Recording...");
        recordAudioButton.setSelected(true);
        recordingsHandler = new RecordButtonHandler();
        recordingsHandler.passVariables("rec", pb, recordingPathTextField, recordAudioButton);
        final int interval = 1000; // 1000 ms
        recCount = new Timer(interval, (java.awt.event.ActionEvent e) -> {
            recTime += interval;
            setTime(recTime, recordAudioButton);
        });
        recCount.start();
    }

    public void stopRecording() {
        recordingsHandler.passVariables("stop", pb, recordingPathTextField, recordAudioButton);
        recordAudioButton.setSelected(false);
        recording = false;
        recCount.stop();
//        getItems().remove(pb);
        getItems().remove(recordingPathTextField);
        recordAudioButton.setText("");
        recordAudioButton.setSelected(false);
        recTime = 0;
    }

    /**
     * @return the selected state of the record button, true while recording.
     */
    public ReadOnlyBooleanProperty recordingProperty() {
        return recordAudioButton.selectedProperty();
    }

    public RecordButtonHandler getRecordButtonHandler() {
        return recordingsHandler;
    }

    /**
     * Method to set elapsed time on ToggleButton
     *
     * @param elapsedTimeMillis Time elapsed recording last was started
     * @param tb                ToggleButton to set time
     */
    private void setTime(long elapsedTimeMillis, ToggleButton tb) {
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        int hours = (int) elapsedTimeSec / 3600;
        int minutes = (int) (elapsedTimeSec % 3600) / 60;
        int seconds = (int) elapsedTimeSec % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        Platform.runLater(() -> {
            tb.setText(time);
        });
    }
}
