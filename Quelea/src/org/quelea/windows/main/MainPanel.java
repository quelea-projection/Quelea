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

import org.quelea.windows.main.actionlisteners.AddSongActionListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.bible.Bible;
import org.quelea.displayable.BiblePassage;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.utils.LoggerUtils;
import org.quelea.windows.library.LibraryPanel;

/**
 * The main body of the main window, containing the schedule, the media bank, the preview and the live panels.
 * @author Michael
 */
public class MainPanel extends JPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final SchedulePanel schedulePanel;
    private final LibraryPanel libraryPanel;
    private final PreviewPanel previewPanel;
    private final LivePanel livePanel;
    private final StatusPanelGroup statusPanelGroup;

    /**
     * Create the new main panel.
     */
    public MainPanel() {
        LOGGER.log(Level.INFO, "Creating main panel");
        setLayout(new BorderLayout());
        LOGGER.log(Level.INFO, "Creating schedule panel");
        schedulePanel = new SchedulePanel();
        LOGGER.log(Level.INFO, "Creating library panel");
        libraryPanel = new LibraryPanel();
        LOGGER.log(Level.INFO, "Creating preview panel");
        previewPanel = new PreviewPanel();
        LOGGER.log(Level.INFO, "Creating live panel");
        livePanel = new LivePanel();

        LOGGER.log(Level.INFO, "Adding listeners");
        addKeyListeners();
        addScheduleListeners();
        addScheduleAddListeners();
        addBibleListeners();

        previewPanel.getLiveButton().addActionListener(new ActionListener() {

            /**
             * Action listener on live button.
             * @param e action event.
             */
            public void actionPerformed(ActionEvent e) {
//                DefaultListModel liveModel = livePanel.getLyricsList().getModel();
//                DefaultListModel previewModel = previewPanel.getLyricsList().getModel();
//                liveModel.clear();
//                for (int i = 0; i < previewModel.getSize(); i++) {
//                    liveModel.addElement(previewModel.get(i));
//                }
//                livePanel.getLyricsList().setSelectedIndex(previewPanel.getLyricsList().getSelectedIndex());
//                if (schedulePanel.getScheduleList().getSelectedIndex() < schedulePanel.getScheduleList().getModel().getSize()) {
//                    schedulePanel.getScheduleList().setSelectedIndex(schedulePanel.getScheduleList().getSelectedIndex() + 1);
//                }
//                previewPanel.pauseVideo();
                livePanel.setDisplayable(previewPanel.getDisplayable(), previewPanel.getIndex());
                if (previewPanel.getDisplayable() instanceof VideoDisplayable) {
                    livePanel.setVideoProperties(previewPanel);
                }
                livePanel.focus();
            }
        });

        JSplitPane scheduleAndLibrary = new JSplitPane(JSplitPane.VERTICAL_SPLIT, schedulePanel, libraryPanel);
        scheduleAndLibrary.setResizeWeight(0.8);
        scheduleAndLibrary.setOneTouchExpandable(true);
        JSplitPane previewAndLive = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, livePanel);
        previewAndLive.setResizeWeight(0.5);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scheduleAndLibrary, previewAndLive);
        mainSplit.setResizeWeight(0.2);
        mainSplit.setSize(300, 300);
        add(mainSplit, BorderLayout.CENTER);
        statusPanelGroup = new StatusPanelGroup();
        add(statusPanelGroup, BorderLayout.SOUTH);
    }

    /**
     * Add the bible listeners to this main panel.
     */
    private void addBibleListeners() {
        libraryPanel.getBiblePanel().getAddToSchedule().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BiblePassage passage = new BiblePassage(((Bible) libraryPanel.getBiblePanel().getBibleSelector().getSelectedItem()).getName(), libraryPanel.getBiblePanel().getBibleLocation(), libraryPanel.getBiblePanel().getVerses());
                schedulePanel.getScheduleList().getModel().addElement(passage);
            }
        });
    }

    /**
     * Add the listeners that add songs to the schedule.
     */
    private void addScheduleAddListeners() {
        libraryPanel.getLibrarySongPanel().getSongList().getPopupMenu().getAddToScheduleButton().addActionListener(new AddSongActionListener());
        libraryPanel.getLibrarySongPanel().getSongList().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                JList songList = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    Song song = (Song) songList.getSelectedValue();
                    schedulePanel.getScheduleList().getModel().addElement(song);
                }
            }
        });
    }

    /**
     * Add the key listeners to the lists used for switching focus between them.
     */
    private void addKeyListeners() {

        /*
         * Schedule panel key listeners...
         */
        schedulePanel.getScheduleList().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    previewPanel.getLiveButton().doClick();
//                    liveLyricsPanel.getLyricsList().ensureIndexIsVisible(liveLyricsPanel.getLyricsList().getSelectedIndex());
                }
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    previewPanel.focus();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });

        /*
         * Preview panel key listeners...
         */
        previewPanel.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    previewPanel.getLiveButton().doClick();
