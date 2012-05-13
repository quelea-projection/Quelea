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
package org.quelea.windows.newsong;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.utils.LoggerUtils;
import org.quelea.windows.main.LyricCanvas;

/**
 * A button used to select a background for a particular canvas.
 *
 * @author Michael
 */
public abstract class BackgroundFileSelectButton extends JButton {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String location;
    private final JFileChooser fileChooser;

    /**
     * Create and initialise the button.
     *
     * @param locationField the location field tied to this button.
     * @param canvas the canvas tied to the file selected by this button.
     * @param buttonLabel the label for this button.
     * @param defaultFolder the default folder this button should navigate to.
     * @param fileFilter the file filter this button should use.
     */
    public BackgroundFileSelectButton(final JTextField locationField, final LyricCanvas canvas, final String buttonLabel, final String defaultFolder,
            final FileFilter fileFilter) {
        super(buttonLabel);
        fileChooser = new JLocationFileChooser(defaultFolder);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(fileFilter);
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int ret = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(BackgroundFileSelectButton.this));
                if(ret == JFileChooser.APPROVE_OPTION) {
                    File imageDir = new File(defaultFolder);
                    File selectedFile = fileChooser.getSelectedFile();
                    File newFile = new File(imageDir, selectedFile.getName());
                    try {
                        if(!selectedFile.getCanonicalPath().startsWith(imageDir.getCanonicalPath())) {
                            FileUtils.copyFile(selectedFile, newFile);
                        }
                    }
                    catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error occured while fetching the background file", ex);
                    }

                    location = imageDir.toURI().relativize(newFile.toURI()).getPath();
                    locationField.setText(location);
                    canvas.setTheme(new Theme(canvas.getTheme().getFont(), canvas.getTheme().getFontColor(), getCanvasBackground()));
                }
            }
        });
    }
    
    public abstract Background getCanvasBackground();

    /**
     * Get the location of the selected file.
     *
     * @return the selected file location.
     */
    public String getFileLocation() {
        return location;
    }
}
