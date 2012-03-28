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
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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
    private JList<String> searchResults;

    /**
     * Create a new bible searcher dialog.
     */
    public BibleSearchDialog() {
        super(Application.get().getMainWindow());
        setLayout(new BorderLayout());
        setTitle(LabelGrabber.INSTANCE.getLabel("bible.search.title"));
        setIconImage(Utils.getImage("icons/search.png"));
        searchField = new JTextField(20);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(searchField);
        add(northPanel, BorderLayout.NORTH);
        searchResults = new JList<>(new DefaultListModel<String>());
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
        centrePanel.add(new JScrollPane(searchResults));
        add(centrePanel, BorderLayout.CENTER);
        searchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent ke) {
                update();
            }
        });
        pack();
        reset();
    }

    /**
     * Reset this dialog.
     */
    public final void reset() {
        DefaultListModel<String> model = (DefaultListModel<String>) searchResults.getModel();
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
                    DefaultListModel<String> model = (DefaultListModel<String>) searchResults.getModel();
                    model.clear();
                    if(!text.trim().isEmpty()) {
                        for(BibleChapter chapter : results) {
                            model.addElement(chapter.getText());
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
}
