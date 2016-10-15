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
package org.quelea.windows.imagegroup;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.imagegroup.ImageGroupSlide;
import org.quelea.windows.image.ImageDrawer;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.LivePreviewPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel for displaying a group of images in the live / preview panels.
 * <p/>
 * @author Arvid, based on PresentationPanel
 */
public class ImageGroupPanel extends AbstractPanel {

    private ImageGroupPreview imageGroupPreview;
    private ImageGroupDisplayable displayable;
    private boolean live;
    private ImageGroupDrawer drawer = new ImageGroupDrawer();
    private ImageGroupSlide currentSlide = null;
    private LivePreviewPanel containerPanel;
    private Timeline loopTimeline;

    /**
     * Create a new presentation panel.
     * <p/>
     * @param containerPanel the panel to create.
     */
    public ImageGroupPanel(final LivePreviewPanel containerPanel) {
        this.containerPanel = containerPanel;
        BorderPane mainPanel = new BorderPane();
        imageGroupPreview = new ImageGroupPreview();
        imageGroupPreview.addSlideChangedListener(new SlideChangedListener() {
            @Override
            public void slideChanged(ImageGroupSlide newSlide) {
                if (live) {
                    LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                    if (newSlide != null && displayable != null) {
                            currentSlide = newSlide;
                            updateCanvas();
                    }
                }
            }
        });
        imageGroupPreview.select(0);

        imageGroupPreview.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode().equals(KeyCode.PAGE_DOWN) || t.getCode().equals(KeyCode.DOWN)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
                } else if (t.getCode().equals(KeyCode.PAGE_UP) || t.getCode().equals(KeyCode.UP)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
                }
            }
        });
        mainPanel.setCenter(imageGroupPreview);
        setCenter(mainPanel);
    }

    @Override
    public void requestFocus() {
        imageGroupPreview.requestFocus();
    }

    public void buildLoopTimeline() {
        loopTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                if (containerPanel instanceof LivePanel) {
                                    LivePanel livePanel = ((LivePanel) containerPanel);
                                    if (livePanel.isLoopSelected()) {
                                        imageGroupPreview.advanceSlide(true);
                                    }
                                }
                            }
                        }
                ),
                new KeyFrame(Duration.seconds(10))
        );
        loopTimeline.setCycleCount(Animation.INDEFINITE);
        loopTimeline.play();
        QueleaApp.get().doOnLoad(new Runnable() {

            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLoopDurationTextField().textProperty().addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        int newTime;
                        try {
                            newTime = Integer.parseInt(t1);
                        } catch (NumberFormatException ex) {
                            return;
                        }
                        loopTimeline.stop();
                        loopTimeline = new Timeline(
                                new KeyFrame(Duration.seconds(0),
                                        new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent actionEvent) {
                                                if (containerPanel instanceof LivePanel) {
                                                    LivePanel livePanel = ((LivePanel) containerPanel);
                                                    if (livePanel.isLoopSelected()) {
                                                        imageGroupPreview.advanceSlide(true);
                                                    }
                                                }
                                            }
                                        }
                                ),
                                new KeyFrame(Duration.seconds(newTime))
                        );
                        loopTimeline.setCycleCount(Animation.INDEFINITE);
                        loopTimeline.play();
                    }
                });
            }
        });
    }

    private void drawSlide(ImageGroupSlide newSlide, DisplayCanvas canvas) {
        Image displayImage = newSlide.getImage();
        ImageDisplayable imageDisplayable = new ImageDisplayable(displayImage);
        drawer.setCanvas(canvas);
        drawer.draw(imageDisplayable);
    }

    public void stopCurrent() {
    }

    /**
     * Let this panel know it is live and should update accordingly.
     */
    public void setLive() {
        live = true;
    }

    /**
     * Set the displayable to be on this presentation panel.
     * <p/>
     * @param displayable the presentation displayable to display.
     * @param index the index to display.
     */
    public void showDisplayable(final ImageGroupDisplayable displayable, final int index) {
        if (this.displayable == displayable) {
            return;
        }
        this.displayable = displayable;
        if (displayable == null) {
            imageGroupPreview.clear();
            return;
        }
        ImageGroupSlide[] slides = displayable.getPresentation().getSlides();
        imageGroupPreview.setSlides(slides);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (index < 1) {
                    imageGroupPreview.select(1, true);
                } else {
                    imageGroupPreview.select(index, true);
                }
            }
        });

        /*
         * TODO
         * For some reason the following scroll to line causes a bug whereby 
         * the contents are only registered the second time of viewing? So 
         * leave commented out until we can get to the bottom of it.
         */
//        presentationList.scrollTo(getIndex());
        updateCanvas();
    }

    /**
     * Get the currently selected index on this panel.
     * <p/>
     * @return the currently selected index on this panel.
     */
    public int getIndex() {
        return imageGroupPreview.getSelectedIndex();
    }
    
    /**
     * Get the length of the item on this panel.
     * <p/>
     * @return the length of the item on this panel.
     */
    public int getSlideCount() {
        return imageGroupPreview.getSlidesCount();
    }

    /**
     * Clear this panel (well, actually don't do anything because we can't clear
     * a presentation.)
     */
    @Override
    public void removeCurrentDisplayable() {
        //Doesn't apply
    }

    @Override
    public int getCurrentIndex() {
        return imageGroupPreview.getSelectedIndex();
    }

    @Override
    public void updateCanvas() {
        for (DisplayCanvas canvas : getCanvases()) {
            if (currentSlide != null) {
                drawSlide(currentSlide, canvas);
            }
        }
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        return drawer;
    }

    public void advance() {
        imageGroupPreview.advanceSlide(false);
    }

    public void previous() {
        imageGroupPreview.previousSlide();
    }

    public void selectLast() {
        imageGroupPreview.selectLast();
    }
    
    public void selectFirst() {
        imageGroupPreview.select(1, true);
    }

    public ImageGroupPreview getPresentationPreview() {
        return imageGroupPreview;
    }
}
