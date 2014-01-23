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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
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
        clear();
        if(getCanvas().getPlayVideo()) {
            VLCWindow.INSTANCE.stop();
        }
        imageView = getCanvas().getNewImageView();
        imageView.setFitWidth(getCanvas().getWidth());
        if(getCanvas().isStageView()) {
            image = Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor());
        }
        else {
            image = ((ImageDisplayable) displayable).getImage();
            imageView.setPreserveRatio(true);
        }
        imageView.setImage(image);
        StackPane imageBox = new StackPane();
        imageBox.getChildren().add(imageView);
        if(getCanvas() != QueleaApp.get().getProjectionWindow().getCanvas()
                && getCanvas() != QueleaApp.get().getStageWindow().getCanvas()) {
            imageBox.setStyle("-fx-background-color:#dddddd;");
        }
        imageBox.setVisible(false);
        getCanvas().getChildren().add(imageBox);
        getCanvas().pushLogoNoticeToFront();
        imageBox.setVisible(true);
        getCanvas().setOpacity(1);
    }

    @Override
    public void clear() {
        if(getCanvas().getChildren() != null) {
            getCanvas().clearNonPermanentChildren();
        }
    }

    @Override
    public void requestFocus() {
        imageView.requestFocus();
    }
}
