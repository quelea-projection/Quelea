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

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import javax.swing.JFrame;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Class for setting up a new instance of VLC in the background
 * and converting a file.
 * 
 * @author Arvid
 */
public class RecordingEncoder {

    private final JFrame ourFrame = new JFrame();
    private EmbeddedMediaPlayerComponent mp;
    private String mediaPath = "";
    private final String[] options;
    private StatusPanel statusPanel;
    private boolean converting;

    public RecordingEncoder(String mediaUrl, String[] options) {
        this.options = options;
        this.mediaPath = mediaUrl;
        new NativeDiscovery().discover();
        mp = new EmbeddedMediaPlayerComponent() {

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                // Delete the WAV file when convertion is done
                String error;
                Path path = Paths.get(mediaUrl);
                do {
                    try {
                        Files.delete(path);
                        error = "";
                        mediaPlayer.release();
                    } catch (NoSuchFileException | DirectoryNotEmptyException x) {
                        error = "";
                    } catch (IOException x) {
                        // File is still being read by the system,
                        // keep trying to delete until it's avaiable again.
                        error = "busy";
                    }
                } while (!error.equals(""));
                if (statusPanel != null) {
                    Platform.runLater(() -> {
                        statusPanel.done();
                    });
                }
                converting = false;
            }
        };
        
        // Set up VLC window
        ourFrame.setContentPane(mp);
        ourFrame.setSize(1, 1);
        ourFrame.setVisible(true);
        ourFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void run() {
        // Start converting
        mp.getMediaPlayer().playMedia(mediaPath, options);
        // Hide the VLC window
        ourFrame.setVisible(false);
        ourFrame.dispose();
        // Start loading panel
        Platform.runLater(() -> {
            statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("converting.to.mp3"));
        });
        converting = true;
    }

    /**
     * @return the converting
     */
    public boolean isConverting() {
        return converting;
    }

}
