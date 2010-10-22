package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.quelea.Utils;

/**
 * A colour selection window where the user can select a colour they require.
 * @author Michael
 */
public class ColourSelectionWindow extends JDialog {
    
    private JColorChooser chooser;
    private JButton confirmButton;
    private JButton cancelButton;

    /**
     * Create and initialise the colour selection window.
     */
    public ColourSelectionWindow(Window owner) {
        super(owner, "Colour chooser");
        setLayout(new BorderLayout());
        chooser = new JColorChooser(Color.WHITE);
        add(chooser, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        confirmButton = new JButton("Select Colour", Utils.getImageIcon("icons/tick.png"));
        cancelButton = new JButton("Cancel", Utils.getImageIcon("icons/cross.png"));
        addHideListeners();
        bottomPanel.add(confirmButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Add the listeners to the JButtons that hide the window when the user
     * has selected a colour.
     */
    private void addHideListeners() {
        final ActionListener hideListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        confirmButton.addActionListener(hideListener);
        cancelButton.addActionListener(hideListener);
    }

    /**
     * Get the confirm button on the new song window.
     * @return the confirm button.
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the cancel button on the new song window.
     * @return the cancel button.
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Get the color that the user has selected.
     * @return the selected color.
     */
    public Color getSelectedColour() {
        return chooser.getColor();
    }

}
