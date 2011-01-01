package org.quelea.windows.options;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * The dialog that holds all the options the user can set.
 * @author Michael
 */
public class OptionsDialog extends JDialog {

    private final JButton okButton;
    private final DisplaySetupPanel displayPanel;
    private final OptionsGeneralPanel generalPanel;
    private final OptionsBiblePanel biblePanel;
    private final JFrame owner;

    /**
     * Create a new options dialog.
     * @param owner the owner of the dialog - should be the main window.
     */
    public OptionsDialog(JFrame owner) {
        super(owner, "Options", true);
        this.owner = owner;
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        generalPanel = new OptionsGeneralPanel();
        tabbedPane.add(generalPanel);
        displayPanel = new DisplaySetupPanel();
        tabbedPane.add(displayPanel);
        biblePanel = new OptionsBiblePanel();
        tabbedPane.add(biblePanel);
        add(tabbedPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setResizable(false);
    }

    /**
     * When the dialog is made visible, centre it on its owner and sync the
     * forms.
     * @param visible true if the dialog should be made visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            setLocationRelativeTo(owner);
            displayPanel.update();
            generalPanel.update();
            biblePanel.update();
        }
        super.setVisible(visible);
    }

    /**
     * Get the general panel used in this options dialog.
     * @return the general panel.
     */
    public OptionsGeneralPanel getGeneralPanel() {
        return generalPanel;
    }

    /**
     * Get the display panel used in this options dialog.
     * @return the display panel.
     */
    public DisplaySetupPanel getDisplayPanel() {
        return displayPanel;
    }

    /**
     * Get the bible panel used in this options dialog.
     * @return the bible panel.
     */
    public OptionsBiblePanel getBiblePanel() {
        return biblePanel;
    }

    /**
     * Get the OK button used to affirm the change in options.
     * @return the OK button.
     */
    public JButton getOKButton() {
        return okButton;
    }

}
