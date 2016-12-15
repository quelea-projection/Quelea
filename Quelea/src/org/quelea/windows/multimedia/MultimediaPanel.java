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
package org.quelea.windows.multimedia;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * A panel used in the live / preview panels for playing audio.
 * <p/>
 * @author tomaszpio@gmail.com
 */
public class MultimediaPanel extends AbstractPanel {

    private final MultimediaDrawer drawer;
    private final MultimediaControls controlPanel;
    private final Text previewText;
    private final ImageView imgView;
    private MultimediaDisplayable displayable;
    private boolean live = false;

    /**
     * Create a new image panel.
     */
    public MultimediaPanel() {
        this.controlPanel = new MultimediaControls();
        controlPanel.setDisableControls(true);
        drawer = new MultimediaDrawer(controlPanel);
        imgView = new ImageView(new Image("file:icons/vid preview.png"));
        BorderPane.setMargin(controlPanel, new Insets(30));
        setCenter(controlPanel);
        VBox centerBit = new VBox(5);
        centerBit.setAlignment(Pos.CENTER);
        previewText = new Text();
        previewText.setFont(Font.font("Verdana", 20));
        previewText.setFill(Color.WHITE);
        BorderPane.setMargin(centerBit, new Insets(10));
        centerBit.getChildren().add(previewText);
        imgView.fitHeightProperty().bind(heightProperty().subtract(200));
        imgView.fitWidthProperty().bind(widthProperty().subtract(20));
        centerBit.getChildren().add(imgView);
        setBottom(centerBit);
        setMinWidth(50);
        setMinHeight(50);
        setStyle("-fx-background-color:grey;");
        
        addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent t) -> {
            if (t.getCode().equals(KeyCode.PAGE_DOWN) || t.getCode().equals(KeyCode.DOWN)) {
                t.consume();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
            } else if (t.getCode().equals(KeyCode.PAGE_UP) || t.getCode().equals(KeyCode.UP)) {
                t.consume();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
            }
        });
        
        DisplayCanvas dummyCanvas = new DisplayCanvas(false, false, false, this::updateCanvas, DisplayCanvas.Priority.LOW);
        registerDisplayCanvas(dummyCanvas);
    }

    @Override
    public void updateCanvas() {
        MultimediaDisplayable displayable = (MultimediaDisplayable) getCurrentDisplayable();
        if (displayable instanceof VideoDisplayable) {
            new Thread() {
                @Override
                public void run() {
                    Image img = Utils.getVidBlankImage(((VideoDisplayable) displayable).getLocationAsFile());
                    Platform.runLater(() -> {
                        imgView.setImage(img);
                    });
                }
            }.start();
        }
        previewText.setText(displayable.getName());
        boolean playVideo = false;
        for (DisplayCanvas canvas : getCanvases()) {
            drawer.setCanvas(canvas);
            if (canvas.getPlayVideo()) {
                playVideo = true;
            }
            canvas.setCurrentDisplayable(displayable);
            drawer.setPlayVideo(canvas.getPlayVideo());
            drawer.draw(displayable);
        }
        if (playVideo) {
            controlPanel.setDisableControls(!playVideo);
        }
    }

    public void play() {
        controlPanel.play();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        drawer.setCanvas(canvas);
        return drawer;
    }

    /**
     * Advances the current slide.
     * <p/>
     */
    public void advance() {
        MainPanel qmp = QueleaApp.get().getMainWindow().getMainPanel();
        boolean lastItemTest = qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1);
        if (QueleaProperties.get().getAdvanceOnLive() && QueleaProperties.get().getSongOverflow() && !lastItemTest) {
            qmp.getPreviewPanel().goLive();
        }
    }

    /**
     * Moves to the previous slide.
     * <p/>
     */
    public void previous() {
        MainPanel qmp = QueleaApp.get().getMainWindow().getMainPanel();
        boolean firstItemTest = qmp.getSchedulePanel().getScheduleList().getItems().get(0) == qmp.getLivePanel().getDisplayable();
        if (QueleaProperties.get().getAdvanceOnLive() && QueleaProperties.get().getSongOverflow() && !firstItemTest) {
            //Assuming preview panel is one ahead, and should be one behind
            int index = qmp.getSchedulePanel().getScheduleList().getSelectionModel().getSelectedIndex();
            if (qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1)) {
                index -= 1;
            } else {
                index -= 2;
            }
            if (index >= 0) {
                qmp.getSchedulePanel().getScheduleList().getSelectionModel().clearAndSelect(index);
                qmp.getPreviewPanel().selectLastLyric();
                qmp.getPreviewPanel().goLive();
            }
        }
    }
    
    
    /**
     * Set the displayable to be on this presentation panel.
     * <p/>
     * @param displayable the presentation displayable to display.
     * @param index the index to display.
     */
    public void showDisplayable(final MultimediaDisplayable displayable, final int index) {
        if (this.displayable == displayable) {
            return;
        }
        this.displayable = displayable;
        if (displayable == null) {
            return;
        }
        
        if (live) {
            drawer.setVisible(true);
        }
        super.showDisplayable(displayable);
    }
    
    public void stopCurrent() {
        if (live && displayable != null) {
            if (displayable != null) {
                drawer.setVisible(false);
                displayable = null;
            }
        }
    }
    
    /**
     * Let this panel know it is live and should update accordingly.
     */
    public void setLive() {
        live = true;
    }
    
    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void removeCurrentDisplayable() {
        super.removeCurrentDisplayable();
    }
}
