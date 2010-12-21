package org.quelea.windows.main;

import java.awt.BorderLayout;
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
import org.quelea.utils.Utils;

/**
 * The panel used for browsing the database of songs and adding any songs to
 * the order of service.
 * @author Michael
 */
public class LibrarySongPanel extends JPanel {

    private final JTextField searchBox;
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
        searchBox.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                updateList();
            }

            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            private void updateList() {
                songList.filter(searchBox.getText());
            }
        });
        northPanel.add(searchBox);
        add(northPanel, BorderLayout.NORTH);

        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setFloatable(false);
        addButton = new JButton(Utils.getImageIcon("icons/newsong.png"));
        addButton.setToolTipText("Add song");
        toolbar.add(addButton);
        removeButton = new JButton(Utils.getImageIcon("icons/remove.png"));
        removeButton.setToolTipText("Remove song");
        removeButton.setEnabled(false);
        toolbar.add(removeButton);
        add(toolbar, BorderLayout.EAST);

    }

    /**
     * Check whether the remove button should be enabled or disabled and set
     * it accordingly.
     */
    private void checkRemoveButton() {
        if(songList.getSelectedIndex() == -1 || songList.getModel().getSize()==0) {
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
}
