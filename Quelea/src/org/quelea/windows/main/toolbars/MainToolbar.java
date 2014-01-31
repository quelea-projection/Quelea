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
package org.quelea.windows.main.toolbars;

import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.AddDVDActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.AddVideoActionHandler;
import org.quelea.windows.main.actionhandlers.AddYoutubeActionHandler;
import org.quelea.windows.main.actionhandlers.NewScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;
import org.quelea.windows.main.actionhandlers.OpenScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.PrintScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.QuickInsertActionHandler;
import org.quelea.windows.main.actionhandlers.SaveScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.ShowNoticesActionHandler;
import org.quelea.windows.main.actionhandlers.ViewTagsActionHandler;

/**
 * Quelea's main toolbar.
 * <p/>
 * @author Michael
 */
public class MainToolbar extends ToolBar {

    private final Button newScheduleButton;
    private final Button openScheduleButton;
    private final Button saveScheduleButton;
    private final Button printScheduleButton;
    private final Button newSongButton;
    private final Button quickInsertButton;
    private final Button addPresentationButton;
    private final Button addYoutubeButton;
    private final Button addDVDButton;
    private final Button addVideoButton;
    private final Button manageNoticesButton;
    private final Button manageTagsButton;

    /**
     * Create the toolbar and any associated shortcuts.
     */
    public MainToolbar() {
        newScheduleButton = new Button("", new ImageView(new Image("file:icons/filenew.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(newScheduleButton);
        newScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("new.schedule.tooltip")));
        newScheduleButton.setOnAction(new NewScheduleActionHandler());
        getItems().add(newScheduleButton);

        openScheduleButton = new Button("", new ImageView(new Image("file:icons/fileopen.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(openScheduleButton);
        openScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("open.schedule.tooltip")));
        openScheduleButton.setOnAction(new OpenScheduleActionHandler());
        getItems().add(openScheduleButton);

        saveScheduleButton = new Button("", new ImageView(new Image("file:icons/filesave.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(saveScheduleButton);
        saveScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("save.schedule.tooltip")));
        saveScheduleButton.setOnAction(new SaveScheduleActionHandler(false));
        getItems().add(saveScheduleButton);

        printScheduleButton = new Button("", new ImageView(new Image("file:icons/fileprint.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(printScheduleButton);
        printScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("print.schedule.tooltip")));
        printScheduleButton.setOnAction(new PrintScheduleActionHandler());
        getItems().add(printScheduleButton);

        getItems().add(new Separator());

        newSongButton = new Button("", new ImageView(new Image("file:icons/newsong.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(newSongButton);
        newSongButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("new.song.tooltip")));
        newSongButton.setOnAction(new NewSongActionHandler());
        getItems().add(newSongButton);

        getItems().add(new Separator());

        quickInsertButton = new Button("", new ImageView(new Image("file:icons/lightning.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(quickInsertButton);
        quickInsertButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("quick.insert.text")));
        quickInsertButton.setOnAction(new QuickInsertActionHandler());
        getItems().add(quickInsertButton);

        addPresentationButton = new Button("", new ImageView(new Image("file:icons/powerpoint.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(addPresentationButton);
        addPresentationButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.presentation.tooltip")));
        addPresentationButton.setOnAction(new AddPowerpointActionHandler());
        getItems().add(addPresentationButton);

        addVideoButton = new Button("", new ImageView(new Image("file:icons/video file.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(addVideoButton);
        addVideoButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.video.tooltip")));
        addVideoButton.setOnAction(new AddVideoActionHandler());
        getItems().add(addVideoButton);

        addYoutubeButton = new Button("", new ImageView(new Image("file:icons/youtube.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(addYoutubeButton);
        addYoutubeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.youtube.button")));
        addYoutubeButton.setOnAction(new AddYoutubeActionHandler());
        getItems().add(addYoutubeButton);

        addDVDButton = new Button("", new ImageView(new Image("file:icons/dvd.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(addDVDButton);
        addDVDButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.dvd.button")));
        addDVDButton.setOnAction(new AddDVDActionHandler());
        getItems().add(addDVDButton);

        getItems().add(new Separator());

        manageTagsButton = new Button("", new ImageView(new Image("file:icons/tag.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(manageTagsButton);
        manageTagsButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("manage.tags.tooltip")));
        manageTagsButton.setOnAction(new ViewTagsActionHandler());
//        getItems().add(manageTagsButton);

        manageNoticesButton = new Button("", new ImageView(new Image("file:icons/info.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(manageNoticesButton);
        manageNoticesButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("manage.notices.tooltip")));
        manageNoticesButton.setOnAction(new ShowNoticesActionHandler());
        getItems().add(manageNoticesButton);
    }

}
