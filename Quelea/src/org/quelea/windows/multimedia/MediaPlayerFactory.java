package org.quelea.windows.multimedia;

import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
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

    public static MediaPlayer getInstance(final String filePath) {
        if (player_ == null || (!filePath_.equals(filePath))) {
            player_ = new MediaPlayer(new Media(filePath));
            player_.errorProperty().addListener(new ChangeListener<MediaException>() {
                @Override
                public void changed(ObservableValue<? extends MediaException> ov, MediaException t, MediaException t1) {
                    LOGGER.info("MEDIAPLAYER EXCEPTION" + ov.getValue().toString());
                }
            });
            player_.setOnError(new Runnable() {
                @Override
                public void run() {

                    LOGGER.info("RUNTIME MEDIAPLAYER EXCEPTION");
                    player_ = null;
                }
            });
        }
        return player_;
    }
}
