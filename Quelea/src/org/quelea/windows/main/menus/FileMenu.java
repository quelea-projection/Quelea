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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.actionlisteners.*;

/**
 * Quelea's file menu.
 *
 * @author Michael
 */
public class FileMenu extends JMenu {

    private JMenuItem newItem;
    private JMenuItem openItem;
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenuItem printItem;
    private JMenuItem optionsItem;
    private JMenuItem exitItem;

    /**
     * Create the file menu.
     */
    public FileMenu() {
        super(LabelGrabber.INSTANCE.getLabel("file.menu"));
        setMnemonic('f');

        newItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("new.schedule.button"), Utils.getImageIcon("icons/filenew.png", 20, 20));
        newItem.setMnemonic('n');
        newItem.addActionListener(new NewScheduleActionListener());
        add(newItem);

        openItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("open.schedule.button"), Utils.getImageIcon("icons/fileopen.png", 20, 20));
        openItem.setMnemonic('o');
        openItem.addActionListener(new OpenScheduleActionListener());
        add(openItem);

        saveItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("save.schedule.button"), Utils.getImageIcon("icons/filesave.png", 20, 20));
        saveItem.setMnemonic('s');
        saveItem.addActionListener(new SaveScheduleActionListener(false));
        add(saveItem);

        saveAsItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("save.as.schedule.button"), Utils.getImageIcon("icons/filesaveas.png", 20, 20));
        saveAsItem.setMnemonic('a');
        saveAsItem.addActionListener(new SaveScheduleActionListener(true));
        add(saveAsItem);

        printItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("print.schedule.button"), Utils.getImageIcon("icons/fileprint.png", 20, 20));
        printItem.setMnemonic('p');
        printItem.addActionListener(new PrintScheduleActionListener());
        add(printItem);

        optionsItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("options.button"), Utils.getImageIcon("icons/options.png", 20, 20));
        optionsItem.setMnemonic('t');
        optionsItem.addActionListener(new ShowOptionsActionListener());
        add(optionsItem);

        exitItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("exit.button"), Utils.getImageIcon("icons/exit.png", 20, 20));
        exitItem.setMnemonic('e');
        exitItem.addActionListener(new ExitActionListener());
        add(exitItem);
    }

}
