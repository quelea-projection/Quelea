/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.main.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.importexport.SelectExportedSongsDialog;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * Quelea's export menu.
 * @author Michael
 */
public class ExportMenu extends JMenu {
    
    private JMenuItem qspItem;
    
    /**
     * Create the export menu.
     */
    public ExportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("export.heading"));
        setIcon(Utils.getImageIcon("icons/right.png", 16, 16));
        setMnemonic('x');
        
        qspItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), Utils.getImageIcon("icons/logo.png", 16, 16));
        qspItem.setMnemonic('q');
        qspItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(Application.get().getMainWindow());
                dialog.setLocationRelativeTo(dialog.getOwner());
                dialog.setSongs(Arrays.asList(SongDatabase.get().getSongs()), null, false);
                dialog.setVisible(true);
            }
        });
        add(qspItem);
    }
    
}
