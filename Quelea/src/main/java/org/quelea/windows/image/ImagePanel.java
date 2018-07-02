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

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * A panel used in the live / preview panels for displaying images.
 * <p/>
 * @author Michael
 */
public class ImagePanel extends AbstractPanel {

    private final DisplayCanvas previewCanvas;
    private final ImageDrawer drawer = new ImageDrawer();

    /**
     * Create a new image panel.
     */
    public ImagePanel() {
        setStyle("-fx-background-color: rgba(0, 0, 0);");
        previewCanvas = new DisplayCanvas(false, false, false, super::updateCanvas, Priority.LOW);
        registerDisplayCanvas(previewCanvas);
        setCenter(previewCanvas);
        addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent t) -> {
            if (t.getCode().equals(KeyCode.PAGE_DOWN) || t.getCode().equals(KeyCode.DOWN)) {
                t.consume();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
            } else if (t.getCode().equals(KeyCode.PAGE_UP) || t.getCode().equals(KeyCode.UP)) {
                t.consume();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
            }
        });
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        drawer.setCanvas(canvas);
        return drawer;
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void removeCurrentDisplayable() {
        super.removeCurrentDisplayable();
    }

    /**
     * Show a given image displayable on the panel.
     * <p/>
     * @param displayable the image displayable.
     */
    public void showDisplayable(ImageDisplayable displayable) {
        setCurrentDisplayable(displayable);
        updateCanvas();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
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
            if(qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1)) {
                index -= 1;
            }
            else{
                index -= 2;
            }
            if (index >= 0) {
                qmp.getSchedulePanel().getScheduleList().getSelectionModel().clearAndSelect(index);
                qmp.getPreviewPanel().selectLastLyric();
                qmp.getPreviewPanel().goLive();
            }
        }
    }
}
