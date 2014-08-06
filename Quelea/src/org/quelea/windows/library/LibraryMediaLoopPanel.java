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
package org.quelea.windows.library;

import java.util.logging.Logger;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.NewMediaLoopActionHandler;

/**
 * The image panel in the library.
 * <p/>
 * @author Michael
 */
public class LibraryMediaLoopPanel extends BorderPane {

    private final MediaLoopListPanel mediaLoopListPanel;
    private final ToolBar toolbar;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new library media loop panel.
     */
    public LibraryMediaLoopPanel() {
        mediaLoopListPanel = new MediaLoopListPanel();
        setCenter(mediaLoopListPanel);
        toolbar = new ToolBar();

        Button addButton = new Button("", new ImageView(new Image("file:icons/add.png")));
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.mediaLoop.panel")));
       addButton.setOnAction(new NewMediaLoopActionHandler());
        HBox toolbarBox = new HBox();
        toolbar.setOrientation(Orientation.VERTICAL);
        toolbarBox.getChildren().add(toolbar);
        Utils.setToolbarButtonStyle(addButton);
        toolbar.getItems().add(addButton);
        setLeft(toolbarBox);
    }

    /**
     * Get the media loop list panel.
     * <p/>
     * @return the media loop list panel.
     */
    public MediaLoopListPanel getMediaLoopPanel() {
        return mediaLoopListPanel;
    }
}
