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
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.powerpoint.PresentationSlide;

/**
 * The panel for displaying powerpoint slides in the live / preview panels.
 *
 * @author Michael
 */
public class PowerpointPanel extends ContainedPanel {

    private PowerpointList powerpointList;

    /**
     * Create a new powerpoint panel.
     *
     * @param containerPanel the panel to create.
     */
    public PowerpointPanel(final LivePreviewPanel containerPanel) {
        setLayout(new BorderLayout());
        powerpointList = new PowerpointList();
        powerpointList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!powerpointList.getValueIsAdjusting()) {
                    HashSet<LyricCanvas> canvases = new HashSet<>();
                    canvases.addAll(containerPanel.getCanvases());
                    for(LyricCanvas lc : canvases) {
                        lc.eraseText();
                        BufferedImage displayImage = powerpointList.getCurrentImage(lc.getWidth(), lc.getHeight());
                        lc.setTheme(new Theme(null, null, new Background(null, displayImage)));
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(powerpointList);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Set the displayable to be on this powerpoint panel.
     *
     * @param displayable the presentation displayable to display.
     * @param index the index to display.
     */
    public void setDisplayable(PresentationDisplayable displayable, int index) {
        DefaultListModel<PresentationSlide> model = (DefaultListModel<PresentationSlide>) powerpointList.getModel();
        if(displayable == null) {
            model.clear();
            return;
        }
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        model.clear();
        for(PresentationSlide slide : slides) {
            model.addElement(slide);
        }
        powerpointList.setSelectedIndex(index);
        if(powerpointList.getSelectedIndex() == -1) {
            powerpointList.setSelectedIndex(0);
        }
        powerpointList.ensureIndexIsVisible(powerpointList.getSelectedIndex());
    }

    /**
     * Get the currently selected index on this panel.
     *
     * @return the currently selected index on this panel.
     */
    public int getIndex() {
        return powerpointList.getSelectedIndex();
    }

    /**
     * Add a key listener to this powerpoint panel.
     *
     * @param l the key listener to add.
     */
    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        powerpointList.addKeyListener(l);
    }

    /**
     * Focus on this panel.
     */
    @Override
    public void focus() {
        powerpointList.requestFocus();
    }

    /**
     * Clear this panel (well, actually don't do anything because we can't clear
     * a presentation.)
     */
    @Override
    public void clear() {
        //Doesn't really apply
    }
//    public static void main(String[] args) {
//        final PowerpointPanel panel = new PowerpointPanel(null);
//        final PresentationDisplayable presentation = new PresentationDisplayable(new File("C:\\java.ppt"));
//        SwingUtilities.invokeLater(new Runnable() {
//
//            public void run() {
//                JFrame frame = new JFrame();
//                frame.setLayout(new BorderLayout());
//                frame.add(panel, BorderLayout.CENTER);
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.pack();
//                frame.setVisible(true);
//                panel.setDisplayable(presentation);
//            }
//        });
//    }
}
