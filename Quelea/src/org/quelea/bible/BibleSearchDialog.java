/*
 * This file is part of Quelea, free projection software for churches.
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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * A dialog that can be used for searching for bible passages.
 *
 * @author mjrb5
 */
public class BibleSearchDialog extends JDialog {

    private JTextField searchField;
    private JList<BibleChapter> searchResults;

    /**
     * Create a new bible searcher dialog.
     */
    public BibleSearchDialog() {
        super(Application.get().getMainWindow());
        setLayout(new BorderLayout());
        setTitle(LabelGrabber.INSTANCE.getLabel("bible.search.title"));
        setIconImage(Utils.getImage("icons/search.png"));
        searchField = new JTextField(30);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(searchField);
        add(northPanel, BorderLayout.NORTH);
        searchResults = new JList<>(new DefaultListModel<BibleChapter>());
        searchResults.setCellRenderer(new SearchPreviewRenderer());
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
        centrePanel.add(new JScrollPane(searchResults));
        add(centrePanel, BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent de) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                update();
            }
        });
        setSize(600,400);
        reset();
    }

    /**
     * Reset this dialog.
     */
    public final void reset() {
        DefaultListModel<BibleChapter> model = (DefaultListModel<BibleChapter>) searchResults.getModel();
        model.clear();
        searchField.setText(LabelGrabber.INSTANCE.getLabel("initial.search.text"));
        searchField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent fe) {
                searchField.setText("");
            }
        });
        searchField.setEnabled(false);
        BibleManager.get().runOnIndexInit(new Runnable() {

            @Override
            public void run() {
                searchField.setEnabled(true);
            }
        });
    }

    /**
     * Update the results based on the entered text.
     */
    private void update() {
        if(BibleManager.get().isIndexInit()) {
            final String text = searchField.getText();
            final BibleChapter[] results = BibleManager.get().getIndex().filter(text, null);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    DefaultListModel<BibleChapter> model = (DefaultListModel<BibleChapter>) searchResults.getModel();
                    model.clear();
                    if(!text.trim().isEmpty()) {
                        for(BibleChapter chapter : results) {
                            model.addElement(chapter);
                        }
                    }
                }
            });
        }
    }

    /**
     * Centre the dialog on the parent before displaying.
     *
     * @param visible true if visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            reset();
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }

    /**
     * Just for testing.
     *
     * @param args command line arguments (not used.)
     */
    public static void main(String[] args) {
        BibleSearchDialog dialog = new BibleSearchDialog();
//        dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        dialog.setVisible(true);
    }

    /**
     * Renderer for displaying a preview of the part of the bible chapter
     * containing the search text.
     */
    private class SearchPreviewRenderer extends JLabel implements ListCellRenderer<BibleChapter> {

        /**
         * @inheritDoc
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends BibleChapter> list, BibleChapter value, int index, boolean isSelected, boolean cellHasFocus) {
            String introText = "<b>" + value.getBook().getBookName() + " " + value.getNum() + " (" + value.getBook().getBible().getName() + ")" + ": </b>";
            String searchText = searchField.getText().trim().toLowerCase();
            String passageText = value.getText().trim();
            int pos = passageText.toLowerCase().indexOf(searchText);
            int startIndex = pos - 10;
            while(startIndex >= 0 && !Character.isWhitespace(value.getText().charAt(startIndex))) {
                startIndex++;
            }
            if(startIndex < 0) {
                startIndex = 0;
            }
            int endIndex = pos + 10 + searchText.length();
            while(endIndex < passageText.length() && !Character.isWhitespace(value.getText().charAt(endIndex))) {
                endIndex++;
            }
            if(endIndex > passageText.length()) {
                endIndex = passageText.length();
            }
            String subStr = "..." + passageText.substring(startIndex, endIndex).trim() + "...";
            setBorder(new EmptyBorder(5, 5, 5, 5));
            StringBuilder labelHTML = new StringBuilder();
            labelHTML.append("<html>");
            labelHTML.append(introText);
            labelHTML.append(subStr);
            labelHTML.append("</html>");
            setText(labelHTML.toString());
            if(isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
}
