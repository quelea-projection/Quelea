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
package org.quelea.importexport;

import javax.swing.JFrame;
import org.quelea.utils.FileFilters;

/**
 * An import dialog for the survivor song books in PDF format.
 * @author Michael
 */
public class SourceImportDialog extends ImportDialog {

    /**
     * Create a new survivor import dialog.
     * @param owner the owner of the dialog.
     */
    public SourceImportDialog(JFrame owner) {
        super(owner, new String[]{
                "<html>Select the location of the <b>hymns</b> directory on the source CD.</html>"
        }, FileFilters.DIR_ONLY, new SourceParser(), true);
    }
}
