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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.AddDVDActionHandler;
import org.quelea.windows.main.actionhandlers.AddPdfActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.AddVideoActionHandler;
import org.quelea.windows.main.actionhandlers.AddYoutubeActionHandler;
import org.quelea.windows.main.actionhandlers.AddTimerActionHandler;
import org.quelea.windows.main.actionhandlers.RecordButtonHandler;
import org.quelea.windows.main.actionhandlers.NewScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;
import org.quelea.windows.main.actionhandlers.OpenScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.PrintScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.QuickInsertActionHandler;
import org.quelea.windows.main.actionhandlers.SaveScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.ShowNoticesActionHandler;

/**
 * Quelea's main toolbar.
 * <p/>
 * @author Michael
 */
public class MainToolbar extends ToolBar {

    private final Button newScheduleButton;
    private final Button openScheduleButton;
    private final Button saveScheduleButton;
    private final Button printScheduleButton;
    private final Button newSongButton;
    private final Button quickInsertButton;
    private final Button addPresentationButton;
    private final Button addYoutubeButton;
    private final Button addTimerButton;
    private final Button addDVDButton;
    private final Button addVideoButton;
    private final Button addPDFButton;
    private final Button manageNoticesButton;
    private final ImageView loadingView;
    private final StackPane dvdImageStack;
    private final ToggleButton recordAudioButton;
    private final ProgressBar pb = new ProgressBar(0);
    private Dialog setRecordinPathWarning;
    private RecordButtonHandler recordingsHandler;
    private TextField recordingPathTextField;
    private boolean recording;

