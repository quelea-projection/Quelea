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

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import static javafx.scene.layout.StackPane.setMargin;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.SelectLyricsList;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.widgets.DisplayPreview;

/**
 * A panel used in the live / preview panels for playing audio.
 * <p/>
 * @author tomaszpio@gmail.com
 */
public class MultimediaPanel extends AbstractPanel {

    private MultimediaDrawer drawer;
    private MultimediaControls controlPanel;
    private Text previewText;
    private ImageView img;
    private final SplitPane splitPane;
    private static final double DEFAULT_RATIO = 4.0 / 3;

    /**
     * Create a new image panel.
     */
    public MultimediaPanel() {
        this.controlPanel = new MultimediaControls();
        controlPanel.setDisableControls(true);
        drawer = new MultimediaDrawer(controlPanel);
        img = new ImageView(new Image("file:icons/vid preview.png"));
        if (getCurrentDisplayable() != null && getCurrentDisplayable() instanceof VideoDisplayable) {
            img.setImage(Utils.getVidBlankImage(new File(((VideoDisplayable) getCurrentDisplayable()).getLocation())));
        }
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setStyle("-fx-background-color: rgba(0, 0, 0);");
        setCenter(splitPane);
        img.fitHeightProperty().bind(heightProperty().subtract(200));
        img.fitWidthProperty().bind(widthProperty().subtract(20));
        previewText = new Text();
        previewText.setFont(Font.font("Verdana", 20));
        previewText.setFill(Color.WHITE);
        setMinWidth(50);
        setMinHeight(50);
        setStyle("-fx-background-color:grey;");
        DisplayCanvas dummyCanvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateCanvas();
            }
        }, DisplayCanvas.Priority.LOW);
        registerDisplayCanvas(dummyCanvas);
        splitPane.getItems().add(new VBox(10, controlPanel, previewText));
        splitPane.getItems().add(img);
        updateSize();
    }

    @Override
    public void updateCanvas() {
        MultimediaDisplayable displayable = (MultimediaDisplayable) getCurrentDisplayable();
        if (displayable instanceof VideoDisplayable) {
            img.setImage(Utils.getVidBlankImage(new File(((VideoDisplayable) displayable).getLocation())));
        }
        previewText.setText(displayable.getName());
        boolean playVideo = false;
        for (DisplayCanvas canvas : getCanvases()) {
            drawer.setCanvas(canvas);
            if (canvas.getPlayVideo()) {
                playVideo = true;
            }
            drawer.setPlayVideo(canvas.getPlayVideo());
            canvas.setCurrentDisplayable(displayable);
            drawer.draw(displayable);
        }
        if (playVideo) {
            controlPanel.setDisableControls(!playVideo);
        }
        updateSize();
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
     * Calculate and update the new size of the image.
     */
    private void updateSize() {
        double width = getWidth();
        double height = getHeight();
        double currentRatio = width / height;
        double ratio = getRatio();
        if (currentRatio < ratio) { //height too big
            double hDiff = (getHeight() - (width / ratio)) / 2;
            if (hDiff < 10) {
                hDiff = 10;
            }
            setMargin(img, new Insets(hDiff, 10, hDiff, 10));
        } else { //width too big
            double vDiff = (getWidth() - (ratio * height)) / 2;
            if (vDiff < 10) {
                vDiff = 10;
            }
            setMargin(img, new Insets(10, vDiff, 10, vDiff));
        }
    }

    private double getRatio() {
        if (QueleaApp.get().getProjectionWindow() == null || QueleaApp.get().getProjectionWindow().isShowing()) {
            return DEFAULT_RATIO;
        }
        double width = QueleaApp.get().getProjectionWindow().getWidth();
        double height = QueleaApp.get().getProjectionWindow().getHeight();
        if (height == 0) {
            return DEFAULT_RATIO;
        }
        return width / height;
    }
}
