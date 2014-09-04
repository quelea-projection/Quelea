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
package org.quelea.windows.image;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Responsible for drawing an image onto the DisplayCanvas.
 * <p/>
 * @author tomaszpio@gmail.com
 * @author berry120@gmail.com
 */
public class ImageDrawer extends DisplayableDrawer {

    private ImageView imageView;
    private Image image;

    @Override
    public void draw(Displayable displayable) {
        if (getCanvas().getPlayVideo()) {
            VLCWindow.INSTANCE.stop(false);
        }
        imageView = getCanvas().getNewImageView();
        imageView.setFitWidth(getCanvas().getWidth());
        if (getCanvas().isStageView() && !QueleaProperties.get().getStageDrawImages()) {
            image = Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor());
        } else if (getCanvas().isTextOnlyView() && !QueleaProperties.get().getTextOnlyUseThemeBackground()) {
            image = Utils.getImageFromColour(QueleaProperties.get().getTextOnlyBackgroundColor());
        } else {
            image = ((ImageDisplayable) displayable).getImage();
            imageView.setPreserveRatio(true);
        }
        imageView.setImage(image);
        final StackPane imageBox = new StackPane();
        imageBox.getChildren().add(imageView);
        if (getCanvas() != QueleaApp.get().getProjectionWindow().getCanvas()
                && getCanvas() != QueleaApp.get().getStageWindow().getCanvas()) {
            imageBox.setStyle("-fx-background-color:#dddddd;");
        }
        imageBox.setVisible(false);
        imageBox.setOpacity(0);
        getCanvas().getChildren().add(imageBox);
        getCanvas().pushLogoNoticeToFront();
        imageBox.setVisible(true);
        getCanvas().setOpacity(1);
        final DisplayCanvas currentCanvas = getCanvas();
        if (getCanvas() == QueleaApp.get().getProjectionWindow().getCanvas()) {
            Utils.fadeNodeOpacity(imageBox.getOpacity(), 1, QueleaProperties.get().getFadeDuration(), imageBox, 0.0, new Runnable() {

                @Override
                public void run() {
                    clear(currentCanvas, imageBox);

                }
            });
        } else if ((getCanvas() == QueleaApp.get().getTextOnlyWindow().getCanvas())
                && QueleaProperties.get().getTextOnlyUseThemeBackground()) {
            Utils.fadeNodeOpacity(imageBox.getOpacity(), 1, QueleaProperties.get().getFadeDuration(), imageBox, 0.0, new Runnable() {

                @Override
                public void run() {
                    clear(currentCanvas, imageBox);

                }
            });
        } else {
            imageBox.setOpacity(1);
            clear(currentCanvas, imageBox);
        }
    }

    @Override
    public void clear() {
        clear(getCanvas(), null);
    }

    /**
     * Clears drawing except the passed node
     *
     * @param canvas the canvas to clear
     * @param exception the node that should be kept
     */
    public void clear(DisplayCanvas canvas, Node exception) {
        if (canvas.getChildren() != null) {
            canvas.clearNonPermanentChildren(exception);
        }
    }

    @Override
    public void requestFocus() {
        imageView.requestFocus();
    }
}
