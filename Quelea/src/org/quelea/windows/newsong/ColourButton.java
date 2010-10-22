package org.quelea.windows.newsong;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.quelea.Utils;

/**
 * The colour button where the user selects a colour.
 * @author Michael
 */
public class ColourButton extends JButton {

    private Color color;
    private ColourSelectionWindow selectionWindow;

    /**
     * Create and initialise the colour button.
     * @param defaultColor the default colour of the button.
     */
    public ColourButton(final Color defaultColor) {
        super("Choose colour...");
        selectionWindow = new ColourSelectionWindow(SwingUtilities.getWindowAncestor(this));
        this.color = defaultColor;
        setIcon(new ImageIcon(Utils.getImageFromColour(Color.BLACK, 16, 16)));
        selectionWindow.getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setIcon(new ImageIcon(Utils.getImageFromColour(selectionWindow.getSelectedColour(), 16, 16)));
            }
        });
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectionWindow.setLocation((int)getLocationOnScreen().getX(), (int)getLocationOnScreen().getY());
                selectionWindow.setVisible(true);
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
