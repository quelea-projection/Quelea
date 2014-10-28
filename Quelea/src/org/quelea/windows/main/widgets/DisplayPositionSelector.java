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
package org.quelea.windows.main.widgets;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.quelea.data.ThemeDTO;
import org.quelea.windows.newsong.ThemePanel;

/**
 *
 * @author Michael
 */
public class DisplayPositionSelector extends BorderPane {

    private List<ToggleButton> buttons;

    public DisplayPositionSelector(final ThemePanel panel) {
        buttons = new ArrayList<>();
        ToggleGroup group = new ToggleGroup();
        GridPane selectorPane = new GridPane();
        selectorPane.prefWidthProperty().bind(widthProperty());
        selectorPane.prefHeightProperty().bind(heightProperty().subtract(50));
        for (int i = 0; i < 9; i++) {
            ToggleButton but = new ToggleButton();
            but.prefWidthProperty().bind(widthProperty().divide(3));
            but.prefHeightProperty().bind(heightProperty().divide(3));
            but.setToggleGroup(group);
            but.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    if (panel != null) {
                        panel.updateTheme(true);
                    }
                }
            });
            selectorPane.add(but, i % 3, i / 3);
            buttons.add(but);
        }
        setCenter(selectorPane);
        setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                FadeTransition trans = new FadeTransition(Duration.seconds(0.2), DisplayPositionSelector.this);
                trans.setToValue(0.7);
                trans.play();
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                FadeTransition trans = new FadeTransition(Duration.seconds(0.2), DisplayPositionSelector.this);
                trans.setToValue(0);
                trans.play();
            }
        });
        setOpacity(0);
    }

    public void setTheme(ThemeDTO theme) {
        if (theme.getTextPosition() == -1) {
            for (ToggleButton button : buttons) {
                button.setSelected(false);
            }
        } else {
            buttons.get(theme.getTextPosition()).setSelected(true);
        }
    }

    public int getSelectedButtonIndex() {
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                return buttons.indexOf(button);
            }
        }
        return -1;
    }

    public static Pos getPosFromIndex(int index) {
        switch (index) {
            case -1:
                return Pos.CENTER;
            case 0:
                return Pos.TOP_LEFT;
            case 1:
                return Pos.TOP_CENTER;
            case 2:
                return Pos.TOP_RIGHT;
            case 3:
                return Pos.CENTER_LEFT;
            case 4:
                return Pos.CENTER;
            case 5:
                return Pos.CENTER_RIGHT;
            case 6:
                return Pos.BOTTOM_LEFT;
            case 7:
                return Pos.BOTTOM_CENTER;
            case 8:
                return Pos.BOTTOM_RIGHT;
            default:
                return Pos.CENTER;
        }
    }

}
