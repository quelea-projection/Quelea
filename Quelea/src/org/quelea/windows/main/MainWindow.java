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

import org.quelea.windows.main.actionlisteners.EditSongDBActionListener;
import org.quelea.windows.main.actionlisteners.NewSongActionListener;
import org.quelea.windows.main.actionlisteners.EditSongScheduleActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.quelea.Application;
import org.quelea.bible.BibleBrowseDialog;
import org.quelea.bible.BibleSearchDialog;
import org.quelea.notice.NoticeDialog;
import org.quelea.tags.TagDialog;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.main.actionlisteners.*;
import org.quelea.windows.main.menus.MainMenuBar;
import org.quelea.windows.main.toolbars.MainToolbar;
import org.quelea.windows.newsong.SongEntryWindow;
import org.quelea.windows.options.OptionsDialog;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JFrame implements ApplicationListener {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final MainPanel mainpanel;
    private final SongEntryWindow songEntryWindow;
    private final NoticeDialog noticeDialog;
    private final MainMenuBar menuBar;
    private final MainToolbar mainToolbar;
    private final TagDialog tagDialog;
    private final OptionsDialog optionsDialog;
    private final BibleSearchDialog bibleSearchDialog;
    private final BibleBrowseDialog bibleBrowseDialog;
    private final org.simplericity.macify.eawt.Application macApp;

    /**
     * Create a new main window.
     * @param setApplicationWindow true if this main window should be set as
     * the application-wide main window, false otherwise.
     */
    public MainWindow(boolean setApplicationWindow) {
        super("Quelea " + QueleaProperties.VERSION.getVersionString());
        macApp = new org.simplericity.macify.eawt.DefaultApplication();
        macApp.addApplicationListener(this);
        macApp.setApplicationIconImage(Utils.getImage("icons/logo.png"));
        
        setLayout(new BorderLayout());
        noticeDialog = new NoticeDialog(this);
        
        LOGGER.log(Level.INFO, "Creating main window");
        if(setApplicationWindow) {
            Application.get().setMainWindow(this);
        }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                new ExitActionListener().actionPerformed(null);
            }
        });
        try {
            setIconImage(ImageIO.read(new File("icons/logo.png")));
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't set JFrame image", ex);
        }
        
        LOGGER.log(Level.INFO, "Creating tag dialog");
        tagDialog = new TagDialog();
        
        LOGGER.log(Level.INFO, "Creating options dialog");
        optionsDialog = new OptionsDialog(Application.get().getMainWindow());
        
        LOGGER.log(Level.INFO, "Creating bible search dialog");
        bibleSearchDialog = new BibleSearchDialog();
        LOGGER.log(Level.INFO, "Creating bible browse dialog");
        bibleBrowseDialog = new BibleBrowseDialog();

        mainpanel = new MainPanel();
        songEntryWindow = new SongEntryWindow(this);
        mainpanel.getLibraryPanel().getLibrarySongPanel().getAddButton().addActionListener(new NewSongActionListener());
        mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getPopupMenu().getEditDBButton().addActionListener(new EditSongDBActionListener());
        mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getPopupMenu().getRemoveFromDBButton().addActionListener(new RemoveSongDBActionListener());
        
        menuBar = new MainMenuBar();
        setJMenuBar(menuBar);
        
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
        mainToolbar = new MainToolbar();
        toolbarPanel.add(mainToolbar);
        add(toolbarPanel, BorderLayout.NORTH);
        
        add(mainpanel, BorderLayout.CENTER);
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
     * Get the tag dialog on this main window.
     * @return the tag dialog.
     */
    public TagDialog getTagDialog() {
        return tagDialog;
    }

    /**
     * Get the options dialog on this main window.
     * @return the options dialog.
     */
    public OptionsDialog getOptionsDialog() {
        return optionsDialog;
    }

    /**
     * Get the bible search dialog on this main window.
     * @return the bible search dialog.
     */
    public BibleSearchDialog getBibleSearchDialog() {
        return bibleSearchDialog;
    }
    
    /**
     * Get the bible browse dialog on this main window.
     * @return the bible browse dialog.
     */
    public BibleBrowseDialog getBibleBrowseDialog() {
        return bibleBrowseDialog;
    }
    
    /**
     * Get the new song window used for this window.
     * @return the song entry window.
     */
    public SongEntryWindow getSongEntryWindow() {
        return songEntryWindow;
    }

    @Override
    public void handleAbout(ApplicationEvent ae) {
        System.out.println("ABOUT");
    }

    @Override
    public void handleOpenApplication(ApplicationEvent ae) {
        
    }

    @Override
    public void handleOpenFile(ApplicationEvent ae) {
        
    }

    @Override
    public void handlePreferences(ApplicationEvent ae) {
        
    }

    @Override
    public void handlePrintFile(ApplicationEvent ae) {
        
    }

    @Override
    public void handleQuit(ApplicationEvent ae) {
        
    }

    @Override
    public void handleReOpenApplication(ApplicationEvent ae) {
        
    }

}
