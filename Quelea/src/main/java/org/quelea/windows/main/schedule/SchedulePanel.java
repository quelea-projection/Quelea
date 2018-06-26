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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler;

/**
 * The panel displaying the schedule / order of service. Items from here are
 * loaded into the preview panel where they are viewed and then projected live.
 * Items can be added here from the library.
 * <p/>
 * @author Michael
 */
public class SchedulePanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ScheduleList scheduleList;
    private final Button removeButton;
    private final Button upButton;
    private final Button downButton;
    private final Button themeButton;
    private final ScheduleThemeNode scheduleThemeNode;
    private Stage themePopup;

    /**
     * Create and initialise the schedule panel.
     */
    public SchedulePanel() {
        ImageView themeButtonIcon = new ImageView(new Image("file:icons/theme.png"));
        themeButtonIcon.setFitWidth(16);
        themeButtonIcon.setFitHeight(16);
        themeButton = new Button("", themeButtonIcon);
        themeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("theme.button.tooltip")));
        scheduleList = new ScheduleList();
        scheduleList.itemsProperty().get().addListener(new ListChangeListener<Displayable>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Displayable> change) {
                scheduleThemeNode.updateTheme();
            }
        });

        themePopup = new Stage();
        themePopup.setTitle(LabelGrabber.INSTANCE.getLabel("theme.select.text"));
        Utils.addIconsToStage(themePopup);
        themePopup.initStyle(StageStyle.UNDECORATED);
        themePopup.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t && !t1) {
                    if (Utils.isMac()) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                themePopup.hide();
                            }
                        });
                        
                    } else {
                        themePopup.hide();
                    }
                }
            }
        });

        scheduleThemeNode = new ScheduleThemeNode(new ScheduleThemeNode.UpdateThemeCallback() {
            @Override
            public void updateTheme(ThemeDTO theme) {
                if (scheduleList == null) {
                    LOGGER.log(Level.WARNING, "Null schedule, not setting theme");
                    return;
                }
                for (int i = 0; i < scheduleList.itemsProperty().get().size(); i++) {
                    Displayable displayable = scheduleList.itemsProperty().get().get(i);
                    if (displayable instanceof TextDisplayable) {
                        TextDisplayable textDisplayable = (TextDisplayable) displayable;
                        for (TextSection section : textDisplayable.getSections()) {
                            section.setTempTheme(theme);
                        }
                    }
                }
            }
        }, themePopup, themeButton);
        scheduleThemeNode.setStyle("-fx-background-color:WHITE;-fx-border-color: rgb(49, 89, 23);-fx-border-radius: 5;");
        themePopup.setScene(new Scene(scheduleThemeNode));

        themeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (themePopup.isShowing()) {
                    //fixes a JVM crash
                    if (Utils.isMac()) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                themePopup.hide();
                            }
                        });
                    } else {
                        themePopup.hide();
                    }

                } else {
                    themePopup.setX(themeButton.localToScene(0, 0).getX() + QueleaApp.get().getMainWindow().getX());
                    themePopup.setY(themeButton.localToScene(0, 0).getY() + 45 + QueleaApp.get().getMainWindow().getY());
                    themePopup.show();
                }
            }
        });
//        themeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("adjust.theme.tooltip")));

        ToolBar toolbar = new ToolBar();
        toolbar.setOrientation(Orientation.VERTICAL);
        ImageView removeIV = new ImageView(new Image("file:icons/cross.png"));
        removeIV.setFitWidth(16);
        removeIV.setFitHeight(16);
        removeButton = new Button("", removeIV);
        Utils.setToolbarButtonStyle(removeButton);
        removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.song.schedule.tooltip")));
        removeButton.setDisable(true);
        removeButton.setOnAction(new RemoveScheduleItemActionHandler());

        ImageView upIV = new ImageView(new Image("file:icons/up.png"));
        upIV.setFitWidth(16);
        upIV.setFitHeight(16);
        upButton = new Button("", upIV);
        Utils.setToolbarButtonStyle(upButton);
        upButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("move.up.schedule.tooltip")));
        upButton.setDisable(true);
        upButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.UP);
            }
        });

        ImageView downIV = new ImageView(new Image("file:icons/down.png"));
        downIV.setFitWidth(16);
        downIV.setFitHeight(16);
        downButton = new Button("", downIV);
        Utils.setToolbarButtonStyle(downButton);
        downButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("move.down.schedule.tooltip")));
        downButton.setDisable(true);
        downButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.DOWN);
            }
        });

        scheduleList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                updateScheduleDisplay();
            }
        });

        ToolBar header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("order.service.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        header.getItems().add(themeButton);

        toolbar.getItems().add(removeButton);
        toolbar.getItems().add(upButton);
        toolbar.getItems().add(downButton);

        setTop(header);
        setLeft(toolbar);
        setCenter(scheduleList);
    }

    public void updateScheduleDisplay() {
        if (scheduleList.getItems().isEmpty()) {
            removeButton.setDisable(true);
            upButton.setDisable(true);
            downButton.setDisable(true);
        } else {
            removeButton.setDisable(false);
            upButton.setDisable(false);
            downButton.setDisable(false);
            QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(scheduleList.getSelectionModel().getSelectedItem(), 0);
        }
    }

    /**
     * Get the schedule list backing this panel.
     * <p/>
     * @return the schedule list.
     */
    public ScheduleList getScheduleList() {
        return scheduleList;
    }

    public Button getThemeButton() {
        return themeButton;
    }
    
    public ScheduleThemeNode getThemeNode() {
        return scheduleThemeNode;
    }

}
