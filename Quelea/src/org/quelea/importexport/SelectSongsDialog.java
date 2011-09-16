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
package org.quelea.importexport;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.quelea.displayable.Song;
import org.quelea.utils.Utils;

/**
 * A dialog where given songs can be selected.
 * @author Michael
 */
public class SelectSongsDialog extends JDialog {

    private final JButton addButton;
    private final JTable table;
    private List<Song> songs;
    private boolean[] checkList;
    private final String checkboxText;

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     * @param text a list of lines to be shown in the dialog.
     * @param acceptText text to place on the accpet button.
     * @param checkboxText text to place in the column header for the
     * checkboxes.
     */
    public SelectSongsDialog(JFrame owner, String[] text, String acceptText,
                             String checkboxText) {
        super(owner, "Select Songs", true);
        this.checkboxText = checkboxText;
        songs = new ArrayList<>();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        for(String str : text) {
            mainPanel.add(new JLabel(str));
        }
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mainPanel.add(new JScrollPane(table));
        JPanel optionsPanel = new JPanel();
        JToolBar options = new JToolBar();
        options.setFloatable(false);
        options.add(createCheckAllButton());
        optionsPanel.add(options);
        addButton = new JButton(acceptText);
        mainPanel.add(addButton);
        add(mainPanel);
        add(optionsPanel);
        pack();
    }

    /**
     * Create the button that checks all the boxes.
     * @return the newly created check all button.
     */
    private JButton createCheckAllButton() {
        JButton checkButton = new JButton(Utils.getImageIcon("icons/checkbox.jpg"));
        checkButton.setToolTipText("Check / Uncheck all");
        checkButton.setMargin(new Insets(0, 0, 0, 0));
        checkButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        checkButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(getSongs().isEmpty()) {
                    return;
                }
                boolean val = !(Boolean) getTable().getValueAt(0, 2);
                for(int i = 0; i < getSongs().size(); i++) {
                    getTable().setValueAt(val, i, 2);
                }
            }
        });
        return checkButton;
    }

    /**
     * Set the songs to be shown in the dialog.
     * @param songs         the list of songs to be shown.
     * @param checkList     a list corresponding to the song list - each position is true if the checkbox should be
     *                      selected, false otherwise.
     * @param defaultVal    the default value to use for the checkbox if checkList is null or smaller than the songs
     *                      list.
     */
    public void setSongs(List<Song> songs, boolean[] checkList, boolean defaultVal) {
        Collections.sort(songs);
        this.songs = songs;
        this.checkList = checkList;
        DefaultTableModel model = new DefaultTableModel(songs.size(), 3);
        table.setModel(model);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setHeaderValue("Name");
        table.getColumnModel().getColumn(1).setHeaderValue("Author");
        table.getColumnModel().getColumn(2).setHeaderValue(checkboxText);
        table.getColumnModel().getColumn(2).setCellEditor(table.getDefaultEditor(Boolean.class));
        table.getColumnModel().getColumn(2).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        for(int i = 0; i < songs.size(); i++) {
            table.getModel().setValueAt(songs.get(i).getTitle(), i, 0);
            table.getModel().setValueAt(songs.get(i).getAuthor(), i, 1);
            boolean val;
            if(checkList != null && i < checkList.length) {
                val = !checkList[i]; //invert
            }
            else {
                val = defaultVal;
            }
            table.getModel().setValueAt(val, i, 2);
        }
    }

    /**
     * Get the check list. This list corresponds with the list of songs to determine whether the checkbox by each song
     * should be checked or not.
     * @return the check list.
     */
    public boolean[] getCheckList() {
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
