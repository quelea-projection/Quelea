/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.windows.main;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.quelea.Application;
import org.quelea.notice.NoticeDialog;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.main.ribbon.RibbonPopulator;
import org.quelea.windows.main.ribbon.RibbonUtils;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JRibbonFrame {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final MainPanel mainpanel;
    private final SongEntryWindow songEntryWindow;
    private final NoticeDialog noticeDialog;

    /**
     * Create a new main window.
     * @param setApplicationWindow true if this main window should be set as
     * the application-wide main window, false otherwise.
     */
    public MainWindow(boolean setApplicationWindow) {
        super("Quelea " + QueleaProperties.VERSION.getVersionString());
        
        noticeDialog = new NoticeDialog(this);
        
        LOGGER.log(Level.INFO, "Creating main window");
        if(setApplicationWindow) {
            Application.get().setMainWindow(this);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setApplicationIcon(RibbonUtils.getRibbonIcon("img/logo.png", 100, 100));

        mainpanel = new MainPanel();
        songEntryWindow = new SongEntryWindow(this);
        mainpanel.getLibraryPanel().getLibrarySongPanel().getAddButton().addActionListener(new NewSongActionListener());
        mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getPopupMenu().getEditDBButton().addActionListener(new EditSongDBActionListener());
        mainpanel.getSchedulePanel().getScheduleList().getPopupMenu().getEditSongButton().addActionListener(new EditSongScheduleActionListener());
        
        new RibbonPopulator(getRibbon()).populate();
        add(mainpanel);
        mainpanel.getLibraryPanel().getImagePanel().setPreferredSize(new Dimension(100, 200));
        setSize(800,600);
        LOGGER.log(Level.INFO, "Created main window.");
    }

    /**
     * Get the main panel on this window.
     * @return the main panel part of this window.
     */
    public MainPanel getMainPanel() {
        return mainpanel;
    }
    
    /**
     * Get the notice dialog on this main window.
     * @return the notice dialog.
     */
    public NoticeDialog getNoticeDialog() {
        return noticeDialog;
    }

    /**
     * Get the new song window used for this window.
     * @return the song entry window.
     */
    public SongEntryWindow getSongEntryWindow() {
        return songEntryWindow;
    }

}
