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
package org.quelea.windows.main.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.quelea.Application;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.utils.VideoFileFilter;

/**
 * The action listener for adding a video.
 *
 * @author Michael
 */
public class AddVideoActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new VideoFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        int val = fileChooser.showOpenDialog(Application.get().getMainWindow());
        if (val == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                VideoDisplayable displayable = new VideoDisplayable(fileChooser.getSelectedFile(), VideoDisplayable.VideoType.FILE);
                Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().addElement(displayable);
            }
        }
    }
}
