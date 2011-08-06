package org.quelea.windows.newsong;

import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * The panel where the user chooses what visual theme a song should have.
 * @author Michael
 */
public class ThemePanel extends JPanel {

    private static final String[] SAMPLE_LYRICS = {"Amazing Grace! how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private JToolBar fontToolbar;
    private JToolBar backgroundToolbar;
    private JComboBox fontSelection;
    private ColourButton fontColourButton;
    private ColourButton backgroundColourButton;
    private JComboBox backgroundTypeSelect;
    private JTextField backgroundImageLocation;
    private JComboBox fontSizeSelection;
    private JToggleButton boldButton;
    private JToggleButton italicButton;
    private final LyricCanvas canvas;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        setName("Theme");
        setLayout(new BorderLayout());
        canvas = new LyricCanvas(4, 3);
        canvas.setText(SAMPLE_LYRICS, null);
        add(canvas, BorderLayout.CENTER);
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new GridLayout(2, 1));
        setupFontToolbar();
        toolbarPanel.add(fontToolbar);
        setupBackgroundToolbar();
        toolbarPanel.add(backgroundToolbar);
        backgroundToolbar.setMaximumSize(new Dimension(1000, 10));
        add(toolbarPanel, BorderLayout.NORTH);
    }

    /**
     * Setup the background toolbar.
     */
    private void setupBackgroundToolbar() {
        backgroundToolbar = new JToolBar();
        backgroundToolbar.setFloatable(false);
        backgroundToolbar.add(new JLabel("Background:"));
        backgroundTypeSelect = new JComboBox();
        backgroundTypeSelect.addItem("Colour");
        backgroundTypeSelect.addItem("Image");
        backgroundTypeSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme();
            }
        });
        backgroundToolbar.add(backgroundTypeSelect);

        final JPanel colourPanel = new JPanel();

        backgroundColourButton = new ColourButton(Color.BLACK);
        colourPanel.add(backgroundColourButton);
        backgroundColourButton.getColourSelectionWindow().getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme();
            }
        });
        backgroundToolbar.add(colourPanel);

        final JPanel imagePanel = new JPanel();
        backgroundImageLocation = new JTextField(25);
        backgroundImageLocation.setEditable(false);
        imagePanel.add(backgroundImageLocation);
        imagePanel.add(new ImageButton(backgroundImageLocation, canvas));

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
        fontSelection.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                updateTheme();
            }
        });
        fontToolbar.add(fontSelection);
        fontSizeSelection = new JComboBox();
        for(int i = 8; i <= 100; i += 2) {
            fontSizeSelection.addItem(i);
        }
        fontSizeSelection.setSelectedItem(72);
        fontSizeSelection.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                updateTheme();
            }
        });
        fontToolbar.add(fontSizeSelection);
        boldButton = new JToggleButton(Utils.getImageIcon("icons/bold.png"));
        boldButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme();
            }
        });
        fontToolbar.add(boldButton);
        italicButton = new JToggleButton(Utils.getImageIcon("icons/italic.png"));
        italicButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme();
            }
        });
        fontToolbar.add(italicButton);
        fontColourButton = new ColourButton(Color.WHITE);
        fontColourButton.getColourSelectionWindow().getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme();
            }
        });
        fontToolbar.add(fontColourButton);

    }

    /**
     * Update the canvas with the current theme.
     */
    private void updateTheme() {
        canvas.setTheme(getTheme());
    }

    /**
     * Set the current theme to represent in this panel.
     * @param theme the theme to represent.
     */
    public void setTheme(Theme theme) {
        if(theme == null) {
            theme = Theme.DEFAULT_THEME;
        }
        Font font = theme.getFont();
        if(font.isBold()) {
            boldButton.setSelected(true);
        }
        if(font.isItalic()) {
            italicButton.setSelected(true);
        }
        int fontSize = font.getSize();
        if(fontSize % 2 != 0) {
            fontSize--;
        }
        fontSizeSelection.setSelectedItem(fontSize);
        fontColourButton.getColourSelectionWindow().setSelectedColour(theme.getFontColor());
        fontColourButton.setIconColour(theme.getFontColor());
        Background background = theme.getBackground();
        if(background.isColour()) {
            backgroundTypeSelect.getModel().setSelectedItem("Colour");
            backgroundColourButton.getColourSelectionWindow().setSelectedColour(background.getColour());
            backgroundColourButton.setIconColour(background.getColour());
        }
        else {
            backgroundTypeSelect.getModel().setSelectedItem("Image");
            backgroundImageLocation.setText(background.getImageLocation());
        }
        updateTheme();
    }

    /**
     * Get the theme currently represented by the state of this panel.
     * @return the current theme.
     */
    public Theme getTheme() {
        int fontStyle = 0;
        if(boldButton.isSelected()) {
            fontStyle |= Font.BOLD;
        }
        if(italicButton.isSelected()) {
            fontStyle |= Font.ITALIC;
        }
        Font font = new Font(fontSelection.getSelectedItem().toString(), fontStyle, Integer.parseInt(fontSizeSelection.getSelectedItem().toString()));
        Background background;
        if(backgroundTypeSelect.getModel().getSelectedItem().equals("Colour") || backgroundImageLocation.getText().isEmpty()) {
            background = new Background(backgroundColourButton.getColourSelectionWindow().getSelectedColour());
        }
        else if(backgroundTypeSelect.getModel().getSelectedItem().equals("Image")) {
            background = new Background(backgroundImageLocation.getText(), null);
        }
        else {
            throw new AssertionError("Bug - " + backgroundTypeSelect.getModel().getSelectedItem() + " is an unknown selection value");
        }
        return new Theme(font, fontColourButton.getColourSelectionWindow().getSelectedColour(), background);
    }
}
