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

import org.quelea.windows.main.actionlisteners.AddVideoActionListener;
import org.quelea.windows.main.actionlisteners.AddSongActionListener;
import org.quelea.windows.main.actionlisteners.RemoveSongScheduleActionListener;
import org.quelea.windows.main.actionlisteners.ShowNoticesActionListener;
import org.quelea.windows.main.actionlisteners.AddPowerpointActionListener;
import org.quelea.windows.main.actionlisteners.AddDVDActionListener;
import org.quelea.windows.main.actionlisteners.EditSongScheduleActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.Application;
import org.quelea.Schedule;
import org.quelea.languages.LabelGrabber;
import org.quelea.mail.Mailer;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.*;

/**
 * Quelea's schedule menu.
 *
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
                ButtonChecker.INSTANCE.checkEditRemoveButtons(editSongItem, removeSongItem);
            }
        });
        final LibrarySongList songList = mainPanel.getLibraryPanel().getLibrarySongPanel().getSongList();
        songList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                ButtonChecker.INSTANCE.checkAddButton(addSongItem);
            }
        });

        addSeparator();

        addPowerpointItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.presentation.button"), Utils.getImageIcon("icons/powerpoint.png", 16, 16));
        addPowerpointItem.setMnemonic('p');
        addPowerpointItem.addActionListener(new AddPowerpointActionListener());
        add(addPowerpointItem);

        addVideoItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.video.button"), Utils.getImageIcon("icons/video file.png", 16, 16));
        addVideoItem.setMnemonic('v');
        addVideoItem.addActionListener(new AddVideoActionListener());
        add(addVideoItem);

        addDVDItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.dvd.button"), Utils.getImageIcon("icons/dvd.png", 16, 16));
        addDVDItem.setMnemonic('d');
        addDVDItem.addActionListener(new AddDVDActionListener());
        add(addDVDItem);

        addSeparator();

        manageNoticesItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("manage.notices.button"), Utils.getImageIcon("icons/info.png", 16, 16));
        manageNoticesItem.setMnemonic('n');
        manageNoticesItem.addActionListener(new ShowNoticesActionListener());
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
}
