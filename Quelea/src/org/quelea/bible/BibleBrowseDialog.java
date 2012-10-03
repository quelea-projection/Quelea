/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.bible;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * A dialog where the user can browse through the installed bibles.
 * <p/>
 * @author Michael
 */
public class BibleBrowseDialog extends JDialog implements BibleChangeListener {

    private JComboBox<Bible> bibles;
    private JList<BibleBook> books;
    private JEditorPane bibleText;

    /**
     * Create the bible browse dialog.
     */
    public BibleBrowseDialog() {
//        super(Application.get().getMainWindow());
        setTitle(LabelGrabber.INSTANCE.getLabel("bible.browser.title"));
        setIconImage(Utils.getImage("icons/bible.png"));
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        bibles = new JComboBox<>(new DefaultComboBoxModel<Bible>());
        bibles.setEditable(false);
        bibles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                updateBooks();
            }
        });
        BibleManager.get().registerBibleChangeListener(this);
        JLabel selectBibleLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("bible.heading"));
        selectBibleLabel.setLabelFor(bibles);
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(selectBibleLabel);
        northPanel.add(bibles);
        add(northPanel, BorderLayout.NORTH);

        books = new JList<>(new DefaultListModel<BibleBook>());
        books.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        books.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                BibleBook book = books.getSelectedValue();
                if(book != null) {
                    bibleText.setText(book.getHTML());
                    bibleText.setCaretPosition(0);
                }
            }
        });
        add(new JScrollPane(books), BorderLayout.WEST);

        bibleText = new JEditorPane();
        HTMLEditorKit kit = new HTMLEditorKit();
        Document doc = kit.createDefaultDocument();
        bibleText.setDocument(doc);
        bibleText.setEditorKit(kit);
        bibleText.setEditable(false);
        add(new JScrollPane(bibleText), BorderLayout.CENTER);
        updateBibles();
        setSize(800, 600);
    }

    /**
     * Set the current selected bible on this dialog.
     * <p/>
     * @param bible the bible to select.
     */
    public void setBible(Bible bible) {
        bibles.setSelectedItem(bible);
    }

    /**
     * Set the current selected book on this dialog.
     * <p/>
     * @param book the book to select.
     */
    public void setBook(BibleBook book) {
        setBible(book.getBible());
        DefaultListModel<BibleBook> model = (DefaultListModel<BibleBook>) books.getModel();
        books.setSelectedIndex(model.indexOf(book));
    }

    /**
     * Set the current chapter on this dialog.
     * <p/>
     * @param chapter the chapter to select.
     */
    public void setChapter(BibleChapter chapter) {
        setBook(chapter.getBook());
//        bibleText.setCaretPosition(chapter.getBook().getCaretIndexOfChapter(chapter.getNum()));
    }

    /**
     * Update the books based on the bible selection
     */
    public final void updateBooks() {
        int index = books.getSelectedIndex();
        Bible currentBible = bibles.getItemAt(bibles.getSelectedIndex());
        DefaultListModel<BibleBook> model = (DefaultListModel<BibleBook>) books.getModel();
        model.clear();
        if(currentBible != null) {
            for(BibleBook book : currentBible.getBooks()) {
                model.addElement(book);
            }
            books.setSelectedIndex(index);
        }
    }

    /**
     * Update the list of bibles on this dialog.
     */
    @Override
    public final void updateBibles() {
        DefaultComboBoxModel<Bible> model = (DefaultComboBoxModel<Bible>) bibles.getModel();
        model.removeAllElements();
        for(Bible bible : BibleManager.get().getBibles()) {
            model.addElement(bible);
        }
    }

    /**
     * Centre the dialog on the parent before displaying.
     * <p/>
     * @param visible true if visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }

    /**
     * Just for testing.
     * <p/>
     * @param args command line args (not used.)
     */
    public static void main(String[] args) {
        BibleBrowseDialog dialog = new BibleBrowseDialog();
//        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialog.setVisible(true);
    }
}
