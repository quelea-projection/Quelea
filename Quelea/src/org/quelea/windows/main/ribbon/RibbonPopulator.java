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
package org.quelea.windows.main.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.quelea.Application;
import org.quelea.ScheduleSaver;
import org.quelea.languages.LabelGrabber;
import org.quelea.print.Printer;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;

/**
 * Manages populating the ribbon.
 * @author Michael
 */
public class RibbonPopulator {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private JRibbon ribbon;
    private final ScheduleTask scheduleTask;
    private final DatabaseTask databaseTask;
    private final ProjectorTask projectorTask;
    private final RibbonMenu ribbonMenu;

    /**
     * Create a new ribbon populator for a given ribbon.
     * @param ribbon the ribbon to populate.
     */
    public RibbonPopulator(JRibbon ribbon) {
        this.ribbon = ribbon;
        LOGGER.log(Level.INFO, "Creating schedule ribbon task");
        scheduleTask = new ScheduleTask();
        LOGGER.log(Level.INFO, "Creating database ribbon task");
        databaseTask = new DatabaseTask();
        LOGGER.log(Level.INFO, "Creating projector ribbon task");
        projectorTask = new ProjectorTask();
        LOGGER.log(Level.INFO, "Creating ribbon menu");
        ribbonMenu = new RibbonMenu();
        LOGGER.log(Level.INFO, "Done populating ribbon.");
    }

    /**
     * Populate the ribbon with the various tasks.
     */
    public void populate() {
        ribbon.addTask(scheduleTask);
        ribbon.addTask(databaseTask);
        ribbon.addTask(projectorTask);
        ribbon.setApplicationMenu(ribbonMenu);
        addTaskbar();
    }

    /**
     * Add the task bar to the ribbon.
     */
    private void addTaskbar() {
        JButton saveButton = new JButton(Utils.getImageIcon("icons/filesave.png", 15, 15));
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(false);
            }
        });
        saveButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        saveButton.setBorderPainted(false);
        saveButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("save.schedule.tooltip"));
        ribbon.addTaskbarComponent(saveButton);

        JButton printButton = new JButton(Utils.getImageIcon("icons/fileprint.png", 15, 15));
        printButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Printer.getInstance().print(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule());
            }
        });
        printButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        printButton.setBorderPainted(false);
        printButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("print.schedule.tooltip"));
        ribbon.addTaskbarComponent(printButton);
    }
}
