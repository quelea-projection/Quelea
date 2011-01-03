package org.quelea.importsong;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import org.quelea.SongDatabaseChecker;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;

/**
 * An import dialog for the survivor song books in PDF format.
 * @author Michael
 */
public class SurvivorImportDialog extends ImportDialog {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    /**
     * The file filter used for the import dialog, this always references
     * acetates.pdf since this should always be the name of the file.
     */
    private static final FileFilter FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()
                    || f.getName().trim().equalsIgnoreCase("acetates.pdf")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "acetates.pdf";
        }
    };

    /**
     * Create a new survivor import dialog.
     * @param owner the owner of the dialog.
     */
    public SurvivorImportDialog(JFrame owner) {
        super(owner, new String[]{
                    "Select the location of the Survivor Songbook PDF below.",
                    "<html>This must be the <b>acetates.pdf</b> file, <i>not</i> the guitar chords or the sheet music.</html>"
                }, FILE_FILTER);
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
                            for (int i = 0; i < localSongs.size(); i++) {
                                localSongsDuplicate.add(new SongDatabaseChecker().checkSong(localSongs.get(i)));
                                setProgress((int) (((double) i / localSongs.size()) * 100));
                            }
                            return localSongs;
                        }
                        catch (IOException ex) {
                            JOptionPane.showMessageDialog(getOwner(), "Sorry, there was an error importing the songs.", "Error", JOptionPane.ERROR_MESSAGE, null);
                            LOGGER.log(Level.WARNING, "Error importing songs", ex);
                            return null;
                        }
                    }

                    @Override
                    protected void done() {
                        if (localSongs == null || localSongs.isEmpty()) {
                            JOptionPane.showMessageDialog(getOwner(), "Sorry, couldn't find any songs to import in the given document. "
                                    + "Are you sure it's the right type?", "No songs", JOptionPane.WARNING_MESSAGE, null);
                        }
                        else {
                            getImportedDialog().setSongs(localSongs, localSongsDuplicate);
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
