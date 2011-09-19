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

import javax.swing.JButton;
import javax.swing.JToolBar;
import org.quelea.utils.Utils;

/**
 * The toolbar displayed on the main window. We don't use this now, have the
 * ribbon instead...
 * @author Michael
 */
public class MainToolbar extends JToolBar {

    private final JButton newButton;
    private final JButton openButton;
    private final JButton saveButton;

    /**
     * Initialise the icons and their actions on the main toolbar.
     */
    public MainToolbar() {
        setFloatable(false);
        newButton = new JButton(Utils.getImageIcon("icons/filenew.png"));
        newButton.setRequestFocusEnabled(false);
        newButton.setToolTipText("New");
        add(newButton);
        openButton = new JButton(Utils.getImageIcon("icons/fileopen.png"));
        newButton.setRequestFocusEnabled(false);
        openButton.setToolTipText("Open...");
        add(openButton);
        saveButton = new JButton(Utils.getImageIcon("icons/filesave.png"));
        newButton.setRequestFocusEnabled(false);
        saveButton.setToolTipText("Save...");
        add(saveButton);
    }

    /**
     * Get the new button on the toolbar.
     * @return the new button.
     */
    public JButton getNewButton() {
        return newButton;
    }

    /**
     * Get the open button on the toolbar.
     * @return the open button.
     */
    public JButton getOpenButton() {
        return openButton;
    }

    /**
     * Get the save button on the toolbar.
     * @return the save button.
     */
    public JButton getSaveButton() {
        return saveButton;
    }

}
