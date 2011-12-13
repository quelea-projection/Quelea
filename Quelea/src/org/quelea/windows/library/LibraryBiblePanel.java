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
package org.quelea.windows.library;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.quelea.bible.Bible;
import org.quelea.bible.BibleBook;
import org.quelea.bible.BibleChangeListener;
import org.quelea.bible.BibleManager;
import org.quelea.bible.BibleVerse;
import org.quelea.bible.ChapterVerseParser;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * The panel used to get bible verses.
 * @author Michael
 */
public class LibraryBiblePanel extends JPanel implements BibleChangeListener {

    private final JComboBox<Bible> bibleSelector;
    private final JComboBox<BibleBook> bookSelector;
    private final JTextField passageSelector;
    private final JTextArea preview;
    private final JButton addToSchedule;
    private final List<BibleVerse> verses;

    /**
     * Create and populate a new library bible panel.
     */
    public LibraryBiblePanel() {
        setName(LabelGrabber.INSTANCE.getLabel("bible.heading"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        verses = new ArrayList<>();
        BibleManager.get().registerBibleChangeListener(this);
        bibleSelector = new JComboBox<>(BibleManager.get().getBibles());
        String selectedBibleName = QueleaProperties.get().getDefaultBible();
        for (int i = 0; i < getBibleSelectorModel().getSize(); i++) {
            Bible bible = bibleSelector.getItemAt(i);
            if (bible.getName().equals(selectedBibleName)) {
                bibleSelector.setSelectedIndex(i);
            }
        }
        add(bibleSelector);
        JPanel chapterPanel = new JPanel();
        chapterPanel.setLayout(new BoxLayout(chapterPanel, BoxLayout.X_AXIS));
        bookSelector = new JComboBox<>(getBibleSelectorModel().getElementAt(bibleSelector.getSelectedIndex()).getBooks());
        chapterPanel.add(bookSelector);
        passageSelector = new JTextField();
        chapterPanel.add(passageSelector);
        passageSelector.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                //Nothing needed here
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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
        addToSchedule = new JButton(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"), Utils.getImageIcon("icons/tick.png"));
        JPanel addPanel = new JPanel();
        addToSchedule.setEnabled(false);
        addPanel.add(addToSchedule);
        add(addPanel);

        addUpdateListeners();
        bibleSelector.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (bibleSelector.getSelectedIndex() == -1) {
                    return;
                }
                getBibleBookSelectorModel().removeAllElements();
                for (BibleBook book : ((Bible) bibleSelector.getSelectedItem()).getBooks()) {
                    getBibleBookSelectorModel().addElement(book);
                }
                update();
            }
        });

        chapterPanel.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, bookSelector.getHeight()));
        bibleSelector.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, bookSelector.getHeight()));
        addPanel.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, addToSchedule.getHeight()));
    }

    /**
     * Update the bibles in the panel based on the current bibles the bible
     * manager is aware of.
     */
    @Override
    public void updateBibles() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                DefaultComboBoxModel<Bible> model = getBibleSelectorModel();
                model.removeAllElements();
                for (Bible bible : BibleManager.get().getBibles()) {
                    model.addElement(bible);
                }
            }
        });
    }

    /**
     * Get the bible selector model.
     */
    @SuppressWarnings("unchecked")
    private DefaultComboBoxModel<Bible> getBibleSelectorModel() {
        return (DefaultComboBoxModel<Bible>) bibleSelector.getModel();
    }

    /**
     * Get the book selector model.
     */
    @SuppressWarnings("unchecked")
    private DefaultComboBoxModel<BibleBook> getBibleBookSelectorModel() {
        return (DefaultComboBoxModel<BibleBook>) bookSelector.getModel();
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
        if (book == null || book.getChapter(cvp.getFromChapter()) == null
                || book.getChapter(cvp.getToChapter()) == null
                || passageSelector.getText().isEmpty()) {
            getAddToSchedule().setEnabled(false);
            preview.setText("");
            return;
        }
        StringBuilder ret = new StringBuilder();
        int toVerse = book.getChapter(cvp.getFromChapter()).getVerses().length - 1;
        if ((cvp.getFromChapter() == cvp.getToChapter()) && cvp.getToVerse() >= 0 && cvp.getToVerse() < book.getChapter(cvp.getFromChapter()).getVerses().length) {
            toVerse = cvp.getToVerse();
        }

        for (int v = cvp.getFromVerse(); v <= toVerse; v++) {
            BibleVerse verse = book.getChapter(cvp.getFromChapter()).getVerse(v);
            ret.append(verse.getText()).append(' ');
            verses.add(verse);
        }
        for (int c = cvp.getFromChapter() + 1; c < cvp.getToChapter(); c++) {
            for (BibleVerse verse : book.getChapter(c).getVerses()) {
                ret.append(verse.getText()).append(' ');
                verses.add(verse);
            }
        }
        if (cvp.getFromChapter() != cvp.getToChapter()) {
            for (int v = 0; v <= cvp.getToVerse(); v++) {
                BibleVerse verse = book.getChapter(cvp.getToChapter()).getVerse(v);
                if (verse != null) {
                    ret.append(verse.getText()).append(' ');
                    verses.add(verse);
                }
            }
        }
        int maxVerses = QueleaProperties.get().getMaxVerses();
        if (verses.size() > maxVerses) {
            preview.setText(LabelGrabber.INSTANCE.getLabel("too.many.verses.error")
                    .replace("$(MAXVERSE)", Integer.toString(maxVerses))
                    .replace("$(VERSENUM)", Integer.toString(verses.size())));
//            preview.setText("Sorry, no more than " + maxVerses + " verses allowed "
//                    + "(at the moment you've selected a total off " + verses.size()
//                    + ".) You can increase this value by going to Tools => Options "
//                    + "and clicking the \"Bible\" tab, but setting this value "
//                    + "too high will crash the program if you're computer isn't "
//                    + "fast enough.");
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
    public JComboBox<Bible> getBibleSelector() {
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
    public JComboBox<BibleBook> getBookSelector() {
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
