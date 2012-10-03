/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricCanvas;

/**
 * The panel where the user chooses what visual theme a song should have.
 * @author Michael
 */
public class ThemePanel extends JPanel {

    private static final int THRESHOLD = 30;
    public static final String[] SAMPLE_LYRICS = {"Amazing Grace how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private JPanel fontToolbar;
    private JPanel backgroundPanel;
    private JComboBox<String> fontSelection;
    private ColourButton fontColourButton;
    private ColourButton backgroundColourButton;
    private JComboBox<String> backgroundTypeSelect;
    private JTextField backgroundImageLocation;
    private JToggleButton boldButton;
    private JToggleButton italicButton;
    private final LyricCanvas canvas;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        setName(LabelGrabber.INSTANCE.getLabel("theme.heading"));
        setLayout(new BorderLayout());
        canvas = new LyricCanvas(false, false);
        canvas.setText(SAMPLE_LYRICS, null);
//        add(canvas, BorderLayout.CENTER);
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        setupFontToolbar();
        toolbarPanel.add(fontToolbar);
        setupBackgroundToolbar();
        toolbarPanel.add(backgroundPanel);
        backgroundPanel.setMaximumSize(new Dimension(1000, 10));
        add(toolbarPanel, BorderLayout.NORTH);
        updateTheme(false);
    }

    /**
     * Setup the background toolbar.
     */
    private void setupBackgroundToolbar() {
        backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        final JPanel backgroundChooserPanel = new JPanel();
        final CardLayout layout = new CardLayout();
        backgroundChooserPanel.setLayout(layout);

        backgroundTypeSelect = new JComboBox<>();
        backgroundTypeSelect.addItem(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
        backgroundTypeSelect.addItem(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backgroundTypeSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme(false);
            }
        });
        backgroundPanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("background.theme.label")+":"));
        backgroundPanel.add(backgroundTypeSelect);
        backgroundPanel.add(backgroundChooserPanel);

        final JPanel colourPanel = new JPanel();
        colourPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        backgroundColourButton = new ColourButton(Color.BLACK);
        colourPanel.add(backgroundColourButton);
        backgroundColourButton.getColourSelectionWindow().getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme(true);
            }
        });

        final JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        backgroundImageLocation = new JTextField(25);
        backgroundImageLocation.setEditable(false);
        imagePanel.add(backgroundImageLocation);
        imagePanel.add(new ImageButton(backgroundImageLocation, canvas));

        backgroundChooserPanel.add(colourPanel, "colour");
        backgroundChooserPanel.add(imagePanel, "image");

        backgroundTypeSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (backgroundTypeSelect.getModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label"))) {
                    layout.show(backgroundChooserPanel, "colour");
                }
                else if (backgroundTypeSelect.getModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
                    layout.show(backgroundChooserPanel, "image");
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
        fontToolbar = new JPanel();
        fontToolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        fontToolbar.add(new JLabel(LabelGrabber.INSTANCE.getLabel("font.theme.label")+":"));
        fontSelection = new JComboBox<>();
        for (String font : Utils.getAllFonts()) {
            fontSelection.addItem(font);
        }
        fontSelection.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                updateTheme(false);
            }
        });
        fontToolbar.add(fontSelection);
        boldButton = new JToggleButton(Utils.getImageIcon("icons/bold.png"));
        boldButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme(false);
            }
        });
        fontToolbar.add(boldButton);
        italicButton = new JToggleButton(Utils.getImageIcon("icons/italic.png"));
        italicButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme(false);
            }
        });
        fontToolbar.add(italicButton);
        fontColourButton = new ColourButton(Color.WHITE);
        fontColourButton.getColourSelectionWindow().getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateTheme(true);
            }
        });
        fontToolbar.add(fontColourButton);

    }

    /**
     * Update the canvas with the current theme.
     */
    private void updateTheme(boolean warning) {
        Theme theme = getTheme();
        if (warning && theme.getBackground().isColour()) {
//            checkAccessibility(theme.getFontColor(), theme.getBackground().getColour());
        }
        canvas.setTheme(theme);
    }

    /**
     * Set the current theme to represent in this panel.
     * @param theme the theme to represent.
     */
    public void setTheme(Theme theme) {
        if (theme == null) {
            theme = Theme.DEFAULT_THEME;
        }
//        Font font = theme.getFont();
//        if (font.isBold()) {
//            boldButton.setSelected(true);
//        }
//        if (font.isItalic()) {
//            italicButton.setSelected(true);
//        }
//        int fontSize = font.getSize();
//        if (fontSize % 2 != 0) {
//            fontSize--;
//        }
//        fontSelection.setSelectedItem(font.getFamily());
//        fontColourButton.getColourSelectionWindow().setSelectedColour(theme.getFontColor());
//        fontColourButton.setIconColour(theme.getFontColor());
//        Background background = theme.getBackground();
//        if (background.isColour()) {
//            backgroundTypeSelect.getModel().setSelectedItem(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
//            backgroundColourButton.getColourSelectionWindow().setSelectedColour(background.getColour());
//            backgroundColourButton.setIconColour(background.getColour());
//        }
//        else {
//            backgroundTypeSelect.getModel().setSelectedItem(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
//            backgroundImageLocation.setText(background.getImageLocation());
//        }
        updateTheme(false);
    }

    /**
     * Check whether the two colours are too closely mathced to read clearly.
     * If they are, display a warning message.
     * @param col1 first colour.
     * @param col2 second colour.
     */
    private void checkAccessibility(Color col1, Color col2) {
        int diff = Utils.getColorDifference(col1, col2);
        if (diff < THRESHOLD) {
            JOptionPane.showMessageDialog(this, LabelGrabber.INSTANCE.getLabel("similar.colors.text"),
                    LabelGrabber.INSTANCE.getLabel("warning.label"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Get the canvas on this theme panel.
     * @return the canvas on this theme panel.
     */
    public LyricCanvas getCanvas() {
        return canvas;
    }
    
    /**
     * Get the theme currently represented by the state of this panel.
     * @return the current theme.
     */
    public Theme getTheme() {
        int fontStyle = 0;
        if (boldButton.isSelected()) {
            fontStyle |= Font.BOLD;
        }
        if (italicButton.isSelected()) {
            fontStyle |= Font.ITALIC;
        }
        Font font = new Font(fontSelection.getSelectedItem().toString(), fontStyle, 72);
        Background background;
        if (backgroundTypeSelect.getModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("color.theme.label")) || backgroundImageLocation.getText().isEmpty()) {
//            background = new Background(backgroundColourButton.getColourSelectionWindow().getSelectedColour());
        }
        else if (backgroundTypeSelect.getModel().getSelectedItem().equals(LabelGrabber.INSTANCE.getLabel("image.theme.label"))) {
            background = new Background(backgroundImageLocation.getText(), null);
        }
        else {
            throw new AssertionError("Bug - " + backgroundTypeSelect.getModel().getSelectedItem() + " is an unknown selection value");
        }
//        return new Theme(font, fontColourButton.getColourSelectionWindow().getSelectedColour(), background);
        return null;
    }

    /**
     * Test the themepanel.
     * @param args 
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ThemePanel());
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
