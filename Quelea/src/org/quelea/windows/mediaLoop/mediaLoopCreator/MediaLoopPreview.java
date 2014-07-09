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
package org.quelea.windows.mediaLoop.mediaLoopCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.data.powerpoint.SlideChangedListener;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.mediaLoop.MediaLoopThumbnail;

/**
 * A JList for specifically displaying mediaLoop slides.
 * <p/>
 * @author Michael
 */
public class MediaLoopPreview extends ScrollPane {

    private FlowPane flow;
    private List<MediaLoopThumbnail> thumbnails = new ArrayList<>();
    private ArrayList<MediaFile> slides = new ArrayList();
    private MediaFile selectedSlide;
    private int selectedIndex = 0;
    private int elapsedTimeCurrentItem = 0;
    private List<SlideChangedListener> listeners = new ArrayList<>();
    private final boolean isMediaLoopCreator;
    private boolean runLoop = false;
    private int localDragIndex = -1;
    private Rectangle markerRect;
    private Thread loopThread;

    /**
     * Create a new mediaLoop list.
     */
    public MediaLoopPreview(boolean isMediaLoopCreator) {
        this.isMediaLoopCreator = isMediaLoopCreator;
        setStyle("-fx-focus-color: transparent;-fx-background-color:linear-gradient(to bottom right, #c0c0c0, #e8e8e8);");
        flow = new FlowPane(20, 20);
        flow.setAlignment(Pos.CENTER);
        markerRect = new Rectangle(10, QueleaApp.get().getProjectionWindow().getCanvas().getHeight()
                / (QueleaApp.get().getProjectionWindow().getCanvas().getWidth() / 200), Color.GRAY);
        markerRect.setVisible(false);
        flow.getChildren().add(markerRect);
        markerRect.toFront();
        viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
                flow.setPrefWidth(newBounds.getWidth());
            }
        });
        setContent(flow);
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                int selected = -1;
                for (int i = 0; i < thumbnails.size(); i++) {
                    MediaLoopThumbnail thumbnail = thumbnails.get(i);
                    Bounds bounds = new BoundingBox(thumbnail.getLayoutX(), thumbnail.getLayoutY() + getScrollOffset(), thumbnail.getWidth(), thumbnail.getHeight());
                    if (bounds.contains(t.getX(), t.getY())) {
                        selected = i;
                    }
                }
                if (selected != -1) {
                    select(selected, true);
                }
            }
        });
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.DOWN || t.getCode() == KeyCode.RIGHT) {
                    if (selectedIndex > 0 && selectedIndex <= slides.size() - 1) {
                        select(selectedIndex + 1);
                    }
                }
                if (t.getCode() == KeyCode.UP || t.getCode() == KeyCode.LEFT) {
                    if (selectedIndex >= 2) {
                        select(selectedIndex - 1);
                    }
                }
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean focused) {
                for (MediaLoopThumbnail slide : thumbnails) {
                    slide.setActive(focused);
                }
            }
        });
    }

    /**
     * Add a slide change listener that listens for changes to selected slides
     *
     * @param listener the listener that listens for changes
     */
    public void addSlideChangedListener(SlideChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * Fire the slide changed event to all listeners
     */
    private void fireSlideChangedListeners() {
        for (SlideChangedListener listener : listeners) {
            listener.slideChanged(selectedIndex);
        }
    }

    /**
     * Clear all current slides and set the slides in the list.
     * <p/>
     * @param slides the slides to put in the list.
     */
    public void setSlides(final ArrayList<MediaFile> slides) {
        this.slides = slides;
        flow.getChildren().clear();
        thumbnails.clear();

        for (int i = 0; i < slides.size(); i++) {
            final MediaLoopThumbnail thumbnail = new MediaLoopThumbnail(slides.get(i));
            thumbnail.setIndex(i);
            if (i == 0) {
                thumbnail.setSelected(true);
            }
            thumbnail.setActive(true);

            thumbnails.add(thumbnail);
            flow.getChildren().add(thumbnail);
            thumbnail.setActive(focusedProperty().get());
            thumbnail.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    localDragIndex = thumbnail.getIndex();
                    Dragboard db = startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.put(MediaLoopThumbnail.MEDIA_LOOP_THUMBNAIL_FORMAT, thumbnail);
                    db.setContent(content);
                    t.consume();
                }
            });
            thumbnail.setOnDragEntered(new EventHandler<DragEvent>() {

                @Override
                public void handle(DragEvent event) {
                    int size = thumbnails.size();
                    if (thumbnails.isEmpty()) {
                        if (event.getDragboard().getContent(MediaLoopThumbnail.MEDIA_LOOP_THUMBNAIL_FORMAT) != null) {
                            for (MediaLoopThumbnail thumbnail : thumbnails) {
                                if (thumbnail.isVisible() && thumbnail.getIndex() == size) {
                                    markerRect.setTranslateX(thumbnail.getLayoutX() + thumbnail.getTranslateX());
                                    markerRect.setTranslateY(thumbnail.getLayoutY() + thumbnail.getTranslateY());
                                    markerRect.setVisible(true);
                                    break;
                                }
                            }
                        }
                    } else {
                        if (event.getDragboard().getString() != null) {
                            if (thumbnail instanceof MediaLoopThumbnail) {
                                thumbnail.setStyle("-fx-background-color: #99cccc;");
                            }
                        }
                    }
                }
            });
            thumbnail.setOnDragExited(new EventHandler<DragEvent>() {

                @Override
                public void handle(DragEvent t) {

                    markerRect.setVisible(false);
                }
            });
            thumbnail.setOnDragOver(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (event.getDragboard().getContent(MediaLoopThumbnail.MEDIA_LOOP_THUMBNAIL_FORMAT) != null) {
                        event.acceptTransferModes(TransferMode.ANY);
                    }
                }
            });
            thumbnail.setOnDragDropped(new EventHandler<DragEvent>() {

                @Override
                public void handle(DragEvent event) {
                    if (event.getDragboard().getContent(MediaLoopThumbnail.MEDIA_LOOP_THUMBNAIL_FORMAT) instanceof MediaLoopThumbnail) {
                        final MediaLoopThumbnail finalThumbnail = (MediaLoopThumbnail) event.getDragboard().getContent(MediaLoopThumbnail.MEDIA_LOOP_THUMBNAIL_FORMAT);
                        if (finalThumbnail != null) {
                            if (thumbnail.getIndex() != localDragIndex) {
                                if (localDragIndex > -1) {
                                    slides.remove(localDragIndex);
                                    localDragIndex = -1;
                                }
                                slides.add(thumbnail.getIndex(), finalThumbnail.getMediaFile());
                                int selected = -1;
                                for (int i = 0; i < thumbnails.size(); i++) {
                                    MediaLoopThumbnail thumbnail = thumbnails.get(i);
                                    Bounds bounds = new BoundingBox(thumbnail.getLayoutX(), thumbnail.getLayoutY() + getScrollOffset(), thumbnail.getWidth(), thumbnail.getHeight());
                                    if (bounds.contains(event.getX(), event.getY())) {
                                        selected = i;
                                    }
                                }
                                setSlides(slides);
                                if (selected != -1) {
                                    select(selected, true);
                                }

                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * Get currently selected index
     *
     * @return the selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Get all media files as array list
     *
     * @return all the media files
     */
    public ArrayList<MediaFile> getMediaFiles() {
        return slides;
    }

    /**
     * Get the selected slide as a media file
     *
     * @return the media file of the currently selected slide
     */
    public MediaFile getSelectedSlide() {
        if (selectedSlide == null) {
            if (slides.size() > 0) {
                selectedSlide = slides.get(0);
            }

        }
        return selectedSlide;
    }

    /**
     * Advances the current slide.
     * <p/>
     */
    public void advanceSlide() {
        if (selectedIndex == slides.size() - 1) {
            select(0);
        } else if (selectedIndex >= -1 && selectedIndex <= slides.size() - 1) {
            select(selectedIndex + 1);
        }

    }

    /**
     * Moves to the previous slide.
     * <p/>
     */
    public void previousSlide() {
        if (selectedIndex >= 1) {
            select(selectedIndex - 1);
        }
    }

    /**
     * Gets the size of the media loop
     *
     * @return the number of items in the media loop
     */
    public int size() {
        return thumbnails.size();
    }

    /**
     * Select a slide in the media loop
     *
     * @param index the index of the desired slide
     */
    public void select(int index) {
        select(index, true);
    }

    /**
     * Select a slide in the media loop
     *
     * @param index the index of the desired slide
     * @param fireUpdate true if the listeners should update, false otherwise.
     */
    public void select(int index, boolean fireUpdate) {
        if (selectedIndex == index) {

        } else {
            for (int i = 0; i < thumbnails.size(); i++) {
                MediaLoopThumbnail thumbnail = thumbnails.get(i);
                boolean selected = thumbnail.getIndex() == index;
                thumbnail.setSelected(selected);
                if (selected) {
                    selectedIndex = index;
                    if (selectedIndex >= 0) {
                        selectedSlide = slides.get(i);
                    } else {
                        selectedSlide = null;
                    }
                }
                ensureVisible(selectedIndex);
            }
        }
        if (fireUpdate) {
            fireSlideChangedListeners();
        }
        elapsedTimeCurrentItem = 0;
    }

    /**
     * Ensure that the chosen slide is visible
     *
     * @param index the index of the slide that is to be visible
     */
    private void ensureVisible(int index) {
        if (index < 1) {
            return;
        }
        Bounds slideBounds = thumbnails.get(index - 1).getBoundsInParent();
        Bounds scrollBounds = new BoundingBox(0, getScrollFraction() * (flow.getHeight() - getHeight()), getWidth(), getHeight());
        if (!scrollBounds.contains(slideBounds)) {
            while (slideBounds.getMinY() < scrollBounds.getMinY() && getVvalue() > getVmin()) {
                slideBounds = thumbnails.get(index - 1).getBoundsInParent();
                scrollBounds = new BoundingBox(0, getScrollFraction() * (flow.getHeight() - getHeight()), getWidth(), getHeight());
                setVvalue(getVvalue() - 0.01);
            }
            while (slideBounds.getMaxY() > scrollBounds.getMaxY() && getVvalue() < getVmax()) {
                slideBounds = thumbnails.get(index - 1).getBoundsInParent();
                scrollBounds = new BoundingBox(0, getScrollFraction() * (flow.getHeight() - getHeight()), getWidth(), getHeight());
                setVvalue(getVvalue() + 0.01);
            }
        }
    }

    /**
     * Get the amount that should be offset so scroll is correct
     *
     * @return the scroll offset
     */
    private double getScrollOffset() {
        return getScrollFraction() * (getHeight() - flow.getHeight());
    }

    /**
     * Get the scroll fraction for calculating correct sizes
     *
     * @return the scroll fraction
     */
    private double getScrollFraction() {
        double vMin = getVmin();
        double vMax = getVmax();
        double vFrac = (getVvalue() - vMin) * (vMax - vMin);
        return vFrac;
    }

    /**
     * Clear all items in this preview
     */
    public void clear() {
        thumbnails.clear();
        flow.getChildren().clear();
        slides.clear();
        selectedIndex = -1;
        selectedSlide = null;
    }

    /**
     * Get the hash
     *
     * @return The hash as a long
     */
    public long getHash() {
        String hash = "" + slides.size();
        long runningCharacters = 0;
        for (MediaFile file : slides) {
            runningCharacters = runningCharacters + file.getName().length();
        }
        hash = hash + runningCharacters;
        return Long.parseLong(hash);
    }

    /**
     * Stop the loop
     */
    public void stopLoop() {
        runLoop = false;

        if (!(loopThread == null)) {
            loopThread.interrupt();
        }

    }

    /**
     * Run the loop
     */
    public void runLoop() {
        runLoop = true;
        loopThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (runLoop) {
                    if (slides.size() > 0) {
                        boolean advance = false;
                        while (!advance) {
                            if (!(loopThread.isInterrupted())) {
                                Utils.fxRunAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getSecondsLeftLabel().setText(
                                                (getSelectedSlide().getAdvanceTime() - elapsedTimeCurrentItem) + " "
                                                + LabelGrabber.INSTANCE.getLabel("mediaLoop.secondsRemaining.text"));
                                    }
                                });
                            }

                            try {
                                Thread.sleep(1000);
                                elapsedTimeCurrentItem++;
                            } catch (InterruptedException ex) {
                                runLoop = false;
                                return;
                            }

                            if (elapsedTimeCurrentItem >= getSelectedSlide().getAdvanceTime()) {
                                advance = true;

                            }

                        }
                        if (!(loopThread.isInterrupted())) {

                            Utils.fxRunAndWait(new Runnable() {

                                @Override
                                public void run() {
                                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getSecondsLeftLabel().setText(
                                            LabelGrabber.INSTANCE.getLabel("mediaLoop.changing.text"));
                                    advanceSlide();

                                }
                            });
                        }

                    }
                }
            }
        });
        loopThread.start();

    }
}
