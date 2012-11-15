package org.quelea.data.tags.services.multimedia;

import java.util.logging.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.quelea.services.utils.LoggerUtils;

/**
 * Mediaplayer singleton
 *
 * @author tomaszpio@gmail.com
 */
public class MediaPlayerFactory {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static MediaPlayer player_ = null;
    private static String filePath_ = "";

    private MediaPlayerFactory() {
    }

    public static MediaPlayer getInstance(String filePath) {
        if (player_ == null || (!filePath_.equals(filePath))) {
            if(player_ != null) {
                player_.stop();
            }
            player_ = new MediaPlayer(new Media(filePath));
        }
        return player_;
    }
}
