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
package org.quelea.windows.newsong;

import org.quelea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The colour button where the user selects a colour.
 * @author Michael
 */
public class ColourButton extends JButton {

    private Color colour;
    private final ColourSelectionWindow selectionWindow;

    /**
     * Create and initialise the colour button.
     * @param defaultColor the default colour of the button.
     */
    public ColourButton(final Color defaultColor) {
        super("Choose colour...");
        selectionWindow = new ColourSelectionWindow(SwingUtilities.getWindowAncestor(this));
        this.colour = defaultColor;
        selectionWindow.setSelectedColour(colour);
        setIcon(new ImageIcon(Utils.getImageFromColour(colour, 16, 16)));
        selectionWindow.getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                colour = selectionWindow.getSelectedColour();
                setIcon(new ImageIcon(Utils.getImageFromColour(colour, 16, 16)));
            }
        });
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectionWindow.setLocation((int) getLocationOnScreen().getX(), (int) getLocationOnScreen().getY());
                selectionWindow.setVisible(true);
                selectionWindow.toFront();
            }
        });
    }

    /**
     * Set the icon colour of this button.
     * @param colour the colour of the button's icon.
     */
    public void setIconColour(Color colour) {
        setIcon(new ImageIcon(Utils.getImageFromColour(colour, 16, 16)));
    }

    /**
     * Get the colour selection window.
     * @return the colour selection window.
     */
    public ColourSelectionWindow getColourSelectionWindow() {
        return selectionWindow;
    }

}
