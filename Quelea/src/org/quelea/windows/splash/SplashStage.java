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

    /**
     * Create a new splash window.
     */
    public SplashStage() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        Utils.addIconsToStage(this);
        setTitle("Quelea " + LabelGrabber.INSTANCE.getLabel("loading.text") + "...");
        String minorVersion = QueleaProperties.VERSION.getMinorVersionString();
        Image splashImage = new Image("file:icons/splash-bare.png");
        version = Version.FINAL;
        if(minorVersion.toLowerCase().trim().startsWith("alpha")) {
            splashImage = new Image("file:icons/splash-alpha.png");
            version = Version.ALPHA;
        }
        else if(minorVersion.toLowerCase().trim().startsWith("beta")) {
            splashImage = new Image("file:icons/splash-beta.png");
            version = Version.BETA;
        }
        ImageView imageView = new ImageView(splashImage);
        Text loadingText = new Text(LabelGrabber.INSTANCE.getLabel("loading.text"));
        Font loadingFont = Font.loadFont("file:icons/Ubuntu-RI.ttf", 33);
        FontMetrics loadingMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(loadingFont);
        LinearGradient loadingGrad = new LinearGradient(0, 1, 0, 0, true, CycleMethod.REPEAT, new Stop(0, Color.web("#666666")), new Stop(1, Color.web("#000000")));
        loadingText.setFill(loadingGrad);
        loadingText.setFont(loadingFont);
        loadingText.setLayoutX(splashImage.getWidth() - loadingMetrics.computeStringWidth(loadingText.getText() + "...")-20);
        loadingText.setLayoutY(325);
        Text versionText = new Text(QueleaProperties.VERSION.getVersionString());
        LinearGradient versionGrad = new LinearGradient(0, 1, 0, 0, true, CycleMethod.REPEAT, new Stop(0, Color.web("#000000")), new Stop(1, Color.web("#666666")));
        versionText.setFill(versionGrad);
        versionText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 35));
        versionText.setLayoutX(447);
        versionText.setLayoutY(183);
        Text minorText = null;
        String minorNum = null;
        if(version != Version.FINAL) {
            String[] parts = minorVersion.split(" ");
            if(parts.length > 1) {
                minorNum = parts[1];
            }
        }
        if(minorNum != null) {
            minorText = new Text(minorNum);
            minorText.setFill(versionGrad);
            if(version == Version.ALPHA) {
                minorText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 30));
                minorText.setLayoutX(30);
                minorText.setLayoutY(305);
            }
            else if(version == Version.BETA) {
                minorText.setFont(Font.loadFont("file:icons/Ubuntu-RI.ttf", 26));
                minorText.setLayoutX(70);
                minorText.setLayoutY(305);
            }
        }
        Pips pips = new Pips(loadingFont, loadingGrad);
        pips.setLayoutX(loadingText.getLayoutX()+loadingMetrics.computeStringWidth(LabelGrabber.INSTANCE.getLabel("loading.text")));
        pips.setLayoutY(loadingText.getLayoutY());
        Group mainPane = new Group();
        mainPane.getChildren().add(imageView);
        mainPane.getChildren().add(loadingText);
        mainPane.getChildren().add(versionText);
        mainPane.getChildren().add(pips);
        if(minorText != null) {
            mainPane.getChildren().add(minorText);
        }
        setScene(new Scene(mainPane));

        ObservableList<Screen> monitors = Screen.getScreens();
        Screen screen;
        int controlScreenProp = QueleaProperties.get().getControlScreen();
        if(controlScreenProp < monitors.size() && controlScreenProp >=0) {
            screen = monitors.get(controlScreenProp);
        }
        else {
            screen = Screen.getPrimary();
        }

        Rectangle2D bounds = screen.getVisualBounds();
        setX(bounds.getMinX()+((bounds.getWidth() / 2) - splashImage.getWidth() / 2));
        setY(bounds.getMinY()+((bounds.getHeight() / 2) - splashImage.getHeight() / 2));
    }
}
