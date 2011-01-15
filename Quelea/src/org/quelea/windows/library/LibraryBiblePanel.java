package org.quelea.windows.library;

import org.quelea.bible.*;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The panel used to get bible verses.
 * @author Michael
 */
public class LibraryBiblePanel extends JPanel {

    private final JComboBox bibleSelector;
    private final JComboBox bookSelector;
    private final JTextField passageSelector;
    private final JTextArea preview;
    private final JButton addToSchedule;
    private final List<BibleVerse> verses;

    /**
     * Create and populate a new library bible panel.
     */
    public LibraryBiblePanel() {
        setName("Bible");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        verses = new ArrayList<BibleVerse>();
        bibleSelector = new JComboBox(BibleManager.get().getBibles());
        String selectedBibleName = QueleaProperties.get().getDefaultBible();
        for(int i = 0; i < bibleSelector.getModel().getSize(); i++) {
            Bible bible = (Bible) bibleSelector.getItemAt(i);
            if(bible.getName().equals(selectedBibleName)) {
                bibleSelector.setSelectedIndex(i);
            }
        }
        add(bibleSelector);
        JPanel chapterPanel = new JPanel();
        chapterPanel.setLayout(new BoxLayout(chapterPanel, BoxLayout.X_AXIS));
        bookSelector = new JComboBox(((Bible) bibleSelector.getSelectedItem()).getBooks());
        chapterPanel.add(bookSelector);
        passageSelector = new JTextField();
        chapterPanel.add(passageSelector);
        passageSelector.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                //Nothing needed here
            }

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addToSchedule.doClick();
                    passageSelector.setText("");
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });
        add(chapterPanel);
        preview = new JTextArea();
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        add(new JScrollPane(preview));
        addToSchedule = new JButton("Add to schedule", Utils.getImageIcon("icons/tick.png"));
        JPanel addPanel = new JPanel();
        addToSchedule.setEnabled(false);
        addPanel.add(addToSchedule);
        add(addPanel);

        addUpdateListeners();
        bibleSelector.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultComboBoxModel model = (DefaultComboBoxModel) bookSelector.getModel();
                model.removeAllElements();
                for(BibleBook book : ((Bible) bibleSelector.getSelectedItem()).getBooks()) {
                    model.addElement(book);
                }
                update();
            }
        });

        chapterPanel.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, bookSelector.getHeight()));
        bibleSelector.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, bookSelector.getHeight()));
        addPanel.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, addToSchedule.getHeight()));
    }

    /**
     * Add the listeners that should call the update() method.
     */
    private void addUpdateListeners() {
        passageSelector.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });
        bookSelector.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                update();
            }
        });

    }

    /**
     * Update the text in the preview panel based on the contents of the fields.
     */
    private void update() {
        verses.clear();
        ChapterVerseParser cvp = new ChapterVerseParser(passageSelector.getText());
        BibleBook book = (BibleBook) bookSelector.getSelectedItem();
        if(book == null || book.getChapter(cvp.getFromChapter()) == null
                || book.getChapter(cvp.getToChapter()) == null
                || passageSelector.getText().isEmpty()) {
            getAddToSchedule().setEnabled(false);
            preview.setText("");
            return;
        }
        StringBuilder ret = new StringBuilder();
        int toVerse = book.getChapter(cvp.getFromChapter()).getVerses().length - 1;
        if((cvp.getFromChapter() == cvp.getToChapter()) && cvp.getToVerse() >= 0 && cvp.getToVerse() < book.getChapter(cvp.getFromChapter()).getVerses().length) {
            toVerse = cvp.getToVerse();
        }

        for(int v = cvp.getFromVerse(); v <= toVerse; v++) {
            BibleVerse verse = book.getChapter(cvp.getFromChapter()).getVerse(v);
            ret.append(verse.getText()).append(' ');
            verses.add(verse);
        }
        for(int c = cvp.getFromChapter() + 1; c < cvp.getToChapter(); c++) {
            for(BibleVerse verse : book.getChapter(c).getVerses()) {
                ret.append(verse.getText()).append(' ');
                verses.add(verse);
            }
        }
        if(cvp.getFromChapter() != cvp.getToChapter()) {
            for(int v = 0; v <= cvp.getToVerse(); v++) {
                BibleVerse verse = book.getChapter(cvp.getToChapter()).getVerse(v);
                if(verse != null) {
                    ret.append(verse.getText()).append(' ');
                    verses.add(verse);
                }
            }
        }
        int maxVerses = QueleaProperties.get().getMaxVerses();
        if(verses.size() > maxVerses) {
            preview.setText("Sorry, no more than " + maxVerses + " verses allowed "
                    + "(at the moment you've selected a total off " + verses.size()
                    + ".) You can increase this value by going to Tools => Options "
                    + "and clicking the \"Bible\" tab, but setting this value "
                    + "too high will crash the program if you're computer isn't "
                    + "fast enough.");
            preview.setBackground(Color.RED);
            getAddToSchedule().setEnabled(false);
            return;
        }
        else {
            preview.setBackground(null);
            getAddToSchedule().setEnabled(true);
        }

        preview.setText(ret.toString());
        preview.setSelectionStart(0);
        preview.setSelectionEnd(0);
    }

    /**
     * Get all the verses currently shown in this panel.
     * @return all the verses in the current preview
     */
    public BibleVerse[] getVerses() {
        return verses.toArray(new BibleVerse[verses.size()]);
    }

    /**
     * Return the book, chapter and verse numbers as a string.
     * @return the location of the current passage.
     */
    public String getBibleLocation() {
        StringBuilder ret = new StringBuilder();
        ret.append(bookSelector.getSelectedItem()).append(" ");
        ret.append(passageSelector.getText());
        return ret.toString();
    }

    /**
     * Get the bible selector used to select the type of bible to use.
     * @return the bible selector used to select the type of bible to use.
     */
    public JComboBox getBibleSelector() {
        return bibleSelector;
    }

    /**
     * Get the preview text area.
     * @return the preview text area.
     */
    public JTextArea getPreview() {
        return preview;
    }

    /**
     * Get the add to schedule button.
     * @return the add to schedule button.
     */
    public JButton getAddToSchedule() {
        return addToSchedule;
    }

    /**
     * Get the book selector.
     * @return the book selector.
     */
    public JComboBox getBookSelector() {
        return bookSelector;
    }

    /**
     * Get the passage selector.
     * @return the passage selector.
     */
    public JTextField getPassageSelector() {
        return passageSelector;
    }
}
