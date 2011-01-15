package org.quelea.importexport;

import org.quelea.SongDatabaseChecker;
import org.quelea.displayable.Song;
import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.SongPack;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
        }, FileFilters.SCHEDULE);
        getImportButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setActive();
                SwingWorker worker = new SwingWorker() {

                    private List<Song> localSongs;
                    private List<Boolean> localSongsDuplicate;

                    @Override
                    protected Object doInBackground() {
                        try {
                            localSongsDuplicate = new ArrayList<Boolean>();
                            localSongs = SongPack.fromFile(new File(getLocationField().getText())).getSongs();
                            for(int i = 0; i < localSongs.size(); i++) {
                                localSongsDuplicate.add(!new SongDatabaseChecker().checkSong(localSongs.get(i)));
                                setProgress((int) (((double) i / localSongs.size()) * 100));
                            }
                            return localSongs;
                        }
                        catch(IOException ex) {
                            JOptionPane.showMessageDialog(getOwner(), "Sorry, there was an error importing the songs.", "Error", JOptionPane.ERROR_MESSAGE, null);
                            LOGGER.log(Level.WARNING, "Error importing songs", ex);
                            return null;
                        }
                    }

                    @Override
                    protected void done() {
                        if(localSongs == null || localSongs.isEmpty()) {
                            JOptionPane.showMessageDialog(getOwner(), "Sorry, couldn't find any songs to import in the given document. "
                                    + "Are you sure it's the right type?", "No songs", JOptionPane.WARNING_MESSAGE, null);
                        }
                        else {
                            getImportedDialog().setSongs(localSongs, localSongsDuplicate, true);
                            getImportedDialog().setLocationRelativeTo(getImportedDialog().getOwner());
                            getImportedDialog().setVisible(true);
                        }
                        setIdle();
                    }
                };
                worker.addPropertyChangeListener(QSPImportDialog.this);
                worker.execute();
            }
        });
    }
}
