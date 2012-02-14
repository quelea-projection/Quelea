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

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.actionlisteners.RemoveSongDBActionListener;

/**
 * The panel used for browsing the database of songs and adding any songs to the order of service.
 * @author Michael
 */
public class LibrarySongPanel extends JPanel {

    private final JTextField searchBox;
    private final JButton searchCancelButton;
    private final LibrarySongList songList;
    private final JButton removeButton;
    private final JButton addButton;

    /**
     * Create and initialise the library song panel.
     */
    public LibrarySongPanel() {
        setLayout(new BorderLayout());
        songList = new LibrarySongList(true);
        songList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                checkRemoveButton();
            }
        });
        songList.getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                checkRemoveButton();
            }

            public void intervalRemoved(ListDataEvent e) {
                checkRemoveButton();
            }

            public void contentsChanged(ListDataEvent e) {
                checkRemoveButton();
            }
        });
        JScrollPane listScrollPane = new JScrollPane(songList);
        listScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(listScrollPane, BorderLayout.CENTER);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("library.song.search")));
        searchBox = new JTextField();
        searchBox.setDragEnabled(false);
        searchBox.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                searchBox.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                //Nothing needed here
            }
        });
        searchBox.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchCancelButton.doClick();
                }
            }
        });
        searchBox.getDocument().addDocumentListener(new DocumentListener() {

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
                if(searchBox.getText().isEmpty()) {
                    searchCancelButton.setEnabled(false);
                }
                else {
                    searchCancelButton.setEnabled(true);
                }
                songList.filter(searchBox.getText());
            }
        });
        northPanel.add(searchBox);
        searchCancelButton = new JButton(Utils.getImageIcon("icons/cross.png"));
        searchCancelButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("clear.search.box"));
        searchCancelButton.setRequestFocusEnabled(false);
        searchCancelButton.setEnabled(false);
        searchCancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                searchBox.setText("");
            }
        });
        northPanel.add(searchCancelButton);
        add(northPanel, BorderLayout.NORTH);

        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setFloatable(false);
        addButton = new JButton(Utils.getImageIcon("icons/add.png"));
        addButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("add.song.text"));
        addButton.setRequestFocusEnabled(false);
        toolbar.add(addButton);
        removeButton = new JButton(Utils.getImageIcon("icons/remove.png"));
        removeButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("remove.song.text"));
        removeButton.setRequestFocusEnabled(false);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new RemoveSongDBActionListener());
        toolbar.add(removeButton);
        add(toolbar, BorderLayout.EAST);

    }

    /**
     * Check whether the remove button should be enabled or disabled and set it accordingly.
     */
    private void checkRemoveButton() {
        if(songList.getSelectedIndex() == -1 || songList.getModel().getSize() == 0) {
            removeButton.setEnabled(false);
        }
        else {
            removeButton.setEnabled(true);
        }
    }

    /**
     * Get the song list behind this panel.
     * @return the song list.
     */
    public LibrarySongList getSongList() {
        return songList;
    }

    /**
     * Get the add button on the panel.
     * @return the add button.
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     * Get the remove button on the panel.
     * @return the remove button.
     */
    public JButton getRemoveButton() {
        return removeButton;
    }

    /**
     * Get the search box.
     * @return the search box.
     */
    public JTextField getSearchBox() {
        return searchBox;
    }
}
