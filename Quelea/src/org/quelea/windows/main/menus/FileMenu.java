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
import javax.swing.*;
import org.quelea.Application;
import org.quelea.ScheduleSaver;
import org.quelea.languages.LabelGrabber;
import org.quelea.print.Printer;
import org.quelea.utils.Utils;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.options.OptionsDialog;

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
    private final OptionsDialog optionsDialog;

    /**
     * Create the file menu.
     */
    public FileMenu() {
        super(LabelGrabber.INSTANCE.getLabel("file.menu"));
        setMnemonic('f');
        optionsDialog = new OptionsDialog(Application.get().getMainWindow());

        newItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("new.schedule.button"), Utils.getImageIcon("icons/filenew.png", 20, 20));
        newItem.setMnemonic('n');
        newItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().clearSchedule();
            }
        });
        add(newItem);

        openItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("open.schedule.button"), Utils.getImageIcon("icons/fileopen.png", 20, 20));
        openItem.setMnemonic('o');
        openItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if(confirmClear()) {
                    JFileChooser chooser = Utils.getScheduleFileChooser();
                    if(chooser.showOpenDialog(Application.get().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                        Application.get().openSchedule(chooser.getSelectedFile());
                    }
                }
            }
        });
        add(openItem);

        saveItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("save.schedule.button"), Utils.getImageIcon("icons/filesave.png", 20, 20));
        saveItem.setMnemonic('s');
        saveItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                new ScheduleSaver().saveSchedule(false);
            }
        });
        add(saveItem);

        saveAsItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("save.as.schedule.button"), Utils.getImageIcon("icons/filesaveas.png", 20, 20));
        saveAsItem.setMnemonic('a');
        saveAsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                new ScheduleSaver().saveSchedule(true);
            }
        });
        add(saveAsItem);

        printItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("print.schedule.button"), Utils.getImageIcon("icons/fileprint.png", 20, 20));
        printItem.setMnemonic('p');
        printItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Printer.getInstance().print(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule());
            }
        });
        add(printItem);

        optionsItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("options.button"), Utils.getImageIcon("icons/options.png", 20, 20));
        optionsItem.setMnemonic('t');
        optionsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                optionsDialog.setVisible(true);
            }
        });
        add(optionsItem);

        exitItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("exit.button"), Utils.getImageIcon("icons/exit.png", 20, 20));
        exitItem.setMnemonic('e');
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        add(exitItem);
    }

    /**
     * Confirm whether it's ok to clear the current schedule.
     *
     * @return true if this is ok, false otherwise.
     */
    private boolean confirmClear() {
        MainPanel mainpanel = Application.get().getMainWindow().getMainPanel();
        if(mainpanel.getSchedulePanel().getScheduleList().isEmpty()) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("schedule.clear.text"), LabelGrabber.INSTANCE.getLabel("confirm.label"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
        if(result == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }
}
