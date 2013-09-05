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
package org.quelea.windows.video;

import java.util.logging.Logger;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.multimedia.MultimediaControlPanel;

/**
 * The control panel for displaying the video.
 * <p/>
 * @author Michael
 */
public class VideoControlPanel extends MultimediaControlPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    
    /**
     * Load the given video to be controlled via this panel.
     * <p/>
     * @param videoPath the video path to load.
     */
    @Override
    public void loadMultimedia(MultimediaDisplayable video) {
        this.filePath = video.getFile().getAbsolutePath();
//        try {
//            player = new VLCMediaPlayer();
//            player.load(filePath);
//            player = MediaPlayerFactory.getInstance(new File(filePath).toURI().toString());
//            player.stop();
//            player.currentTimeProperty().addListener(new CurrentTimeListener());
//        }
//        catch(MediaException ex) {
//            LOGGER.log(Level.WARNING, "Video Error with path: " + filePath, ex);
//            MediaException.Type type = ex.getType();
//            switch(type) {
//                case MEDIA_UNSUPPORTED:
//                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("video.error.title"), LabelGrabber.INSTANCE.getLabel("video.error.unsupported"));
//                    break;
//                default:
//                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("video.error.title"), LabelGrabber.INSTANCE.getLabel("video.error.general"));
//            }
//        }
    }
}
