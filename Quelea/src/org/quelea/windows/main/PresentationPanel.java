/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.Application;
import org.quelea.ImageBackground;
import org.quelea.Theme;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.powerpoint.OOPresentation;
import org.quelea.powerpoint.PresentationSlide;
import org.quelea.powerpoint.SlideChangedListener;
import org.quelea.utils.QueleaProperties;

/**
 * The panel for displaying presentation slides in the live / preview panels.
 *
 * @author Michael
 */
public class PresentationPanel extends ContainedPanel {

    private PresentationList presentationList;
    private PresentationDisplayable displayable;
    private boolean live;

    /**
     * Create a new presentation panel.
     *
     * @param containerPanel the panel to create.
     */
    public PresentationPanel(final LivePreviewPanel containerPanel) {
        setLayout(new BorderLayout());
        presentationList = new PresentationList();
        presentationList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(live) {
                    if(!presentationList.getValueIsAdjusting() && !presentationList.isUpdating()) {
                        if(displayable != null && displayable.getOOPresentation() == null) {
                            HashSet<LyricCanvas> canvases = new HashSet<>();
                            canvases.addAll(containerPanel.getCanvases());
                            for(LyricCanvas lc : canvases) {
                                lc.eraseText();
                                BufferedImage displayImage = presentationList.getCurrentImage(lc.getWidth(), lc.getHeight());
                                lc.setTheme(new Theme(null, null, new ImageBackground(null, displayImage)));
                            }
                        }
                        else if(displayable != null) {
                            OOPresentation pres = displayable.getOOPresentation();
                            pres.addSlideListener(new SlideChangedListener() {

                                @Override
                                public void slideChanged(final int newSlideIndex) {
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            presentationList.setUpdating(true);
                                            presentationList.ensureIndexIsVisible(newSlideIndex);
                                            presentationList.setSelectedIndex(newSlideIndex);
                                            presentationList.setUpdating(false);
                                        }
                                    });
                                }
                            });
                            startOOPres();
                            java.awt.EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    Application.get().getMainWindow().toFront();
                                    Application.get().getMainWindow().repaint();
                                }
                            });
                            pres.gotoSlide(presentationList.getSelectedIndex());
                        }
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(presentationList);
        add(scroll, BorderLayout.CENTER);
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
     *
     * @param displayable the presentation displayable to display.
     * @param index the index to display.
     */
    public void setDisplayable(final PresentationDisplayable displayable, int index) {
        DefaultListModel<PresentationSlide> model = (DefaultListModel<PresentationSlide>) presentationList.getModel();
        if(displayable == null) {
            model.clear();
            return;
        }
        this.displayable = displayable;
        if(live && OOPresentation.isInit()) {
            for(KeyListener listener : presentationList.getKeyListeners()) {
                presentationList.removeKeyListener(listener);
            }
            presentationList.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent ke) {
                    if(ke.getKeyCode() == KeyEvent.VK_RIGHT || ke.getKeyCode() == KeyEvent.VK_SPACE || ke.getKeyCode() == KeyEvent.VK_DOWN) {
                        displayable.getOOPresentation().goForward();
                        ke.consume();
                    }
                    if(ke.getKeyCode() == KeyEvent.VK_LEFT) {
                        displayable.getOOPresentation().goBack();
                    }
                }
            });
        }
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        model.clear();
        for(PresentationSlide slide : slides) {
            model.addElement(slide);
        }
        presentationList.setSelectedIndex(index);
        if(presentationList.getSelectedIndex() == -1) {
            presentationList.setSelectedIndex(0);
        }
        presentationList.ensureIndexIsVisible(presentationList.getSelectedIndex());
    }

    /**
     * Get the currently selected index on this panel.
     *
     * @return the currently selected index on this panel.
     */
    public int getIndex() {
        return presentationList.getSelectedIndex();
    }

    /**
     * Add a key listener to this presentation panel.
     *
     * @param l the key listener to add.
     */
    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        presentationList.addKeyListener(l);
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
}
