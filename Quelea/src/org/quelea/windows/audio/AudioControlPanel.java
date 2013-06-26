/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.audio;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.MediaException;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.multimedia.MediaPlayerFactory;
import org.quelea.windows.multimedia.MultimediaControlPanel;

/**
 * The control panel for displaying the audio.
 * <p/>
 * @author tomaszpio@gmail.com
 */
public class AudioControlPanel extends MultimediaControlPanel {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new video control panel.
     */
    public AudioControlPanel() {
    }

    /**
     * Load the given video to be controlled via this panel.
     * <p/>
     * @param videoPath the video path to load.
     */
    @Override
    public void loadMultimedia(MultimediaDisplayable audio) {
        this.filePath = audio.getFile().getAbsolutePath();
        try {
            player = MediaPlayerFactory.getInstance(new File(filePath).toURI().toString());
            player.stop();
            player.currentTimeProperty().addListener(new CurrentTimeListener());;
        } catch (MediaException ex) {
            LOGGER.log(Level.WARNING, "Audio Error", ex);
            MediaException.Type type = ex.getType();
            switch (type) {
                case MEDIA_UNSUPPORTED:
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("audio.error.title"), LabelGrabber.INSTANCE.getLabel("audio.error.unsupported"));
                    break;
                default:
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("audio.error.title"), LabelGrabber.INSTANCE.getLabel("audio.error.general"));
            }
        }
    }
}
