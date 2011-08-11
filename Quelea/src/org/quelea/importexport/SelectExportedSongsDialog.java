package org.quelea.importexport;

import org.quelea.Application;
import org.quelea.utils.FileFilters;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SongPack;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A dialog used for selecting the songs to be put in the song pack after export.
 * @author Michael
 */
public class SelectExportedSongsDialog extends SelectSongsDialog {

    /**
     * Create a new exported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectExportedSongsDialog(final JFrame owner) {
        super(owner, new String[]{
                "The following songs are in the database.",
                "Select the ones you want to add to the song pack then hit \"Add\"."
        }, "Add", "Add to song pack?");

        getAddButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String extension = QueleaProperties.get().getSongPackExtension();
                JFileChooser chooser = getChooser();
                int chooserResult = chooser.showSaveDialog(owner);
                if(chooserResult == JFileChooser.APPROVE_OPTION) {
                    final File file;
                    if(chooser.getSelectedFile().getName().endsWith("." + extension)) {
                        file = chooser.getSelectedFile();
                    }
                    else {
                        file = new File(chooser.getSelectedFile().getAbsoluteFile() + "." + extension);
                    }

                    boolean writeFile = true;
                    if(file.exists()) {
                        int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), file.getName() + " already exists. Overwrite?",
                                "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
                        if(result != JOptionPane.YES_OPTION) {
                            writeFile = false;
                        }
                    }
                    if(writeFile) {
                        writeSongPack(file);
                    }

                }
            }

        });
    }

    /**
     * Get the JFileChooser to be used.
     * @return the song pack JFileChooser.
     */
    private JFileChooser getChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(FileFilters.SCHEDULE);
        return chooser;
    }

    /**
     * Write the song pack to the specified file, closing the window when done.
     * @param file the file to write the song pack to.
     */
    private void writeSongPack(final File file) {
        final SongPack pack = new SongPack();
        getAddButton().setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                for(int i = 0; i < getSongs().size(); i++) {
                    if((Boolean) getTable().getValueAt(i, 2)) {
                        pack.addSong(getSongs().get(i));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                pack.writeToFile(file);
                setVisible(false);
                getAddButton().setEnabled(true);
            }
        };
        worker.execute();
    }
}
