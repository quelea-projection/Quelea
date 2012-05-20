/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import javax.swing.JButton;
import javax.swing.JToolBar;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.actionlisteners.*;

/**
 * Quelea's main toolbar.
 *
 * @author Michael
 */
public class MainToolbar extends JToolBar {

    private JButton newScheduleButton;
    private JButton openScheduleButton;
    private JButton saveScheduleButton;
    private JButton printScheduleButton;
    private JButton newSongButton;
    private JButton addPresentationButton;
    private JButton addVideoButton;
    private JButton addDVDButton;
    private JButton manageNoticesButton;
    private JButton manageTagsButton;
    private JButton addAudioButton;
    private JButton playButton;
    private JButton pauseButton;
    private JButton skipButton;
    private JButton muteButton;

    /**
     * Create the toolbar.
     */
    public MainToolbar() {
        setFloatable(false);

        newScheduleButton = new JButton(Utils.getImageIcon("icons/filenew.png", 24, 24));
        newScheduleButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("new.schedule.tooltip"));
        newScheduleButton.addActionListener(new NewScheduleActionListener());
        add(newScheduleButton);

        openScheduleButton = new JButton(Utils.getImageIcon("icons/fileopen.png", 24, 24));
        openScheduleButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("open.schedule.tooltip"));
        openScheduleButton.addActionListener(new OpenScheduleActionListener());
        add(openScheduleButton);

        saveScheduleButton = new JButton(Utils.getImageIcon("icons/filesave.png", 24, 24));
        saveScheduleButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("save.schedule.tooltip"));
        saveScheduleButton.addActionListener(new SaveScheduleActionListener(false));
        add(saveScheduleButton);

        printScheduleButton = new JButton(Utils.getImageIcon("icons/fileprint.png", 24, 24));
        printScheduleButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("print.schedule.tooltip"));
        printScheduleButton.addActionListener(new PrintScheduleActionListener());
        add(printScheduleButton);

        addSeparator();

        newSongButton = new JButton(Utils.getImageIcon("icons/newsong.png", 24, 24));
        newSongButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("new.song.tooltip"));
        newSongButton.addActionListener(new NewSongActionListener());
        add(newSongButton);

        addSeparator();

        addPresentationButton = new JButton(Utils.getImageIcon("icons/powerpoint.png", 24, 24));
        addPresentationButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("add.presentation.tooltip"));
        addPresentationButton.addActionListener(new AddPowerpointActionListener());
        add(addPresentationButton);

        addVideoButton = new JButton(Utils.getImageIcon("icons/video file.png", 24, 24));
        addVideoButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("add.video.tooltip"));
        addVideoButton.addActionListener(new AddVideoActionListener());
        add(addVideoButton);
        
        addAudioButton = new JButton(Utils.getImageIcon("icons/audio30.png", 24, 24));
        addAudioButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("add.audio.tooltip"));
        addAudioButton.addActionListener(new AddAudioActionListener());
        add(addAudioButton);

        addDVDButton = new JButton(Utils.getImageIcon("icons/dvd.png", 24, 24));
        addDVDButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("add.dvd.tooltip"));
        addDVDButton.addActionListener(new AddDVDActionListener());
        add(addDVDButton);

        addSeparator();

        manageTagsButton = new JButton(Utils.getImageIcon("icons/tag.png", 24, 24));
        manageTagsButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("manage.tags.tooltip"));
        manageTagsButton.addActionListener(new ViewTagsActionListener());
        add(manageTagsButton);

        manageNoticesButton = new JButton(Utils.getImageIcon("icons/info.png", 24, 24));
        manageNoticesButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("manage.notices.tooltip"));
        manageNoticesButton.addActionListener(new ShowNoticesActionListener());
        add(manageNoticesButton);
        
        addSeparator(); 
        //Ideally right align the following (may need panel)
        
        playButton = new JButton(Utils.getImageIcon("icons/play.png", 24, 24));
        playButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("play.audio.control.tooltip"));
        playButton.addActionListener(new PlayActionListener());
        add(playButton);
        
        pauseButton = new JButton(Utils.getImageIcon("icons/pause.png", 24, 24));
        pauseButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.audio.control.tooltip"));
        pauseButton.addActionListener(new PauseActionListener());
        add(pauseButton);
        
        skipButton = new JButton(Utils.getImageIcon("icons/skip.png", 24, 24));
        skipButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("skip.audio.control.tooltip"));
        skipButton.addActionListener(new SkipActionListener());
        add(skipButton);
        
        muteButton = new JButton(Utils.getImageIcon("icons/mute.png", 24, 24));
        muteButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("mute.audio.control.tooltip"));
        muteButton.addActionListener(new MuteActionListener());
        add(muteButton);

    }
}
