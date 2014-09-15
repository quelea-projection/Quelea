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
package org.quelea.windows.mediaLoop;

import javafx.animation.Timeline;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.data.powerpoint.SlideChangedListener;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.mediaLoop.mediaLoopCreator.MediaLoopPreview;
import org.quelea.windows.multimedia.MultimediaControls;

/**
 * The panel for displaying mediaLoop slides in the live / preview panels.
 * <p/>
 * @author Michael
 */
public class MediaLoopPanel extends AbstractPanel {

    private MediaLoopPreview mediaLoopPreview;
    private MediaLoopDisplayable displayable;
    private boolean live;
    private MultimediaControls dummyControls = new MultimediaControls();
    private MediaLoopDrawer mediaLoopDrawer = new MediaLoopDrawer(dummyControls);
    private Timeline loopTimeline;

    /**
     * Create a new mediaLoop panel.
     * <p/>
     */
    public MediaLoopPanel() {
        BorderPane mainPanel = new BorderPane();
        mediaLoopPreview = new MediaLoopPreview(false, null);
        mediaLoopPreview.addSlideChangedListener(new SlideChangedListener() {
            @Override
            public void slideChanged(int newSlide) {
                if (live) {
                    if (newSlide >= 0 && displayable != null) {
                        if (live) {
                            displaySlide(mediaLoopPreview.getSelectedSlide());
                        }
                    } else {
                        dummyControls.stop();
                    }
                }
            }
        });

        mainPanel.setCenter(mediaLoopPreview);
        setCenter(mainPanel);
    }

    /**
     * Display slide
     *
     * @param newSlide the media file to be displayed
     */
    private void displaySlide(MediaFile slide) {

        for (DisplayCanvas canvas : getCanvases()) {
            mediaLoopDrawer.setCanvas(canvas);
            mediaLoopDrawer.setPlayVideo(canvas.getPlayVideo());
            canvas.setCurrentDisplayable(displayable);
            mediaLoopDrawer.setSlideToShow(slide);
            mediaLoopDrawer.draw(displayable);
            if (canvas.isStageView()) {
                if (QueleaProperties.get().getStageUsePreview()) {

                    AbstractPanel.setIsNextPreviewed(false);
                    mediaLoopDrawer.setCanvas(canvas.getPreviewCanvas());
                    mediaLoopDrawer.setPlayVideo(canvas.getPreviewCanvas().getPlayVideo());
                    canvas.setCurrentDisplayable(displayable);
                    mediaLoopDrawer.setSlideToShow(mediaLoopPreview.getNextSlide());
                    mediaLoopDrawer.draw(displayable);

                }
            }

        }

    }

    /**
     * Stop current slide
     */
    public void stopCurrent() {
        if (live && displayable != null) {
            stopLoop();
            mediaLoopPreview.select(-1);
            displayable = null;

        }
    }

    /**
     * Let this panel know it is live and should update accordingly.
     */
    public void setLive() {
        live = true;

    }

    /**
     * Start media loop
     */
    public void startLoop() {
        mediaLoopPreview.runLoop();
    }

    /**
     * Stop loop
     */
    public void stopLoop() {
        mediaLoopPreview.stopLoop();
    }

    /**
     * Set the displayable to be on this mediaLoop panel.
     * <p/>
     * @param displayable the mediaLoop displayable to display.
     * @param index the index to display.
     */
    public void showDisplayable(final MediaLoopDisplayable displayable, final int index) {

        this.displayable = displayable;

        if (displayable == null) {
            mediaLoopPreview.clear();
            return;
        }

        mediaLoopPreview.setSlides(displayable.getMediaFiles());

        mediaLoopPreview.createGraphics();

        if (index < 0) {
            mediaLoopPreview.select(0, true);
        } else {
            mediaLoopPreview.select(index, true);
        }


        /*
         * TODO
         * For some reason the following scroll to line causes a bug whereby 
         * the contents are only registered the second time of viewing? So 
         * leave commented out until we can get to the bottom of it.
         */
//        mediaLoopList.scrollTo(getIndex());
        updateCanvas();
    }

    /**
     * Get the currently selected index on this panel.
     * <p/>
     * @return the currently selected index on this panel.
     */
    public int getIndex() {
        return mediaLoopPreview.getSelectedIndex();
    }

    /**
     * Clear this panel (well, actually don't do anything because we can't clear
     * a mediaLoop.)
     */
    @Override
    public void removeCurrentDisplayable() {
        //Doesn't apply
    }

    /**
     * Get current selected index
     *
     * @return the index as an it
     */
    @Override
    public int getCurrentIndex() {
        return mediaLoopPreview.getSelectedIndex();
    }

    /**
     * Update canvas
     */
    @Override
    public void updateCanvas() {

//        if (mediaLoopPreview.getSelectedSlide() != null) {
//            displaySlide(mediaLoopPreview.getSelectedSlide());
//        }
    }

    /**
     * Get the display drawer for the specific canvas
     *
     * @param canvas The canvas that the drawer is attached to.
     * @return the drawer as a displayable drawer
     */
    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        return mediaLoopDrawer;

    }

    /**
     * Advance to the next media item manually
     */
    public void advance() {
        mediaLoopPreview.advanceSlide();
    }

    /**
     * Advance to the next media item manually
     */
    public void previous() {
        mediaLoopPreview.previousSlide();
    }
}
