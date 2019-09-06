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
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleComboBoxControl;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleIntegerControl;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.stage.Screen;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.PercentMargins;
import org.quelea.services.utils.QueleaProperties;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

public class DisplayGroup {
    private boolean displayChange = false;
    private Group group;

    DisplayGroup(String groupName, HashMap<Field, ObservableValue> bindings) {

        boolean isControl = groupName.equals(LabelGrabber.INSTANCE.getLabel(("control.screen.label")));
        boolean isProjector = groupName.equals(LabelGrabber.INSTANCE.getLabel(("projector.screen.label")));
        boolean isStage = groupName.equals(LabelGrabber.INSTANCE.getLabel("stage.screen.label"));

        // can be disabled?
        boolean noScreen = isProjector || isStage;
        // Support a custom position?
        boolean useCustom = isProjector || isStage;
        // Support display margins?
        boolean useMargins = isProjector;

        ObservableList<String> availableScreens = getAvailableScreens(useCustom);
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

        int screen;
        String screenKey;
        if (isControl)
        {
            screen = QueleaProperties.get().getControlScreen();
            screenKey = controlScreenKey;
        } else if (isProjector) {
            screen = QueleaProperties.get().getProjectorScreen();
            screenKey = projectorScreenKey;
        } else if (isStage) {
            screen = QueleaProperties.get().getStageScreen();
            screenKey = stageScreenKey;
        } else {
            throw new IllegalArgumentException("Unsupported groupName: " + groupName);
        }

        if (noScreen) {
            screen++; // Compensate for "none" value in available screens
            screenSelectProperty.setValue(screen > 0 && screen < availableScreens.size() ? availableScreens.get(screen) : availableScreens.get(0));
        } else {
            screenSelectProperty.setValue(screen > -1 ? availableScreens.get(screen) : availableScreens.get(0));
        }

        settings.add(Setting.of(groupName, customControl, screenSelectProperty).customKey(screenKey));

        if (useCustom) {
            Bounds bounds;
            BooleanProperty useCustomPosition;
            if (isProjector) {
                bounds = QueleaProperties.get().getProjectorCoords();
                useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isProjectorModeCoords());
            } else if (isStage) {
                bounds = QueleaProperties.get().getStageCoords();
                useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isStageModeCoords());
            } else {
                throw new IllegalArgumentException("Unsupported groupName: " + groupName);
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

            useCustomPosition.addListener(e -> {
                displayChange = true;
            });


            settings.add(Setting.of(LabelGrabber.INSTANCE.getLabel("custom.position.text"), useCustomPosition)
                            .customKey(isProjector ? projectorModeKey : stageModeKey));
            settings.add(Setting.of("W", sizeWith, widthProperty)
                            .customKey(isProjector ? projectorWCoordKey : stageWCoordKey));
            settings.add(Setting.of("H", sizeHeight, heightProperty)
                            .customKey(isProjector ? projectorHCoordKey : stageHCoordKey));
            settings.add(Setting.of("X", posX, xProperty)
                            .customKey(isProjector ? projectorXCoordKey : stageXCoordKey));
            settings.add(Setting.of("Y", posY, yProperty)
                            .customKey(isProjector ? projectorYCoordKey : stageYCoordKey));

            bindings.put(sizeWith, useCustomPosition.not());
            bindings.put(sizeHeight, useCustomPosition.not());
            bindings.put(posX, useCustomPosition.not());
            bindings.put(posY, useCustomPosition.not());
            bindings.put(customControl, useCustomPosition);
        }

        if (useMargins) {
            PercentMargins margins;
            if (isProjector) {
                margins = QueleaProperties.get().getProjectorMargin();
            } else {
                throw new IllegalArgumentException("Unsupported groupName: " + groupName);
            }

            IntegerRangeValidator validator = IntegerRangeValidator.upTo(99, "Maximum value of 99");
            IntegerProperty marginTopProperty = new SimpleIntegerProperty((int)(margins.getTop() * 100));
            IntegerProperty marginRightProperty = new SimpleIntegerProperty((int)(margins.getRight() * 100));
            IntegerProperty marginBottomProperty = new SimpleIntegerProperty((int)(margins.getBottom() * 100));
            IntegerProperty marginLeftProperty = new SimpleIntegerProperty((int)(margins.getLeft() * 100));
            IntegerField marginTop = Field.ofIntegerType(marginTopProperty).render(
                    new SimpleIntegerControl());
            IntegerField marginRight = Field.ofIntegerType(marginRightProperty).render(
                    new SimpleIntegerControl());
            IntegerField marginBottom = Field.ofIntegerType(marginBottomProperty).render(
                    new SimpleIntegerControl());
            IntegerField marginLeft = Field.ofIntegerType(marginLeftProperty).render(
                    new SimpleIntegerControl());

            marginTop.validate(validator);
            marginRight.validate(validator);
            marginBottom.validate(validator);

            ChangeListener<Number> onMarginNumberChange = (observable, oldValue, newValue) -> {
                IntegerField field;
                IntegerField oppositeField;
                if (observable == marginTopProperty) {
                    field = marginTop;
                    oppositeField = marginBottom;
                } else if (observable == marginRightProperty) {
                    field = marginRight;
                    oppositeField = marginLeft;
                } else if (observable == marginBottomProperty) {
                    field = marginBottom;
                    oppositeField = marginTop;
                } else if (observable == marginLeftProperty) {
                    field = marginLeft;
                    oppositeField = marginRight;
                } else {
                    throw new IllegalArgumentException();
                }

                displayChange = true;

                // make sure the margins only add up to 99 at most, leaving 1% for content
                int total = field.getValue() + oppositeField.getValue();
                if (total > 99) {
                    int suggestedOpposite = 99 - field.getValue();
                    if (suggestedOpposite > 0) {
                        oppositeField.valueProperty().set(suggestedOpposite);
//                        oppositeField.setValue(suggestedOpposite);
                    } else {
                        // A number greater than 99 has been entered in the field
                        // clamp to 99
                        field.valueProperty().set(99);
                        oppositeField.valueProperty().set(0);
                    }
                }
            };


            marginTopProperty.addListener(onMarginNumberChange);
            marginRightProperty.addListener(onMarginNumberChange);
            marginBottomProperty.addListener(onMarginNumberChange);
            marginLeftProperty.addListener(onMarginNumberChange);


            settings.add(Setting.of(LabelGrabber.INSTANCE.getLabel("projector.margin.top"), marginTop, marginTopProperty)
                    .customKey(projectorMarginTopKey));
            settings.add(Setting.of(LabelGrabber.INSTANCE.getLabel("projector.margin.right"), marginRight, marginRightProperty)
                    .customKey(projectorMarginRightKey));
            settings.add(Setting.of(LabelGrabber.INSTANCE.getLabel("projector.margin.bottom"), marginBottom, marginBottomProperty)
                    .customKey(projectorMarginBottomKey));
            settings.add(Setting.of(LabelGrabber.INSTANCE.getLabel("projector.margin.left"), marginLeft, marginLeftProperty)
                    .customKey(projectorMarginLeftKey));
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
