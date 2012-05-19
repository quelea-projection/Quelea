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
package org.quelea.windows.newsong;

import java.io.File;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.quelea.ImageBackground;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.ImageFileTypeChecker;
import org.quelea.windows.main.LyricCanvas;

/**
 * The image button where the user selects an image.
 *
 * @author Michael
 */
public class ImageButton extends BackgroundFileSelectButton {

    /**
     * Create and initialise the image button.
     *
     * @param imageLocationField the image location field that goes with this
     * button.
     * @param canvas the preview canvas to update.
     */
    public ImageButton(final JTextField imageLocationField, final LyricCanvas canvas) {
        super(imageLocationField, canvas, LabelGrabber.INSTANCE.getLabel("select.image.button"), "img", new FileFilter() {

            @Override
            public boolean accept(File f) {
                return new ImageFileTypeChecker().isType(f) || (f.isDirectory() && !f.isHidden());
            }

            @Override
            public String getDescription() {
                return LabelGrabber.INSTANCE.getLabel("image.files.description");
            }
        });
    }

    @Override
    public ImageBackground getCanvasBackground() {
        return new ImageBackground(getFileLocation(), null);
    }
}
