/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.displayable.ImageDisplayable;

/**
 * A panel used in the live / preview panels for displaying images.
 * @author Michael
 */
public class ImagePanel extends BorderPane implements ContainedPanel {

    private ImageView imageView;
    private LivePreviewPanel containerPanel;

    /**
     * Create a new image panel.
     * @param container the container this panel is contained within.
     */
    public ImagePanel(LivePreviewPanel panel) {
        this.containerPanel = panel;
        imageView = new ImageView();
        getChildren().add(imageView);
    }

    @Override
    public void focus() {
        //TODO: Something probably
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        imageView.setImage(null);
    }

    /**
     * Show a given image displayable on the panel.
     * @param displayable the image displayable.
     */
    public void showDisplayable(ImageDisplayable displayable) {
        Image image = new Image("file:"+displayable.getFile().getAbsolutePath());
        imageView.setImage(image);
        for(LyricCanvas canvas : containerPanel.getCanvases()) {
            canvas.setText(null, null, true);
            canvas.setTheme(new Theme(null, null, new Background(imageView.getImage())));
        }
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

}
