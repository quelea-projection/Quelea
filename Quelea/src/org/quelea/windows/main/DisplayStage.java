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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.widgets.Clock;
import org.quelea.windows.main.widgets.TestImage;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * The full screen window used for displaying the projection.
 * <p/>
 * @author Michael
 */
public class DisplayStage extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Cursor BLANK_CURSOR;
    private final DisplayCanvas canvas;
    private final DisplayCanvas previewCanvas;
    private final TestImage testImage;
    private Label titleLabel = null;
    private AnchorPane screens = null;

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
        final boolean playVideo;
        final boolean textOnly;
        initStyle(StageStyle.TRANSPARENT);
        Utils.addIconsToStage(this);
        setTitle(LabelGrabber.INSTANCE.getLabel("projection.window.title"));
        setArea(area);
        StackPane scenePane = new StackPane();
        Priority priority;
        if (stageView) {
            priority = Priority.HIGH;
            playVideo = false;
            textOnly = false;
        } else if (QueleaApp.get().getProjectionWindow() != null) {
            priority = Priority.MID;
            playVideo = false;
            textOnly = true;
        } else {
            priority = Priority.HIGH_MID;
            playVideo = true;
            textOnly = false;
        }
        if (stageView) {
            previewCanvas = new DisplayCanvas(true, stageView, playVideo, null, priority, textOnly, null);
        } else {
            previewCanvas = null;
        }
        canvas = new DisplayCanvas(true, stageView, playVideo, null, priority, textOnly, previewCanvas);

        canvas.setType(stageView ? DisplayCanvas.Type.STAGE : DisplayCanvas.Type.FULLSCREEN);
        canvas.setCursor(BLANK_CURSOR);

        if (stageView) {
            titleLabel = canvas.getTitleLabel();

            screens = new AnchorPane();
            AnchorPane.setTopAnchor(canvas, 0.0);
            AnchorPane.setLeftAnchor(canvas, 0.0);
            AnchorPane.setBottomAnchor(previewCanvas, 0.0);
            AnchorPane.setLeftAnchor(previewCanvas, 0.0);

            AnchorPane.setLeftAnchor(titleLabel, 5.0 * (getWidth() / 8.0));

            screens.getChildren().add(canvas);
            screens.getChildren().add(previewCanvas);
            screens.getChildren().add(titleLabel);

            screens.setStyle("-fx-background-color: " + Utils.getHexFromColor(QueleaProperties.get().getStageBackgroundColor()) + ";");
            scenePane.getChildren().add(screens);

            screens.toFront();

            final Clock clock = new Clock();
            ChangeListener<Number> cl = new ChangeListener<Number>() {

                @Override
                public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                    double size = getWidth();
                    if (getHeight() < size) {
                        size = getHeight();
                    }
                    clock.setFontSize(size / 24);
                    updateStage();
                }
            };

            widthProperty().addListener(cl);
            heightProperty().addListener(cl);
            StackPane.setAlignment(clock, Pos.BOTTOM_RIGHT);
            scenePane.getChildren().add(clock);
            clock.toFront();
        } else {
            scenePane.getChildren().add(canvas);
        }

        testImage = new TestImage();
        testImage.getImageView().setPreserveRatio(true);
        testImage.getImageView().fitWidthProperty().bind(widthProperty());
        testImage.getImageView().fitHeightProperty().bind(heightProperty());
        scenePane.getChildren().add(testImage);
        testImage.setVisible(false);
        testImage.toFront();
        Scene scene = new Scene(scenePane);
        scene.setFill(null);
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
     * Set the area of the display window.
     * <p/>
     * @param area the area of the window.
     */
    public final void setArea(final Bounds area) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setWidth(area.getMaxX() - area.getMinX());
                setHeight(area.getMaxY() - area.getMinY());
                setX(area.getMinX());
                setY(area.getMinY());
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

    /**
     * Gets the preview canvas object that shows the preview of the next item
     *
     * @return the preview canvas is stage view
     */
    public DisplayCanvas getStagePreviewCanvas() {
        return previewCanvas;
    }

    /**
     * Update Stage
     */
    public void updateStage() {
        if (getCanvas().isStageView()) {
            screens.setStyle("-fx-background-color: " + Utils.getHexFromColor(QueleaProperties.get().getStageBackgroundColor()) + ";");
            double size = getWidth();
            if (getHeight() < size) {
                size = getHeight();
            }
            if (QueleaProperties.get().getStageUsePreview()) {
                canvas.setMinSize(getWidth(), 2.0 * (getHeight() / 3.0));
                previewCanvas.setMinSize(5.0 * (getWidth() / 8.0), 1.0 * (getHeight() / 3.0));
                canvas.setMaxSize(getWidth(), 2.0 * (getHeight() / 3.0));
                previewCanvas.setMaxSize(5.0 * (getWidth() / 8.0), 1.0 * (getHeight() / 3.0));
                previewCanvas.toFront();
                canvas.setPrefSize(getWidth(), 2.0 * (getHeight() / 3.0));
                previewCanvas.setPrefSize(5.0 * (getWidth() / 8.0), 1.0 * (getHeight() / 3.0));
                AnchorPane.setLeftAnchor(titleLabel, (5.0 * (getWidth() / 8.0)) + 10);
                AnchorPane.setTopAnchor(titleLabel, (2.0 * (getHeight() / 3.0)) + ((getHeight() - (2.0 * (getHeight() / 3.0))) / 3));
                titleLabel.setFont(Font.font("Noto Sans", FontWeight.BOLD, FontPosture.REGULAR, size / 32));
                titleLabel.setTextFill(QueleaProperties.get().getStageChordColor());
                titleLabel.toFront();
            } else {
                canvas.setMinSize(getWidth(), getHeight());
                canvas.setPrefSize(getWidth(), getHeight());

                previewCanvas.clearCurrentDisplayable();
                previewCanvas.toBack();
                titleLabel.toBack();

            }
        }
    }

}
