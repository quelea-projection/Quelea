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
package org.quelea.windows.newsong;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.SerializableFont;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.QueleaApp;

/**
 * The video button where the user selects a video.
 * <p/>
 * @author Michael
 */
public class VideoButton extends Button {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String vidLocation;
    private final FileChooser fileChooser;

    /**
     * Create and initialise the video button.
     * <p/>
     * @param videoLocationField the video location field that goes with this
     * button.
     * @param canvas the preview canvas to update.
     */
    public VideoButton(final TextField videoLocationField, final DisplayCanvas canvas) {
        super(LabelGrabber.INSTANCE.getLabel("select.video.button"));
        fileChooser = new FileChooser();
        final File vidDir = new File("vid");
        fileChooser.setInitialDirectory(vidDir);
        fileChooser.getExtensionFilters().add(FileFilters.VIDEOS);
        setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File selectedFile = fileChooser.showOpenDialog(QueleaApp.get().getMainWindow());
                if (selectedFile != null) {
                    File newFile = new File(vidDir, selectedFile.getName());
                    try {
                        if (!selectedFile.getCanonicalPath().startsWith(vidDir.getCanonicalPath())) {
                            FileUtils.copyFile(selectedFile, newFile);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "", ex);
                    }

                    vidLocation = vidDir.toURI().relativize(newFile.toURI()).getPath();
                    videoLocationField.setText(vidLocation);
                    LyricDrawer drawer = new LyricDrawer();
                    drawer.setCanvas(canvas);
                    ThemeDTO theme = new ThemeDTO(new SerializableFont(drawer.getTheme().getFont()),
                            drawer.getTheme().getFontPaint(), new VideoBackground(vidLocation),
                            drawer.getTheme().getShadow(),
                            drawer.getTheme().isBold(),
                            drawer.getTheme().isBold());
                    drawer.setTheme(theme);
                }
            }
        });
    }

    /**
     * Get the location of the selected video.
     * <p/>
     * @return the selected video location.
     */
    public String getVideoLocation() {
        return vidLocation;
    }
}
