package org.quelea.importsong;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;

/**
 * A dialog where selected songs can be imported into the database.
 * @author Michael
 */
public class SelectImportedSongsDialog extends JDialog {

    private final JButton addButton;
    private final JTable table;
    private List<Song> songs;

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     */
    public SelectImportedSongsDialog(JFrame owner) {
        super(owner, "Select Songs", true);
        songs = new ArrayList<Song>();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(new JLabel("The following songs have been imported."));
        add(new JLabel("Select the ones you want to add to the database then hit \"Add\"."));
        add(new JLabel("Songs that Quelea thinks are duplicates have been unchecked."));
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table));
        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addButton.setEnabled(false);
                SwingWorker worker = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() {
                        for (int i = 0; i < songs.size(); i++) {
                            if ((Boolean) table.getValueAt(i, 2)) {
                                SongDatabase.get().addSong(songs.get(i), false);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        SongDatabase.get().fireUpdate();
                        setVisible(false);
                        addButton.setEnabled(true);
                    }

                };
                worker.execute();
            }
        });
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
    public void setSongs(List<Song> songs, List<Boolean> existsAlready) {
        this.songs = songs;
        table.setModel(new DefaultTableModel(songs.size(), 3));
        table.getColumnModel().getColumn(0).setHeaderValue("Name");
        table.getColumnModel().getColumn(1).setHeaderValue("Author");
        table.getColumnModel().getColumn(2).setHeaderValue("Add to database?");
        table.getColumnModel().getColumn(2).setCellEditor(table.getDefaultEditor(Boolean.class));
        table.getColumnModel().getColumn(2).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        for (int i = 0; i < songs.size(); i++) {
            table.getModel().setValueAt(songs.get(i).getTitle(), i, 0);
            table.getModel().setValueAt(songs.get(i).getAuthor(), i, 1);
            table.getModel().setValueAt(!existsAlready.get(i), i, 2);
        }
    }

    /**
     * Get the add button.
     * @return the add button.
     */
    public JButton getAddButton() {
        return addButton;
    }
}
