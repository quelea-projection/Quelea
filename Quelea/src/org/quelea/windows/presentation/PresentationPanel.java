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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.powerpoint.OOPresentation;
import org.quelea.data.powerpoint.PresentationSlide;
import org.quelea.data.powerpoint.SlideChangedListener;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.lyrics.DisplayCanvas;
import org.quelea.windows.main.ContainedPanel;
import org.quelea.windows.main.LivePreviewPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel for displaying presentation slides in the live / preview panels.
 * <p/>
 * @author Michael
 */
public class PresentationPanel extends BorderPane implements ContainedPanel {

    private PresentationList presentationList;
    private PresentationDisplayable displayable;
    private boolean live;

    /**
     * Create a new presentation panel.
     * <p/>
     * @param containerPanel the panel to create.
     */
    public PresentationPanel(final LivePreviewPanel containerPanel) {
        presentationList = new PresentationList();
        presentationList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PresentationSlide>() {
            @Override
            public void changed(ObservableValue<? extends PresentationSlide> val, PresentationSlide oldSlide, PresentationSlide newSlide) {
                if(live) {
                    if(newSlide != null && displayable != null) {
                        if(displayable.getOOPresentation() == null) {
                            for(DisplayCanvas canvas : containerPanel.getCanvases()) {
                                canvas.eraseText();
                                Image displayImage = newSlide.getImage();
                                canvas.setTheme(new ThemeDTO(null, null, new ImageBackground(displayImage), null));
                            }
                        }
                        else {
                            OOPresentation pres = displayable.getOOPresentation();
                            pres.addSlideListener(new SlideChangedListener() {
                                @Override
                                public void slideChanged(final int newSlideIndex) {
                                    presentationList.scrollTo(newSlideIndex);
                                }
                            });
                            startOOPres();
                            QueleaApp.get().getMainWindow().toFront();
                            pres.gotoSlide(presentationList.getSelectionModel().getSelectedIndex());
                        }
                    }
                }
            }
        });
        setCenter(presentationList);
    }

    public void stopCurrent() {
        if(live && displayable != null && displayable.getOOPresentation() != null) {
            displayable.getOOPresentation().stop();
            displayable = null;
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
        if(displayable == null) {
            presentationList.itemsProperty().get().clear();
            return;
        }
        this.displayable = displayable;
//        if(live && OOPresentation.isInit()) {
//            for(KeyListener listener : presentationList.getKeyListeners()) {
//                presentationList.removeKeyListener(listener);
//            }
//            presentationList.addKeyListener(new KeyAdapter() {
//
//                @Override
//                public void keyPressed(KeyEvent ke) {
//                    if(ke.getKeyCode() == KeyEvent.VK_RIGHT || ke.getKeyCode() == KeyEvent.VK_SPACE || ke.getKeyCode() == KeyEvent.VK_DOWN) {
//                        displayable.getOOPresentation().goForward();
//                        ke.consume();
//                    }
//                    if(ke.getKeyCode() == KeyEvent.VK_LEFT) {
//                        displayable.getOOPresentation().goBack();
//                    }
//                }
//            });
//        }
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        presentationList.setSlides(slides);
        presentationList.selectionModelProperty().get().select(index);
        if(presentationList.selectionModelProperty().get().isEmpty()) {
            presentationList.selectionModelProperty().get().select(0);
        }
    }

    /**
     * Get the currently selected index on this panel.
     * <p/>
     * @return the currently selected index on this panel.
     */
    public int getIndex() {
        return presentationList.selectionModelProperty().get().getSelectedIndex();
    }

    /**
     * Focus on this panel.
     */
    @Override
    public void focus() {
        presentationList.requestFocus();
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
        return presentationList.getSelectionModel().getSelectedIndex();
    }
}
