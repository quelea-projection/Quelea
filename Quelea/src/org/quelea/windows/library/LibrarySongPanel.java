package org.quelea.windows.library;

import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.SortedListModel;
import org.quelea.displayable.Song;
import org.quelea.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import org.quelea.windows.main.RemoveSongDBActionListener;

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
        songList = new LibrarySongList();
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
        northPanel.add(new JLabel("Search "));
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
        searchBox.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                //Nothing needed here
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchCancelButton.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //Nothing needed here
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
                songList.filter(searchBox.getText(), true);
            }
        });
        northPanel.add(searchBox);
        searchCancelButton = new JButton(Utils.getImageIcon("icons/cross.png"));
        searchCancelButton.setToolTipText("Clear search box (Esc)");
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
        addButton.setToolTipText("Add song");
        addButton.setRequestFocusEnabled(false);
        toolbar.add(addButton);
        removeButton = new JButton(Utils.getImageIcon("icons/remove.png"));
        removeButton.setToolTipText("Remove song");
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
