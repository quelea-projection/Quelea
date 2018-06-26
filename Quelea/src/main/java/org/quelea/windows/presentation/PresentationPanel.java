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
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.powerpoint.OOPresentation;
import org.quelea.data.powerpoint.PresentationSlide;
import org.quelea.services.languages.LabelGrabber;
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
    private PresentationControls controlPanel;
    private Timeline loopTimeline;
    private final boolean usePowerPoint;

    /**
     * Create a new presentation panel.
     * <p/>
     * @param containerPanel the panel to create.
     */
    public PresentationPanel(final LivePreviewPanel containerPanel) {
        usePowerPoint = QueleaProperties.get().getUsePP();
        if (usePowerPoint) {
            this.controlPanel = new PresentationControls();
            drawer = new PresentationDrawer(controlPanel);
            BorderPane.setMargin(controlPanel, new Insets(30));
            setBottom(controlPanel);
            setMinWidth(50);
            setMinHeight(50);
        }
        this.containerPanel = containerPanel;
        BorderPane mainPanel = new BorderPane();
        presentationPreview = new PresentationPreview();
        presentationPreview.addSlideChangedListener(new org.quelea.windows.presentation.SlideChangedListener() {
            @Override
            public void slideChanged(PresentationSlide newSlide) {
                if (live) {
                    LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                    if (lp.getDisplayable() instanceof PresentationDisplayable) {
                        if (!PowerPointHandler.getCurrentSlide().equals(String.valueOf(presentationPreview.getSelectedIndex()))) {
                            PowerPointHandler.gotoSlide(presentationPreview.getSelectedIndex());
                        }
                    }
                    if (newSlide != null && displayable != null) {
                        if (displayable.getOOPresentation() == null) {
                            currentSlide = newSlide;
                            updateCanvas();
                        } else {
                            OOPresentation pres = displayable.getOOPresentation();
                            pres.setSlideListener((final int newSlideIndex) -> {
                                presentationPreview.select(newSlideIndex + 1);
                            });
                            currentSlide = newSlide;
                            startOOPres();
                            QueleaApp.get().getMainWindow().toFront();
                            pres.gotoSlide(presentationPreview.getSelectedIndex() - 1);
                        }
                    }
                    if (lp.getBlacked() && !PowerPointHandler.screenStatus().equals("3")) {
                        lp.setBlacked(false);
                    }
                }
            }
        });
        presentationPreview.select(0);

        presentationPreview.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode().equals(KeyCode.PAGE_DOWN) || t.getCode().equals(KeyCode.DOWN) || t.getCode().equals(KeyCode.RIGHT)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
                } else if (t.getCode().equals(KeyCode.PAGE_UP) || t.getCode().equals(KeyCode.UP) || t.getCode().equals(KeyCode.LEFT)) {
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
                        if (containerPanel instanceof LivePanel) {
                            LivePanel livePanel = ((LivePanel) containerPanel);
                            if (livePanel.isLoopSelected()) {
                                if (QueleaProperties.get().getUsePP() && livePanel.getDisplayable() instanceof PresentationDisplayable) {
                                    String result = PowerPointHandler.gotoNext();
                                    if (result.contains("not running")) {
                                        Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("set.loop.manually.title"), LabelGrabber.INSTANCE.getLabel("set.loop.manually.message"));
                                        livePanel.stopLoop();
                                    }
                                } else if (livePanel.getDisplayable() instanceof PresentationDisplayable) {
                                    presentationPreview.advanceSlide(true);
                                } else {
                                    if (livePanel.getIndex() != livePanel.getLenght()) {
                                        livePanel.advance();
                                    } else {
                                        livePanel.selectFirstLyric();
                                    }
                                }
                                LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                                if (lp.getDisplayable() instanceof PresentationDisplayable) {
                                    String result = PowerPointHandler.getCurrentSlide();
                                    if (!result.contains("not running") && !result.equals("")) {
                                        int i = Integer.parseInt(result);
                                        presentationPreview.select(i, false);
                                    }
                                }
                                if (lp.getDisplayable() instanceof PresentationDisplayable && QueleaProperties.get().getUsePP() && !QueleaProperties.get().getPPPath().contains("PPTVIEW") && lp.getBlacked() && !PowerPointHandler.screenStatus().equals("3")) {
                                    lp.setBlacked(false);
                                }
                            }
                        }
                    }
                }
                ),
                new KeyFrame(Duration.seconds(10))
        );
        loopTimeline.setCycleCount(Animation.INDEFINITE);

        loopTimeline.play();

        QueleaApp.get()
                .doOnLoad(new Runnable() {

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
                                                        if (QueleaProperties.get().getUsePP() && livePanel.getDisplayable() instanceof PresentationDisplayable) {
                                                            String result = PowerPointHandler.gotoNext();
                                                            if (result.contains("not running")) {
                                                                Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("set.loop.manually.title"), LabelGrabber.INSTANCE.getLabel("set.loop.manually.message"));
                                                                livePanel.stopLoop();
                                                            }
                                                        } else if (livePanel.getDisplayable() instanceof PresentationDisplayable) {
                                                            presentationPreview.advanceSlide(true);
                                                        } else {
                                                            if (livePanel.getIndex() != livePanel.getLenght()) {
                                                                livePanel.advance();
                                                            } else {
                                                                livePanel.selectFirstLyric();
                                                            }
                                                        }
                                                        LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                                                        if (lp.getDisplayable() instanceof PresentationDisplayable) {
                                                            String result = PowerPointHandler.getCurrentSlide();
                                                            if (!result.contains("not running") && !result.equals("")) {
                                                                int i = Integer.parseInt(result);
                                                                presentationPreview.select(i, false);
                                                            }
                                                        }
                                                        if (lp.getDisplayable() instanceof PresentationDisplayable && QueleaProperties.get().getUsePP() && !QueleaProperties.get().getPPPath().contains("PPTVIEW") && lp.getBlacked() && !PowerPointHandler.screenStatus().equals("3")) {
                                                            lp.setBlacked(false);
                                                        }
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
                }
                );
    }

    private void drawSlide(PresentationSlide newSlide, DisplayCanvas canvas) {
        Image displayImage = newSlide.getImage();
        ImageDisplayable imageDisplayable = new ImageDisplayable(displayImage);
        drawer.setCanvas(canvas);
        drawer.draw(imageDisplayable);
    }

    public void stopCurrent() {
        if (live && displayable != null) {
            if (displayable.getOOPresentation() != null) {
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
        if (pres != null && !pres.isRunning()) {
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
        if (this.displayable == displayable) {
            return;
        }
        this.displayable = displayable;
        if (displayable == null) {
            presentationPreview.clear();
            return;
        }
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        presentationPreview.setSlides(slides);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (index < 1) {
                    presentationPreview.select(1, true);
                } else {
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
     * Get the length of the item on this panel.
     * <p/>
     * @return the length of the item on this panel.
     */
    public int getSlideCount() {
        return presentationPreview.getSlideCount();
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
        if (QueleaProperties.get().getUsePP()) {
            PowerPointHandler.gotoNext();
            String result = PowerPointHandler.getCurrentSlide();
            if (!result.contains("not running") && !result.equals("")) {
                int i = Integer.parseInt(result);
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getPresentationPanel().getPresentationPreview().select(i, true);
            }
        } else if (displayable.getOOPresentation() != null) {
            displayable.getOOPresentation().goForward();
        } else {
            presentationPreview.advanceSlide(false);
        }
    }

    public void previous() {
        if (QueleaProperties.get().getUsePP()) {
            PowerPointHandler.gotoPrevious();
            String result = PowerPointHandler.getCurrentSlide();
            if (!result.contains("not running") && !result.equals("")) {
                int i = Integer.parseInt(result);
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getPresentationPanel().getPresentationPreview().select(i, true);
            }
        } else if (displayable.getOOPresentation() != null) {
            displayable.getOOPresentation().goBack();
        }else {
            presentationPreview.previousSlide();
        }
    }

    public void selectLast() {
        presentationPreview.selectLast();
    }

    public PresentationPreview getPresentationPreview() {
        return presentationPreview;
    }
}
