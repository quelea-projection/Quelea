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

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
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

/**
 * The splash screen to display when the program starts.
 * <p/>
 * @author Michael
 */
public class SplashStage extends Stage {

    private enum Version {

        ALPHA, BETA, FINAL
    }
    private Version version;
    private Thread animationT;
    private final double STAGE_FADE_SPEED = 0.2; //seconds 
    private final double FIRST_FADE_SPEED = 0.6; //seconds
    private final double SECOND_FADE_SPEED = 1.2; //seconds

    /**
     * Create a new splash window.
     */
    public SplashStage() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        Utils.addIconsToStage(this);
        setTitle("Quelea " + LabelGrabber.INSTANCE.getLabel("loading.text") + "...");
        String minorVersion = QueleaProperties.VERSION.getUnstableName();
        Image bareSplashImage = new Image("file:icons/splash-bare.png");
        Image splashImage = new Image("file:icons/splash-bare.png");
        final boolean isNightly = splashImage.getPixelReader().getColor(0, 0).equals(Color.web("#8c8c8c"));
        version = Version.FINAL;
        if (minorVersion.toLowerCase().trim().startsWith("alpha")) {
            splashImage = new Image("file:icons/splash-alpha.png");
            version = Version.ALPHA;
        } else if (minorVersion.toLowerCase().trim().startsWith("beta")) {
            splashImage = new Image("file:icons/splash-beta.png");
            version = Version.BETA;
        }
        final ImageView imageView = new ImageView(splashImage);
        ImageView imageViewOrig = new ImageView(bareSplashImage);
        final Text loadingText = new Text(LabelGrabber.INSTANCE.getLabel("loading.text"));
        Font loadingFont = Font.loadFont("file:icons/Ubuntu-RI.ttf", 33);
        FontMetrics loadingMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(loadingFont);
        LinearGradient loadingGrad = new LinearGradient(0, 1, 0, 0, true, CycleMethod.REPEAT, new Stop(0, Color.web("#666666")), new Stop(1, Color.web("#000000")));
        loadingText.setFill(loadingGrad);
        loadingText.setFont(loadingFont);
        loadingText.setLayoutX(splashImage.getWidth() - loadingMetrics.computeStringWidth(loadingText.getText() + "...") - 20);
        loadingText.setLayoutY(325);
        final Text versionText = new Text(QueleaProperties.VERSION.getMajorVersionNumber());
        LinearGradient versionGrad = new LinearGradient(0, 1, 0, 0, true, CycleMethod.REPEAT, new Stop(0, Color.web("#000000")), new Stop(1, Color.web("#666666")));
        versionText.setFill(versionGrad);
        versionText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 35));
        versionText.setLayoutX(447);
        versionText.setLayoutY(183);
        Text minorText = null;
        String minorNum = QueleaProperties.VERSION.getMinorName();
        if (minorNum != null) {
            minorText = new Text(minorNum);
            minorText.setFill(versionGrad);
            if (version == Version.ALPHA) {
                minorText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 30));
                minorText.setLayoutX(30);
                minorText.setLayoutY(305);
            } else if (version == Version.BETA) {
                minorText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 26));
                minorText.setLayoutX(70);
                minorText.setLayoutY(305);
            } else {
                minorText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 30));

                if (isNightly) {
                    minorText.setLayoutX(20);
                    minorText.setLayoutY(285);
                } else {
                    minorText.setLayoutX(10);
                    minorText.setLayoutY(325);
                }
            }
        }
        final Pips pips = new Pips(loadingFont, loadingGrad);
        pips.setLayoutX(loadingText.getLayoutX() + loadingMetrics.computeStringWidth(LabelGrabber.INSTANCE.getLabel("loading.text")));
        pips.setLayoutY(loadingText.getLayoutY());
        imageView.setOpacity(0);
        loadingText.setOpacity(0);
        versionText.setOpacity(0);
        pips.setOpacity(0);
        final Text minorTextFin = minorText;
        if (minorTextFin != null) {
            minorTextFin.setOpacity(0);
        }
        Group mainPane = new Group();
        mainPane.getChildren().add(imageViewOrig);
        mainPane.getChildren().add(imageView);
        mainPane.getChildren().add(loadingText);
        mainPane.getChildren().add(versionText);
        mainPane.getChildren().add(pips);
        if (minorTextFin != null) {
            mainPane.getChildren().add(minorTextFin);
        }
        setScene(new Scene(mainPane));

        ObservableList<Screen> monitors = Screen.getScreens();
        Screen screen;
        int controlScreenProp = QueleaProperties.get().getControlScreen();
        if (controlScreenProp < monitors.size() && controlScreenProp >= 0) {
            screen = monitors.get(controlScreenProp);
        } else {
            screen = Screen.getPrimary();
        }

        Rectangle2D bounds = screen.getVisualBounds();
        setX(bounds.getMinX() + ((bounds.getWidth() / 2) - splashImage.getWidth() / 2));
        setY(bounds.getMinY() + ((bounds.getHeight() / 2) - splashImage.getHeight() / 2));

        animationT = new Thread(new Runnable() {

            @Override
            public void run() {
                  Utils.fxRunAndWait(new Runnable() {

                    @Override
                    public void run() {
                        FadeTransition ft = new FadeTransition(Duration.seconds(FIRST_FADE_SPEED), imageView);
                        FadeTransition ft2 = new FadeTransition(Duration.seconds(FIRST_FADE_SPEED), versionText);
                        FadeTransition ft3 = new FadeTransition(Duration.seconds(FIRST_FADE_SPEED), minorTextFin);
                        ft.setToValue(1);
                        ft2.setToValue(1);
                        ft3.setToValue(1);
                        ft.setInterpolator(Interpolator.EASE_BOTH);
                        ft2.setInterpolator(Interpolator.EASE_BOTH);
                        ft3.setInterpolator(Interpolator.EASE_BOTH);
                        ft.play();
                        ft2.play();
                        ft3.play();

                    }
                });
                Utils.sleep(500);
                Utils.fxRunAndWait(new Runnable() {

                    @Override
                    public void run() {
                        FadeTransition ft4 = new FadeTransition(Duration.seconds(SECOND_FADE_SPEED), loadingText);
                        FadeTransition ft5 = new FadeTransition(Duration.seconds(SECOND_FADE_SPEED), pips);
                        ft4.setToValue(1);
                        ft5.setToValue(1);
                        ft4.setInterpolator(Interpolator.EASE_BOTH);
                        ft5.setInterpolator(Interpolator.EASE_BOTH);
                        ft4.play();
                        ft5.play();

                    }
                });

            }
        });

    }

    public void showAndAnimate() {
        this.setOpacity(0);
        this.show();
        final int steps = (int) (STAGE_FADE_SPEED * 60);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                double op = 0.0;
                animationT.start();
                for (int i = 0; i < steps; i++) {
                    op = op + 1.0 / (double) steps;
                    final double opFin = op;
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            SplashStage.this.setOpacity(opFin);
                        }
                    });

                    Utils.sleep((long) ((1000 * STAGE_FADE_SPEED) / steps));
                }
               
            }
        });
        t.start();

    }
}
