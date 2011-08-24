package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
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

    /**
     * Create a new detailed song panel.
     */
    public DetailedSongPanel() {
        super(new BorderLayout());
        setName("Detailed information");
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new SpringLayout());
        ccli = new JTextField(10);
        year = new JTextField(10);
        publisher = new JTextField(10);
        copyright = new JTextField(10);
        tags = new JTextField(10);
        
        addBlock(formPanel, "CCLI number", ccli);
        addBlock(formPanel, "Copyright", copyright);
        addBlock(formPanel, "Year", year);
        addBlock(formPanel, "Publisher", publisher);
        addBlock(formPanel, "Tags", tags);
        
        SpringUtilities.makeCompactGrid(formPanel, 5, 2, 6, 6, 6, 6);
        
        add(formPanel, BorderLayout.NORTH);
        
    }
    
    private void addBlock(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(field);
        panel.add(label);
        panel.add(field);
    }
    
    public void resetNewSong() {
        ccli.setText("");
        year.setText("");
        publisher.setText("");
        tags.setText("");
        copyright.setText("");
    }
    
    public void resetEditSong(Song song) {
        ccli.setText(song.getCcli());
        copyright.setText(song.getCopyright());
        tags.setText(song.getTagsAsString());
        publisher.setText(song.getPublisher());
        year.setText(song.getYear());
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
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new DetailedSongPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
