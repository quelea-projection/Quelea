package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.Utils;

/**
 * The panel where the user chooses what visual theme a song should have.
 * @author Michael
 */
public class ThemePanel extends JPanel {

    private JToolBar fontToolbar;
    private JComboBox fontSelection;
    private ColourButton colourButton;
    private JComboBox fontSizeSelection;
    private JToggleButton boldButton;
    private JToggleButton italicButton;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel(ColourSelectionWindow selectionWindow) {
        setName("Theme");
        setLayout(new BorderLayout());
        setupToolbar(selectionWindow);
        add(fontToolbar, BorderLayout.NORTH);
    }

    /**
     * Setup the font toolbar.
     */
    private void setupToolbar(ColourSelectionWindow selectionWindow) {
        fontToolbar = new JToolBar();
        fontToolbar.setFloatable(false);
        fontToolbar.add(new JLabel("Font:"));
        fontSelection = new JComboBox();
        for(String font : Utils.getAllFonts()) {
            fontSelection.addItem(font);
        }
        fontToolbar.add(fontSelection);
        fontSizeSelection = new JComboBox();
        for(int i=8 ; i<=100 ; i+=2) {
            fontSizeSelection.addItem(i);
        }
        fontSizeSelection.setSelectedItem(72);
        fontToolbar.add(fontSizeSelection);
        boldButton = new JToggleButton(Utils.getImageIcon("icons/bold.png"));
        fontToolbar.add(boldButton);
        italicButton = new JToggleButton(Utils.getImageIcon("icons/italic.png"));
        italicButton.setBorderPainted(false);
        fontToolbar.add(italicButton);
        colourButton = new ColourButton(Color.BLACK, selectionWindow);
        fontToolbar.add(colourButton);

    }

}
