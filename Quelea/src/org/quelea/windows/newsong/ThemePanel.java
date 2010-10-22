package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.Utils;
import org.quelea.windows.main.LyricCanvas;

/**
 * The panel where the user chooses what visual theme a song should have.
 * @author Michael
 */
public class ThemePanel extends JPanel {

    public static final String[] SAMPLE_LYRICS = {"Amazing Grace, how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now, I see."};
    private JToolBar fontToolbar;
    private JToolBar backgroundToolbar;
    private JComboBox fontSelection;
    private ColourButton colourButton;
    private JComboBox fontSizeSelection;
    private JToggleButton boldButton;
    private JToggleButton italicButton;
    private LyricCanvas canvas;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        setName("Theme");
        setLayout(new BorderLayout());
        canvas = new LyricCanvas(4, 3);
        canvas.setText(SAMPLE_LYRICS);
        canvas.setSize(100, 100);
        add(canvas, BorderLayout.CENTER);
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        setupFontToolbar();
        toolbarPanel.add(fontToolbar);
        setupBackgroundToolbar();
        toolbarPanel.add(backgroundToolbar);
        add(toolbarPanel, BorderLayout.NORTH);
    }

    /**
     * Setup the background toolbar.
     */
    private void setupBackgroundToolbar() {
        backgroundToolbar = new JToolBar();
        backgroundToolbar.setFloatable(false);
        backgroundToolbar.add(new JLabel("Background:"));
        final JComboBox backgroundTypeSelect = new JComboBox();
        backgroundTypeSelect.addItem("Colour");
        backgroundTypeSelect.addItem("Image");
        backgroundToolbar.add(backgroundTypeSelect);

        final JPanel colourPanel = new JPanel();
        colourPanel.add(new ColourButton(Color.BLACK));
        backgroundToolbar.add(colourPanel);

        final JPanel imagePanel = new JPanel();
        JTextField imageLocation = new JTextField(25);
        imageLocation.setEditable(false);
        imagePanel.add(imageLocation);
        imagePanel.add(new ImageButton(imageLocation, canvas));

        backgroundTypeSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(backgroundTypeSelect.getModel().getSelectedItem().equals("Colour")) {
                    backgroundToolbar.remove(imagePanel);
                    backgroundToolbar.add(colourPanel);
                }
                else if(backgroundTypeSelect.getModel().getSelectedItem().equals("Image")) {
                    backgroundToolbar.remove(colourPanel);
                    backgroundToolbar.add(imagePanel);
                }
                else {
                    throw new AssertionError("Bug - " + backgroundTypeSelect.getModel().getSelectedItem() + " is an unknown selection value");
                }
            }
        });
    }

    /**
     * Setup the font toolbar.
     */
    private void setupFontToolbar() {
        fontToolbar = new JToolBar();
        fontToolbar.setFloatable(false);
        fontToolbar.add(new JLabel("Font:"));
        fontSelection = new JComboBox();
        for(String font : Utils.getAllFonts()) {
            fontSelection.addItem(font);
        }
        fontToolbar.add(fontSelection);
        fontSizeSelection = new JComboBox();
        for(int i = 8; i <= 100; i += 2) {
            fontSizeSelection.addItem(i);
        }
        fontSizeSelection.setSelectedItem(72);
        fontToolbar.add(fontSizeSelection);
        boldButton = new JToggleButton(Utils.getImageIcon("icons/bold.png"));
        fontToolbar.add(boldButton);
        italicButton = new JToggleButton(Utils.getImageIcon("icons/italic.png"));
        italicButton.setBorderPainted(false);
        fontToolbar.add(italicButton);
        colourButton = new ColourButton(Color.WHITE);
        fontToolbar.add(colourButton);

    }
}
