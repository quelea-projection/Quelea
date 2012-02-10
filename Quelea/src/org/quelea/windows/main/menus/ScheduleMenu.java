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
package org.quelea.windows.main.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.quelea.Application;
import org.quelea.Schedule;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.displayable.Song;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.mail.Mailer;
import org.quelea.powerpoint.PowerpointFileFilter;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.quelea.utils.VideoFileFilter;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.*;

/**
 * Quelea's schedule menu.
 * @author Michael
 */
public class ScheduleMenu extends JMenu {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    
    private JMenuItem addSongItem;
    private JMenuItem editSongItem;
    private JMenuItem removeSongItem;
    
    private JMenuItem addPowerpointItem;
    private JMenuItem addVideoItem;
    private JMenuItem addDVDItem;
    
    private JMenuItem manageNoticesItem;
    private JMenuItem shareScheduleItem;
    
    /**
     * Create the scheudle menu
     */
    public ScheduleMenu() {
        super(LabelGrabber.INSTANCE.getLabel("schedule.menu"));
        setMnemonic('s');
        
        Application.get().getMainWindow().getNoticeDialog().registerCanvas(Application.get().getLyricWindow().getCanvas());
        
        addSongItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.song.button"), Utils.getImageIcon("icons/newsong.png", 16, 16));
        addSongItem.setMnemonic('a');
        addSongItem.addActionListener(new AddSongActionListener());
        addSongItem.setEnabled(false);
        add(addSongItem);
        
        editSongItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("edit.song.button"), Utils.getImageIcon("icons/edit.png", 16, 16));
        editSongItem.setMnemonic('e');
        editSongItem.addActionListener(new EditSongScheduleActionListener());
        editSongItem.setEnabled(false);
        add(editSongItem);
        
        removeSongItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("remove.item.button"), Utils.getImageIcon("icons/remove 2.png", 16, 16));
        removeSongItem.setMnemonic('r');
        removeSongItem.addActionListener(new RemoveSongScheduleActionListener());
        removeSongItem.setEnabled(false);
        add(removeSongItem);
        
        final MainPanel mainPanel = Application.get().getMainWindow().getMainPanel();
        final ScheduleList scheduleList = mainPanel.getSchedulePanel().getScheduleList();
        scheduleList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkEditRemoveButtons(editSongItem, removeSongItem);
            }
        });
        final LibrarySongList songList = mainPanel.getLibraryPanel().getLibrarySongPanel().getSongList();
        songList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkAddButton(addSongItem);
            }
        });
        
        addSeparator();
        
        addPowerpointItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.presentation.button"), Utils.getImageIcon("icons/powerpoint.png", 16, 16));
        addPowerpointItem.setMnemonic('p');
        addPowerpointItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new PowerpointFileFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.showOpenDialog(Application.get().getMainWindow());
                File file = fileChooser.getSelectedFile();
                if(file != null) {
                    new Thread() {

                        private StatusPanel panel;
                        private boolean halt;

                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    panel = Application.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("adding.presentation.status"));
                                    panel.getProgressBar().setIndeterminate(true);
                                    panel.getCancelButton().addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            panel.done();
                                            halt = true;
                                        }
                                    });
                                }
                            });
