/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main;

import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.widgets.Clock;
import org.quelea.windows.main.widgets.TestImage;
import org.quelea.windows.multimedia.VLCWindow;
import utils.PlatformUtils;

/**
 * The full screen window used for displaying the projection.
 * <p/>
 * @author Michael
 */
public class DisplayStage extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Cursor BLANK_CURSOR;
    private final DisplayCanvas canvas;
    private final TestImage testImage;

    /**
     * Initialise cursor hiding.
     */
    static {
        BLANK_CURSOR = javafx.scene.Cursor.NONE;
    }

    /**
     * Create a new display window positioned to fill the given rectangle.
     * <p/>
     * @param area the area in which the window should be drawn.
     * @param stageView true if the display stage is a stage view, false if it's
     * a normal projection view.
     */
    public DisplayStage(Bounds area, boolean stageView) {
        final boolean playVideo = !stageView;
        initStyle(StageStyle.TRANSPARENT);
        Utils.addIconsToStage(this);
        setTitle(LabelGrabber.INSTANCE.getLabel("projection.window.title"));
        setAreaImmediate(area);
        StackPane scenePane = new StackPane();
        scenePane.setStyle("-fx-background-color: transparent;");
        canvas = new DisplayCanvas(true, stageView, playVideo, null, stageView ? Priority.HIGH : Priority.MID);
        canvas.setCursor(BLANK_CURSOR);
        scenePane.getChildren().add(canvas);
        if (stageView) {
            final Clock clock = new Clock();
            ChangeListener<Number> cl = new ChangeListener<Number>() {

                @Override
                public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                    double size = getWidth();
                    if (getHeight() < size) {
                        size = getHeight();
                    }
                    clock.setFontSize(size / 24);
                }
            };
            widthProperty().addListener(cl);
            heightProperty().addListener(cl);
            StackPane.setAlignment(clock, Pos.BOTTOM_RIGHT);
            scenePane.getChildren().add(clock);
            clock.toFront();
        }
        testImage = new TestImage();
        testImage.getImageView().setPreserveRatio(true);
        testImage.getImageView().fitWidthProperty().bind(widthProperty());
        testImage.getImageView().fitHeightProperty().bind(heightProperty());
        scenePane.getChildren().add(testImage);
        testImage.setVisible(false);
        testImage.toFront();
        Scene scene = new Scene(scenePane);
        if (!stageView) {
            scene.setFill(null);
        }
        setScene(scene);
        if (playVideo) {
            addVLCListeners();
        }
    }

    private void addVLCListeners() {
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                VLCWindow.INSTANCE.refreshPosition();
            }
        });
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                VLCWindow.INSTANCE.refreshPosition();
            }
        });
        xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                VLCWindow.INSTANCE.refreshPosition();
            }
        });
        yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                VLCWindow.INSTANCE.refreshPosition();
            }
        });
    }

    /**
     * Set a test image to appear on this stage.
     * <p>
     * @param img the test img, or null to clear.
     * @param preserveAspect true if the aspect ratio should be preserved, false
     * if it should be stretched to fit.
     */
    public void setTestImage(Image img, boolean preserveAspect) {
        testImage.getImageView().setPreserveRatio(preserveAspect);
        testImage.setVisible(img != null);
        testImage.setImage(img);
    }
    
    /**
     * Set the Stage to be fullscreen or to make it non-fullscreen.
     * <p/>
     * @param area the area of the window.
     */
    public final void setFullScreenAlwaysOnTopImmediate(boolean fullscreen) {
        PlatformUtils.setFullScreenAlwaysOnTop(this, fullscreen);
    }
    
    /**
     * Set the Stage to be fullscreen or to make it non-fullscreen using run later.
     * <p/>
     * @param area the area of the window.
     */
    public final void setFullScreenAlwaysOnTop(boolean fullscreen) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setFullScreenAlwaysOnTopImmediate(fullscreen);  
            }
        });
    }

    /**
     * Set the area of the display window.
     * <p/>
     * @param area the area of the window.
     */
    public final void setAreaImmediate(final Bounds area) {
        setWidth(area.getMaxX() - area.getMinX());
        setHeight(area.getMaxY() - area.getMinY());
        setX(area.getMinX());
        setY(area.getMinY());
    }
    
    /**
     * Set the area of the display window using run later.
     * <p/>
     * @param area the area of the window.
     */
    public final void setArea(final Bounds area) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // ensure the window is not fullscreen always on top here
                setFullScreenAlwaysOnTopImmediate(false);
                setAreaImmediate(area);
            }
        });
    }

    /**
     * Get the canvas object that underlines this display window.
     * <p/>
     * @return the lyric canvas backing this window.
     */
    public DisplayCanvas getCanvas() {
        return canvas;
    }
}
