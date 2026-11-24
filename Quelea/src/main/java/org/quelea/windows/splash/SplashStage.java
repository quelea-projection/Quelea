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
package org.quelea.windows.splash;

import java.io.File;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

import org.quelea.windows.style.StyleManager;

/**
 * The splash screen to display when the program starts.
 * <p/>
 * @author Michael
 */
public class SplashStage extends Stage {

    private static final double MAX_WIDTH = 640;
    private static final Duration PROGRESS_ANIMATION_DURATION = Duration.millis(220);
    private final ProgressBar progressBar = new ProgressBar(0);
    private Timeline progressAnimation;
    private double targetProgress;

    /**
     * Create a new splash window.
     */
    public SplashStage() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        Utils.addIconsToStage(this);
        setTitle("Quelea " + LabelGrabber.INSTANCE.getLabel("loading.text") + "...");
        ObservableList<Screen> monitors = Screen.getScreens();
        Screen screen;
        int controlScreenProp = QueleaProperties.get().getControlScreen();
        if (controlScreenProp < monitors.size() && controlScreenProp >= 0) {
            screen = monitors.get(controlScreenProp);
        } else {
            screen = Screen.getPrimary();
        }

        Rectangle2D bounds = screen.getVisualBounds();
        Image splashImage = new Image(new File(QueleaProperties.VERSION.getUnstableName().getSplashPath()).getAbsoluteFile().toURI().toString());
        double aspect = splashImage.getWidth() / splashImage.getHeight();
        double width = Math.min(MAX_WIDTH, Math.min(bounds.getWidth() * 0.9, bounds.getHeight() * 0.9 * aspect));
        double height = width / aspect;
        ImageView imageView = new ImageView(splashImage);
        imageView.setFitWidth(width);
        imageView.setPreserveRatio(true);

        // The artwork has a white version badge near its top-right corner.
        Text version = new Text(QueleaProperties.VERSION.getVersionString());
        version.setId("splash-version");
        version.setFill(Color.web("#10102b"));
        version.setFont(new Font("Arial", 14 * width / MAX_WIDTH));
        double availableVersionWidth = width * 0.073;
        if (version.getLayoutBounds().getWidth() > availableVersionWidth) {
            version.setFont(new Font("Arial", version.getFont().getSize() * availableVersionWidth / version.getLayoutBounds().getWidth()));
        }
        version.setTextOrigin(VPos.CENTER);
        version.setX(width * 0.926 - version.getLayoutBounds().getWidth() / 2);
        version.setY(height * 0.075);

        progressBar.setId("splash-progress");
        progressBar.getStyleClass().add("splash-progress");
        progressBar.setAccessibleText(LabelGrabber.INSTANCE.getLabel("loading.text"));
        progressBar.setPrefSize(width * 0.82, Math.max(4, height * 0.014));
        progressBar.setLayoutX(width * 0.14);
        progressBar.setLayoutY(height * 0.913 - progressBar.getPrefHeight() / 2);

        Group mainPane = new Group(imageView, version, progressBar);
        Scene scene = new Scene(mainPane, width, height);
        StyleManager.apply(scene);
        setScene(scene);
        setX(bounds.getMinX() + (bounds.getWidth() - width) / 2);
        setY(bounds.getMinY() + (bounds.getHeight() - height) / 2);
    }

    /** Update completed startup milestones; safe to call from the startup thread. */
    public void setProgress(double progress) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setProgress(progress));
            return;
        }
        double next = Math.max(0, Math.min(1, progress));
        if (Double.isNaN(next) || next <= targetProgress) {
            return;
        }
        targetProgress = next;
        animateProgress(next, null);
    }

    /** Finish the bar before continuing with the final startup steps. */
    public void finish(Runnable onFinished) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> finish(onFinished));
            return;
        }
        targetProgress = 1;
        animateProgress(1, onFinished);
    }

    private void animateProgress(double progress, Runnable onFinished) {
        if (progressAnimation != null) {
            progressAnimation.stop();
        }
        progressAnimation = new Timeline(new KeyFrame(PROGRESS_ANIMATION_DURATION,
                new KeyValue(progressBar.progressProperty(), progress, Interpolator.EASE_OUT)));
        if (onFinished != null) {
            progressAnimation.setOnFinished(event -> onFinished.run());
        }
        progressAnimation.play();
    }
}
