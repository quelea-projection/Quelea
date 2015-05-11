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

import java.io.File;
import java.util.HashSet;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 * <p/>
 * @author Michael
 */
public class LivePanel extends LivePreviewPanel {

    private final HBox loopBox;
    private final ToggleButton loop;
    private final TextField loopDuration;

    private final ToggleButton logo;
    private final ToggleButton black;
    private final ToggleButton clear;
    private final ToggleButton hide;

    private final ToolBar header;

    /**
     * Create a new live lyrics panel.
     */
    public LivePanel() {
        getPresentationPanel().setLive();
        header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("live.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        loop = new ToggleButton(LabelGrabber.INSTANCE.getLabel("loop.label") + ":");
        loopDuration = new TextField("10");
        loopDuration.setMaxWidth(40);
        loopDuration.setMinWidth(40);
        loopDuration.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                String text = t.getCharacter();
                if(text.isEmpty()) {
                    return;
                }
                char arr[] = text.toCharArray();
                char ch = arr[text.toCharArray().length - 1];
                if (!(ch >= '0' && ch <= '9')) {
                    t.consume();
                }
                try {
                    String newText = loopDuration.getText() + ch;
                    int num = Integer.parseInt(newText);
                    if (num > 100 || num <= 0) {
                        t.consume();
                    }
                } catch (NumberFormatException ex) {
                    t.consume();
                }
            }
        });
        loopBox = new HBox(5);
        loopBox.getChildren().add(new Label("   "));
        loopBox.getChildren().add(loop);
        loopBox.getChildren().add(loopDuration);
        loopBox.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("seconds.label")));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        ImageView logoIV;
        if (Utils.isMac()) {
            logoIV = new ImageView(new Image("file:icons/logo48.png"));
        } else {
            logoIV = new ImageView(new Image("file:icons/logo16.png"));
        }
        logoIV.setFitHeight(16);
        logoIV.setFitWidth(16);
        logo = new ToggleButton("", logoIV);
        Utils.setToolbarButtonStyle(logo);
        logo.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("logo.screen.tooltip") + " (F5)"));
        logo.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.SECONDARY)) {
                    FileChooser chooser = new FileChooser();
                    if (QueleaProperties.get().getLastDirectory() != null) {
                        chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
                    }
                    chooser.getExtensionFilters().add(FileFilters.IMAGES);
                    chooser.setInitialDirectory(QueleaProperties.get().getImageDir().getAbsoluteFile());
                    File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
                    if (file != null) {
                        QueleaProperties.get().setLastDirectory(file.getParentFile());
                        QueleaProperties.get().setLogoImage(file.getAbsolutePath());
                        updateLogo();
                    }
                }
            }
        });
        logo.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                HashSet<DisplayCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for (DisplayCanvas canvas : canvases) {
                    canvas.setLogoDisplaying(logo.isSelected());
                }
            }
        });
        header.getItems().add(logo);
        black = new ToggleButton("", new ImageView(new Image("file:icons/black.png")));
        Utils.setToolbarButtonStyle(black);
        black.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("black.screen.tooltip") + " (F6)"));
        black.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                HashSet<DisplayCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for (DisplayCanvas canvas : canvases) {
                    canvas.setBlacked(black.isSelected());
                }
            }
        });
        header.getItems().add(black);
        ImageView clearIV = new ImageView(new Image("file:icons/clear.png"));
        clearIV.setFitWidth(16);
        clearIV.setFitHeight(16);
        clear = new ToggleButton("", clearIV);
        Utils.setToolbarButtonStyle(clear);
        clear.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("clear.text.tooltip") + " (F7)"));
        clear.setOnAction((javafx.event.ActionEvent t) -> {
            HashSet<DisplayCanvas> canvases = new HashSet<>();
            canvases.addAll(getCanvases());
            for (DisplayCanvas canvas : canvases) {
                if (canvas.isStageView() && !QueleaProperties.get().getClearStageWithMain()) {
                    canvas.setCleared(false);
                } else {
                    canvas.setCleared(clear.isSelected());
                }
            }
        });
        header.getItems().add(clear);
        ImageView hideIV = new ImageView(new Image("file:icons/cross.png"));
        hideIV.setFitWidth(16);
        hideIV.setFitHeight(16);
        hide = new ToggleButton("", hideIV);
        Utils.setToolbarButtonStyle(hide);
        hide.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("hide.display.output.tooltip") + " (F8)"));
        hide.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                int projectorScreen = QueleaProperties.get().getProjectorScreen();
                int stageScreen = QueleaProperties.get().getStageScreen();
                final ObservableList<Screen> monitors = Screen.getScreens();

                DisplayStage appWindow = QueleaApp.get().getProjectionWindow();
                DisplayStage stageWindow = QueleaApp.get().getStageWindow();

                final boolean lyricsHidden;
                if (!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitors.size() || projectorScreen < 0)) {
                    lyricsHidden = true;
                } else {
                    lyricsHidden = false;
                }

                final boolean stageHidden;
                if (!QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitors.size() || stageScreen < 0)) {
                    stageHidden = true;
                } else {
                    stageHidden = false;
                }

                if (!lyricsHidden) {
                    if (hide.isSelected()) {
                        appWindow.hide();
                    } else {
                        appWindow.show();
                    }
                }
                if (!stageHidden) {
                    if (hide.isSelected()) {
                        stageWindow.hide();
                    } else {
                        stageWindow.show();
                    }
                }
                VLCWindow.INSTANCE.refreshPosition();
