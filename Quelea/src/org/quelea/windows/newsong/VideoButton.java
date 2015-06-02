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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Cancellable;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableFont;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.ModalCancellableStage;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.QueleaApp;

/**
 * The video button where the user selects a video.
 * <p/>
 * @author Michael
 */
public class VideoButton extends Button implements Cancellable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String vidLocation;
    private final FileChooser fileChooser;
    private final ModalCancellableStage copyStage = new ModalCancellableStage(LabelGrabber.INSTANCE.getLabel("copying.video.please.wait.text"));
    private Thread copyThread;

    /**
     * Create and initialise the video button.
     * <p/>
     * @param videoLocationField the video location field that goes with this
     * button.
     * @param canvas the preview canvas to update.
     */
    public VideoButton(final TextField videoLocationField, final DisplayCanvas canvas) {
        super("..");
        fileChooser = new FileChooser();
        if (QueleaProperties.get().getLastDirectory() != null) {
            fileChooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
        }
        final File vidDir = QueleaProperties.get().getVidDir();
        fileChooser.setInitialDirectory(vidDir);
        fileChooser.getExtensionFilters().add(FileFilters.VIDEOS);
        setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File selectedFile = fileChooser.showOpenDialog(QueleaApp.get().getMainWindow());
                if (selectedFile != null) {
                    QueleaProperties.get().setLastDirectory(selectedFile.getParentFile());
                    copyStage.showAndAssociate(VideoButton.this);
                    copyThread = new Thread() {
                        public void run() {
                            boolean interrupt = false;
                            File newFile = new File(vidDir, selectedFile.getName());
                            try {
                                if (!selectedFile.getCanonicalPath().startsWith(vidDir.getCanonicalPath())) {
                                    VideoButton.copyFile(selectedFile.getAbsolutePath(), newFile.getAbsolutePath());
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.INFO, "Interrupted copying vid file", ex);
                                newFile.delete();
                                interrupt = true;
                            }

                            if (!interrupt) {
                                Platform.runLater(() -> {
                                    copyStage.hide();
                                    vidLocation = vidDir.toURI().relativize(newFile.toURI()).getPath();
                                    videoLocationField.setText(vidLocation);
                                    LyricDrawer drawer = new LyricDrawer();
                                    drawer.setCanvas(canvas);
                                    ThemeDTO theme = new ThemeDTO(new SerializableFont(drawer.getTheme().getFont()),
                                            drawer.getTheme().getFontPaint(),
                                            new SerializableFont(drawer.getTheme().getTranslateFont()),
                                            drawer.getTheme().getTranslateFontPaint(),
                                            new VideoBackground(vidLocation, 0, false),
                                            drawer.getTheme().getShadow(),
                                            drawer.getTheme().isBold(),
                                            drawer.getTheme().isItalic(),
                                            drawer.getTheme().isTranslateBold(),
                                            drawer.getTheme().isTranslateItalic(),
                                            drawer.getTheme().getTextPosition(),
                                            drawer.getTheme().getTextAlignment());
                                    drawer.setTheme(theme);
                                });
                            }
                        }
                    };
                    copyThread.start();
                }
            }
        });
    }

    public Thread getCopyThread() {
        return copyThread;
    }
    
    /**
     * Cancel the copying.
     */
    @Override
    public void cancelOp() {
        getCopyThread().interrupt();
    }

    private static void copyFile(String in, String out) throws Exception {
        try (FileChannel fin = new FileInputStream(in).getChannel();
                FileChannel fout = new FileOutputStream(out).getChannel();) {

            ByteBuffer buff = ByteBuffer.allocate(4096);
            while (fin.read(buff) != -1 || buff.position() > 0) {
                buff.flip();
                fout.write(buff);
                buff.compact();
            }
        }
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
