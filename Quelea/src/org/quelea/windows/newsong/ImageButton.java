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
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.stage.StageDrawer;

/**
 * The image button where the user selects a image.
 * <p>
 * @author Michael
 */
public class ImageButton extends Button {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String imageLocation;
    private final FileChooser fileChooser;

    /**
     * Create and initialise the image button.
     * <p>
     * @param imageLocationField the image location field that goes with this
     * button.
     * @param canvas the preview canvas to update.
     */
    public ImageButton(final TextField imageLocationField, final DisplayCanvas canvas) {
        super("..");
//        super(LabelGrabber.INSTANCE.getLabel("select.image.button"));
        final File imageDir = QueleaProperties.get().getImageDir();
        fileChooser = new FileChooser();
        if (QueleaProperties.get().getLastDirectory() != null) {
            fileChooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
        }
        fileChooser.setInitialDirectory(imageDir);
        fileChooser.getExtensionFilters().add(FileFilters.IMAGES);
        setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File selectedFile = fileChooser.showOpenDialog(QueleaApp.get().getMainWindow());
                if (selectedFile != null) {
                    QueleaProperties.get().setLastDirectory(selectedFile.getParentFile());
                    File newFile = new File(imageDir, selectedFile.getName());
                    try {
                        if (!Utils.isInDir(imageDir, selectedFile)) {
                            FileUtils.copyFile(selectedFile, newFile);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "", ex);
                    }

                    imageLocation = imageDir.toURI().relativize(newFile.toURI()).getPath();
                    imageLocationField.setText(imageLocation);
                    WordDrawer drawer;
                    if (canvas.isStageView()) {
                        drawer = new StageDrawer();
                    } else {
                        drawer = new LyricDrawer();
                    }
                    drawer.setCanvas(canvas);
                    ThemeDTO theme = new ThemeDTO(new SerializableFont(drawer.getTheme().getFont()),
                            drawer.getTheme().getFontPaint(), new SerializableFont(drawer.getTheme().getTranslateFont()),
                            drawer.getTheme().getTranslateFontPaint(), new ImageBackground(imageLocation),
                            drawer.getTheme().getShadow(), drawer.getTheme().isBold(),
                            drawer.getTheme().isItalic(), drawer.getTheme().isTranslateBold(),
                            drawer.getTheme().isTranslateItalic(), drawer.getTheme().getTextPosition(),
                            drawer.getTheme().getTextAlignment());

                    drawer.setTheme(theme);
                }
            }
        });
    }

    /**
     * Get the location of the selected image.
     * <p>
     * @return the selected image location.
     */
    public String getImageLocation() {
        return imageLocation;
    }
}
