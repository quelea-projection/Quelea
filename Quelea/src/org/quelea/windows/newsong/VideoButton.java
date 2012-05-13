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
import org.quelea.VideoBackground;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricCanvas;

/**
 * The video button where the user selects an video.
 *
 * @author Michael
 */
public class VideoButton extends BackgroundFileSelectButton {

    /**
     * Create and initialise the image button.
     *
     * @param videoLocationField the video location field that goes with this
     * button.
     * @param canvas the preview canvas to update.
     */
    public VideoButton(final JTextField videoLocationField, final LyricCanvas canvas) {
        super(videoLocationField, canvas, LabelGrabber.INSTANCE.getLabel("select.vid.button"), "vid", new FileFilter() {

            @Override
            public boolean accept(File f) {
                return Utils.fileIsVideo(f);
            }

            @Override
            public String getDescription() {
                return LabelGrabber.INSTANCE.getLabel("vid.files.description");
            }
        });
    }

    @Override
    public VideoBackground getCanvasBackground() {
        return new VideoBackground(getFileLocation());
    }
}
