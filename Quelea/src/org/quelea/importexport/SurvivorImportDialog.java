package org.quelea.importexport;

import org.quelea.SongDatabaseChecker;
import org.quelea.displayable.Song;
import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An import dialog for the survivor song books in PDF format.
 * @author Michael
 */
public class SurvivorImportDialog extends ImportDialog {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new survivor import dialog.
     * @param owner the owner of the dialog.
     */
    public SurvivorImportDialog(JFrame owner) {
        super(owner, new String[]{
                "Select the location of the Survivor Songbook PDF below.",
                "<html>This must be the <b>acetates.pdf</b> file, <i>not</i> the guitar chords or the sheet music.</html>"
        }, FileFilters.SURVIVOR_SONGBOOK);
        getImportButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setActive();
                final SurvivorSongbookParser parser = new SurvivorSongbookParser(getLocationField().getText());
                SwingWorker worker = new SwingWorker() {

                    private List<Song> localSongs;
                    private List<Boolean> localSongsDuplicate;

                    @Override
                    protected Object doInBackground() {
                        try {
                            localSongsDuplicate = new ArrayList<Boolean>();
                            localSongs = parser.getSongs();
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
                worker.addPropertyChangeListener(SurvivorImportDialog.this);
                worker.execute();
            }
        });
    }
}