    /**
     * Create the toolbar and any associated shortcuts.
     */
    public MainToolbar() {
        if (Utils.isMac()) {
            newScheduleButton = getButtonFromImage("file:icons/filenewbig.png");
        } else {
            newScheduleButton = getButtonFromImage("file:icons/filenew.png");
        }
        Utils.setToolbarButtonStyle(newScheduleButton);
        newScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("new.schedule.tooltip")));
        newScheduleButton.setOnAction(new NewScheduleActionHandler());
        getItems().add(newScheduleButton);

        if (Utils.isMac()) {
            openScheduleButton = getButtonFromImage("file:icons/fileopenbig.png");
        } else {
            openScheduleButton = getButtonFromImage("file:icons/fileopen.png");
        }
        Utils.setToolbarButtonStyle(openScheduleButton);
        openScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("open.schedule.tooltip")));
        openScheduleButton.setOnAction(new OpenScheduleActionHandler());
        getItems().add(openScheduleButton);

        if (Utils.isMac()) {
            saveScheduleButton = getButtonFromImage("file:icons/filesavebig.png");
        } else {
            saveScheduleButton = getButtonFromImage("file:icons/filesave.png");
        }
        Utils.setToolbarButtonStyle(saveScheduleButton);
        saveScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("save.schedule.tooltip")));
        saveScheduleButton.setOnAction(new SaveScheduleActionHandler(false));
        getItems().add(saveScheduleButton);

        if (Utils.isMac()) {
            printScheduleButton = getButtonFromImage("file:icons/fileprintbig.png");
        } else {
            printScheduleButton = getButtonFromImage("file:icons/fileprint.png");
        }
        Utils.setToolbarButtonStyle(printScheduleButton);
        printScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("print.schedule.tooltip")));
        printScheduleButton.setOnAction(new PrintScheduleActionHandler());
        getItems().add(printScheduleButton);

        getItems().add(new Separator());

        if (Utils.isMac()) {
            newSongButton = getButtonFromImage("file:icons/newsongbig.png");
        } else {
            newSongButton = getButtonFromImage("file:icons/newsong.png");
        }
        Utils.setToolbarButtonStyle(newSongButton);
        newSongButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("new.song.tooltip")));
        newSongButton.setOnAction(new NewSongActionHandler());
        getItems().add(newSongButton);

        getItems().add(new Separator());

        if (Utils.isMac()) {
            quickInsertButton = getButtonFromImage("file:icons/lightningbig.png");
        } else {
            quickInsertButton = getButtonFromImage("file:icons/lightning.png");
        }
        Utils.setToolbarButtonStyle(quickInsertButton);
        quickInsertButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("quick.insert.text")));
        quickInsertButton.setOnAction(new QuickInsertActionHandler());
        getItems().add(quickInsertButton);

        if (Utils.isMac()) {
            addPresentationButton = getButtonFromImage("file:icons/powerpointbig.png");
        } else {
            addPresentationButton = getButtonFromImage("file:icons/powerpoint.png");
        }
        Utils.setToolbarButtonStyle(addPresentationButton);
        addPresentationButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.presentation.tooltip")));
        addPresentationButton.setOnAction(new AddPowerpointActionHandler());
        getItems().add(addPresentationButton);

        if (Utils.isMac()) {
            addVideoButton = getButtonFromImage("file:icons/video file big.png");
        } else {
            addVideoButton = getButtonFromImage("file:icons/video file.png");
        }
        Utils.setToolbarButtonStyle(addVideoButton);
        addVideoButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.video.tooltip")));
        addVideoButton.setOnAction(new AddVideoActionHandler());
        getItems().add(addVideoButton);

        if (Utils.isMac()) {
            addYoutubeButton = getButtonFromImage("file:icons/youtube.png", 24, 24, false, true);
        } else {
            addYoutubeButton = getButtonFromImage("file:icons/youtube.png");
        }
        Utils.setToolbarButtonStyle(addYoutubeButton);
        addYoutubeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.youtube.button")));
        addYoutubeButton.setOnAction(new AddYoutubeActionHandler());
        getItems().add(addYoutubeButton);

        if (Utils.isMac()) {
            addTimerButton = getButtonFromImage("file:icons/timer-dark.png");
        } else {
            addTimerButton = getButtonFromImage("file:icons/timer-dark.png", 24, 24, false, true);
        }
        Utils.setToolbarButtonStyle(addTimerButton);
        addTimerButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.timer.tooltip")));
        addTimerButton.setOnAction(new AddTimerActionHandler());
        getItems().add(addTimerButton);

        if (Utils.isMac()) {
            loadingView = new ImageView(new Image("file:icons/loading.gif"));
        } else {
            loadingView = new ImageView(new Image("file:icons/loading.gif", 24, 24, false, true));
        }
        loadingView.setFitHeight(24);
        loadingView.setFitWidth(24);
        dvdImageStack = new StackPane();
        ImageView dvdIV;
        if (Utils.isMac()) {
            dvdIV = new ImageView(new Image("file:icons/dvd.png"));
        } else {
            dvdIV = new ImageView(new Image("file:icons/dvd.png", 24, 24, false, true));
        }
        dvdIV.setFitWidth(24);
        dvdIV.setFitHeight(24);
        dvdImageStack.getChildren().add(dvdIV);
        addDVDButton = new Button("", dvdImageStack);
        Utils.setToolbarButtonStyle(addDVDButton);
        addDVDButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.dvd.button")));
        addDVDButton.setOnAction(new AddDVDActionHandler());
        getItems().add(addDVDButton);

        if (Utils.isMac()) {
            addPDFButton = getButtonFromImage("file:icons/add_pdfbig.png");
        } else {
            addPDFButton = getButtonFromImage("file:icons/add_pdf.png");
        }
        Utils.setToolbarButtonStyle(addPDFButton);
        addPDFButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.pdf.tooltip")));
        addPDFButton.setOnAction(new AddPdfActionHandler());
        getItems().add(addPDFButton);

        getItems().add(new Separator());

        if (Utils.isMac()) {
            manageNoticesButton = getButtonFromImage("file:icons/infobig.png");
        } else {
            manageNoticesButton = getButtonFromImage("file:icons/info.png");
        }
        Utils.setToolbarButtonStyle(manageNoticesButton);
        manageNoticesButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("manage.notices.tooltip")));
        manageNoticesButton.setOnAction(new ShowNoticesActionHandler());
        getItems().add(manageNoticesButton);

        recordingsHandler = new RecordButtonHandler();

        recordingPathTextField = new TextField();
        recordingPathTextField.setMinWidth(Region.USE_PREF_SIZE);
        recordingPathTextField.setMaxWidth(Region.USE_PREF_SIZE);

        // Set dynamic TextField width
        recordingPathTextField.textProperty().addListener((ov, prevText, currText) -> {
            Platform.runLater(() -> {
                Text text = new Text(currText);
                text.setFont(recordingPathTextField.getFont());
                double width = text.getLayoutBounds().getWidth()
                        + recordingPathTextField.getPadding().getLeft() + recordingPathTextField.getPadding().getRight()
                        + 2d;
                recordingPathTextField.setPrefWidth(width);
                recordingPathTextField.positionCaret(recordingPathTextField.getCaretPosition());
            });
        });

        recordAudioButton = getToggleButtonFromImage("file:icons/record.png");
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

    private Button getButtonFromImage(String uri) {
        ImageView iv = new ImageView(new Image(uri));
        iv.setSmooth(true);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        return new Button("", iv);
    }

    private ToggleButton getToggleButtonFromImage(String uri) {
        ImageView iv = new ImageView(new Image(uri));
        iv.setSmooth(true);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        return new ToggleButton("", iv);
    }

    private Button getButtonFromImage(String uri, int width, int height, boolean preserveRatio, boolean smooth) {
        ImageView iv = new ImageView(new Image(uri, width, height, preserveRatio, smooth));
        iv.setSmooth(true);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        return new Button("", iv);
    }

    /**
     * Set if the DVD is loading.
     * <p>
     * @param loading true if it's loading, false otherwise.
     */
    public void setDVDLoading(boolean loading) {
        addDVDButton.setDisable(loading);
        if (loading && !dvdImageStack.getChildren().contains(loadingView)) {
            dvdImageStack.getChildren().add(loadingView);
        } else if (!loading) {
            dvdImageStack.getChildren().remove(loadingView);
        }
    }

    public void startRecording() {
        recordAudioButton.setSelected(true);
        recording = true;
        getItems().add(pb);
        getItems().add(recordingPathTextField);
        recordAudioButton.setText("Recording...");
        recordAudioButton.setSelected(true);
        recordingsHandler = new RecordButtonHandler();
        recordingsHandler.passVariables("rec", pb, recordingPathTextField, recordAudioButton);
    }

    public void stopRecording() {
        recordingsHandler.passVariables("stop", pb, recordingPathTextField, recordAudioButton);
        recordAudioButton.setSelected(false);
        recording = false;
        getItems().remove(pb);
        getItems().remove(recordingPathTextField);
        recordAudioButton.setText("");
        recordAudioButton.setSelected(false);
    }

    public RecordButtonHandler getRecordButtonHandler() {
        return recordingsHandler;
    }
}