//                VLCWindow.INSTANCE.setHideButton(hide.isSelected());
            }
        });
//        header.getItems().add(hide);
        setTop(header);
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCharacter().equals(" ")) {
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
        loop.setSelected(false);
        if (d instanceof PresentationDisplayable) {
            if (!header.getItems().contains(loopBox)) {
                header.getItems().add(1, loopBox);
            }
        } else {
            header.getItems().remove(loopBox);
        }
        if (d == null) {
            clear.setSelected(false);
            clear.setDisable(true);
        } else {
            clear.setDisable(!d.supportClear());
            if (!d.supportClear()) {
                clear.setSelected(false);
            }
        }
        HashSet<DisplayCanvas> canvases = new HashSet<>();
        canvases.addAll(getCanvases());
        for (DisplayCanvas canvas : canvases) {
            canvas.setBlacked(black.isSelected());
            if (canvas.isStageView() && !QueleaProperties.get().getClearStageWithMain()) {
                canvas.setCleared(false);
            } else {
                canvas.setCleared(clear.isSelected());
            }
        }
    }

    /**
     * Toggle the "black" button.
     */
    public void toggleBlack() {
        if (!black.isDisable()) {
            black.fire();
        }
    }

    /**
     * Toggle the "clear" button.
     */
    public void toggleClear() {
        if (!clear.isDisable()) {
            clear.fire();
        }
    }

    /**
     * Toggle the "hide" button.
     */
    public void toggleHide() {
        if (!hide.isDisable()) {
            hide.fire();
        }
    }

    /**
     * Toggle the "logo" button.
     */
    public void toggleLogo() {
        if (!logo.isDisable()) {
            logo.fire();
        }
    }

    /**
     * Determine if the loop button is selected.
     * <p>
     * @return true if it's selected, false otherwise.
     */
    public boolean isLoopSelected() {
        return loop.isSelected();
    }

    public TextField getLoopDurationTextField() {
        return loopDuration;
    }

    /**
     * Get the hide button.
     * <p>
     * @return the hide button.
     */
    public ToggleButton getHide() {
        return hide;
    }

    /**
     * Calls updateLogo() on each canvas. A tidy method.
     */
    private void updateLogo() {
        HashSet<DisplayCanvas> canvases = new HashSet<>();
        canvases.addAll(getCanvases());
        for (DisplayCanvas canvas : canvases) {
            canvas.updateLogo();
        }
    }

    public void updateCanvases() {
        HashSet<DisplayCanvas> canvases = new HashSet<>();
        canvases.addAll(getCanvases());
        for (DisplayCanvas canvas : canvases) {
            canvas.update();
        }
    }

    /**
     * Determine if content is currently being shown on this panel, if not it
     * may be showing the logo, cleared, blacked or hidden.
     * <p>
     * @return true if content is showing, false otherwise.
     */
    public boolean isContentShowing() {
        return !(logo.isSelected() || clear.isSelected() || black.isSelected() || hide.isSelected());
    }

    public boolean getLogoed() {
        return logo.isSelected();
    }

    public boolean getBlacked() {
        return black.isSelected();
    }

    public boolean getCleared() {
        return clear.isSelected();
    }

}
