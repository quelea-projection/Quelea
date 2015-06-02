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
package org.quelea.windows.presentation;

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
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.powerpoint.OOPresentation;
import org.quelea.data.powerpoint.PresentationSlide;
import org.quelea.data.powerpoint.SlideChangedListener;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.image.ImageDrawer;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.LivePreviewPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel for displaying presentation slides in the live / preview panels.
 * <p/>
 * @author Michael
 */
public class PresentationPanel extends AbstractPanel {

    private PresentationPreview presentationPreview;
    private PresentationDisplayable displayable;
    private boolean live;
    private DisplayableDrawer drawer = new ImageDrawer();
    private PresentationSlide currentSlide = null;
    private LivePreviewPanel containerPanel;
    private Timeline loopTimeline;

    /**
     * Create a new presentation panel.
     * <p/>
     * @param containerPanel the panel to create.
     */
    public PresentationPanel(final LivePreviewPanel containerPanel) {
        this.containerPanel = containerPanel;
        BorderPane mainPanel = new BorderPane();
        presentationPreview = new PresentationPreview();
        presentationPreview.addSlideChangedListener(new org.quelea.windows.presentation.SlideChangedListener() {
            @Override
            public void slideChanged(PresentationSlide newSlide) {
                if(live) {
                    if(newSlide != null && displayable != null) {
                        if(displayable.getOOPresentation() == null) {
                            currentSlide = newSlide;
                            updateCanvas();
                        }
                        else {
                            OOPresentation pres = displayable.getOOPresentation();
                            pres.addSlideListener(new SlideChangedListener() {
                                @Override
                                public void slideChanged(final int newSlideIndex) {
                                    presentationPreview.select(newSlideIndex);
                                }
                            });
                            currentSlide = newSlide;
                            startOOPres();
                            QueleaApp.get().getMainWindow().toFront();
                            pres.gotoSlide(presentationPreview.getSelectedIndex());
                        }
                    }
                }
            }
        });
        presentationPreview.select(0);

        presentationPreview.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode().equals(KeyCode.PAGE_DOWN) || t.getCode().equals(KeyCode.DOWN)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
                } else if (t.getCode().equals(KeyCode.PAGE_UP)|| t.getCode().equals(KeyCode.UP)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
                }
            }
        });
        mainPanel.setCenter(presentationPreview);
        setCenter(mainPanel);
    }
    
    @Override
    public void requestFocus() {
        presentationPreview.requestFocus();
    }
    
    public void buildLoopTimeline() {
        loopTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                if(containerPanel instanceof LivePanel) {
                                    LivePanel livePanel = ((LivePanel) containerPanel);
                                    if(livePanel.isLoopSelected()) {
                                        presentationPreview.advanceSlide(true);
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
                        }
                        catch(NumberFormatException ex) {
                            return;
                        }
                        loopTimeline.stop();
                        loopTimeline = new Timeline(
                                new KeyFrame(Duration.seconds(0),
                                        new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent actionEvent) {
                                                if(containerPanel instanceof LivePanel) {
                                                    LivePanel livePanel = ((LivePanel) containerPanel);
                                                    if(livePanel.isLoopSelected()) {
                                                        presentationPreview.advanceSlide(true);
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

    private void drawSlide(PresentationSlide newSlide, DisplayCanvas canvas) {
        Image displayImage = newSlide.getImage();
        ImageDisplayable imageDisplayable = new ImageDisplayable(displayImage);
        drawer.setCanvas(canvas);
        drawer.draw(imageDisplayable);
    }

    public void stopCurrent() {
        if(live && displayable != null) {
            if(displayable.getOOPresentation() != null) {
                displayable.getOOPresentation().stop();
                displayable = null;
            }
        }
    }

    /**
     * If not started already, start the OO presentation.
     */
    private void startOOPres() {
        OOPresentation pres = displayable.getOOPresentation();
        if(pres != null && !pres.isRunning()) {
            pres.start(QueleaProperties.get().getProjectorScreen());
        }
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
    public void showDisplayable(final PresentationDisplayable displayable, final int index) {
        if(this.displayable == displayable) {
            return;
        }
        this.displayable = displayable;
        if(displayable == null) {
            presentationPreview.clear();
            return;
        }
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        presentationPreview.setSlides(slides);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(index < 1) { 
                    presentationPreview.select(1, true);
                }
                else { 
                    presentationPreview.select(index, true);
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
        return presentationPreview.getSelectedIndex();
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
        return presentationPreview.getSelectedIndex();
    }

    @Override
    public void updateCanvas() {
        for(DisplayCanvas canvas : getCanvases()) {
            if(currentSlide != null) {
                drawSlide(currentSlide, canvas);
            }
        }
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        return drawer;
    }

    public void advance() {
        presentationPreview.advanceSlide(false);
    }
    
    public void previous() {
        presentationPreview.previousSlide();
    }

    public void selectLast() {
        presentationPreview.selectLast();
    }
}
