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
package org.quelea.splash;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.utils.QueleaProperties;

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
        text.setStroke(Color.WHITE);
        text.setFont(new Font("Verdana", 30));
        text.setLayoutX(170);
        text.setLayoutY(230);

        Group mainPane = new Group();
        mainPane.getChildren().add(imageView);
        mainPane.getChildren().add(text);
        setScene(new Scene(mainPane));

        
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        final GraphicsDevice[] gds = ge.getScreenDevices();
//        if(controlScreenProp >= gds.length) {
//            controlScreenProp = gds.length - 1;
//        }
//        Rectangle bounds = gds[controlScreenProp].getDefaultConfiguration().getBounds();
        
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
//        
//        
//        
//        
//        //Centre on monitor
//        setX((bounds.getLocation().x + bounds.getWidth() / 2) - splashImage.getWidth() / 2);
//        setY((bounds.getLocation().y + bounds.getHeight() / 2) - splashImage.getHeight() / 2);
    
    // centerOnScreen();
    
    }
}
