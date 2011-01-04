package org.quelea.windows.newsong;

import com.inet.jortho.SpellChecker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import org.quelea.utils.SpringUtilities;
import org.quelea.displayable.Song;
import org.quelea.utils.Utils;

/**
 * The panel that manages the basic input of song information - the title,
 * author and lyrics.
 * @author Michael
 */
public class BasicSongPanel extends JPanel {

    private final JTextArea lyricsArea;
    private final JTextField titleField;
    private final JTextField authorField;

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

        JTextField[] attributes = new JTextField[]{titleField, authorField};

        JPanel topPanel = new JPanel(new SpringLayout());
        for (int i = 0; i < attributes.length; i++) {
            JLabel label = new JLabel(attributes[i].getName(), JLabel.TRAILING);
            topPanel.add(label);
            label.setLabelFor(attributes[i]);
            topPanel.add(attributes[i]);
        }
        SpringUtilities.makeCompactGrid(topPanel, attributes.length, 2, 6, 6, 6, 6);
        centrePanel.add(topPanel);
        lyricsArea = new JTextArea(25, 50);
        SpellChecker.register(lyricsArea);
        lyricsArea.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                doHighlight();
            }
        });
        JPanel lyricsPanel = new JPanel();
        lyricsPanel.setLayout(new BoxLayout(lyricsPanel, BoxLayout.X_AXIS));
        JToolBar lyricsToolbar = new JToolBar("Tools", JToolBar.VERTICAL);
        lyricsToolbar.setFloatable(false);
        JButton dictButton = getDictButton();
        lyricsToolbar.add(dictButton);
        JButton fixAposButton = getAposButton();
        lyricsToolbar.add(fixAposButton);
        lyricsPanel.add(new JScrollPane(lyricsArea));
        JPanel lyricsToolbarPanel = new JPanel();
        lyricsToolbarPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        lyricsToolbarPanel.add(lyricsToolbar);
        lyricsPanel.add(lyricsToolbarPanel);
        centrePanel.add(lyricsPanel);
        add(centrePanel, BorderLayout.CENTER);

    }

    private final List<Object> highlights = new ArrayList<Object>();

    /**
     * Manage the highlighting.
     */
    private void doHighlight() {
        for(Object highlight : highlights) {
            lyricsArea.getHighlighter().removeHighlight(highlight);
        }
        highlights.clear();
        try {
            Highlighter hilite = lyricsArea.getHighlighter();
            String text = lyricsArea.getText();
            String[] lines = text.split("\n\n");
            List<Integer> startIndexes = new ArrayList<Integer>();
            List<Integer> endIndexes = new ArrayList<Integer>();
            int offset = 0;
            for (String line : lines) {
                if (Utils.isTitle(line)) {
                    startIndexes.add(text.indexOf(line, offset));
                    int endIndex = text.indexOf('\n', text.indexOf(line));
                    if (endIndex < text.indexOf(line, offset)) {
                        endIndex = text.length();
                    }
                    endIndexes.add(endIndex);
                }
                offset += line.length();
            }

            for (int i = 0; i < startIndexes.size(); i++) {
                highlights.add(hilite.addHighlight(startIndexes.get(i), endIndexes.get(i), new DefaultHighlightPainter(Color.YELLOW)));
            }
        }
        catch (BadLocationException ex) {
        }
    }

    /**
     * Get the spell checker button.
     * @return the spell checker button.
     */
    private JButton getDictButton() {
        JButton spellButton = new JButton(Utils.getImageIcon("icons/dictionary.png"));
        spellButton.setMargin(new Insets(0, 0, 0, 0));
        spellButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        spellButton.setToolTipText("Run spellcheck (F7)");
        spellButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SpellChecker.showSpellCheckerDialog(lyricsArea, SpellChecker.getOptions());
            }
        });
        return spellButton;
    }

    /**
     * Get the button to fix apostrophes.
     * @return the button to fix apostrophes.
     */
    private JButton getAposButton() {
        JButton aposButton = new JButton(Utils.getImageIcon("icons/apos.png"));
        aposButton.setMargin(new Insets(0, 0, 0, 0));
        aposButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        aposButton.setToolTipText("Fix weird apostrophes");
        aposButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int pos = lyricsArea.getCaretPosition();
                lyricsArea.setText(lyricsArea.getText().replace("`", "'").replace("’", "'"));
                lyricsArea.setCaretPosition(pos);
            }
        });
        return aposButton;
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
                //Nothing needs to be done here.
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
