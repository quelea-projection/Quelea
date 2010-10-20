package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import org.quelea.SpringUtilities;
import org.quelea.display.Song;

/**
 * The panel that manages the basic input of song information - the title,
 * author and lyrics.
 * @author Michael
 */
public class BasicSongPanel extends JPanel {

    private JTextArea lyricsArea;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField[] attributes;

    /**
     * Create and initialise the song panel.
     */
    public BasicSongPanel() {
        setName("Basic information");
        setLayout(new BorderLayout());
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));

        JPanel titleAuthorPanel = new JPanel();
        titleAuthorPanel.setLayout(new BorderLayout());

        titleField = new JTextField();
        titleField.setName("Title");

        authorField = new JTextField();
        authorField.setName("Author");

        attributes = new JTextField[]{titleField, authorField};

        JPanel topPanel = new JPanel(new SpringLayout());
        for(int i = 0; i < attributes.length; i++) {
            JLabel label = new JLabel(attributes[i].getName(), JLabel.TRAILING);
            topPanel.add(label);
            label.setLabelFor(attributes[i]);
            topPanel.add(attributes[i]);
        }
        SpringUtilities.makeCompactGrid(topPanel, attributes.length, 2, 6, 6, 6, 6);

        centrePanel.add(topPanel);
        lyricsArea = new JTextArea(25, 50);
        lyricsArea.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                checkHighlight();
            }

            public void keyPressed(KeyEvent e) {
                checkHighlight();
            }

            public void keyReleased(KeyEvent e) {
                checkHighlight();
            }

            private void checkHighlight() {
                //TODO: Highlighting
            }
        });
        JScrollPane textAreaScroll = new JScrollPane(lyricsArea);
        centrePanel.add(textAreaScroll);
        add(centrePanel, BorderLayout.CENTER);

    }

    /**
     * Reset this panel so new song data can be entered.
     */
    public void resetNewSong() {
        getTitleField().setText("");
        getAuthorField().setText("");
        getLyricsField().setText("<Type lyrics here>");
        getLyricsField().addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        getLyricsField().setText("");
                    }
                });
                getLyricsField().removeFocusListener(this);
            }

            public void focusLost(FocusEvent e) {
            }
        });
        getTitleField().requestFocus();
    }

    /**
     * Reset this panel so an existing song can be edited.
     * @param song the song to edit.
     */
    public void resetEditSong(Song song) {
        getTitleField().setText(song.getTitle());
        getAuthorField().setText(song.getAuthor());
        getLyricsField().setText(song.getLyrics());
        getLyricsField().requestFocus();
    }

    /**
     * Get the lyrics field.
     * @return the lyrics field.
     */
    public JTextArea getLyricsField() {
        return lyricsArea;
    }

    /**
     * Get the title field.
     * @return the title field.
     */
    public JTextField getTitleField() {
        return titleField;
    }

    /**
     * Get the author field.
     * @return the author field.
     */
    public JTextField getAuthorField() {
        return authorField;
    }
}
