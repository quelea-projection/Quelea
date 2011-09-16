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

import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * An import dialog for Quelea song packs.
 * @author Michael
 */
public class QSPImportDialog extends ImportDialog {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new SCHEDULE import dialog.
     * @param owner the owner of the dialog.
     */
    public QSPImportDialog(JFrame owner) {
        super(owner, new String[]{
                "Select the location of the Quelea songpack below."
        }, FileFilters.SCHEDULE, new QSPParser(), false);
    }
}
