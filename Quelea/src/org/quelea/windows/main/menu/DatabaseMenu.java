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
package org.quelea.windows.main.menu;

import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.importexport.SelectExportedSongsDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * The database menu.
 * @author Michael
 */
public class DatabaseMenu extends JMenu {

    private final JMenuItem newSong;
    private final JMenuItem exportSongs;

    /**
     * Create a new database menu.
     */
    public DatabaseMenu() {
        super("Database");
        newSong = new JMenuItem("New song...");
        newSong.setMnemonic(KeyEvent.VK_N);
        add(newSong);
        exportSongs = new JMenuItem("Export songs...");
        exportSongs.setMnemonic(KeyEvent.VK_E);
        exportSongs.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(Application.get().getMainWindow());
                dialog.setLocationRelativeTo(dialog.getOwner());
                dialog.setSongs(Arrays.asList(SongDatabase.get().getSongs()), null, false);
                dialog.setVisible(true);
            }
        });
        add(exportSongs);
    }

    /**
     * Get the "export songs" menu item.
     * @return the "export songs" menu item.
     */
    public JMenuItem getExportSongs() {
        return exportSongs;
    }

    /**
     * Get the "new song" menu item.
     * @return the "new song" menu item.
     */
    public JMenuItem getNewSong() {
        return newSong;
    }

}
