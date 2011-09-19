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

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

/**
 * A chooser that sets the location to the absolute location of the parent.
 * @author Michael
 */
public class JLocationFileChooser extends JFileChooser {

    /**
     * Create a file chooser pointing to the default location.
     */
    public JLocationFileChooser() {
        super();
    }

    /**
     * Create the file chooser pointed to a specified folder.
     * @param folder the folder to point to.
     */
    public JLocationFileChooser(String folder) {
        super(folder);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setLocation((int) parent.getLocationOnScreen().getX(), (int) parent.getLocationOnScreen().getY());
        return dialog;
    }
}
