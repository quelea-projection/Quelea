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

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.utils.QueleaProperties;

/**
 * The splash screen to display when the program starts.
 * <p/>
 * @author Michael
 */
public class SplashStage extends Stage {

    /**
     * Create a new splash window.
     */
    public SplashStage() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        getIcons().add(new Image("file:icons/logo.png"));
        setTitle("Quelea loading...");
        Image splashImage = new Image("file:icons/splash.png");
        ImageView imageView = new ImageView(splashImage);
        Text text = new Text(QueleaProperties.VERSION.getVersionString());
        text.setFont(Font.font("SansSerif", FontWeight.BOLD, FontPosture.ITALIC, 30));
        text.setLayoutX(447);
        text.setLayoutY(183);
        InnerShadow is = new InnerShadow();
        is.setOffsetX(2.0f);
        is.setOffsetY(2.0f);
        is.setColor(Color.GRAY);
        text.setEffect(is);
        Text minorText = null;
        if(QueleaProperties.VERSION.getMinorVersionString() != null) {
            minorText = new Text(QueleaProperties.VERSION.getMinorVersionString());
            minorText.setFont(Font.font("SansSerif", FontWeight.BOLD, FontPosture.ITALIC, 30));
            minorText.setLayoutX(40);
            minorText.setLayoutY(235);
            minorText.setEffect(is);
        }

        Group mainPane = new Group();
        mainPane.getChildren().add(imageView);
        mainPane.getChildren().add(text);
        if(minorText != null) {
            mainPane.getChildren().add(minorText);
        }
        setScene(new Scene(mainPane));

        ObservableList<Screen> monitors = Screen.getScreens();
        Screen screen;
        int controlScreenProp = QueleaProperties.get().getControlScreen();
        if(controlScreenProp < monitors.size()) {
            screen = monitors.get(controlScreenProp);
        }
        else {
            screen = Screen.getPrimary();
        }

        Rectangle2D bounds = screen.getVisualBounds();

        setX((bounds.getWidth() / 2) - splashImage.getWidth() / 2);
        setY((bounds.getHeight() / 2) - splashImage.getHeight() / 2);
    }
}
