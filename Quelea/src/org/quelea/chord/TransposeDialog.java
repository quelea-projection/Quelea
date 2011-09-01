package org.quelea.chord;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.quelea.Application;

/**
 * The dialog shown to the user when choosing how to transpose the chords of 
 * a song.
 * @author Michael
 */
public class TransposeDialog extends JDialog {

    private JComboBox<String> keySelection;
    private int semitones = 0;

    /**
     * Create a new transpose dialog.
     */
    public TransposeDialog() {
        super(Application.get().getMainWindow(), "Transpose song chords", true);
        keySelection = new JComboBox<>();
        keySelection.setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Select the amount of semitones you want to transpose up or down:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(5));
        add(keySelection);
        add(Box.createVerticalStrut(5));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton okButton = new JButton("Transpose");
        okButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                semitones = keySelection.getSelectedIndex() - 4;
                if (semitones <= 0) {
                    semitones--;
                }
                setVisible(false);
            }
        });
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(cancelButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(buttonPanel);
        getRootPane().setDefaultButton(okButton);
        pack();
    }

    /**
     * Set the root key of the dialog, adjusting the options accordingly.
     * @param key the key the song is currently in.
     */
    public void setKey(String key) {
        DefaultComboBoxModel<String> model = ((DefaultComboBoxModel<String>) keySelection.getModel());
        model.removeAllElements();
        for (int i = -5; i < 7; i++) {
            if (i == 0) {
                continue;
            }
            String transKey = new ChordTransposer(key).transpose(i, null);
            String istr = Integer.toString(i);
            if (i > 0) {
                istr = "+" + istr;
            }
            model.addElement(transKey + " (" + istr + ")");
        }
        
        keySelection.setSelectedIndex(5);
        keySelection.setMaximumSize(keySelection.getPreferredSize());
    }

    /**
     * Get the amount of semitones the user has selected to transpose by.
     * @return the amount of semitones to transpose by.
     */
    public int getSemitones() {
        return semitones;
    }

    /**
     * If setting the dialog to visible, reset it and position it appropriately.
     * @param visible true if visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            semitones = 0;
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }
}
