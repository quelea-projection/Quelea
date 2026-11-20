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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.EditThemeScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.EditTimerThemeActionHandler;
import org.quelea.windows.style.QueleaIcon;

/**
 * One item in the schedule: its type icon, name and live marker, plus a theme
 * button that appears on hover. The button lies on top of the row at its
 * right edge, so it can always be reached, even over a long name in a narrow
 * schedule (ScheduleList keeps the row as wide as its cell).
 *
 * @author Michael
 */
public class ScheduleListNode extends StackPane {

    private FadeTransition fade;
    private Button themeButton;
    private Region liveIcon;

    public ScheduleListNode(Displayable displayable) {
        setAlignment(Pos.CENTER_LEFT);
        setMinWidth(0);
        Node icon = displayable.getPreviewIcon();
        liveIcon = new Region();
        liveIcon.getStyleClass().add("live-indicator");
        liveIcon.setVisible(false);
        Label name = new Label(displayable.getPreviewText());
        name.setMinWidth(0);
        HBox content = new HBox(10, icon, name, liveIcon);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMinWidth(0);
        getChildren().add(content);

        if (displayable instanceof TextDisplayable || displayable instanceof TimerDisplayable) {
            themeButton = new Button("", QueleaIcon.THEME.create());
            if (displayable instanceof TextDisplayable) {
                themeButton.setOnAction(new EditThemeScheduleActionHandler((TextDisplayable) displayable));
            } else {
                themeButton.setOnAction(new EditTimerThemeActionHandler((TimerDisplayable) displayable));
            }
            Utils.setToolbarButtonStyle(themeButton);
            themeButton.getStyleClass().add("schedule-theme-button");
            StackPane.setAlignment(themeButton, Pos.CENTER_RIGHT);
            getChildren().add(themeButton);
            themeButton.setOpacity(0);
            themeButton.setMouseTransparent(true);
            fade = new FadeTransition(Duration.millis(100), themeButton);
            setOnMouseEntered((event) -> {
                themeButton.setMouseTransparent(false);
                fade.stop();
                fade.setToValue(1);
                fade.play();
            });
            setOnMouseExited((event) -> {
                themeButton.setMouseTransparent(true);
                fade.stop();
                fade.setToValue(0);
                fade.play();
            });
        }
    }

    public void setLive(boolean live) {
        if (liveIcon != null) {
            liveIcon.setVisible(live);
        }
    }

}
