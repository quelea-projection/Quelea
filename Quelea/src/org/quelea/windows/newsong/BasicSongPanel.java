/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import org.quelea.Application;
import org.quelea.chord.ChordLineTransposer;
import org.quelea.chord.ChordTransposer;
import org.quelea.chord.TransposeDialog;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.LineTypeChecker;
import org.quelea.utils.SpringUtilities;
import org.quelea.utils.Utils;

/**
 * The panel that manages the basic input of song information - the title,
 * author and lyrics.
 *
 * @author Michael
 */
public class BasicSongPanel extends JPanel {

    private final JTextArea lyricsArea;
    private final JTextField titleField;
    private final JTextField authorField;
    private final TransposeDialog transposeDialog;

    /**
     * Create and initialise the song panel.
     */
    public BasicSongPanel() {
        setName(LabelGrabber.INSTANCE.getLabel("basic.information.heading"));
        setLayout(new BorderLayout());
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));

        JPanel titleAuthorPanel = new JPanel();
        titleAuthorPanel.setLayout(new BorderLayout());

        titleField = new JTextField();
        titleField.setName(LabelGrabber.INSTANCE.getLabel("title.label"));

        authorField = new JTextField();
        authorField.setName(LabelGrabber.INSTANCE.getLabel("author.label"));

        transposeDialog = new TransposeDialog();

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
        JToolBar lyricsToolbar = new JToolBar(LabelGrabber.INSTANCE.getLabel("tools.label"), JToolBar.VERTICAL);
        lyricsToolbar.setFloatable(false);
        lyricsToolbar.add(getDictButton());
        lyricsToolbar.add(getAposButton());
        lyricsToolbar.add(getTrimLinesButton());
        lyricsToolbar.add(getTransposeButton());
        lyricsPanel.add(new JScrollPane(lyricsArea));
        JPanel lyricsToolbarPanel = new JPanel();
        lyricsToolbarPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        lyricsToolbarPanel.add(lyricsToolbar);
        lyricsPanel.add(lyricsToolbarPanel);
        centrePanel.add(lyricsPanel);
        add(centrePanel, BorderLayout.CENTER);

    }
    private final List<Object> highlights = new ArrayList<>();

    /**
     * Manage the highlighting.
     */
    private void doHighlight() {
        for (Object highlight : highlights) {
            lyricsArea.getHighlighter().removeHighlight(highlight);
        }
        highlights.clear();
        try {
            Highlighter hilite = lyricsArea.getHighlighter();
            String text = lyricsArea.getText();
            String[] lines = text.split("\n");
            List<HighlightIndex> indexes = new ArrayList<>();
            int offset = 0;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                LineTypeChecker.Type type = new LineTypeChecker(line).getLineType();
                if (type == LineTypeChecker.Type.TITLE && i > 0 && !lines[i - 1].trim().isEmpty()) {
                    type = LineTypeChecker.Type.NORMAL;
                }
                if (type != LineTypeChecker.Type.NORMAL) {
                    int startIndex = offset;
                    int endIndex = startIndex + line.length();
                    Color highlightColor = type.getHighlightColor();
                    if (highlightColor != null) {
                        indexes.add(new HighlightIndex(startIndex, endIndex, highlightColor));
                    }
                }
                offset += line.length() + 1;
            }

            for (HighlightIndex index : indexes) {
                highlights.add(hilite.addHighlight(index.getStartIndex(), index.getEndIndex(), new DefaultHighlightPainter(index.getHighlightColor())));
            }
        }
        catch (BadLocationException ex) {
        }
    }

    /**
     * Get the button used for transposing the chords.
     * @return the button used for transposing the chords.
     */
    private JButton getTransposeButton() {
        JButton ret = new JButton(Utils.getImageIcon("icons/transpose.png", 16, 16));
        ret.setMargin(new Insets(0, 0, 0, 0));
        ret.setBorder(new EmptyBorder(0, 0, 0, 0));
        ret.setToolTipText(LabelGrabber.INSTANCE.getLabel("transpose.tooltip"));
        ret.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                transposeDialog.setKey(getKey(0));
                transposeDialog.setVisible(true);
                int semitones = transposeDialog.getSemitones();

                JTextField keyField = Application.get().getMainWindow().getSongEntryWindow().getDetailedSongPanel().getKeyField();
                if (!keyField.getText().isEmpty()) {
                    keyField.setText(new ChordTransposer(keyField.getText()).transpose(semitones, null));
                }

                String key = getKey(semitones);

                StringBuilder newText = new StringBuilder(getLyricsField().getText().length());
                for (String line : getLyricsField().getText().split("\n")) {
                    if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                        newText.append(new ChordLineTransposer(line).transpose(semitones, key));
                    }
                    else {
                        newText.append(line);
                    }
                    newText.append('\n');
                }
                int pos = getLyricsField().getCaretPosition();
                getLyricsField().setText(newText.toString());
                getLyricsField().setCaretPosition(pos);
            }
        });
        return ret;
    }

    /**
     * Get the given key of the song (or as best we can work out if it's not 
     * specified) transposed by the given number of semitones.
     * @param semitones the number of semitones to transpose the key.
     * @return the key, transposed.
     */
    private String getKey(int semitones) {
        JTextField keyField = Application.get().getMainWindow().getSongEntryWindow().getDetailedSongPanel().getKeyField();
        String key = keyField.getText();
        if (key == null || key.isEmpty()) {
            for (String line : getLyricsField().getText().split("\n")) {
                if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                    String first;
                    int i = 0;
                    do {
                        first = line.split("\\s+")[i++];
                    } while (first.isEmpty());
                    key = new ChordTransposer(first).transpose(semitones, null);
                    if (key.length() > 2) {
                        key = key.substring(0, 2);
                    }
                    if (key.length() == 2) {
                        if (key.charAt(1) == 'B') {
                            key = Character.toString(key.charAt(0)) + "b";
                        }
                        else if (key.charAt(1) != 'b' && key.charAt(1) != '#') {
                            key = Character.toString(key.charAt(0));
                        }
                    }
                    break;
                }
            }
        }

        if (key.isEmpty()) {
            key = null;
        }
        return key;
    }

    /**
     * Get the remove chords button.
     * @return the remove chords button.
     */
    private JButton getTrimLinesButton() {
        JButton button = new JButton(Utils.getImageIcon("icons/trimLines.png"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setToolTipText(LabelGrabber.INSTANCE.getLabel("trim.lines.tooltip"));
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                StringBuilder newText = new StringBuilder();
                for (String line : lyricsArea.getText().split("\n")) {
                    newText.append(line.trim()).append("\n");
                }
                lyricsArea.setText(newText.toString());
            }
        });
        return button;
    }