//                            presentationButton.setIcon(RibbonUtils.getRibbonIcon("icons/hourglass.png", 100, 100));
//                            presentationButton.setEnabled(false);
                            try {
                                final PresentationDisplayable displayable = new PresentationDisplayable(fileChooser.getSelectedFile());
                                if(!halt) {
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().addElement(displayable);
                                        }
                                    });
                                }
                            }
                            catch (OfficeXmlFileException ex) {
                                if(!halt) {
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            JOptionPane.showMessageDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("pptx.error"), LabelGrabber.INSTANCE.getLabel("adding.presentation.error.title"), JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                            }
                            catch (RuntimeException ex) {
                                LOGGER.log(Level.WARNING, "Couldn't import presentation", ex);
                            }
//                            presentationButton.setIcon(RibbonUtils.getRibbonIcon("icons/powerpoint.png", 100, 100));
//                            presentationButton.setEnabled(true);
                            while(panel == null) {
                                Utils.sleep(1000); //Quick bodge but hey, it works
                            }
                            panel.done();
                        }
                    }.start();
                }
            }
        });
        add(addPowerpointItem);
        
        addVideoItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.video.button"), Utils.getImageIcon("icons/video file.png", 16, 16));
        addVideoItem.setMnemonic('v');
        addVideoItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new VideoFileFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.showOpenDialog(Application.get().getMainWindow());
                File file = fileChooser.getSelectedFile();
                if(file != null) {
                    VideoDisplayable displayable = new VideoDisplayable(fileChooser.getSelectedFile(), VideoDisplayable.VideoType.FILE);
                    Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().addElement(displayable);
                }
            }
        });
        add(addVideoItem);
        
        addDVDItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.dvd.button"), Utils.getImageIcon("icons/dvd.png", 16, 16));
        addDVDItem.setMnemonic('d');
        addDVDItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                File[] arr = File.listRoots();
                File file = null;
                for(File f : arr) {
                    if(f.getUsableSpace() == 0 && f.getTotalSpace() > 0) {
                        file = f;
                    }
                }
                if(file == null) {
                    JOptionPane.showMessageDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("no.dvd.error"), LabelGrabber.INSTANCE.getLabel("no.dvd.heading"), JOptionPane.ERROR_MESSAGE);
                }
                else {
                    VideoDisplayable displayable = new VideoDisplayable(file, VideoDisplayable.VideoType.DVD);
                    Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().addElement(displayable);
                }
            }
        });
        add(addDVDItem);
        
        addSeparator();
        
        manageNoticesItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("manage.notices.button"), Utils.getImageIcon("icons/info.png", 16, 16));
        manageNoticesItem.setMnemonic('n');
        manageNoticesItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.get().getMainWindow().getNoticeDialog().setVisible(true);
            }
        });
        add(manageNoticesItem);
        
        shareScheduleItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("email.button"), Utils.getImageIcon("icons/email.png", 16, 16));
        shareScheduleItem.setMnemonic('m');
        shareScheduleItem.setEnabled(false);
        scheduleList.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                check();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                check();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                check();
            }

            private void check() {
                Schedule schedule = scheduleList.getSchedule();
                if(schedule == null || !schedule.iterator().hasNext()) {
                    shareScheduleItem.setEnabled(false);
                }
                else {
                    shareScheduleItem.setEnabled(true);
                }
            }
        });
        shareScheduleItem.addActionListener(new ActionListener() {

            //TODO: Put this message in some form of properties file
            @Override
            public void actionPerformed(ActionEvent e) {
                Mailer.getInstance().sendSchedule(scheduleList.getSchedule(), LabelGrabber.INSTANCE.getLabel("email.text"));
            }
        });
        add(shareScheduleItem);
        
    }
    
    /**
     * Check whether the edit or remove buttons should be set to enabled or
     * disabled.
     *
     * @param editSongButton the edit button to check.
     * @param removeSongButton the remove button to check.
     */
    private void checkEditRemoveButtons(JMenuItem editSongButton, JMenuItem removeSongButton) {
        final MainPanel mainPanel = Application.get().getMainWindow().getMainPanel();
        final ScheduleList scheduleList = mainPanel.getSchedulePanel().getScheduleList();
        if(!scheduleList.isFocusOwner()) {
            editSongButton.setEnabled(false);
            removeSongButton.setEnabled(false);
            return;
        }
        if(scheduleList.getSelectedIndex() == -1) {
            editSongButton.setEnabled(false);
            removeSongButton.setEnabled(false);
        }
        else {
            if(scheduleList.getSelectedValue() instanceof Song) {
                editSongButton.setEnabled(true);
            }
            else {
                editSongButton.setEnabled(false);
            }
            removeSongButton.setEnabled(true);
        }
    }

    /**
     * Check whether the add to schedule button should be set enabled or
     * disabled.
     *
     * @param addSongButton the button to check.
     */
    private void checkAddButton(JMenuItem addSongButton) {
        final MainPanel mainPanel = Application.get().getMainWindow().getMainPanel();
        final LibrarySongList songList = mainPanel.getLibraryPanel().getLibrarySongPanel().getSongList();
        if(!songList.isFocusOwner()) {
            addSongButton.setEnabled(false);
            return;
        }
        if(songList.getSelectedIndex() == -1) {
            addSongButton.setEnabled(false);
        }
        else {
            addSongButton.setEnabled(true);
        }
    }
    
}
