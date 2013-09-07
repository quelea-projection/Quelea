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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;

/**
 * A panel used in the live / preview panels for playing audio.
 * <p/>
 * @author tomaszpio@gmail.com
 */
public class MultimediaPanel extends AbstractPanel {

    private MultimediaDrawer drawer;
    private MultimediaControls controlPanel;
    private Text previewText;

    /**
     * Create a new image panel.
     */
    public MultimediaPanel() {
        this.controlPanel = new MultimediaControls();
        controlPanel.setDisableControls(true);
        drawer = new MultimediaDrawer(controlPanel);
        ImageView img = new ImageView(new Image("file:icons/vid preview.png"));
        BorderPane.setMargin(controlPanel, new Insets(30));
        setTop(controlPanel);
        VBox centerBit = new VBox(5);
        centerBit.setAlignment(Pos.CENTER);
        previewText = new Text();
        previewText.setFont(Font.font("Verdana", 20));
        previewText.setFill(Color.WHITE);
        BorderPane.setMargin(centerBit, new Insets(10));
        centerBit.getChildren().add(previewText);
        centerBit.getChildren().add(img);
        setCenter(centerBit);
        setMinWidth(50);
        setStyle("-fx-background-color:grey;");
        DisplayCanvas dummyCanvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateCanvas();
            }
        }, DisplayCanvas.Priority.LOW);
        registerDisplayCanvas(dummyCanvas);
    }

    @Override
    public void updateCanvas() {
        MultimediaDisplayable displayable = (MultimediaDisplayable) getCurrentDisplayable();
        previewText.setText(displayable.getFile().getName());
        for(DisplayCanvas canvas : getCanvases()) {
            drawer.setCanvas(canvas);
            drawer.setPlayVideo(canvas.getPlayVideo());
            canvas.setCurrentDisplayable(displayable);
            drawer.draw(displayable);
        }
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
}
