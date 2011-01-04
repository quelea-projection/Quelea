package org.quelea.importexport;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
 * An import dialog used for importing songs.
 * @author Michael
 */
public abstract class ImportDialog extends JDialog implements PropertyChangeListener {

    private final JTextField locationField;
    private final JButton importButton;
    private final JProgressBar progressBar;
    private final SelectSongsDialog importedDialog;

    /**
     * Create a new import dialog.
     * @param owner the owner of this dialog.
     */
    public ImportDialog(JFrame owner, String[] text, FileFilter fileFilter) {
        super(owner, "Import", true);
        progressBar = new JProgressBar(0, 100);
        importedDialog = new SelectImportedSongsDialog(owner);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        final JFileChooser locationChooser = new JFileChooser();
        locationChooser.setFileFilter(fileFilter);
        locationChooser.setAcceptAllFileFilterUsed(false);
        locationChooser.setMultiSelectionEnabled(false);

        for(String str : text) {
            add(new JLabel(str));
        }

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
     * Called when the import is taking place, this disables the appropriate
     * controls.
     */
    public void setActive() {
        getProgressBar().setIndeterminate(true);
        getImportButton().setEnabled(false);
        getLocationField().setEnabled(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        getImportButton().setText("Importing, please wait...");
    }

    /**
     * Called when the import has finished taking place, this resets the
     * controls.
     */
    public void setIdle() {
        getProgressBar().setIndeterminate(false);
        getProgressBar().setValue(0);
        getLocationField().setText("");
        getLocationField().setEnabled(true);
        getImportButton().setText("Import");
        setVisible(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
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
