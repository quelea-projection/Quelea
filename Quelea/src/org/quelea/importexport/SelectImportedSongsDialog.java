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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import org.quelea.SongDatabase;

/**
 * A dialog used for selecting the songs to be entered into the database after they've been imported.
 * @author Michael
 */
public class SelectImportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectImportedSongsDialog(JFrame owner) {
        super(owner, new String[]{
                    "The following songs have been imported.",
                    "Select the ones you want to add to the database then hit \"Add\".",
                    "Songs that Quelea thinks are duplicates have been unchecked."
                }, "Add", "Add to database?");

        getAddButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getAddButton().setEnabled(false);
                SwingWorker worker = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() {
                        for (int i = 0; i < getSongs().size(); i++) {
                            if ((Boolean) getTable().getValueAt(i, 2)) {
                                SongDatabase.get().addSong(getSongs().get(i), false);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        SongDatabase.get().fireUpdate();
                        setVisible(false);
                        getAddButton().setEnabled(true);
                    }
                };
                worker.execute();
            }
        });
    }
}
