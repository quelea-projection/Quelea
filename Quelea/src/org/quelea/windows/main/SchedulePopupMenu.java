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
package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.quelea.Application;
import org.quelea.displayable.Displayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.FileFilters;
import org.quelea.utils.Utils;

/**
 * The popup menu that appears on all displayables in the schedule.
 *
 * @author Michael
 */
public class SchedulePopupMenu extends JPopupMenu {

    private final JMenuItem addAudio;
    private final JMenuItem clearAudio;
    private Displayable curDisplayable;

    /**
     * Create a new schedule popup menu.
     */
    public SchedulePopupMenu() {
        addAudio = new JMenuItem(LabelGrabber.INSTANCE.getLabel("add.audio.text"), Utils.getImageIcon("icons/audio.png", 16, 16));
        addAudio.setMnemonic(KeyEvent.VK_A);
        addAudio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(FileFilters.AUDIO);
                fileChooser.setMultiSelectionEnabled(false);
                int val = fileChooser.showOpenDialog(Application.get().getMainWindow());
                if(val == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if(file != null) {
                        curDisplayable.setAudio(file.getAbsolutePath());
                    }
                }
            }
        });
        clearAudio = new JMenuItem(LabelGrabber.INSTANCE.getLabel("remove.audio.text"), Utils.getImageIcon("icons/clearaudio.png", 16, 16));
        addAudio.setMnemonic(KeyEvent.VK_R);
        clearAudio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                curDisplayable.setAudio(null);
            }
        });
        add(addAudio);
        add(clearAudio);
    }

    /**
     * Update this popup menu to change it items according with the current
     * state of the displayable.
     *
     * @param d the displayable to update to.
     */
    public void updateDisplayable(Displayable d) {
        this.curDisplayable = d;
        if(d.getAudio() == null) {
            addAudio.setText(LabelGrabber.INSTANCE.getLabel("add.audio.text"));
            addAudio.setMnemonic(KeyEvent.VK_A);
            clearAudio.setEnabled(false);
        }
        else {
            addAudio.setText(LabelGrabber.INSTANCE.getLabel("change.audio.text"));
            addAudio.setMnemonic(KeyEvent.VK_C);
            clearAudio.setEnabled(true);
        }
    }
}
