package org.quelea.windows.mediaLoop;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.image.ImageDrawer;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.multimedia.MultimediaControls;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * drawer that draws media loop items
 *
 * @author tomaszpio@gmail.com, Michael
 */
public class MediaLoopDrawer extends DisplayableDrawer {

    private final MultimediaControls controlPanel;
    private boolean playVideo;
    private MediaFile theSlide;

    /**
     * Initialize media loop drawer
     *
     * @param controlPanel multimedia controls for controling playback
     */
    public MediaLoopDrawer(MultimediaControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    /**
     * Draw item on displays
     *
     * @param displayable the displayable to be drawed
     */
    @Override
    public void draw(Displayable displayable) {
        if (!(displayable instanceof MediaLoopDisplayable)) {
            return;
        }
        if (getCanvas().isStageView()) {
            Image image;
            if (QueleaProperties.get().getStageDrawImages()) {
                image = theSlide.getImage();
            } else {
                image = (Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            }
            ImageDrawer idrawer = new ImageDrawer();
            ImageDisplayable imageNextDisplayable = new ImageDisplayable(image);
            idrawer.setCanvas(getCanvas());
            idrawer.draw(imageNextDisplayable);
        } else if (getCanvas().isTextOnlyView()) {
            Image image;

            image = (Utils.getImageFromColour(QueleaProperties.get().getTextOnlyBackgroundColor()));

            ImageDrawer idrawer = new ImageDrawer();
            ImageDisplayable imageNextDisplayable = new ImageDisplayable(image);
            idrawer.setCanvas(getCanvas());
            idrawer.draw(imageNextDisplayable);
        } else {
            if (playVideo) {
                controlPanel.loadMultimedia(theSlide.getAbsolutePath(), false);
                controlPanel.play();
                VLCWindow.INSTANCE.setRepeat(true);
            } else {
                Image image;

                image = theSlide.getImage();
                ImageDrawer idrawer = new ImageDrawer();
                ImageDisplayable imageNextDisplayable = new ImageDisplayable(image);
                idrawer.setCanvas(getCanvas());
                idrawer.draw(imageNextDisplayable);
            }
        }
    }

    /**
     * Set the slide to show
     *
     * @param file the media file representing the slide
     */
    public void setSlideToShow(MediaFile file) {
        this.theSlide = file;
    }

    /**
     * Set whether this drawer can play video
     *
     * @param playVideo True if it can play video, false otherwise.
     */
    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if (playVideo) {
            getCanvas().clearNonPermanentChildren();
        }
    }

    /**
     * Clear this drawer
     */
    @Override
    public void clear() {
    }

    /**
     * Request focus for this drawer
     */
    @Override
    public void requestFocus() {
    }
}
