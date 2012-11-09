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

import java.util.HashSet;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import org.quelea.QueleaApp;
import org.quelea.displayable.Displayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 * <p/>
 * @author Michael
 */
public class LivePanel extends LivePreviewPanel {

    private final ToggleButton black;
    private final ToggleButton clear;
    private final ToggleButton hide;

    /**
     * Create a new live lyrics panel.
     */
    public LivePanel() {
        getPresentationPanel().setLive();
        ToolBar header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("live.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        black = new ToggleButton("", new ImageView(new Image("file:icons/black.png")));
        black.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("black.screen.tooltip") + " (F1)"));
        black.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                HashSet<LyricCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for(LyricCanvas canvas : canvases) {
                    canvas.toggleBlack();
                }
            }
        });
        header.getItems().add(black);
        clear = new ToggleButton("", new ImageView(new Image("file:icons/clear.png", 16, 16, false, true)));
        clear.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("clear.text.tooltip") + " (F2)"));
        clear.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                HashSet<LyricCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for(LyricCanvas canvas : canvases) {
                    canvas.toggleClear();
                }
            }
        });
        header.getItems().add(clear);
        hide = new ToggleButton("", new ImageView(new Image("file:icons/cross.png")));
        hide.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("hide.display.output.tooltip") + " (F3)"));
        hide.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                int projectorScreen = QueleaProperties.get().getProjectorScreen();
                int stageScreen = QueleaProperties.get().getStageScreen();
                //GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                //final GraphicsDevice[] gds = ge.getScreenDevices();
                final ObservableList<Screen> monitors = Screen.getScreens();
                
                LyricWindow lyricWindow = QueleaApp.get().getLyricWindow();
                LyricWindow stageWindow = QueleaApp.get().getStageWindow();

                final boolean lyricsHidden;
                if(!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitors.size() || projectorScreen < 0)) {
                    lyricsHidden = true;
                }
                else {
                    lyricsHidden = false;
                }

                final boolean stageHidden;
                if(!QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitors.size() || stageScreen < 0)) {
                    stageHidden = true;
                }
                else {
                    stageHidden = false;
                }

                if(!lyricsHidden) {
                    if(lyricWindow.isShowing()) {
                        lyricWindow.hide();
                    }
                    else {
                        lyricWindow.show();
                    }
                }
                if(!stageHidden) {
                    if(stageWindow.isShowing()) {
                        stageWindow.hide();
                    }
                    else {
                        stageWindow.show();
                    }
                }
            }
        });
        header.getItems().add(hide);
        setTop(header);
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCharacter().equals(" ")) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                }
            }
        });
    }

    /**
     * Set the displayable to be shown on this live panel.
     * <p/>
     * @param d the displayable to show.
     * @param index the index to use for the displayable, if relevant.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        if(d == null) {
            clear.setDisable(true);
        }
        else {
            clear.setDisable(!d.supportClear());
        }
    }

    /**
     * Get the "black" toggle button.
     * <p/>
     * @return the "black" toggle button.
     */
    public ToggleButton getBlack() {
        return black;
    }

    /**
     * Get the "clear" toggle button.
     * <p/>
     * @return the "clear" toggle button.
     */
    public ToggleButton getClear() {
        return clear;
    }

    /**
     * Get the "hide" toggle button.
     * <p/>
     * @return the "hide" toggle button.
     */
    public ToggleButton getHide() {
        return hide;
    }
}
