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
package org.quelea.windows.main.schedule;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.EditThemeScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.EditTimerThemeActionHandler;

/**
 *
 * @author Michael
 */
public class ScheduleListNode extends HBox {

    private FadeTransition fade;
    private Button themeButton;

    public ScheduleListNode(Displayable displayable) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);
        ImageView icon = displayable.getPreviewIcon();
        getChildren().add(icon);
        getChildren().add(new Label(displayable.getPreviewText()));

        if (displayable instanceof TextDisplayable || displayable instanceof TimerDisplayable) {
            themeButton = new Button("", new ImageView(new Image("file:icons/theme.png", 16, 16, false, true)));
            if(displayable instanceof TextDisplayable) {
                themeButton.setOnAction(new EditThemeScheduleActionHandler((TextDisplayable)displayable));
            }
            else {
                themeButton.setOnAction(new EditTimerThemeActionHandler((TimerDisplayable)displayable));
            }
            Utils.setToolbarButtonStyle(themeButton);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getChildren().add(spacer);
            getChildren().add(themeButton);
            themeButton.setOpacity(0);
            fade = new FadeTransition(Duration.millis(100), themeButton);
            setOnMouseEntered((event) -> {
                fade.stop();
                fade.setToValue(1);
                fade.play();
            });
            setOnMouseExited((event) -> {
                fade.stop();
                fade.setToValue(0);
                fade.play();
            });
        }
    }

}
