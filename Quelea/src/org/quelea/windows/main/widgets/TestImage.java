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
package org.quelea.windows.main.widgets;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * The test image pane - on a separate stack pane with its background colour set
 * so we don't see any content behind the panel when its aspect ratio doesn't
 * line up exactly with the display canvas size!
 * <p>
 *
 * @author Michael
 */
public class TestImage extends StackPane {

    private final ImageView testImageView;

    /**
     * Create a new test image
     */
    public TestImage() {
        testImageView = new ImageView();
        testImageView.setSmooth(true);
        setStyle("-fx-background-color:#000000;");
        getChildren().add(testImageView);
    }

    /**
     * Set the image used on this pane.
     * @param testImage the image.
     */
    public void setImage(Image testImage) {
        testImageView.setImage(testImage);
    }

    public ImageView getImageView() {
        return testImageView;
    }
    
}
