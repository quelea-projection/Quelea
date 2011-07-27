package org.quelea.importexport;

import java.awt.Font;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.quelea.SongDatabaseChecker;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;

/**
 * An import dialog used for importing songs.
 * @author Michael
 */
public abstract class ImportDialog extends JDialog implements PropertyChangeListener {

    private final JTextField locationField;
    private final JButton importButton;
    private final JCheckBox checkDuplicates;
    private final JProgressBar progressBar;
    private final SelectSongsDialog importedDialog;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new import dialog.
     * @param owner the owner of this dialog.
     */
    public ImportDialog(JFrame owner, String[] dialogLabels, FileFilter fileFilter,
            final SongParser parser, final boolean selectDirectory) {
        super(owner, "Import", true);
        progressBar = new JProgressBar(0, 100);
        importedDialog = new SelectImportedSongsDialog(owner);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        final JFileChooser locationChooser = new JFileChooser();
        locationChooser.setFileFilter(fileFilter);
        locationChooser.setAcceptAllFileFilterUsed(false);
        locationChooser.setMultiSelectionEnabled(false);
        if (selectDirectory) {
            locationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        for (String str : dialogLabels) {
            add(new JLabel(str));
        }

        checkDuplicates = new JCheckBox("Check for duplicates");
        add(checkDuplicates);

        locationField = new JTextField();
        locationField.setEditable(false);
        locationField.setFont(new Font(locationField.getFont().getName(), Font.ITALIC, locationField.getFont().getSize()));
        locationField.setText("Click here to select file");
        locationField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (locationField.isEnabled()) {
                    locationChooser.showOpenDialog(getRootPane());
                    if (locationChooser.getSelectedFile() != null) {
                        locationField.setFont(new Font(locationField.getFont().getName(), 0, locationField.getFont().getSize()));
                        locationField.setText(locationChooser.getSelectedFile().getAbsolutePath());
                        importButton.setEnabled(true);
                    }
                }
            }
        });
        add(locationField);
        add(progressBar);

        importButton = new JButton("Import");
        getRootPane().setDefaultButton(importButton);
        importButton.setEnabled(false);
        add(importButton);
        importButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setActive();
                SwingWorker worker = new SwingWorker() {

                    private List<Song> localSongs;
                    private List<Boolean> localSongsDuplicate;
                    private ExecutorService checkerService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                    @Override
                    protected Object doInBackground() {
                        try {
                            localSongsDuplicate = Collections.synchronizedList(new ArrayList<Boolean>());
                            localSongs = parser.getSongs(new File(locationField.getText()));
                            getProgressBar().setIndeterminate(false);
                            if (checkDuplicates.isSelected()) {
                                for (int i = 0; i < localSongs.size(); i++) {
                                    final int finali = i;
                                    checkerService.submit(new Runnable() {

                                        @Override
                                        public void run() {
                                            localSongsDuplicate.add(finali, new SongDatabaseChecker().checkSong(localSongs.get(finali)));
                                            System.out.println((int) (((double) finali / localSongs.size()) * 100));
                                            setProgress((int) (((double) finali / localSongs.size()) * 100));
                                        }
                                    });
                                }
                                try {
                                    checkerService.awaitTermination(1, TimeUnit.DAYS);
                                }
                                catch (InterruptedException ex) {
                                }
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
                        checkerService.shutdownNow();
                        if (localSongs == null || localSongs.isEmpty()) {
                            JOptionPane.showMessageDialog(getOwner(), "Sorry, couldn't find any songs to import."
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
                worker.addPropertyChangeListener(ImportDialog.this);
                worker.execute();
            }
        });

        pack();
        setResizable(false);

    }

    /**
     * Get the import button.
     * @return the import button.
     */
    public JButton getImportButton() {
        return importButton;
    }

    /**
     * Get the location field.
     * @return the location field.
     */
    public JTextField getLocationField() {
        return locationField;
    }

    /**
     * Get the dialog that appears after the songs have been imported.
     * @return the imported songs dialog.
     */
    public SelectSongsDialog getImportedDialog() {
        return importedDialog;
    }

    /**
     * Get the progress bar on this dialog.
     * @return the progress bar.
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Called when the import is taking place, this disables the appropriate controls.
     */
    public void setActive() {
        getProgressBar().setIndeterminate(true);
        getImportButton().setEnabled(false);
        checkDuplicates.setEnabled(false);
        getLocationField().setEnabled(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        getImportButton().setText("Importing, please wait...");
    }

    /**
     * Called when the import has finished taking place, this resets the controls.
     */
    public void setIdle() {
        getProgressBar().setIndeterminate(false);
        getProgressBar().setValue(0);
        getLocationField().setText("");
        getLocationField().setEnabled(true);
        checkDuplicates.setEnabled(true);
        getImportButton().setText("Import");
        setVisible(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    /**
     * Update the progress bar.
     * @param evt the property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
}
