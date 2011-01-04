package org.quelea.importexport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import org.quelea.SongDatabase;

/**
 * A dialog used for selecting the songs to be entered into the database after
 * they've been imported.
 * @author Michael
 */
public class SelectImportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectImportedSongsDialog(JFrame owner) {
        super(owner, new String[] {
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
