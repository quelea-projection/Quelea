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

import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.quelea.displayable.Displayable;

/**
 * The panel displaying the live lyrics selection - changes made on this panel are reflected on the live projection.
 * @author Michael
 */
public class LivePanel extends LivePreviewPanel {

    private final JToggleButton black;
    private final JToggleButton clear;
    private final JToggleButton hide;

    /**
     * Create a new live lyrics panel.
     * @param fullScreenCanvas the full screen canvas that this live window controls.
     */
    public LivePanel() {
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Live</b></html>"));
        header.add(new JToolBar.Separator());
        black = new JToggleButton(Utils.getImageIcon("icons/black.png"));
        black.setToolTipText("Black screen (F1)");
        black.setRequestFocusEnabled(false);
        black.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleBlack();
                }
            }
        });
        header.add(black);
        clear = new JToggleButton(Utils.getImageIcon("icons/clear.png", 16, 16));
        clear.setToolTipText("Clear text (F2)");
        clear.setRequestFocusEnabled(false);
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleClear();
                }
            }
        });
        header.add(clear);
        hide = new JToggleButton(Utils.getImageIcon("icons/cross.png"));
        hide.setToolTipText("Hide display output (F3)");
        hide.setRequestFocusEnabled(false);
        hide.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(QueleaProperties.get().getProjectorScreen() == -1) {
                    return;
                }
                for(LyricWindow window : getWindows()) {
                    window.setVisible(!window.isVisible());
                }
            }
        });
        header.add(hide);
        add(header, BorderLayout.NORTH);

        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                //Nothing needed here
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_F1) {
                    black.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F2) {
                    clear.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F3) {
                    hide.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });
    }

    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        clear.setEnabled(d.supportClear());
    }

    /**
     * Get the "black" toggle button.
     * @return the "black" toggle button.
     */
    public JToggleButton getBlack() {
        return black;
    }

    /**
     * Get the "clear" toggle button.
     * @return the "clear" toggle button.
     */
    public JToggleButton getClear() {
        return clear;
    }

    /**
     * Get the "hide" toggle button.
     * @return the "hide" toggle button.
     */
    public JToggleButton getHide() {
        return hide;
    }

}
