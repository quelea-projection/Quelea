package org.quelea.importsong;

import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.quelea.displayable.Song;

/**
 * A dialog where given songs can be selected.
 * @author Michael
 */
public class SelectSongsDialog extends JDialog {

    private final JButton addButton;
    private final JTable table;
    private List<Song> songs;
    private List<Boolean> checkList;
    private final String checkboxText;

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectSongsDialog(JFrame owner, String[] text, String acceptText,
            String checkboxText) {
        super(owner, "Select Songs", true);
        this.checkboxText = checkboxText;
        songs = new ArrayList<Song>();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        for(String str : text) {
            add(new JLabel(str));
        }
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table));
        addButton = new JButton(acceptText);
        add(addButton);
        pack();
    }

    /**
     * Set the songs to be shown in the dialog.
     * @param songs the list of songs to be shown.
     * @param existsAlready a list corresponding to the song list - each
     * position is true if Quelea thinks the song already exists in the
     * database, and false otherwise.
     */
    public void setSongs(List<Song> songs, List<Boolean> checkList) {
        this.songs = songs;
        this.checkList = checkList;
        table.setModel(new DefaultTableModel(songs.size(), 3));
        table.getColumnModel().getColumn(0).setHeaderValue("Name");
        table.getColumnModel().getColumn(1).setHeaderValue("Author");
        table.getColumnModel().getColumn(2).setHeaderValue(checkboxText);
        table.getColumnModel().getColumn(2).setCellEditor(table.getDefaultEditor(Boolean.class));
        table.getColumnModel().getColumn(2).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        for (int i = 0; i < songs.size(); i++) {
            table.getModel().setValueAt(songs.get(i).getTitle(), i, 0);
            table.getModel().setValueAt(songs.get(i).getAuthor(), i, 1);
            boolean val;
            if(checkList!=null && i<checkList.size()) {
                val = checkList.get(i)^true; //invert
            }
            else {
                val = true;
            }
            table.getModel().setValueAt(val, i, 2);
        }
    }

    /**
     * Get the check list. This list corresponds with the list of songs to
     * determine whether the checkbox by each song should be checked or not.
     * @return the check list.
     */
    public List<Boolean> getCheckList() {
        return checkList;
    }

    /**
     * Get the song list.
     * @return the list of songs.
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Get the table in this dialog.
     * @return the table.
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Get the add button.
     * @return the add button.
     */
    public JButton getAddButton() {
        return addButton;
    }
}
