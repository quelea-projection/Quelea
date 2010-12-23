package org.quelea.windows.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * A dialog giving some information about Quelea.
 * TODO: A nice logo and better information should go here.
 * @author Michael
 */
public class AboutDialog extends JDialog {

    private final JFrame owner;

    /**
     * Create a new about dialog.
     * @param owner the owner of the dialog (should be the main window.)
     */
    public AboutDialog(JFrame owner) {
        super(owner, "About");
        this.owner = owner;
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        add(new JLabel("<html><h1>Quelea</h1> Version " + QueleaProperties.get().getVersion().getVersionString() + "</html>"));
        add(new JLabel(Utils.getImageIcon("img/logo.png")));
        add(new JLabel(" "));
        add(new JLabel("Quelea is licensed under the GPL (Version 3.)"));
        add(new JLabel("It is, and always will be, free and open source software."));
        add(new JLabel(" "));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(closeButton);
        pack();
    }

    /**
     * When the dialog is made visible, centre it on its owner.
     * @param visible true if the dialog should be made visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(owner);
        }
        super.setVisible(visible);
    }
}
