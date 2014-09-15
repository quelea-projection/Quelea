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
package org.quelea.windows.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.multimedia.MultimediaDrawer;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * The panel displaying the preview lyrics selection - this is viewed before
 * displaying the actual lyrics on the projector.
 */
public class PreviewPanel extends LivePreviewPanel {

    private final Button liveButton;
    private final Button livePlayButton;

    /**
     * Create a new preview lyrics panel.
     */
    public PreviewPanel() {
        ToolBar header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("preview.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        liveButton = new Button(LabelGrabber.INSTANCE.getLabel("go.live.text"), new ImageView(new Image("file:icons/golivearrow.png")));
        liveButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("go.live.text") + " (" + LabelGrabber.INSTANCE.getLabel("space.key") + ")"));
        liveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().removeDisplayable();

                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getMediaLoopPanel().stopLoop();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getVideoPanel().getMultimediaControls().setNoStop(false);
                final Displayable d = getDisplayable();
                int index = ((ContainedPanel) getCurrentPane()).getCurrentIndex();
                if (QueleaProperties.get().getAdvanceScheduleOnGoLive()) {
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSelectionModel().selectNext();
                }
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().setDisplayable(d, index);
              
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        if (d instanceof MediaLoopDisplayable) {
                            QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getMediaLoopPanel().startLoop();
                        }
                    }

                });

                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getCurrentPane().requestFocus();

            }
        });
        livePlayButton = new Button(LabelGrabber.INSTANCE.getLabel("go.live.play.text"), new ImageView(new Image("file:icons/golivearrow.png")));
        livePlayButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("go.live.play.text")));
        livePlayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getVideoPanel().getMultimediaControls().setNoStop(true);
                goLive();
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getVideoPanel().getMultimediaControls().play();
                                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getVideoPanel().getMultimediaControls().setNoStop(false);
                            }
                        });

                    }
                });
                thread.start();

            }
        });
        header.getItems().add(livePlayButton);
        header.getItems().add(liveButton);
        liveButton.setDisable(true);
        livePlayButton.setDisable(true);
        setTop(header);
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCharacter().equals(" ")) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                }
            }
        });
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.RIGHT) {
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().requestFocus();
                } else if (t.getCode() == KeyCode.LEFT) {
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().requestFocus();
                }
            }
        });
    }

    /**
     * Transfer the current preview to live.
     */
    public void goLive() {
        liveButton.fire();
    }

    /**
     * Set the given displayable to be shown on the panel.
     * <p/>
     * @param d the displayable to show.
     * @param index an index that may be used or ignored depending on the
     * displayable.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (AbstractPanel.isIsNextPreviewed()) {
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().updatePreview(
                            QueleaApp.get().getStageWindow().getStagePreviewCanvas());
                }
            }
        });

        liveButton.setDisable(false);
        if (d instanceof MultimediaDisplayable) {
            livePlayButton.setDisable(false);
        } else {
            livePlayButton.setDisable(true);
        }

    }

    /**
     * Clear the preview panel.
     */
    @Override
    public void removeDisplayable() {
        super.removeDisplayable();
        liveButton.setDisable(true);
    }
}
