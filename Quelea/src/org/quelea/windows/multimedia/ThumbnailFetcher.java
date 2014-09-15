/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.multimedia;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * Uses VLCJ to retrieve thumbnails of a video. This only works if using
 * advanced multimedia option.
 *
 * @author Greg
 */
public class ThumbnailFetcher {
    public static final ThumbnailFetcher INSTANCE = QueleaProperties.get().getVLCAdvanced()?new ThumbnailFetcher():null;
    private  final String[] VLC_ARGS = {
        "--intf", "dummy", /* no interface */
        "--vout", "dummy", /* we don't want video (output) */
        "--no-audio", /* we don't want audio (decoding) */
        "--no-video-title-show", /* nor the filename displayed */
        "--no-stats", /* no stats */
        "--no-sub-autodetect-file", /* we don't want subtitles */
        "--no-disable-screensaver", /* we don't want interfaces */
        "--no-snapshot-preview", /* no blending in dummy vout */
    };
    private  float VLC_THUMBNAIL_POSITION = 30.0f / 100.0f;
    private  final Logger LOGGER = LoggerUtils.getLogger();
    private  final MediaPlayerFactory factory;
    private  final MediaPlayer mediaPlayer ;
/**
 * Uses singleton method
 */
    private ThumbnailFetcher(){
        factory =   new MediaPlayerFactory(VLC_ARGS);
        mediaPlayer = factory.newHeadlessMediaPlayer();
        
    }
    /**
     * Gets a snapshot of the video using VLCJ.
     *
     * @param thumbnailPosition percent position of the video (i.e. 1/3 would be
     * 33.33% completed
     * @param mrl the video file
     * @return buffered image a thumbnail. returns null if error, or if VLC
     * Advanced is turned off
     */
    public  BufferedImage getSnapshot(float thumbnailPosition, String mrl) {
        VLC_THUMBNAIL_POSITION = thumbnailPosition;
        if (!QueleaProperties.get().getVLCAdvanced()) {
            return null;
        }
        BufferedImage ret = null;

        final CountDownLatch inPositionLatch = new CountDownLatch(1);
        final CountDownLatch snapshotTakenLatch = new CountDownLatch(1);

        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                if (newPosition >= VLC_THUMBNAIL_POSITION * 0.9f) { /* 90% margin */

                    inPositionLatch.countDown();
                }
            }

            @Override
            public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
                snapshotTakenLatch.countDown();
            }
        });

        if (mediaPlayer.startMedia(mrl)) {
            mediaPlayer.setPosition(VLC_THUMBNAIL_POSITION);
            try {
                inPositionLatch.await(); // Might wait forever if error

                ret = mediaPlayer.getSnapshot();
                snapshotTakenLatch.await(); // Might wait forever if error
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Thumbnail fetcher interrupted!", ex);
            }
            mediaPlayer.stop();
        }

        return ret;
    }
}
