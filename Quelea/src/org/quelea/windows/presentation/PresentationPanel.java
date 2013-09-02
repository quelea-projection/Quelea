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

import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.powerpoint.OOPresentation;
import org.quelea.data.powerpoint.PresentationSlide;
import org.quelea.data.powerpoint.SlideChangedListener;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.image.ImageDrawer;
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

    /**
     * Create a new presentation panel.
     * <p/>
     * @param containerPanel the panel to create.
     */
    public PresentationPanel(final LivePreviewPanel containerPanel) {
        this.containerPanel = containerPanel;
        BorderPane mainPanel = new BorderPane();
        HBox buttons = new HBox(5);
//        buttons.getChildren().add(new Button("Previous"));
//        buttons.getChildren().add(new Button("Next"));
        mainPanel.setTop(buttons);
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

        mainPanel.setCenter(presentationPreview);
        setCenter(mainPanel);
    }

    private void drawSlide(PresentationSlide newSlide, DisplayCanvas canvas) {
        Image displayImage = newSlide.getImage();
        ImageDisplayable displayable = new ImageDisplayable(displayImage);
        drawer.setCanvas(canvas);
        drawer.draw(displayable);
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
    public void showDisplayable(final PresentationDisplayable displayable, int index) {
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
        presentationPreview.select(index);
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
    public void clear() {
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
}
