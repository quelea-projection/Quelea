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
    private JTextField tags;
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
        tags = new JTextField(10);
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

    private void addBlock(JPanel panel, String labelText, Component comp) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(comp);
        panel.add(label);
        panel.add(comp);
    }

    public void resetNewSong() {
        ccli.setText("");
        year.setText("");
        publisher.setText("");
        tags.setText("");
        copyright.setText("");
        key.setText("");
        capo.setText("");
        info.setText("");
    }

    public void resetEditSong(Song song) {
        ccli.setText(song.getCcli());
        copyright.setText(song.getCopyright());
        tags.setText(song.getTagsAsString());
        publisher.setText(song.getPublisher());
        year.setText(song.getYear());
        key.setText(song.getKey());
        capo.setText(song.getCapo());
        info.setText(song.getInfo());
    }

    public JTextField getCcliField() {
        return ccli;
    }

    public JTextField getCopyrightField() {
        return copyright;
    }

    public JTextField getPublisherField() {
        return publisher;
    }

    public JTextField getTagsField() {
        return tags;
    }

    public JTextField getYearField() {
        return year;
    }

    public JTextArea getInfoField() {
        return info;
    }

    public JTextField getKeyField() {
        return key;
    }

    public JTextField getCapoField() {
        return capo;
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new DetailedSongPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