//                    liveLyricsPanel.getLyricsList().ensureIndexIsVisible(liveLyricsPanel.getLyricsList().getSelectedIndex());
                }
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    livePanel.focus();
                }
                else if (e.getKeyCode() == KeyEvent.VK_LEFT && schedulePanel.getScheduleList().getModel().getSize() > 0) {
                    schedulePanel.getScheduleList().requestFocus();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });

        /*
         * Live panel key listeners...
         */
        livePanel.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    previewPanel.getLiveButton().doClick();
//                    livePanel.getLyricsList().ensureIndexIsVisible(liveLyricsPanel.getLyricsList().getSelectedIndex());
                }
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    previewPanel.focus();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });
    }

    /**
     * Add the listeners to check for changes in the schedule panel.
     */
    private void addScheduleListeners() {
        schedulePanel.getScheduleList().getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                //Nothing needs to be done here.
            }

            public void intervalRemoved(ListDataEvent e) {
                //Nothing needs to be done here.
            }

            /**
             * listChanged() must be called in case we're removing the last
             * element in the list, in which case the preview panel must be
             * cleared.
             */
            public void contentsChanged(ListDataEvent e) {
                scheduleListChanged();
            }
        });

        schedulePanel.getScheduleList().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                scheduleListChanged();
            }
        });
    }

    /**
     * This method should be called every time the list values are updated or changed.
     */
    private void scheduleListChanged() {
        if (schedulePanel.getScheduleList().isEmpty()) {
            livePanel.clear();
        }
        if (schedulePanel.getScheduleList().getSelectedIndex() == -1) {
            previewPanel.clear();
            return;
        }
        Displayable newDisplayable = schedulePanel.getScheduleList().getModel().getElementAt(schedulePanel.getScheduleList().getSelectedIndex());
        previewPanel.setDisplayable(newDisplayable, 0);
    }

    /**
     * Get the panel displaying the selection of the preview lyrics.
     * @return the panel displaying the selection of the preview lyrics.
     */
    public PreviewPanel getPreviewPanel() {
        return previewPanel;
    }

    /**
     * Get the panel displaying the selection of the live lyrics.
     * @return the panel displaying the selection of the live lyrics.
     */
    public LivePanel getLivePanel() {
        return livePanel;
    }

    /**
     * Get the panel displaying the order of service.
     * @return the panel displaying the order of service.
     */
    public SchedulePanel getSchedulePanel() {
        return schedulePanel;
    }

    /**
     * Get the panel displaying the library of media.
     * @return the library panel.
     */
    public LibraryPanel getLibraryPanel() {
        return libraryPanel;
    }

    /**
     * Get the status panel.
     * @return the status panel.
     */
    public StatusPanelGroup getStatusPanelGroup() {
        return statusPanelGroup;
    }
}
