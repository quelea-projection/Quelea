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
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    public static final Font VERSION_FONT = new Font("Arial", 14);

    /**
     * Create a new splash window.
     */
    public SplashStage() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        Utils.addIconsToStage(this);
        setTitle("Quelea " + LabelGrabber.INSTANCE.getLabel("loading.text") + "...");
        Image splashImage = new Image(new File(QueleaProperties.VERSION.getUnstableName().getSplashPath()).getAbsoluteFile().toURI().toString());
        ImageView imageView = new ImageView(splashImage);
        Text version = new Text(QueleaProperties.VERSION.getVersionString());
        version.setFont(VERSION_FONT);
        version.setX(321);
        version.setY(208);
        Group mainPane = new Group();
        mainPane.getChildren().add(imageView);
        mainPane.getChildren().add(version);
        Scene scene = new Scene(mainPane);
        if (QueleaProperties.get().getUseDarkTheme()) {
            scene.getStylesheets().add("org/modena_dark.css");
        }
        setScene(scene);

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
    }
}
