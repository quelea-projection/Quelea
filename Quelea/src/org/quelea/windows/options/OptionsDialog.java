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
 *
 * @author Michael
 */
public class OptionsDialog extends JDialog {

    private final JButton okButton;
    private final DisplaySetupPanel displayPanel;
    private final JFrame owner;

    public OptionsDialog(JFrame owner) {
        super(owner, "Options");
        this.owner = owner;
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        displayPanel = new DisplaySetupPanel();
        tabbedPane.addTab("Display", displayPanel);
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

    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            setLocationRelativeTo(owner);
            displayPanel.syncScreens();
        }
        super.setVisible(visible);
    }

    public DisplaySetupPanel getDisplayPanel() {
        return displayPanel;
    }

    public JButton getOKButton() {
        return okButton;
    }

}
