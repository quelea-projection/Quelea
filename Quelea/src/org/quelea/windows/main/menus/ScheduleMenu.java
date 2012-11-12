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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.mail.Mailer;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.ButtonChecker;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.ScheduleList;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.AddSongActionHandler;
import org.quelea.windows.main.actionhandlers.AddVideoActionHandler;
import org.quelea.windows.main.actionhandlers.EditSongScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler;
import org.quelea.windows.main.actionhandlers.ShowNoticesActionHandler;

/**
 * Quelea's schedule menu.
 *
 * @author Michael
 */
public class ScheduleMenu extends Menu {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private MenuItem addSongItem;
    private MenuItem editSongItem;
    private MenuItem removeSongItem;
    private MenuItem addPowerpointItem;
    private MenuItem addVideoItem;
    private MenuItem addDVDItem;
    private MenuItem manageNoticesItem;
    private MenuItem shareScheduleItem;

    /**
     * Create the scheudle menu
     */
    public ScheduleMenu() {
        super(LabelGrabber.INSTANCE.getLabel("schedule.menu"));

//        Application.get().getMainWindow().getNoticeDialog().registerCanvas(Application.get().getLyricWindow().getCanvas());

        addSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.song.button"), new ImageView(new Image("file:icons/newsong.png", 16, 16, false, true)));
        addSongItem.setOnAction(new AddSongActionHandler());
        addSongItem.setDisable(true);
        getItems().add(addSongItem);

        editSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.button"), new ImageView(new Image("file:icons/edit.png", 16, 16, false, true)));
        editSongItem.setOnAction(new EditSongScheduleActionHandler());
        editSongItem.setDisable(true);
        getItems().add(editSongItem);

        removeSongItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("remove.item.button"), new ImageView(new Image("file:icons/remove 2.png", 16, 16, false, true)));
        removeSongItem.setOnAction(new RemoveScheduleItemActionHandler());
        removeSongItem.setDisable(true);
        getItems().add(removeSongItem);

        final MainPanel mainPanel = QueleaApp.get().getMainWindow().getMainPanel();
        final ScheduleList scheduleList = mainPanel.getSchedulePanel().getScheduleList();
        scheduleList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Displayable>() {

            @Override
            public void changed(ObservableValue<? extends Displayable> ov, Displayable t, Displayable t1) {
                ButtonChecker.INSTANCE.checkEditRemoveButtons(editSongItem, removeSongItem);
            }
        });
        final LibrarySongList songList = mainPanel.getLibraryPanel().getLibrarySongPanel().getSongList();
        songList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SongDisplayable>() {

            @Override
            public void changed(ObservableValue<? extends SongDisplayable> ov, SongDisplayable t, SongDisplayable t1) {
                ButtonChecker.INSTANCE.checkAddButton(addSongItem);
            }
        });

        getItems().add(new SeparatorMenuItem());

        addPowerpointItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.presentation.button"), new ImageView(new Image("file:icons/powerpoint.png", 16, 16, false, true)));
        addPowerpointItem.setOnAction(new AddPowerpointActionHandler());
        getItems().add(addPowerpointItem);

        addVideoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.video.button"), new ImageView(new Image("file:icons/video file.png", 16, 16, false, true)));
        addVideoItem.setOnAction(new AddVideoActionHandler());
        getItems().add(addVideoItem);

        //TODO: Investigate putting DVD support back in
//        addDVDItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("add.dvd.button"), new ImageView(new Image("file:icons/dvd.png", 16, 16, false, true)));
//        addDVDItem.setOnAction(new AddDVDActionListener());
//        getItems().add(addDVDItem);

        getItems().add(new SeparatorMenuItem());

        manageNoticesItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("manage.notices.button"), new ImageView(new Image("file:icons/info.png", 16, 16, false, true)));
        manageNoticesItem.setOnAction(new ShowNoticesActionHandler());
        getItems().add(manageNoticesItem);

        shareScheduleItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("email.button"), new ImageView(new Image("file:icons/email.png", 16, 16, false, true)));
        shareScheduleItem.setDisable(true);
        scheduleList.itemsProperty().addListener(new ChangeListener<ObservableList<Displayable>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<Displayable>> ov, ObservableList<Displayable> t, ObservableList<Displayable> t1) {
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
