/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.menus;

import java.util.logging.Logger;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.mail.Mailer;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.main.actionhandlers.AddAudioActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.AddVideoActionHandler;
import org.quelea.windows.main.actionhandlers.ShowNoticesActionHandler;

/**
 * Quelea's schedule menu.
 * <p/>
 * @author Michael
 */
public class ScheduleMenu extends Menu {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private MenuItem addPowerpointItem;
    private MenuItem addVideoItem;
    private MenuItem addAudioItem;
    private MenuItem manageNoticesItem;
    private MenuItem shareScheduleItem;

    /**
     * Create the scheudle menu
     */
    public ScheduleMenu() {
        super(LabelGrabber.INSTANCE.getLabel("schedule.menu"));

        final MainPanel mainPanel = QueleaApp.get().getMainWindow().getMainPanel();
        final ScheduleList scheduleList = mainPanel.getSchedulePanel().getScheduleList();

        addPowerpointItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.presentation.button"), new ImageView(new Image("file:icons/powerpoint.png", 16, 16, false, true)));
        addPowerpointItem.setOnAction(new AddPowerpointActionHandler());
        getItems().add(addPowerpointItem);

        addVideoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.video.button"), new ImageView(new Image("file:icons/video file.png", 16, 16, false, true)));
        addVideoItem.setOnAction(new AddVideoActionHandler());
        getItems().add(addVideoItem);

        addAudioItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.audio.button"), new ImageView(new Image("file:icons/audio30.png", 16, 16, false, true)));
        addAudioItem.setOnAction(new AddAudioActionHandler());
        getItems().add(addAudioItem);

        getItems().add(new SeparatorMenuItem());

        manageNoticesItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("manage.notices.button"), new ImageView(new Image("file:icons/info.png", 16, 16, false, true)));
        manageNoticesItem.setOnAction(new ShowNoticesActionHandler());
        manageNoticesItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
        getItems().add(manageNoticesItem);

        shareScheduleItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("email.button"), new ImageView(new Image("file:icons/email.png", 16, 16, false, true)));
        shareScheduleItem.setDisable(true);
        scheduleList.getItems().addListener(new ListChangeListener<Displayable>() {
            @Override
            public void onChanged(Change<? extends Displayable> change) {
                Schedule schedule = scheduleList.getSchedule();
                if(schedule == null || !schedule.iterator().hasNext()) {
                    shareScheduleItem.setDisable(true);
                }
                else {
                    shareScheduleItem.setDisable(false);
                }
            }
        });
        shareScheduleItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                Mailer.getInstance().sendSchedule(scheduleList.getSchedule(), LabelGrabber.INSTANCE.getLabel("email.text"));
            }
        });
        getItems().add(shareScheduleItem);

    }
}
