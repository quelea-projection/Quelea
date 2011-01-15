package org.quelea.windows.newsong;

import com.inet.jortho.SpellChecker;
import org.quelea.displayable.Song;
import org.quelea.utils.LineTypeChecker;
import org.quelea.utils.SpringUtilities;
import org.quelea.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The panel that manages the basic input of song information - the title, author and lyrics.
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
        for(int i = 0; i < attributes.length; i++) {
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
        lyricsToolbar.add(getDictButton());
        lyricsToolbar.add(getAposButton());
        lyricsToolbar.add(getRemoveChordsButton());
        lyricsToolbar.add(getTrimLinesButton());
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
            String[] lines = text.split("\n");
            List<HighlightIndex> indexes = new ArrayList<HighlightIndex>();
            int offset = 0;
            for(int i = 0; i < lines.length; i++) {
                String line = lines[i];
                LineTypeChecker.Type type = new LineTypeChecker(line).getLineType();
                if(type == LineTypeChecker.Type.TITLE && i > 0 && !lines[i - 1].trim().isEmpty()) {
                    type = LineTypeChecker.Type.NORMAL;
                }
                if(type != LineTypeChecker.Type.NORMAL) {
                    int startIndex = offset;
                    int endIndex = startIndex + line.length();
                    Color highlightColor = type.getHighlightColor();
                    if(highlightColor != null) {
                        indexes.add(new HighlightIndex(startIndex, endIndex, highlightColor));
                    }
                }
                offset += line.length() + 1;
            }

            for(HighlightIndex index : indexes) {
                highlights.add(hilite.addHighlight(index.getStartIndex(), index.getEndIndex(), new DefaultHighlightPainter(index.getHighlightColor())));
            }
        }
        catch(BadLocationException ex) {
        }
    }

    /**
     * Get the remove chords button.
     * @return the remove chords button.
     */
    private JButton getTrimLinesButton() {
        JButton button = new JButton(Utils.getImageIcon("icons/trimLines.png"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setToolTipText("Trim lines");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                StringBuilder newText = new StringBuilder();
                for(String line : lyricsArea.getText().split("\n")) {
                    newText.append(line.trim()).append("\n");
                }
                lyricsArea.setText(newText.toString());
            }
        });
        return button;
    }

    /**
     * Get the remove chords button.
     * @return the remove chords button.
     */
    private JButton getRemoveChordsButton() {
        JButton button = new JButton(Utils.getImageIcon("icons/removeChords.png"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setToolTipText("Remove guitar chords (marked in red)");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                StringBuilder newText = new StringBuilder();
                for(String line : lyricsArea.getText().split("\n")) {
                    if(new LineTypeChecker(line).getLineType() != LineTypeChecker.Type.CHORDS) {
                        newText.append(line).append('\n');
                    }
                }
                lyricsArea.setText(newText.toString());
            }
        });
        return button;
    }

    /**
     * Get the spell checker button.
     * @return the spell checker button.
     */
    private JButton getDictButton() {
        JButton button = new JButton(Utils.getImageIcon("icons/dictionary.png"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setToolTipText("Run spellcheck (F7)");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SpellChecker.showSpellCheckerDialog(lyricsArea, SpellChecker.getOptions());
            }
        });
        return button;
    }

    /**
     * Get the button to fix apostrophes.
     * @return the button to fix apostrophes.
     */
    private JButton getAposButton() {
        JButton button = new JButton(Utils.getImageIcon("icons/apos.png"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setToolTipText("Fix weird apostrophes");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int pos = lyricsArea.getCaretPosition();
                lyricsArea.setText(lyricsArea.getText().replace("`", "'").replace("’", "'"));
                lyricsArea.setCaretPosition(pos);
            }
        });
        return button;
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
