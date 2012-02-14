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
    private JButton newSongButton;
    private JButton addPresentationButton;
    private JButton addVideoButton;
    private JButton addDVDButton;
    private JButton manageNoticesButton;
    private JButton manageTagsButton;

    /**
     * Create the toolbar.
     */
    public MainToolbar() {
        setFloatable(false);

        newScheduleButton = new JButton(Utils.getImageIcon("icons/filenew.png", 24, 24));
        newScheduleButton.addActionListener(new NewScheduleActionListener());
        add(newScheduleButton);

        openScheduleButton = new JButton(Utils.getImageIcon("icons/fileopen.png", 24, 24));
        openScheduleButton.addActionListener(new OpenScheduleActionListener());
        add(openScheduleButton);

        saveScheduleButton = new JButton(Utils.getImageIcon("icons/filesave.png", 24, 24));
        saveScheduleButton.addActionListener(new SaveScheduleActionListener(false));
        add(saveScheduleButton);

        addSeparator();

        newSongButton = new JButton(Utils.getImageIcon("icons/newsong.png", 24, 24));
        newSongButton.addActionListener(new NewSongActionListener());
        add(newSongButton);

        addSeparator();

        addPresentationButton = new JButton(Utils.getImageIcon("icons/powerpoint.png", 24, 24));
        addPresentationButton.addActionListener(new AddPowerpointActionListener());
        add(addPresentationButton);

        addVideoButton = new JButton(Utils.getImageIcon("icons/video file.png", 24, 24));
        addVideoButton.addActionListener(new AddVideoActionListener());
        add(addVideoButton);

        addDVDButton = new JButton(Utils.getImageIcon("icons/dvd.png", 24, 24));
        addDVDButton.addActionListener(new AddDVDActionListener());
        add(addDVDButton);

        addSeparator();

        manageTagsButton = new JButton(Utils.getImageIcon("icons/tag.png", 24, 24));
        manageTagsButton.addActionListener(new ViewTagsActionListener());
        add(manageTagsButton);

        manageNoticesButton = new JButton(Utils.getImageIcon("icons/info.png", 24, 24));
        manageNoticesButton.addActionListener(new ShowNoticesActionListener());
        add(manageNoticesButton);

    }
}
