package org.quelea.windows.newsong;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * The colour button where the user selects a colour.
 * @author Michael
 */
public class ColourButton extends JButton {

    private ColorSelectionWindow selection;
    private Color color;

    /**
     * Create and initialise the colour button.
     * @param defaultColor the default colour of the button.
     */
    public ColourButton(Color defaultColor) {
        super("Colour...");
        this.color = defaultColor;
        setBackground(color);
        selection = new ColorSelectionWindow();
        selection.getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                color = selection.getSelectedColour();
                setBackground(color);
            }
        });
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selection.setLocation(getX(), getY());
                selection.setVisible(true);
            }
        });
    }

    /**
     * Get the colour that the user has selected.
     * @return the user selected colour.
     */
    public Color getColour() {
        return color;
    }

}
