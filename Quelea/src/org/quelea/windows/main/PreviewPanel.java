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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.quelea.displayable.Displayable;
import org.quelea.utils.Utils;

/**
 * The panel displaying the preview lyrics selection - this is viewed before displaying the actual lyrics on the
 * projector.
 */
public class PreviewPanel extends LivePreviewPanel {

    private final JButton liveButton;

    /**
     * Create a new preview lyrics panel.
     */
    public PreviewPanel() {
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Preview</b></html>"));
        header.add(new JToolBar.Separator());
        liveButton = new JButton("Go live", Utils.getImageIcon("icons/2rightarrow.png"));
        liveButton.setToolTipText("Go live (space)");
        liveButton.setRequestFocusEnabled(false);
        header.add(liveButton);
        liveButton.setEnabled(false);
        add(header, BorderLayout.NORTH);
    }

    /**
     * Set the given displayable to be shown on the panel.
     * @param d the displayable to show.
     * @param index an index that may be used or ignored depending on the
     * displayable.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        liveButton.setEnabled(true);
    }

    /**
     * Clear the preview panel.
     */
    @Override
    public void clear() {
        super.clear();
        liveButton.setEnabled(false);
    }

    /**
     * Get the "go live" button.
     * @return the "go live" button.
     */
    public JButton getLiveButton() {
        return liveButton;
    }

}
