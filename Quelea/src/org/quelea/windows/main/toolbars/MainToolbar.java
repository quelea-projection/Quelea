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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.servivces.languages.LabelGrabber;
import org.quelea.windows.main.actionhandlers.AddAudioActionHandler;
import org.quelea.windows.main.actionhandlers.AddPowerpointActionHandler;
import org.quelea.windows.main.actionhandlers.AddVideoActionHandler;
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

    private Button newScheduleButton;
    private Button openScheduleButton;
    private Button saveScheduleButton;
    private Button printScheduleButton;
    private Button newSongButton;
    private Button quickInsertButton;
    private Button addPresentationButton;
    private Button addVideoButton;
    private Button addAudioButton;
    private Button manageNoticesButton;
    private Button manageTagsButton;

    /**
     * Create the toolbar and any associated shortcuts.
     */
    public MainToolbar() {
        newScheduleButton = new Button("", new ImageView(new Image("file:icons/filenew.png", 24, 24, false, true)));
        newScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("new.schedule.tooltip")));
        newScheduleButton.setOnAction(new NewScheduleActionHandler());
        getItems().add(newScheduleButton);

        openScheduleButton = new Button("", new ImageView(new Image("file:icons/fileopen.png", 24, 24, false, true)));
        openScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("open.schedule.tooltip")));
        openScheduleButton.setOnAction(new OpenScheduleActionHandler());
        getItems().add(openScheduleButton);

        saveScheduleButton = new Button("", new ImageView(new Image("file:icons/filesave.png", 24, 24, false, true)));
        saveScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("save.schedule.tooltip")));
        saveScheduleButton.setOnAction(new SaveScheduleActionHandler(false));
        getItems().add(saveScheduleButton);

        printScheduleButton = new Button("", new ImageView(new Image("file:icons/fileprint.png", 24, 24, false, true)));
        printScheduleButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("print.schedule.tooltip")));
        printScheduleButton.setOnAction(new PrintScheduleActionHandler());
        getItems().add(printScheduleButton);

        getItems().add(new Separator());

        newSongButton = new Button("", new ImageView(new Image("file:icons/newsong.png", 24, 24, false, true)));
        newSongButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("new.song.tooltip")));
        newSongButton.setOnAction(new NewSongActionHandler());
        getItems().add(newSongButton);

        getItems().add(new Separator());

        quickInsertButton = new Button("", new ImageView(new Image("file:icons/lightning.png", 24, 24, false, true)));
        quickInsertButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("quick.insert.text")));
        quickInsertButton.setOnAction(new QuickInsertActionHandler());
        getItems().add(quickInsertButton);

        addPresentationButton = new Button("", new ImageView(new Image("file:icons/powerpoint.png", 24, 24, false, true)));
        addPresentationButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.presentation.tooltip")));
        addPresentationButton.setOnAction(new AddPowerpointActionHandler());
        getItems().add(addPresentationButton);

        addVideoButton = new Button("", new ImageView(new Image("file:icons/video file.png", 24, 24, false, true)));
        addVideoButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.video.tooltip")));
        addVideoButton.setOnAction(new AddVideoActionHandler());
        getItems().add(addVideoButton);

        addAudioButton = new Button("", new ImageView(new Image("file:icons/audio30.png", 24, 24, false, true)));
        addAudioButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.audio.tooltip")));
        addAudioButton.setOnAction(new AddAudioActionHandler());
        getItems().add(addAudioButton);

        getItems().add(new Separator());

        manageTagsButton = new Button("", new ImageView(new Image("file:icons/tag.png", 24, 24, false, true)));
        manageTagsButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("manage.tags.tooltip")));
        manageTagsButton.setOnAction(new ViewTagsActionHandler());
        getItems().add(manageTagsButton);

        manageNoticesButton = new Button("", new ImageView(new Image("file:icons/info.png", 24, 24, false, true)));
        manageNoticesButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("manage.notices.tooltip")));
        manageNoticesButton.setOnAction(new ShowNoticesActionHandler());
        getItems().add(manageNoticesButton);
        
        //Add shortcuts when we're part of a scene
        sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> ov, Scene oldScene, Scene newScene) {
                if(newScene != null) {
                    newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN), new Runnable() {
                        @Override
                        public void run() {
                            quickInsertButton.fire();
                        }
                    });
                    sceneProperty().removeListener(this);
                }
            }
        });

    }
}
