package org.quelea.importsong;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * An import dialog used for importing songs from the survivor song book.
 * @author Michael
 */
public class SurvivorImportDialog extends JDialog implements PropertyChangeListener {

    private final JTextField locationField;
    private final JButton importButton;
    private final JProgressBar progressBar;
    private final SelectImportedSongsDialog importedDialog;

    /**
     * Create a new survivor import dialog.
     * @param owner the owner of this dialog.
     */
    public SurvivorImportDialog(JFrame owner) {
        super(owner, "Import", true);
        progressBar = new JProgressBar(0, 100);
        importedDialog = new SelectImportedSongsDialog(owner);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        final JFileChooser locationChooser = new JFileChooser();
        locationChooser.setFileFilter(new FileFilter() {

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
        });
        locationChooser.setAcceptAllFileFilterUsed(false);
        locationChooser.setMultiSelectionEnabled(false);

        add(new JLabel("Select the location of the Survivor Songbook PDF below."));
        add(new JLabel("<html>This must be the <b>acetates.pdf</b> file, <i>not</i> the guitar chords or the sheet music.</html>"));

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
    public SelectImportedSongsDialog getImportedDialog() {
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
     * Update the progress bar.
     * @param evt the property change event.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

}
