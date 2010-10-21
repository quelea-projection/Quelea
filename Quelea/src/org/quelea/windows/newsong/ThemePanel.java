package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.quelea.Utils;

/**
 * The panel where the user chooses what visual theme a song should have.
 * @author Michael
 */
public class ThemePanel extends JPanel {

    private JToolBar themeToolbar;
    private JComboBox fontSelection;
    private ColourButton colourButton;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        setName("Theme");
        setLayout(new BorderLayout());
        setupToolbar();
        add(themeToolbar, BorderLayout.NORTH);
    }

    /**
     * Setup the toolbar
     */
    private void setupToolbar() {
        themeToolbar = new JToolBar();
        themeToolbar.setFloatable(false);
        fontSelection = new JComboBox();
        for(String font : Utils.getAllFonts()) {
            fontSelection.addItem(font);
        }
        themeToolbar.add(fontSelection);
        colourButton = new ColourButton(Color.BLACK);
        themeToolbar.add(colourButton);

    }

}
