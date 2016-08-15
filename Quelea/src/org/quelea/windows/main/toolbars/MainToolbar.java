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
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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
import org.quelea.windows.main.actionhandlers.AddYoutubeActionHandler;
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
    private final Button manageNoticesButton;
    private final MenuButton add;
    private final ImageView loadingView;
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

        add = new MenuButton("");

        ImageView iv = new ImageView(new Image("file:icons/add_item.png"));
        iv.setSmooth(true);
        iv.setFitWidth(20);
        iv.setFitHeight(20);
        add.setGraphic(iv);

        add.setStyle(Utils.TOOLBAR_BUTTON_STYLE);
        getItems().add(add);
        add.setOnMouseEntered(evt -> {
            add.show();
            openTime = new Date().getTime();
        });

        // Avoid menu being closed if users click to open it
        add.setOnMouseClicked(e -> {
            if (new Date().getTime() - openTime < 1000) {
                add.show();
            }
        });

        MenuItem addPresentationButton;
        if (Utils.isMac()) {
            addPresentationButton = getMenuItemFromImage("file:icons/powerpointbig.png");
        } else {
            addPresentationButton = getMenuItemFromImage("file:icons/powerpoint.png");
        }
        addPresentationButton.setText(LabelGrabber.INSTANCE.getLabel("add.presentation.tooltip"));
        addPresentationButton.setOnAction(new AddPowerpointActionHandler());
        add.getItems().add(addPresentationButton);

        MenuItem addMultimediaButton;
        if (Utils.isMac()) {
            addMultimediaButton = getMenuItemFromImage("file:icons/multimedia.png");
        } else {
            addMultimediaButton = getMenuItemFromImage("file:icons/multimedia.png");
        }
        addMultimediaButton.setText(LabelGrabber.INSTANCE.getLabel("add.multimedia.tooltip"));
        addMultimediaButton.setOnAction(new AddVideoActionHandler());
        add.getItems().add(addMultimediaButton);

        MenuItem addYoutubeButton;
        if (Utils.isMac()) {
            addYoutubeButton = getMenuItemFromImage("file:icons/youtube.png");
        } else {
            addYoutubeButton = getMenuItemFromImage("file:icons/youtube.png");
        }
        addYoutubeButton.setText(LabelGrabber.INSTANCE.getLabel("add.youtube.button"));
        addYoutubeButton.setOnAction(new AddYoutubeActionHandler());
        add.getItems().add(addYoutubeButton);

        MenuItem addTimerButton;
        if (Utils.isMac()) {
            addTimerButton = getMenuItemFromImage("file:icons/timer-dark.png");
        } else {
            addTimerButton = getMenuItemFromImage("file:icons/timer-dark.png", 24, 24, false, true);
        }
        addTimerButton.setText(LabelGrabber.INSTANCE.getLabel("add.timer.tooltip"));
        addTimerButton.setOnAction(new AddTimerActionHandler());
        add.getItems().add(addTimerButton);

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

        MenuItem addDVDButton = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.dvd.button"), dvdImageStack);
        addDVDButton.setOnAction(new AddDVDActionHandler());
        add.getItems().add(addDVDButton);

        MenuItem addPdfButton;
        if (Utils.isMac()) {
            addPdfButton = getMenuItemFromImage("file:icons/add_pdfbig.png");
        } else {
            addPdfButton = getMenuItemFromImage("file:icons/add_pdf.png");
        }
        addPdfButton.setText(LabelGrabber.INSTANCE.getLabel("add.pdf.tooltip"));
        addPdfButton.setOnAction(new AddPdfActionHandler());
        add.getItems().add(addPdfButton);

        MenuItem addWebButton;
        if (Utils.isMac()) {
            addWebButton = getMenuItemFromImage("file:icons/web.png");
        } else {
            addWebButton = getMenuItemFromImage("file:icons/web-small.png");
        }
        addWebButton.setText(LabelGrabber.INSTANCE.getLabel("add.website"));
        addWebButton.setOnAction(new AddWebActionHandler());
        add.getItems().add(addWebButton);

        MenuItem addImageGroupButton;
        if (Utils.isMac()) {
            addImageGroupButton = getMenuItemFromImage("file:icons/image.png");
        } else {
            addImageGroupButton = getMenuItemFromImage("file:icons/image.png", 24, 24, false, true);
        }
        addImageGroupButton.setText(LabelGrabber.INSTANCE.getLabel("add.images.panel"));
        addImageGroupButton.setOnAction(new AddImageActionHandler());
        add.getItems().add(addImageGroupButton);

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
    
    private MenuItem getMenuItemFromImage(String uri) {
        ImageView iv = new ImageView(new Image(uri));
        iv.setSmooth(true);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        return new MenuItem("", iv);
    }

    private MenuItem getMenuItemFromImage(String uri, int width, int height, boolean preserveRatio, boolean smooth) {
        ImageView iv = new ImageView(new Image(uri, width, height, preserveRatio, smooth));
        iv.setSmooth(true);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        return new MenuItem("", iv);
    }

    /**
     * Set if the DVD is loading.
     * <p>
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

    public RecordButtonHandler getRecordButtonHandler() {
        return recordingsHandler;
    }

    /**
     * Method to set elapsed time on ToggleButton
     *
     * @param elapsedTimeMillis Time elapsed recording last was started
     * @param tb ToggleButton to set time
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
