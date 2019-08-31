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
package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.IntegerField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleComboBoxControl;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleIntegerControl;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.stage.Screen;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

import java.util.ArrayList;
import java.util.HashMap;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

public class DisplayGroup {
    private boolean displayChange = false;
    private Group group;

    /**
     * @param custom true if a custom position should be allowed for this
     * display panel.
     * @param margins true if margins should be allowed for this
     * display panel.
     */
    DisplayGroup(String groupName, boolean custom, boolean margins, HashMap<Field, ObservableValue> bindings) {
        BooleanProperty useCustomPosition = new SimpleBooleanProperty(false);
        if (groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label"))) {
            useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isProjectorModeCoords());
        } else if (groupName.equals(LabelGrabber.INSTANCE.getLabel("stage.screen.label"))) {
            useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isStageModeCoords());
        }
        useCustomPosition.addListener(e -> {
            displayChange = true;
        });

        ObservableList<String> availableScreens = getAvailableScreens(custom);
        ListProperty<String> screenListProperty = new SimpleListProperty<>(availableScreens);
        ObjectProperty<String> screenSelectProperty = new SimpleObjectProperty<>(availableScreens.get(0));
        Field customControl = Field.ofSingleSelectionType(screenListProperty, screenSelectProperty).render(
                new SimpleComboBoxControl<>());

        availableScreens.addListener((ListChangeListener<? super String>) e -> {
            displayChange = true;
        });

        screenSelectProperty.addListener(e -> {
            displayChange = true;
        });

        ArrayList<Setting> settings = new ArrayList<>();

        if (!custom) {
            int screen = QueleaProperties.get().getControlScreen();
            screenSelectProperty.setValue(screen > -1 ? availableScreens.get(screen) : availableScreens.get(0));
            settings.add(Setting.of(groupName, customControl, screenSelectProperty).customKey(controlScreenKey));
        } else {
            int screen;
            Bounds bounds;
            if (groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label"))) {
                screen = QueleaProperties.get().getProjectorScreen();
                bounds = QueleaProperties.get().getProjectorCoords();
            } else {
                screen = QueleaProperties.get().getStageScreen();
                bounds = QueleaProperties.get().getStageCoords();
            }

            IntegerProperty widthProperty = new SimpleIntegerProperty((int) bounds.getWidth());
            IntegerProperty heightProperty = new SimpleIntegerProperty((int) bounds.getHeight());
            IntegerProperty xProperty = new SimpleIntegerProperty((int) bounds.getMinX());
            IntegerProperty yProperty = new SimpleIntegerProperty((int) bounds.getMinY());
            IntegerField sizeWith = Field.ofIntegerType(widthProperty).render(
                    new SimpleIntegerControl());
            IntegerField sizeHeight = Field.ofIntegerType(heightProperty).render(
                    new SimpleIntegerControl());
            IntegerField posX = Field.ofIntegerType(xProperty).render(
                    new SimpleIntegerControl());
            IntegerField posY = Field.ofIntegerType(yProperty).render(
                    new SimpleIntegerControl());

            widthProperty.addListener(e -> {
                displayChange = true;
            });

            heightProperty.addListener(e -> {
                displayChange = true;
            });

            xProperty.addListener(e -> {
                displayChange = true;
            });

            yProperty.addListener(e -> {
                displayChange = true;
            });

            screen++; // Compensate for "none" value in available screens

            screenSelectProperty.setValue(screen > 0 && screen < availableScreens.size() ? availableScreens.get(screen) : availableScreens.get(0));
            boolean projectorGroup = groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label"));

            settings.add(Setting.of(groupName, customControl, screenSelectProperty)
                            .customKey(projectorGroup ? projectorScreenKey : stageScreenKey));
            settings.add(Setting.of(LabelGrabber.INSTANCE.getLabel("custom.position.text"), useCustomPosition)
                            .customKey(projectorGroup ? projectorModeKey : stageModeKey));
            settings.add(Setting.of("W", sizeWith, widthProperty)
                            .customKey(projectorGroup ? projectorWCoordKey : stageWCoordKey));
            settings.add(Setting.of("H", sizeHeight, heightProperty)
                            .customKey(projectorGroup ? projectorHCoordKey : stageHCoordKey));
            settings.add(Setting.of("X", posX, xProperty)
                            .customKey(projectorGroup ? projectorXCoordKey : stageXCoordKey));
            settings.add(Setting.of("Y", posY, yProperty)
                            .customKey(projectorGroup ? projectorYCoordKey : stageYCoordKey));

            bindings.put(sizeWith, useCustomPosition.not());
            bindings.put(sizeHeight, useCustomPosition.not());
            bindings.put(posX, useCustomPosition.not());
            bindings.put(posY, useCustomPosition.not());
            bindings.put(customControl, useCustomPosition);
        }


        Setting[] settingsArray = new Setting[settings.size()];
        group = Group.of(groupName,
                settings.toArray(settingsArray)
        );
    }


    /**
     * Get a list model describing the available graphical devices.
     *
     * @return a list model describing the available graphical devices.
     */
    private ObservableList<String> getAvailableScreens(boolean none) {
        ObservableList<Screen> monitors = Screen.getScreens();

        ObservableList<String> descriptions = FXCollections.<String>observableArrayList();
        if (none) {
            descriptions.add(LabelGrabber.INSTANCE.getLabel("none.text"));
        }
        for (int i = 0; i < monitors.size(); i++) {
            descriptions.add(LabelGrabber.INSTANCE.getLabel("output.text") + " " + (i + 1));
        }
        return descriptions;
    }

    public boolean isDisplayChange() {
        return displayChange;
    }

    public void setDisplayChange(boolean displayChange) {
        this.displayChange = displayChange;
    }

    public Group getGroup() {
        return group;
    }
}
