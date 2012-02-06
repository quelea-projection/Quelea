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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.quelea.Application;
import org.quelea.SongDuplicateChecker;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * An import dialog used for importing songs.
 * @author Michael
 */
public abstract class ImportDialog extends JDialog implements PropertyChangeListener {

    private final JTextField locationField;
    private final JButton importButton;
    private final JCheckBox checkDuplicates;
    private final SelectSongsDialog importedDialog;
    private StatusPanel statusPanel;
    private boolean halt;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new import dialog.
     * @param owner the owner of this dialog.
     * @param dialogLabels the labels to contain on the dialog as text to the 
     * user before the file box.
     * @param fileFilter the filefilter to use in the file dialog, or null if 
     * there should be no file dialog.
     * @param parser the parser to use for this import dialog.
     * @param selectDirectory true if the user should only be allowed to select
     * directories, false otherwise.
     */
    //TODO What's causing this netbeans warning about exporting non-public API type? Don't think we're doing that...
    protected ImportDialog(JFrame owner, String[] dialogLabels, FileFilter fileFilter,
            final SongParser parser, final boolean selectDirectory) {
        super(owner, LabelGrabber.INSTANCE.getLabel("import.heading"), true);
        halt = false;
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

        checkDuplicates = new JCheckBox(LabelGrabber.INSTANCE.getLabel("check.duplicates.text"));
        add(checkDuplicates);

        locationField = new JTextField();
        if (fileFilter != null) {
            locationField.setEditable(false);
            locationField.setFont(new Font(locationField.getFont().getName(), Font.ITALIC, locationField.getFont().getSize()));
            locationField.setText(LabelGrabber.INSTANCE.getLabel("click.select.file.text"));
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
        }

        importButton = new JButton(LabelGrabber.INSTANCE.getLabel("import.button"));
        getRootPane().setDefaultButton(importButton);
        if (fileFilter != null) {
            importButton.setEnabled(false);
        }
        add(importButton);
        importButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusPanel = Application.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("importing.status"));
                statusPanel.getCancelButton().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        statusPanel.done();
                        halt = true;
                    }
                });
                final String location = locationField.getText();
                setActive();
                SwingWorker worker = new SwingWorker() {

                    private List<Song> localSongs;
                    private boolean[] localSongsDuplicate;
                    private ExecutorService checkerService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                    @Override
                    protected Object doInBackground() {
                        try {
//                            localSongsDuplicate = Collections.synchronizedList(new ArrayList<Boolean>(1000));
                            localSongs = parser.getSongs(new File(location));
                            localSongsDuplicate = new boolean[localSongs.size()];
                            if (halt) {
                                localSongs = null;
                                return null;
                            }
                            statusPanel.getProgressBar().setIndeterminate(false);
                            if (checkDuplicates.isSelected()) {
                                for (int i = 0; i < localSongs.size(); i++) {
                                    final int finali = i;
                                    checkerService.submit(Utils.wrapAsLowPriority(new Runnable() {

                                        @Override
                                        public void run() {
                                            if (!halt) {
                                                final boolean result = new SongDuplicateChecker().checkSong(localSongs.get(finali));
                                                localSongsDuplicate[finali] = result;
                                                final int progress = (int) (((double) finali / localSongs.size()) * 100);
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    public void run() {
                                                        if (statusPanel.getProgressBar().getValue() < progress) {
                                                            statusPanel.getProgressBar().setValue(progress);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }));
                                }
                                try {
                                    checkerService.shutdown();
                                    checkerService.awaitTermination(365, TimeUnit.DAYS); //Year eh? ;-)
                                }
                                catch (InterruptedException ex) {
                                }
                            }
                            return localSongs;
                        }
                        catch (IOException ex) {
                            JOptionPane.showMessageDialog(getOwner(), LabelGrabber.INSTANCE.getLabel("import.error.message"), LabelGrabber.INSTANCE.getLabel("error.text"), JOptionPane.ERROR_MESSAGE, null);
                            LOGGER.log(Level.WARNING, "Error importing songs", ex);
                            return null;
                        }
                    }

                    @Override
                    protected void done() {
                        checkerService.shutdownNow();
                        if ((localSongs == null || localSongs.isEmpty()) && !halt) {
                            JOptionPane.showMessageDialog(getOwner(), LabelGrabber.INSTANCE.getLabel("import.no.songs.text") , LabelGrabber.INSTANCE.getLabel("import.no.songs.title"), JOptionPane.WARNING_MESSAGE, null);
                        }
                        else if (!(localSongs == null || localSongs.isEmpty())) {
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
     * Called when the import is taking place, this disables the appropriate controls.
     */
    public void setActive() {
        statusPanel.getProgressBar().setIndeterminate(true);
        setVisible(false);
        resetDialog();
    }

    /**
     * Called when the import has finished taking place, this resets the controls.
     */
    public void setIdle() {
        statusPanel.done();
        halt = false;
        resetDialog();
    }

    private void resetDialog() {
        getLocationField().setText(LabelGrabber.INSTANCE.getLabel("click.select.file.text"));
        getLocationField().setEnabled(true);
        getImportButton().setText(LabelGrabber.INSTANCE.getLabel("import.button"));
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
        if ("progress".equals(strPropertyName) && statusPanel != null) {
            statusPanel.getProgressBar().setIndeterminate(false);
            int progress = (Integer) evt.getNewValue();
            statusPanel.getProgressBar().setValue(progress);
        }
    }
}