//    /**
//     * Get the remove chords button. We no longer remove chords so this isn't needed - we keep them and use them!
//     * @return the remove chords button.
//     * @deprecated
//     */
//    private JButton getRemoveChordsButton() {
//        JButton button = new JButton(Utils.getImageIcon("icons/removeChords.png"));
//        button.setMargin(new Insets(0, 0, 0, 0));
//        button.setBorder(new EmptyBorder(0, 0, 0, 0));
//        button.setToolTipText("Remove guitar chords (marked in red)");
//        button.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                StringBuilder newText = new StringBuilder();
//                for (String line : lyricsArea.getText().split("\n")) {
//                    if (new LineTypeChecker(line).getLineType() != LineTypeChecker.Type.CHORDS) {
//                        newText.append(line).append('\n');
//                    }
//                }
//                lyricsArea.setText(newText.toString());
//            }
//        });
//        return button;
//    }
    /**
     * Get the spell checker button.
     * @return the spell checker button.
     */
    private JButton getDictButton() {
        JButton button = new JButton(Utils.getImageIcon("icons/dictionary.png"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setToolTipText(LabelGrabber.INSTANCE.getLabel("run.spellcheck.label") + " (F7)");
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
        button.setToolTipText(LabelGrabber.INSTANCE.getLabel("fix.apos.label"));
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
        getLyricsField().setText("<" + LabelGrabber.INSTANCE.getLabel("type.lyrics.here.text") + ">");
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
        getLyricsField().setText(song.getLyrics(true, true));
        getLyricsField().setCaretPosition(0);
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
