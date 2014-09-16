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
package org.quelea.windows.main;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.image.ImageDrawer;
import org.quelea.windows.lyrics.LyricDrawer;

/**
 * Implements ContainedPanel with additional canvas registering
 * <p/>
 * @author tomaszpio@gmail.com
 */
public abstract class AbstractPanel extends BorderPane implements ContainedPanel {

    private static class PriorityComparator implements Comparator<DisplayCanvas> {

        @Override
        public int compare(DisplayCanvas o1, DisplayCanvas o2) {
            return o2.getDravingPriority().getPriority() - o1.getDravingPriority().getPriority();
        }
    }
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private SortedSet<DisplayCanvas> canvases = new TreeSet<>(new PriorityComparator());
    private Displayable currentDisplayable = null;
    private static boolean isNextPreviewed = false;

    public void setCurrentDisplayable(Displayable currentDisplayable) {
        this.currentDisplayable = currentDisplayable;
    }

    public Displayable getCurrentDisplayable() {
        return currentDisplayable;
    }

    @Override
    public Set<DisplayCanvas> getCanvases() {
        return canvases;
    }

    @Override
    public void registerDisplayCanvas(DisplayCanvas canvas) {
        canvases.add(canvas);
    }

    /**
     * Show a given video displayable on the panel.
     * <p/>
     * @param displayable the video displayable.
     */
    public void showDisplayable(MultimediaDisplayable displayable) {
        currentDisplayable = displayable;
        updateCanvas();
    }

    public void updateCanvas() {
        assert Utils.fxThread();
        for (final DisplayCanvas canvas : getCanvases()) {

            canvas.setCurrentDisplayable(currentDisplayable);

            getDrawer(canvas).draw(currentDisplayable);
            Platform.runLater(new Runnable() {
                //in order to get the proper image in the update panel
                @Override
                public void run() {
                    if (canvas.isStageView()) {
                        updatePreview(canvas.getPreviewCanvas());
                    }
                }
            });

        }
    }

    /**
     * Update the stage preview canvas with what is in the live display
     *
     * @param previewCanvas The preview canvas that is to be updated
     */
    public void updatePreview(DisplayCanvas previewCanvas) {

        previewCanvas.clearNonPermanentChildren();
        setIsNextPreviewed(true);
        if (QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable() instanceof TextDisplayable) {

            LyricDrawer ldrawer = new LyricDrawer();
            int nextIndex = QueleaApp.get().getMainWindow()
                    .getMainPanel().getPreviewPanel().getLyricsPanel().
                    getLyricsList().selectionModelProperty().get().getSelectedIndex();
            TextSection nextSection = QueleaApp.get().getMainWindow()
                    .getMainPanel().getPreviewPanel().getLyricsPanel().
                    getLyricsList().selectionModelProperty().get().getSelectedItem();
            ldrawer.setCanvas(previewCanvas);
            if (nextSection.getTempTheme() != null) {
                ldrawer.setTheme(nextSection.getTempTheme());
            } else {
                ThemeDTO newTheme = nextSection.getTheme();
                ldrawer.setTheme(newTheme);
            }
            ldrawer.setCapitaliseFirst(nextSection.shouldCapitaliseFirst());
            ldrawer.setText((TextDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable(), nextIndex);
            previewCanvas.setCurrentDisplayable(QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable());
        } else if (QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable() instanceof ImageDisplayable) {

            Displayable d = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if (d == null) {
                return;
            }
            ImageDrawer idrawer = new ImageDrawer();
            idrawer.setCanvas(previewCanvas);
            idrawer.draw(d);

        } else if (QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable() instanceof PresentationDisplayable) {

            PresentationDisplayable d = (PresentationDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if (d == null) {
                return;
            }
            int index = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getIndex();

            if (index >= d.getPresentation().getSlides().length || index < 0) {
                index = 0;
            }
            ImageDrawer idrawer = new ImageDrawer();
            Image displayNextImage = d.getPresentation().getSlide(index).getImage();
            ImageDisplayable imageNextDisplayable = new ImageDisplayable(displayNextImage);
            idrawer.setCanvas(previewCanvas);
            idrawer.draw(imageNextDisplayable);

        } else if (QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable() instanceof MediaLoopDisplayable) {

            MediaLoopDisplayable d = (MediaLoopDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if (d == null) {
                return;
            }
            int index = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getIndex();

            if (index >= d.getMediaFiles().size()) {
                index = 0;
            }
            Image displayNextImage;
            ImageDrawer idrawer = new ImageDrawer();
            if (index < d.getMediaFiles().size() - 1 && index > -1) {
                displayNextImage = d.getMediaFiles().get(index).getImage();
                ImageDisplayable imageNextDisplayable = new ImageDisplayable(displayNextImage);
                idrawer.setCanvas(previewCanvas);
                idrawer.draw(imageNextDisplayable);
            }

        } else {
            Displayable d = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if (d == null) {
                return;
            }
            ImageDrawer idrawer = new ImageDrawer();
            Image displayNextImage = d.getPreviewIcon().getImage();
            ImageDisplayable imageNextDisplayable = new ImageDisplayable(displayNextImage);
            idrawer.setCanvas(previewCanvas);
            idrawer.draw(imageNextDisplayable);
        }
        this.resize(this.getWidth(), this.getHeight());
    }

    /**
     * Gets whether the next displayable is previewed on the stage display
     *
     * @return True if previewed, false otherwise
     */
    public static boolean isIsNextPreviewed() {
        return isNextPreviewed;
    }

    /**
     * Sets whether the next displayable is previewed on the stage display
     *
     * @param isNextPreviewed True if previewed, false otherwise
     */
    public static void setIsNextPreviewed(boolean isNextPreviewed) {
        AbstractPanel.isNextPreviewed = isNextPreviewed;
    }

    @Override
    public void removeCurrentDisplayable() {
        assert Utils.fxThread();
        for (DisplayCanvas canvas : getCanvases()) {
            /*if(!canvas.isLogoShowing()) {
             canvas.clearCurrentDisplayable();
             }*/
            canvas.clearNonPermanentChildren();
        }
    }

    @Override
    public abstract int getCurrentIndex();

    public abstract DisplayableDrawer getDrawer(DisplayCanvas canvas);
}
