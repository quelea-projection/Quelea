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
import java.awt.Component;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.quelea.displayable.Song;
import org.quelea.tags.TagEntryPanel;
import org.quelea.utils.SpringUtilities;

/**
 * A panel where more detailed information about a song is entered.
 * @author Michael
 */
public class DetailedSongPanel extends JPanel {

    private JTextField ccli;
    private JTextField year;
    private JTextField publisher;
    private JTextField copyright;
    private TagEntryPanel tags;
    private JTextField key;
    private JTextField capo;
    private JTextArea info;

    /**
     * Create a new detailed song panel.
     */
    public DetailedSongPanel() {
        super(new BorderLayout());
        setName("Detailed information");
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new SpringLayout());
        ccli = new JTextField(new PlainDocument() {

            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null) {
                    return;
                }
                if (str.isEmpty() || str.matches("[0-9]")) {
                    super.insertString(offs, str, a);
                }
            }
        }, "", 10);
        year = new JTextField(new PlainDocument() {

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

                if (str == null) {
                    return;
                }
                String oldString = getText(0, getLength());
                String newString = oldString.substring(0, offs) + str
                        + oldString.substring(offs);
                try {
                    if (newString.length() <= 4) {
                        int val = Integer.parseInt(newString + "0") / 10;
                        if (val > 0 && val <= new GregorianCalendar().get(Calendar.YEAR) + 1) {
                            super.insertString(offs, str, a);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    //Not a number
                }
            }
        }, "", 10);
        publisher = new JTextField(10);
        copyright = new JTextField(10);
        tags = new TagEntryPanel(null, true, false);
        key = new JTextField(new PlainDocument() {

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

                if (str == null) {
                    return;
                }
                String oldString = getText(0, getLength());
                String newString = oldString.substring(0, offs) + str
                        + oldString.substring(offs);
                if (newString.isEmpty() || newString.matches("[a-gA-G][#b]?")) {
                    if (Character.isLowerCase(newString.charAt(0))) {
                        str = Character.toString(Character.toUpperCase(str.substring(0, 1).charAt(0))) + str.substring(1);
                    }
                    super.insertString(offs, str, a);
                }
            }
        }, "", 10);
        capo = new JTextField(new PlainDocument() {

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

                if (str == null) {
                    return;
                }
                String oldString = getText(0, getLength());
                String newString = oldString.substring(0, offs) + str
                        + oldString.substring(offs);
                try {
                    if (newString.length() <= 4) {
                        int val = Integer.parseInt(newString + "0") / 10;
                        if (val > 0 && val <= 24) {
                            super.insertString(offs, str, a);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    //Not a number
                }
            }
        }, "", 10);
        info = new JTextArea(new PlainDocument() {

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

                if (str == null) {
                    return;
                }

                if ((getLength() + str.length()) <= 20000) {
                    super.insertString(offs, str, a);
                }
            }
        }, "", 10, 10);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);

        addBlock(formPanel, "CCLI number", ccli);
        addBlock(formPanel, "Copyright", copyright);
        addBlock(formPanel, "Year", year);
        addBlock(formPanel, "Publisher", publisher);
        addBlock(formPanel, "Tags", tags);
        addBlock(formPanel, "Key", key);
        addBlock(formPanel, "Capo", capo);
        JScrollPane pane = new JScrollPane(info);
        pane.setPreferredSize(new Dimension(pane.getPreferredSize().width, 300));
        addBlock(formPanel, "Notes", pane);

        SpringUtilities.makeCompactGrid(formPanel, 8, 2, 6, 6, 6, 6);

        add(formPanel, BorderLayout.NORTH);

    }

    /**
     * Add a label / input block to a panel.
     * @param panel the panel to add to.
     * @param labelText the label text to add to this block.
     * @param comp the component to add to this block.
     */
    private void addBlock(JPanel panel, String labelText, Component comp) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(comp);
        panel.add(label);
        panel.add(comp);
    }

    /**
     * Reset this panel to blank so it can contain a new song.
     */
    public void resetNewSong() {
        ccli.setText("");
        year.setText("");
        publisher.setText("");
        tags.removeTags();
        copyright.setText("");
        key.setText("");
        capo.setText("");
        info.setText("");
    }

    /**
     * Set this panel to edit a song.
     * @param song the song to edit.
     */
    public void resetEditSong(Song song) {
        ccli.setText(song.getCcli());
        copyright.setText(song.getCopyright());
        tags.setTags(song.getTagsAsString());
        publisher.setText(song.getPublisher());
        year.setText(song.getYear());
        key.setText(song.getKey());
        capo.setText(song.getCapo());
        info.setText(song.getInfo());
    }

    /**
     * Get the CCLI field.
     * @return the CCLI field.
     */
    public JTextField getCcliField() {
        return ccli;
    }

    /**
     * Get the copyright field.
     * @return the copyright field.
     */
    public JTextField getCopyrightField() {
        return copyright;
    }

    /**
     * Get the publisher field.
     * @return the publisher field.
     */
    public JTextField getPublisherField() {
        return publisher;
    }

    /**
     * Get the tags panel.
     * @return the tags panel.
     */
    public TagEntryPanel getTagsPanel() {
        return tags;
    }

    /**
     * Get the year field.
     * @return the year field.
     */
    public JTextField getYearField() {
        return year;
    }

    /**
     * Get the info field.
     * @return the info field.
     */
    public JTextArea getInfoField() {
        return info;
    }

    /**
     * Get the key field.
     * @return the key field.
     */
    public JTextField getKeyField() {
        return key;
    }

    /**
     * Get the capo field.
     * @return the capo field.
     */
    public JTextField getCapoField() {
        return capo;
    }
    
    /**
     * Test it.
     * @param args 
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new DetailedSongPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
